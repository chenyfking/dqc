package com.beagledata.gaea.ruleengine.aimodel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beagledata.gaea.ruleengine.util.ClassUtils;
import com.bigdata.bdtm.IModelPredict;
import com.bigdata.bdtm.ModelPredictFactroy;
import com.bigdata.bdtm.exceptions.ModelPredictException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by liulu on 2020/7/6.
 */
public class JarInvoker implements Invoker {
    private IModelPredict predict;

    private JarInvoker() {}

    public static JarInvoker newInstance(File file) throws IOException, InvocationTargetException, IllegalAccessException, ModelPredictException, ClassNotFoundException {
        String className = new ModelMetadata(file).getClassName();
        ClassUtils.loadJar(JarInvoker.class.getClassLoader(), file);
        JarInvoker invoker = new JarInvoker();
        invoker.setPredict(ModelPredictFactroy.getModelPredict(className));
        return invoker;
    }

    @Override
    public String invoke(Object input) throws Exception {
        String inputStr = JSON.toJSONString(input);
        Map<String, Object> map = (Map<String, Object>)JSON.parse(inputStr);
        Map<String, String> params = map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() != null ? entry.getValue().toString() : ""));
        JSONObject result = predict.predictOne(JSON.parseObject(JSON.toJSONString(params)));
        return result.toJSONString();
    }

    public void setPredict(IModelPredict predict) {
        this.predict = predict;
    }
}
