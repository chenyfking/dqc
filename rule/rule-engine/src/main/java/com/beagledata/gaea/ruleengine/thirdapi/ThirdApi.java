package com.beagledata.gaea.ruleengine.thirdapi;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liulu on 2020/7/21.
 */
public class ThirdApi {
    /**
     * 接口标识
     */
    private String id;
    /**
     * 请求地址
     */
    private String url;
    /**
     * 请求方法
     */
    private String method;
    /**
     * 请求头部列表
     */
    private Map<String, String> headers = new HashMap<>();
    /**
     * content-type 是否json
     */
    private boolean jsonType;
    /**
     * 响应json查询path
     */
    private String responseJsonPath;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public boolean isJsonType() {
        return jsonType;
    }

    public void setJsonType(boolean jsonType) {
        this.jsonType = jsonType;
    }

    public String getResponseJsonPath() {
        return responseJsonPath;
    }

    public void setResponseJsonPath(String responseJsonPath) {
        this.responseJsonPath = responseJsonPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThirdApi)) return false;

        ThirdApi thirdApi = (ThirdApi) o;

        return getId() != null ? getId().equals(thirdApi.getId()) : thirdApi.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ThirdApi{");
        sb.append("id='").append(id).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", method='").append(method).append('\'');
        sb.append(", headers=").append(headers);
        sb.append(", jsonType=").append(jsonType);
        sb.append(", responseJsonPath=").append(responseJsonPath);
        sb.append(", toString=").append(super.toString());
        sb.append("}").append(headers);
        return sb.toString();
    }
}
