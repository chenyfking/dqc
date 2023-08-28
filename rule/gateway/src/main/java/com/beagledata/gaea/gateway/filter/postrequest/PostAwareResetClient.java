package com.beagledata.gaea.gateway.filter.postrequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.client.config.IClientConfig;
import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpResponse;
import com.netflix.niws.client.http.RestClient;
import org.springframework.cloud.netflix.zuul.filters.support.ResettableServletInputStreamWrapper;

/**
 * 描述:
 *
 * @author 周庚新
 * @date 2020-11-06
 */
public class PostAwareResetClient extends RestClient {

	public PostAwareResetClient(IClientConfig config) {
		super(config);
	}

	@Override
	public HttpResponse execute(HttpRequest request, IClientConfig config) throws Exception {
		if (request != null) {
			Object entity = request.getEntity();
			((ResettableServletInputStreamWrapper)entity).reset();
		}
		return super.execute(request, config);
	}
}