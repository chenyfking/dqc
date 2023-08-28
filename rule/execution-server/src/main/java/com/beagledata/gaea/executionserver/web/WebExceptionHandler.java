package com.beagledata.gaea.executionserver.web;

import com.beagledata.common.Result;
import com.beagledata.gaea.common.LogManager;
import com.beagledata.gaea.executionserver.common.Constants;
import com.beagledata.gaea.ruleengine.exception.RuleException;
import com.beagledata.license.LicenseException;
import com.beagledata.license.Utils;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by liulu on 2017/12/19.
 */
@ResponseBody
@ControllerAdvice
public class WebExceptionHandler {
    private static Logger logger = LogManager.getLogger(WebExceptionHandler.class);

    /**
     * 未知异常
     *
     * @author liulu
     * 2018/1/11 9:57
     */
    @ExceptionHandler(Exception.class)
    public Result exceptionHandler(Exception e) {
        logger.error(e.getLocalizedMessage(), e);
		return Result.newError().withMsg("系统繁忙，请稍候再试");
    }

    /**
     * 请求方法错误异常
     *
     * @author liulu
     * 2018/1/11 9:56
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException e) {
        String[] supportedMethods = e.getSupportedMethods();
        if (supportedMethods != null && supportedMethods.length > 0) {
            return Result.newError()
                    .withCode(HttpStatus.METHOD_NOT_ALLOWED.value())
                    .withMsg("请使用" + supportedMethods[0] + "请求");
        }
        return Result.newError().withCode(HttpStatus.METHOD_NOT_ALLOWED.value()).withMsg("不支持当前请求方法");
    }

    /**
     * 业务错误异常
     *
     * @author liulu
     * 2018/1/11 9:56
     */
    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            RuleException.class
    })
    public Result illegalExceptionHandler(Exception e) {
        return Result.newError().withMsg(e.getMessage());
    }

    /**
     * 前端参数校验异常
     *
     * @author liulu
     * 2018/1/11 9:56
     */
    @ExceptionHandler({
            BindException.class,
            HttpMessageNotReadableException.class
    })
    public Result bindExceptionHandler() {
        return Result.newError().withMsg("参数校验出错，请检查参数");
    }


    /**
     * license异常
     * @author chenyafeng
     * 2018/2/6
     */
    @ExceptionHandler({
            LicenseException.class
    })
    public Result licenseExceptionHandler(LicenseException le) throws Exception {
        int code = le.getCode();
        String msg;
		switch(code) {
		case LicenseException.CODE_EXPIRED://license过期
			msg = "license过期";
			break;
		case LicenseException.CODE_INVALID://license无效
			msg = "license无效";
			break;
		default:
			msg = "license无效";
			break;
		}
        return Result.newError().withCode(Constants.LICENSE_INVALID).withMsg(msg).withData(Utils.getSn());
    }
}
