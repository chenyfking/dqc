package com.beagledata.gaea.workbench.security;

import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.config.DefaultConfigs;
import com.beagledata.gaea.workbench.entity.User;
import com.beagledata.gaea.workbench.util.UserSessionManager;
import com.beagledata.security.filter.DefaultAuthenticationFilter;
import com.beagledata.security.realm.JdbcTemplateRealm;
import com.beagledata.util.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;

/**
 * Created by Cyf on 2022/8/18
 **/
//@Component
public class CustomAuthenticationFilter extends DefaultAuthenticationFilter {
    @Autowired
    private UserSessionManager userSessionManager;

    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object mappedValue) {
        HttpServletRequest request = WebUtils.toHttp(servletRequest);
        HttpServletResponse response = WebUtils.toHttp(servletResponse);
        DefaultSecurityManager defaultSecurityManager = new DefaultSecurityManager();
        defaultSecurityManager.setRealm(new JdbcTemplateRealm());
        SecurityUtils.setSecurityManager(defaultSecurityManager);
        Subject subject = this.getSubject(request, response);
        //判断是否有token  请求头获取Authorization
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isBlank(authorization)) {
            if (null != subject && subject.isAuthenticated()) {
                // 如果本机登录则退出登录
                subject.logout();
            }
            //未携带token ，返回false 即认证失败
            return false;
        } else {
            if (userSessionManager.containsDqcInfo(new String(Base64.getDecoder().decode(authorization)))) {
                if (null == subject) {
                    SecurityUtils.setSecurityManager(new DefaultSecurityManager());
                    subject = SecurityUtils.getSubject();
                }
                if (!subject.isAuthenticated()) {
                    //没有登录，重新登录
                    AuthenticationToken tokens = new UsernamePasswordToken("superadmin", "JFIBL9l5Pp/7vfvzBqphVlQzFzxHagG28h4VNq2F3ok1u8O5KLmmH53aP20mxg/xY3Gbl42yVF9PCdriNlwCZQ==", false);
                    subject.login(tokens);
                }
                return true;
            } else {
                if (null != subject && subject.isAuthenticated()) {
                    subject.logout();
                }
                return false;
            }
        }
    }
}