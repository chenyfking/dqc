package com.beagledata.gaea.executioncore.handler;

import com.alibaba.fastjson.JSON;
import com.beagledata.gaea.executioncore.domain.ApiDoc;
import com.beagledata.gaea.executioncore.domain.Input;
import com.beagledata.gaea.executioncore.domain.Output;
import com.beagledata.gaea.ruleengine.aimodel.Invoker;
import com.beagledata.gaea.ruleengine.aimodel.JarInvoker;
import com.beagledata.gaea.ruleengine.aimodel.ModelMetadata;
import com.beagledata.gaea.ruleengine.aimodel.PmmlInvoker;
import com.beagledata.gaea.ruleengine.exception.RuleException;
import com.beagledata.util.StringUtils;

import java.io.File;
import java.util.Map;

/**
 * Created by liulu on 2020/5/18.
 */
public class AiModelHandler extends AbstractModelHandler {
    protected Invoker invoker;
    protected ModelMetadata metadata;

    public AiModelHandler(File file) {
        metadata = new ModelMetadata(file);
        try {
            if (file.getName().endsWith(".jar")) {
                invoker = JarInvoker.newInstance(file);
            } else {
                invoker = PmmlInvoker.newInstance(file);
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            throw new RuleException("初始化AI模型处理器失败");
        }
    }

    @Override
    protected Output doHandle(Input input) throws Exception {
        String result = invoker.invoke(input.getData());
        Output output = new Output();
        if (StringUtils.isNotBlank(result)) {
            output.setResponse(JSON.parseObject(result, Map.class));
        }
        return output;
    }

    @Override
    public ApiDoc getApiDoc() {
        Input input = new Input();
        try {
            for (Map.Entry<String, Class> entry : metadata.getParams().entrySet()) {
                input.getData().put(entry.getKey(), metadata.getParamDefaultValue(entry.getValue()));
            }
            ApiDoc apiDoc = new ApiDoc();
            apiDoc.setInput(input);
            apiDoc.setOutput(doHandle(input));
            return apiDoc;
        } catch (Exception e){
            logger.error("获取模型服务调用文档失败.", e);
            throw new IllegalStateException("获取服务调用文档失败");
        }
    }
}
