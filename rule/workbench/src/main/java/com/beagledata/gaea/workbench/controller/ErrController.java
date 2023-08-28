package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.security.listener.SecurityListener;
import org.apache.shiro.authz.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by liulu on 2018/1/9.
 */
@Controller
public class ErrController implements ErrorController {
    private static final Logger logger = LoggerFactory.getLogger(ErrController.class);

    private static final String ERROR_PATH = "/error";

    @Autowired
    private ErrorAttributes errorAttributes;
    @Autowired
    private SecurityListener securityListener;

    @RequestMapping(value = ERROR_PATH)
    @ResponseBody
    public Object handleJsonError(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpStatus.OK.value());

        Throwable e = errorAttributes.getError(new ServletRequestAttributes(request));
        if (e instanceof UnauthorizedException) {
            // 处理权限异常
            return securityListener.onAuthorizeDenied();
        }

        showErrorMsg(request);
        return Result.newError().withCode(HttpStatus.NOT_FOUND.value()).withMsg("404 not found!");
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

    private void showErrorMsg(HttpServletRequest request) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        if (requestAttributes != null) {
            Map<String, Object> errorInfo = errorAttributes.getErrorAttributes(requestAttributes, true);
            if (errorInfo != null && errorInfo.size() > 0) {
                logger.warn("ErrorController捕获异常: ");
                for (Map.Entry<String, Object> entry : errorInfo.entrySet()) {
                    logger.warn("{}: {}", entry.getKey(), entry.getValue());
                }
                logger.warn("异常信息结束");
            }
        }
    }
}
