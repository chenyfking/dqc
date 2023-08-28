package com.beagledata.gaea.executioncore.handler;

import com.beagledata.gaea.executioncore.domain.ApiDoc;
import com.beagledata.gaea.executioncore.domain.Input;
import com.beagledata.gaea.executioncore.domain.Output;
import com.beagledata.gaea.ruleengine.exception.MissingParamsException;
import com.beagledata.gaea.ruleengine.exception.RuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liulu on 2020/5/15.
 */
public abstract class AbstractModelHandler implements ModelHandler {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Output handle(Input input) {
        try {
            return doHandle(input);
        } catch (MissingParamsException e) {
            logger.error("服务执行失败:{} input：{}", e.getLocalizedMessage(), input);
            throw e;
        }  catch (RuleException e) {
            logger.error("服务执行失败. input：{}", input, e);
            throw e;
        } catch (Exception e) {
            logger.error("服务执行失败. input：{}", input, e);
            throw new IllegalStateException("服务执行失败");
        }
    }

    protected abstract Output doHandle(Input input) throws Exception;
}
