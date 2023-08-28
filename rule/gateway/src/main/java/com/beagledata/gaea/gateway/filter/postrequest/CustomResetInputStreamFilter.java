package com.beagledata.gaea.gateway.filter.postrequest;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 描述:
 *
 * @author 周庚新
 * @date 2020-11-06
 */
public class CustomResetInputStreamFilter extends ZuulFilter {
	private Logger logger = LoggerFactory.getLogger(CustomResetInputStreamFilter.class);

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return 1;
	}

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		return !StringUtils.equals("GET", request.getMethod());
	}

	@Override
	public Object run() throws ZuulException {
		RequestContext ctx = RequestContext.getCurrentContext();
		setAutoResetRequestEntity(ctx);
		return null;
	}

	private void setAutoResetRequestEntity(RequestContext ctx) {
		HttpServletRequest request = ctx.getRequest();

		if (request.getContentLength() > 0) {
		    try {
		    	ctx.set("requestEntity", new CustomResetInputStream(IOUtils.toByteArray(request.getInputStream())));
			} catch (IOException e) {
				logger.error("could not get request input stream.", e);
			}
		}
	}
}