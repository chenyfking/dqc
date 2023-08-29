package com.beagledata.gaea.ruleengine.thirdapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beagledata.gaea.ruleengine.exception.RuleException;
import com.beagledata.gaea.ruleengine.runtime.RuleContext;
import com.beagledata.gaea.ruleengine.util.PropertyUtils;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by liulu on 2020/6/16.
 */
public class ThirdApiHandler {
    private static Logger logger = LoggerFactory.getLogger(ThirdApiHandler.class);

    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");
    private static OkHttpClient client = new OkHttpClient();

    public void handle(String id, Object input, Object output) {
        ThirdApi api = RuleContext.getCurrentContext().getRuleBuilder().getThirdApi(id);
        if (api == null) {
            logger.error("外部接口调用失败：接口找不到. id: {}", id);
            throw new RuleException("外部接口调用失败：接口找不到.");
        }

        String result = null;
        try {
            if ("get".equalsIgnoreCase(api.getMethod())) {
                result = doGet(api, input);
            } else {
                result = doPost(api, input);
            }
            setOutput(api, output, result);
        } catch (Exception e) {
            logger.error("调用外部接口失败. api: {}, result: {}, inputFact: {}, outputFact: {}", api, result, input, output, e);
        }
    }

    private String doGet(ThirdApi api, Object input) throws IOException, IllegalAccessException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(api.getUrl()).newBuilder();
        if (input != null) {
            for (Field field : input.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(input);
                if (value != null) {
                    urlBuilder.addQueryParameter(field.getName(), value.toString());
                }
            }
        }

        Request request = getRequestBuilder(api).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    private String doPost(ThirdApi api, Object input) throws IOException {
        RequestBody body = getRequestBody(api, input);
        Request request = getRequestBuilder(api).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    private RequestBody getRequestBody(ThirdApi api, Object input)  {
        if (api.isJsonType()) {
            return RequestBody.create(JSON_TYPE, JSON.toJSONString(input));
        }

        Map<String, Object> fields = PropertyUtils.getProperties(input);
        FormBody.Builder builder = new FormBody.Builder();
        fields.forEach((k, v)-> builder.add(k,v.toString()));
        return builder.build();
    }

    private Request.Builder getRequestBuilder(ThirdApi api) {
        return new Request.Builder().url(api.getUrl()).headers(buildHeaders(api));
    }

    private Headers buildHeaders(ThirdApi api) {
        return Headers.of(api.getHeaders());
    }

    private void setOutput(ThirdApi api, Object output, String result) throws IllegalAccessException {
        if (StringUtils.isNotBlank(api.getResponseJsonPath())) {
            JSONObject json = JSON.parseObject(result);
            Object value = null;
            for (String key : api.getResponseJsonPath().split("\\.")) {
                value = json.get(key);
                if (!(value instanceof JSONObject)) {
                    break;
                }
                json = (JSONObject) value;
            }
            if (value != null) {
                result = value.toString();
            }
        }

        PropertyUtils.setProperties(output, result);
    }
}
