package com.beagledata.gaea.gateway;

import com.beagledata.common.Result;
import com.beagledata.gaea.common.LogManager;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by liulu on 2017/12/19.
 */
@ControllerAdvice
@ResponseBody
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
            IllegalStateException.class
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
    @ExceptionHandler(BindException.class)
    public Result bindExceptionHandler() {
        return Result.newError().withMsg("参数校验出错，请检查参数");
    }
}
