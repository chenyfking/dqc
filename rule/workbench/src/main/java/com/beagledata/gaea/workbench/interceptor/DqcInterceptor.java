package com.beagledata.gaea.workbench.interceptor;

import com.beagledata.gaea.workbench.common.ResourceResolver;
import com.beagledata.gaea.workbench.exception.DqcNoLoginException;
import com.beagledata.gaea.workbench.util.IpUtils;
import com.beagledata.gaea.workbench.util.UserSessionManager;
import com.beagledata.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截请求,判断主系统dqc的用户是否已经退出
 */
public class DqcInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private UserSessionManager userSessionManager;
    @Autowired
    private ResourceResolver resourceResolver;

    @Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//        String authorization = request.getHeader("Authorization");
//        if (StringUtils.isBlank(authorization)) {
//            throw new DqcNoLoginException("您还没有登录");
//        }
//        if (!userSessionManager.containsDqcInfo(new String(Base64.getDecoder().decode(authorization)))) {
//            throw new DqcNoLoginException("您还没有登录");
//        }
        if (!resourceResolver.isOpenDqcSession()) {
            return true;
        }
        userSessionManager.showInfo();
        String ip = IpUtils.getRealIp(request);
        if (StringUtils.isBlank(ip)) {
            throw new DqcNoLoginException("您还没有登录");
        }
        if (!userSessionManager.containsDqcInfo(ip)) {
            throw new DqcNoLoginException("您还没有登录");
        }
        return true;
	}

	private boolean isDefaultUri(String uri) {
        return uri.contains("decision/project") || uri.contains("decision/aimodel");
    }

    public static void main(String[] args) {
        String a = "dfsafda?info=fdsfsd";
        System.out.println(a.substring(a.indexOf("?info=") + 6));
    }
}
