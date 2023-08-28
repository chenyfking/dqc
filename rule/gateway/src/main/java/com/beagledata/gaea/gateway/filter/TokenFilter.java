package com.beagledata.gaea.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.beagledata.common.Result;
import com.beagledata.gaea.gateway.entity.MicroToken;
import com.beagledata.gaea.gateway.mapper.MicroTokenMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 校验请求token
 *
 * Created by liulu on 2020/6/29.
 */
public class TokenFilter extends ZuulFilter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private AtomicReference<Set<MicroToken>> tokensRef = new AtomicReference<>();

    private MicroTokenMapper microTokenMapper;

    @PostConstruct
    public void init() {
        refreshToken();
    }

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String uri = request.getRequestURI();
        boolean flag = uri.startsWith(request.getContextPath() + "/execute/") || uri.startsWith(request.getContextPath() + "/executeasync/");
        return flag;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        if (!verifyToken(request)) {
            refuseRequest(ctx);
            return null;
        }

        acceptRequest(ctx);
        return null;
    }

    private boolean verifyToken(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            if (logger.isDebugEnabled()) {
                logger.debug("令牌校验失败：令牌为空.");
            }
            return false;
        }

        Set<MicroToken> tokens = tokensRef.get();
        Optional<MicroToken> optional = tokens.stream().filter(t -> token.equals(t.getToken())).findFirst();
        if (!optional.isPresent()) {
            if (logger.isDebugEnabled()) {
                logger.debug("令牌校验失败：令牌不存在. token: {}", token);
            }
            return false;
        }

        MicroToken microToken = optional.get();
        if (microToken.isAll()) {
            if (logger.isDebugEnabled()) {
                logger.debug("令牌校验成功：令牌支持所有服务. token: {}", token);
            }
            return true;
        }

        String uri = request.getRequestURI();
        String microUuid =uri.substring(uri.lastIndexOf("/") + 1);
        if (!microToken.getMicroUuids().contains(microUuid)) {
            if (logger.isDebugEnabled()) {
                logger.debug("令牌校验失败：令牌无权限. token: {}, microUuid: {}", token, microUuid);
            }
            return false;
        }

        return true;
    }

    private void acceptRequest(RequestContext ctx) {
        ctx.setSendZuulResponse(true);
        ctx.setResponseStatusCode(200);
    }

    private void refuseRequest(RequestContext ctx) {
        ctx.setSendZuulResponse(false);
        ctx.setResponseStatusCode(HttpStatus.OK.value());
        ctx.setResponseBody(JSON.toJSONString(Result.newInstance(HttpStatus.FORBIDDEN.value(), "没有权限执行此操作")));
        ctx.getResponse().setContentType("application/json;charset=UTF-8");
    }

    @EventListener(TokensRefreshedEvent.class)
    public void refreshToken() {
        logger.info("刷新令牌");
        doRefreshToken();
    }

    private void doRefreshToken() {
        tokensRef.set(new HashSet<>(microTokenMapper.selectAll()));
    }

    public static class TokensRefreshedEvent extends ApplicationEvent {
        public TokensRefreshedEvent(Object source) {
            super(source);
        }
    }

    public void setMicroTokenMapper(MicroTokenMapper microTokenMapper) {
        this.microTokenMapper = microTokenMapper;
    }
}
