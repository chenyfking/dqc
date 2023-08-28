package com.beagledata.gaea.workbench.interceptor;

import com.beagledata.gaea.common.RedisConstants;
import com.beagledata.gaea.workbench.common.BizCode;
import com.beagledata.gaea.workbench.config.DefaultConfigs;
import com.beagledata.gaea.workbench.exception.PasswordInvalidException;
import com.beagledata.gaea.workbench.util.DateUtils;
import com.beagledata.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @Auther: zhouyaming
 * @Date: 0019 2021/1/19 14:57
 * @Description: 密码重置拦截器：初次登陆/密码过期，强制修改密码
 */
public class PasswordValidateInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private DefaultConfigs defaultConfigs;

    @Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//        check(request);
        return true;
	}

	private void check(HttpServletRequest request) throws PasswordInvalidException {
        String firstLogin = (String) request.getSession().getAttribute(RedisConstants.Login.FIRST_LOGIN_TO_EDITPWD);

        if (StringUtils.isNotBlank(firstLogin)) {
            throw new PasswordInvalidException(BizCode.FIRST_LOGIN_TO_EDITPWD, "初次登录必需修改密码");
        }
        Date lastResetPwdDate = (Date) request.getSession().getAttribute(RedisConstants.Login.LAST_RESET_PWD_DATE);
        if (lastResetPwdDate != null) {
            int duration = DateUtils.durationDays(lastResetPwdDate, new Date());
            if (duration >= defaultConfigs.getPwdExpiredTime()) {
                throw new PasswordInvalidException(BizCode.PASSWORD_EXPIRED, "密码过期");
            }
        }
    }
}
