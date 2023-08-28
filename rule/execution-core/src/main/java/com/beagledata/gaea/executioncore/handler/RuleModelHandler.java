package com.beagledata.gaea.executioncore.handler;

import com.beagledata.gaea.executioncore.domain.ApiDoc;
import com.beagledata.gaea.executioncore.domain.Input;
import com.beagledata.gaea.executioncore.domain.Output;
import com.beagledata.gaea.ruleengine.annotation.PassingDirection;
import com.beagledata.gaea.ruleengine.annotation.Required;
import com.beagledata.gaea.ruleengine.exception.MissingParamsException;
import com.beagledata.gaea.ruleengine.exception.RuleException;
import com.beagledata.gaea.ruleengine.runtime.RuleContainer;
import com.beagledata.gaea.ruleengine.runtime.RuleEngine;
import com.beagledata.gaea.ruleengine.runtime.RuleEngineFactory;
import com.beagledata.gaea.ruleengine.util.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * 规则服务执行器
 *
 * Created by liulu on 2020/5/15.
 */
public class RuleModelHandler extends AbstractModelHandler {
    private String id;

    public RuleModelHandler(String id, byte[] bytes) {
        String ruleId= ByteArrayRuleContainer.loadRule(bytes);
        if (!id.equals(ruleId)) {
            logger.warn("模型id和知识包id不一致, ruleId: {}, modelId: {}", ruleId, id);
            RuleContainer.removeRule(ruleId);
            throw new RuleException("模型id和知识包id不一致");
        }
        this.id = id;
    }

    @Override
    protected Output doHandle(Input input) throws Exception {
        RuleEngine ruleEngine = getRuleEngine();
        // 设置参数
        setParams(ruleEngine, input);
        ruleEngine.execute();
        return wrapOutput(ruleEngine);
    }

    private RuleEngine getRuleEngine() {
        return RuleEngineFactory.getRuleEngine(id);
    }

    private void setParams(RuleEngine ruleEngine, Input input) throws IllegalAccessException, IntrospectionException, InvocationTargetException {
        Map<String, Object> objectMap = ruleEngine.getObjectMap();
        Map<String, Map<String, Map<String, Object>>> reqBody = getReqBody(objectMap);
        checkParams(reqBody, input);

        if (objectMap.size() > 1) {
            input.getData().forEach((id, params) -> {
                if (params instanceof Map) {
                    ((Map<String, Object>)params).forEach((name, value) -> ruleEngine.put(id, name, value));
                }
            });
        } else {
           reqBody.keySet().forEach(id -> input.getData().forEach((name, value) -> ruleEngine.put(id, name, value)));
        }
    }

    /**
    * 描述: 检查必输参数
    * @param: [objectMap, input]
    * @author: 周庚新
    * @date: 2020/10/15
    * @return: void
    *
    */
    private void checkParams(Map<String, Map<String, Map<String, Object>>> reqBody, Input input) throws InvocationTargetException, IllegalAccessException, IntrospectionException {
        if (reqBody.size() > 1) {
            for (Map.Entry<String, Map<String, Map<String, Object>>> reqEntry : reqBody.entrySet()) {
                for (Map.Entry<String, Map<String, Object>> entry : reqEntry.getValue().entrySet()) {
                    Boolean required = (Boolean) entry.getValue().get("required");
                    if (required != null && required) {
                        Object objMap = input.getData().get(reqEntry.getKey());
                        if (!(objMap instanceof Map) || objMap == null) {
                            throw new MissingParamsException("参数[" + reqEntry.getKey() + "." + entry.getKey() + "]必传");
                        }
                        Map<String, Object> params = (Map<String, Object>) objMap;
                        Object value = params.get(entry.getKey());
                        if (value == null || StringUtils.isBlank(value.toString())) {
                            throw new MissingParamsException("参数[" + reqEntry.getKey() + "." + entry.getKey() + "]必传");
                        }
                    }
                }
            }
        } else {
            for (Map<String,  Map<String, Object>> data : reqBody.values()) {
                for (Map.Entry<String, Map<String, Object>> entry : data.entrySet()) {
                    Boolean required = (Boolean) entry.getValue().get("required");
                    if (required != null && required) {
                        Object value = input.getData().get(entry.getKey());
                        if (value == null || StringUtils.isBlank(value.toString())) {
                            throw new MissingParamsException("参数[" + entry.getKey() + "]必传");
                        }
                    }
                }
            }
        }
    }

