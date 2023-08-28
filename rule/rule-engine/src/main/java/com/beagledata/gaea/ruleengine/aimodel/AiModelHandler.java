package com.beagledata.gaea.ruleengine.aimodel;

import com.beagledata.gaea.ruleengine.exception.RuleException;
import com.beagledata.gaea.ruleengine.runtime.RuleContext;
import com.beagledata.gaea.ruleengine.util.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liulu on 2018/11/15.
 */
public class AiModelHandler {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public void handle(String fileName, Object input, Object output) {
		try {
			Invoker invoker = RuleContext.getCurrentContext().getRuleBuilder().getModelInvoker(fileName);
			String result = invoker.invoke(input);
			setOutput(output, result);
		} catch (Exception e) {
			logger.error("执行机器学习模型出错. fileName: {}, input: {}, output: {}", fileName, input, output, e);
			throw new RuleException("执行机器学习模型出错");
		}
	}

	private void setOutput(Object output, String result) throws IllegalAccessException {
        PropertyUtils.setProperties(output, result);
    }
}
