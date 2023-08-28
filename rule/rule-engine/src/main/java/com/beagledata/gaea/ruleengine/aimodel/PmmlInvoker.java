package com.beagledata.gaea.ruleengine.aimodel;

import com.alibaba.fastjson.JSONObject;
import com.beagledata.gaea.ruleengine.util.PropertyUtils;
import org.apache.commons.io.FileUtils;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.dom4j.DocumentException;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.evaluator.ProbabilityDistribution;
import org.jpmml.evaluator.tree.NodeScoreDistribution;
import org.jpmml.model.PMMLUtil;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mahongfei on 2019/5/7.
 */
public class PmmlInvoker implements Invoker {
    private ModelEvaluator modelEvaluator;
    private Map<String, Class> params;

    private PmmlInvoker() {}

    public static PmmlInvoker newInstance(File file) throws JAXBException, SAXException, IOException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, DocumentException, NoSuchMethodException, ClassNotFoundException {
        PMML pmml = PMMLUtil.unmarshal(FileUtils.openInputStream(file));
        ModelEvaluator modelEvaluator = ModelEvaluatorFactory.newInstance().newModelEvaluator(pmml);
        modelEvaluator.verify();

        PmmlInvoker invoker = new PmmlInvoker();
        invoker.setModelEvaluator(modelEvaluator);
        invoker.setParams(new ModelMetadata(file).getParams());
        return invoker;
    }

    public void setModelEvaluator(ModelEvaluator modelEvaluator) {
        this.modelEvaluator = modelEvaluator;
    }

    public void setParams(Map<String, Class> params) {
        this.params = params;
    }

    @Override
    public String invoke(Object input) throws Exception {
        Map<FieldName, Object> fieldMap = getFieldMap(input);
        Map<FieldName, Object> resultMap = modelEvaluator.evaluate(fieldMap);
        JSONObject json = new JSONObject();
        for (Map.Entry<FieldName, Object> entry : resultMap.entrySet()) {
            if (entry.getValue() instanceof NodeScoreDistribution) {
                NodeScoreDistribution nsd = (NodeScoreDistribution)entry.getValue();
                json.put(entry.getKey().getValue(), nsd.getResult());
                break;
            } else if (entry.getValue() instanceof ProbabilityDistribution) {
                ProbabilityDistribution pd = (ProbabilityDistribution)entry.getValue();
                json.put(entry.getKey().getValue(), pd.getResult());
                break;
            } else {
                json.put(entry.getKey().getValue(), entry.getValue());
            }
        }
        return json.toJSONString();
    }

    private Map<FieldName, Object> getFieldMap(Object input) throws InstantiationException, IllegalAccessException {
        Map<FieldName, Object> fieldMap = new HashMap<>();
        Map<String, Object> propMap = PropertyUtils.getProperties(input);
        for (Map.Entry<String, Class> entry : params.entrySet()) {
            Object value = propMap.get(entry.getKey());
            if (value == null) {
                value = getDefaultValue(entry.getValue());
            }
            //传入值类型为Bigdecimal ,实际接收参数类型为Double ，需要转换
            if ((value instanceof BigDecimal) && Double.class.equals(entry.getValue())) {
                value = ((BigDecimal) value).doubleValue();
            }
            fieldMap.put(new FieldName(entry.getKey()), value);
        }
        return fieldMap;
    }

    private Object getDefaultValue(Class clazz) throws IllegalAccessException, InstantiationException {
        if (Integer.class.equals(clazz) || Long.class.equals(clazz) || Short.class.equals(clazz)) {
            return 0;
        } else if (Double.class.equals(clazz) || Float.class.equals(clazz)) {
            return 0.0;
        } else if (Boolean.class.equals(clazz)) {
            return false;
        }
        return clazz.newInstance();
    }
}