    private Output wrapOutput(RuleEngine ruleEngine) {
        Output output = new Output();
        Map<String, Object> objectMap = ruleEngine.getObjectMap();
        if (objectMap.size() > 1) {
            output.setResponse(cleanResponse(objectMap));
        } else {
            // 只有一个Fact，直接返回
            for (Object obj : objectMap.values()) {
                output.setResponse(obj);
            }
        }
        output.setRuleEngine(ruleEngine);
        return output;
    }

    /**
    * 描述: 去掉没有返回字段的fact
    * @param: [objectMap]
    * @author: 周庚新
    * @date: 2020/12/15
    * @return: java.lang.Object
    */
    private Object cleanResponse(Map<String, Object> objectMap) {
        Map<String, Object> returnMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            Class<?> factCls = entry.getValue().getClass();
            PassingDirection factDirection = factCls.getAnnotation(PassingDirection.class);
            if (factDirection != null
                    && (PassingDirection.Direction.IN.equals(factDirection.value())
                    || PassingDirection.Direction.NONE.equals(factDirection.value()))) {
                continue;
            }

            Field[] fields = factCls.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                PassingDirection direction = field.getAnnotation(PassingDirection.class);
                if (PassingDirection.Direction.OUT.equals(direction.value())
                        || PassingDirection.Direction.IN_OUT.equals(direction.value())) {
                    returnMap.put(entry.getKey(), entry.getValue());
                    break;
                }
                field.setAccessible(false);
            }
        }
        if (returnMap.size() == 1) {
            for (Object obj : returnMap.values()) {
                return obj;
            }
        }
        return returnMap;
    }

    @Override
    public ApiDoc getApiDoc() {
        try {
            RuleEngine ruleEngine = getRuleEngine();
            Map<String, Object> objectMap = ruleEngine.getObjectMap();
            Map<String, Map<String, Map<String, Object>>> reqBody = getReqBody(objectMap);
            Input input = new Input();
            if (reqBody.size() > 1) {
                reqBody.forEach((k, v) -> {
                    Map<String, Object> data = new HashMap<>(v.size());
                    v.forEach((k2, v2) -> data.put(k2, v2.get("value")));
                    input.getData().put(k, data);
                });
            } else {
                for (Map<String, Map<String, Object>> data : reqBody.values()) {
                    data.forEach((k, v) -> input.getData().put(k, v.get("value")));
                }
            }
            ApiDoc apiDoc = new ApiDoc();
            apiDoc.setInput(input);
            apiDoc.setOutput(wrapOutput(ruleEngine));
            return apiDoc;
        } catch (Exception e){
            logger.error("获取决策服务调用文档失败. id: {}", id, e);
            throw new IllegalStateException("获取服务调用文档失败");
        }
    }

    private Map<String, Map<String, Map<String, Object>>> getReqBody(Map<String, Object> objectMap) throws InvocationTargetException, IllegalAccessException, IntrospectionException {
        Map<String, Map<String, Map<String, Object>>> reqBody = new HashMap<>();
        for (Map.Entry<String, Object> me : objectMap.entrySet()) {
            Map<String, Map<String, Object>> data = parseObject(me.getValue());
            if (!data.isEmpty()) {
                reqBody.put(me.getKey(), data);
            }
        }
        return reqBody;
    }

    /**
    * 描述: 解析对象生产map ，过滤掉非传入字段
    * @param: [value]
    * @author: 周庚新
    * @date: 2020/11/2
    * @return: java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.Object>>
    *
    */
    private Map<String, Map<String, Object>> parseObject(Object obj) throws InvocationTargetException, IllegalAccessException, IntrospectionException {
        Map<String, Map<String, Object>> data = new HashMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            PassingDirection direction = field.getAnnotation(PassingDirection.class);
            if (PassingDirection.Direction.OUT.equals(direction.value())
                    || PassingDirection.Direction.NONE.equals(direction.value())) {
                continue;
            }

            Map<String, Object> map = new HashMap<>(2);
            Object value = PropertyUtils.getProperty(obj, field.getName());
            map.put("value" ,value);
            if (field.isAnnotationPresent(Required.class)) {
                map.put("required", true);
            }
            data.put(field.getName(), map);
            field.setAccessible(false);
        }
        return data;
    }
}
