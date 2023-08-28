package com.beagledata.gaea.workbench;

import com.beagledata.common.Result;
import com.beagledata.gaea.common.LogManager;
import com.beagledata.gaea.ruleengine.exception.RuleException;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.entity.Permission;
import com.beagledata.gaea.workbench.entity.PermissionSet;
import com.beagledata.gaea.workbench.exception.DqcNoLoginException;
import com.beagledata.gaea.workbench.exception.InputNotMatchException;
import com.beagledata.gaea.workbench.exception.NoPermissionException;
import com.beagledata.gaea.workbench.exception.PasswordInvalidException;
import com.beagledata.license.LicenseException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.MultipartProperties;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartException;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by liulu on 2017/12/19.
 */
@ControllerAdvice
@ResponseBody
public class WebExceptionHandler {
    private static Logger logger = LogManager.getLogger(WebExceptionHandler.class);

    @Autowired
    private MultipartProperties multipartProperties;

    /**
     * 未知异常
     *
     * @author liulu
     * 2018/1/11 9:57
     */
    @ExceptionHandler(Exception.class)
    public Result exceptionHandler(Exception e) {
        logger.error(e.getLocalizedMessage(), e);
//		return Result.newError().withMsg("系统繁忙，请稍候再试");
        return Result.newError().withMsg("请检查规则配置");
    }

    @ExceptionHandler(MultipartException.class)
    public Result multipartExceptionHandler(MultipartException e) {
        Throwable t = e.getRootCause();
        if (t instanceof FileUploadBase.SizeLimitExceededException) {
            return Result.newError().withMsg(
                    String.format("上传文件大小不能超过%s", multipartProperties.getMaxFileSize())
            );
        }
		return Result.newError().withMsg("文件上传失败");
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Result unauthorizedExceptionHandler(HttpServletRequest request) {
        HandlerMethod method = (HandlerMethod) request.getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingHandler");
        String msg = "没有可操作的权限";
        RequiresPermissions annotation = method.getMethod().getAnnotation(RequiresPermissions.class);
        if (annotation != null) {
            String[] rps = annotation.value();
            if (rps != null && rps.length > 0) {
                StringBuilder sb = new StringBuilder("没有");
                for (String rp : rps) {
                    for (Permission permission : PermissionSet.CONTEXT) {
                        if (rp.equals(String.format("%s:%s", permission.getResource(), permission.getAction()))) {
                            sb.append(permission.getName()).append("、");
                        }
                    }
                }
                sb.append("的权限");
                msg = sb.toString().replaceAll("、的", "的");
            }
        }
        return Result.newError().withCode(HttpStatus.FORBIDDEN.value()).withMsg(msg);
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
    @ExceptionHandler(BindException.class)
    public Result bindExceptionHandler() {
        return Result.newError().withMsg("参数校验出错，请检查参数");
    }

    /**
     * shiro权限异常
     *
     * @author liulu
     * 2018/1/11 9:56
     */
    @ExceptionHandler({
            AuthenticationException.class,
            AuthorizationException.class
    })
    public Result authorizationExceptionHandler(HttpServletRequest request) {
		return Result.newError().withCode(HttpStatus.FORBIDDEN.value()).withMsg("没有执行此操作的权限");
    }

    /**
     * license异常
     * @author chenyafeng
     * 2018/2/6
     */
    @ExceptionHandler({
            LicenseException.class
    })
    public Result licenseExceptionHandler(LicenseException le) {
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
		return Result.newError().withCode(Constants.License.LOGIN_LICENSE_INVALID).withMsg(msg);
    }

    /**
     * 权限异常
     * @author chenyafeng
     * @date 2018/11/14
     */
    @ExceptionHandler(NoPermissionException.class)
    public Result noPermissionException(NoPermissionException e) {
        return Result.newError().withMsg(e.getMessage());
    }

    /**
     * 输入参数格式不正确异常
     * @author chenyafeng
     * @date 2020/02/10
     */
    @ExceptionHandler(InputNotMatchException.class)
    public Result inputNotMatchExceptionException(InputNotMatchException e) {
        return Result.newError().withMsg(e.getMessage());
    }

    /**
     *密码校验异常
     * @author yinrj
     * @date 2021/01/20
     */
    @ExceptionHandler(PasswordInvalidException.class)
    public Result passwordInvalidException(PasswordInvalidException e) {
        return Result.newError().withCode(e.getCode()).withMsg(e.getMsg());
    }

    /**
     * dqc主系统未登录校验异常
     */
    @ExceptionHandler(DqcNoLoginException.class)
    public Result dqcNoLoginException(DqcNoLoginException e) {
        return Result.newError().withCode(333).withMsg(e.getMessage());
    }
}
