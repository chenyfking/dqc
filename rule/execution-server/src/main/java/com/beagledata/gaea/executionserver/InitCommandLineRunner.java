package com.beagledata.gaea.executionserver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beagledata.common.SpringBeanHolder;
import com.beagledata.gaea.common.OkHttpClientFactory;
import com.beagledata.gaea.common.RestConstants;
import com.beagledata.gaea.executionserver.common.ResourceResolver;
import com.beagledata.gaea.executionserver.config.DefaultConfigs;
import com.beagledata.gaea.ruleengine.runtime.RuleContext;
import com.beagledata.util.StringUtils;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * Created by liulu on 2020/5/20.
 */
@Component
public class InitCommandLineRunner implements CommandLineRunner {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DefaultConfigs defaultConfigs;
	@Autowired
	private ApplicationContext ctx;
	@Autowired
	private ServerProperties serverProperties;
	@Autowired
	private DataSource dataSource;
	@Autowired
	private ResourceResolver resourceResolver;

	@Override
	public void run(String... strings) throws Exception {
		SpringBeanHolder.initApplicationContext(ctx);
		registerClient(); // 注册集群节点到workbench
		// 服务启动把数据源注入到规则上下文
		RuleContext.setDataSource(dataSource);
		System.setProperty("gaea.tmpdir", resourceResolver.getTmpDirPath());
	}

	private void registerClient() throws IOException {
		String registerUrl = getRegisterClientUrl();
		if (registerUrl == null) {
			logger.info("没有配置注册地址，不注册集群节点");
			return;
		}

		String baseUrl = getBaseUrl();
		logger.info("注册集群节点. 注册地址：{}, 当前节点地址：{}", registerUrl, baseUrl);

		OkHttpClient httpClient = OkHttpClientFactory.get();
		RequestBody formBody = new FormBody.Builder()
				.add("baseUrl", baseUrl)
				.build();
		Request request = new Request.Builder().url(registerUrl).post(formBody).build();
		try (Response response = httpClient.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				logger.error("注册集群节点失败, 接口报错: {}", response);
				throw new IllegalStateException("注册集群节点失败");
			}

			JSONObject result = JSON.parseObject(response.body().string());
			if (result.getIntValue("code") != 0) {
				logger.error("注册集群节点失败, 错误码不等0, 返回结果: {}", result);
				throw new IllegalStateException("注册集群节点失败");
			}

			logger.info("注册集群节点成功");
		} catch (IOException e) {
			logger.error("注册集群节点失败", e);
			throw e;
		}
	}

	private String getRegisterClientUrl() {
		if (StringUtils.isBlank(defaultConfigs.getWorkbench().getBaseUrl())) {
			return null;
		}

		return StringUtils.concatUrl(defaultConfigs.getWorkbench().getBaseUrl(), RestConstants.Workbench.Endpoints.REGISTER_CLIENT);
	}

	private String getBaseUrl() {
		StringBuilder buffer = new StringBuilder("http://");
		buffer.append(serverProperties.getAddress().getHostAddress()).append(":");
		buffer.append(serverProperties.getPort());
		buffer.append(serverProperties.getContextPath());
		return buffer.toString();
	}
}
