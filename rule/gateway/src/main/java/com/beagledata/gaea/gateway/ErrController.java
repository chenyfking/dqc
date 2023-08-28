package com.beagledata.gaea.gateway;

import com.beagledata.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by liulu on 2018/1/9.
 */
@RestController
public class ErrController implements ErrorController {
    private static final Logger logger = LoggerFactory.getLogger(ErrController.class);
    private static final String ERROR_PATH = "/error";

    @Autowired
    private ErrorAttributes errorAttributes;

    @RequestMapping(value = ERROR_PATH)
    public Result handleJsonError(HttpServletRequest request, HttpServletResponse response){
        logErrorMsg(request);
        if (response.getStatus() == HttpStatus.NOT_FOUND.value()) {
            return Result.newError().withCode(HttpStatus.NOT_FOUND.value()).withMsg(HttpStatus.NOT_FOUND.getReasonPhrase());
        }
        return Result.newError().withMsg("系统繁忙，请稍候再试");
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

    private void logErrorMsg(HttpServletRequest request) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        if (requestAttributes != null) {
            Map<String, Object> errorInfo = errorAttributes.getErrorAttributes(requestAttributes, true);
            if (errorInfo != null && errorInfo.size() > 0) {
                logger.warn("ErrorController捕获异常: ");
                for (Map.Entry<String, Object> entry : errorInfo.entrySet()) {
                    logger.warn("{} : {}", entry.getKey(), entry.getValue());
                }
                logger.warn("异常信息结束");
            }
        }
    }
}
