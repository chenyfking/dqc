package com.beagledata.gaea.workbench.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beagledata.common.Result;
import com.beagledata.gaea.common.LogManager;
import com.beagledata.gaea.common.OkHttpClientFactory;
import com.beagledata.gaea.common.RestConstants;
import com.beagledata.gaea.workbench.entity.Client;
import com.beagledata.gaea.workbench.mapper.ClientMapper;
import com.beagledata.gaea.workbench.mapper.MicroMapper;
import com.beagledata.gaea.workbench.service.ClientService;
import com.beagledata.util.IdUtils;
import com.beagledata.util.StringUtils;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * yangyongqiang  2019/06/11
 */
@Service
public class ClientServiceImpl implements ClientService {
    private static Logger logger = LogManager.getLogger(ClientServiceImpl.class);

    private static OkHttpClient httpClient = OkHttpClientFactory.get();

    @Autowired
    private ClientMapper clientMapper;
    @Autowired
    private MicroMapper microMapper;

    @Override
    public void add(Client client) {
        validateClient(client);

        try {
            int count = clientMapper.countByBaseUrl(client.getBaseUrl());
            if (count > 0) {
                throw new IllegalArgumentException("集群节点地址已存在，请不要重复添加");
            }

            String serialNumber = validateBaseUrl(client);
            client.setSerialNumber(serialNumber);
            client.setUuid(IdUtils.UUID());
            clientMapper.insert(client);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (DuplicateKeyException de) {
            throw new IllegalArgumentException("集群节点地址已存在，请不要重复添加");
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            throw new IllegalStateException("添加集群节点失败");
        }
    }

    @Override
    public void delete(String uuid) {
        if (StringUtils.isBlank(uuid)) {
            logger.warn("删除集群节点出错, uuid为空, uuid: {}", uuid);
            return;
        }

        try {
            clientMapper.delete(uuid);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            throw new IllegalStateException("删除集群节点失败");
        }
    }

    @Override
    public void update(Client client) {
        validateClient(client);

        try {
            String serialNumber = validateBaseUrl(client);
            client.setSerialNumber(serialNumber);
            clientMapper.update(client);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("集群节点地址已存在，请不要重复添加");
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            throw new IllegalStateException("编辑集群节点失败");
        }
    }

    @Override
    public void enable(String uuid) {
        if (StringUtils.isBlank(uuid)) {
            logger.warn("启用集群节点失败, uuid为空");
            throw new IllegalArgumentException("启用集群节点失败, uuid为空");
        }

        try {
            Client client = new Client();
            client.setUuid(uuid);
            client.setDisabled(false);
            clientMapper.update(client);
        } catch (Exception e) {
            logger.error("启用集群节点失败. uuid: {}", uuid, e);
            throw new IllegalStateException("启用集群节点失败");
        }
    }

    @Override
    public void disable(String uuid) {
        if (StringUtils.isBlank(uuid)) {
            logger.warn("禁用集群节点失败, uuid为空");
            throw new IllegalArgumentException("禁用集群节点失败, uuid为空");
        }

        try {
            Client client = new Client();
            client.setUuid(uuid);
            client.setDisabled(true);
            clientMapper.update(client);
        } catch (Exception e) {
            logger.error("禁用集群节点失败. uuid: {}", uuid, e);
            throw new IllegalStateException("禁用集群节点失败");
        }
    }

    @Override
    public Result getRunningMicro(String clientUuid) {
        if (StringUtils.isBlank(clientUuid)) {
            logger.info("查询集群节点的服务，集群节点uuid为空");
            return Result.newError().withMsg("参数不能为空");
        }
        try {
            return Result.newSuccess().withData(microMapper.selectByClient(clientUuid));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            throw new IllegalArgumentException("查询集群节点上服务出错");
        }
    }

    @Override
    public Result getClientsByMicro(String microUuid) {
        if (StringUtils.isBlank(microUuid)) {
            logger.info("查询服务上线的集群节点，服务uuid为空");
            return Result.newError().withMsg("参数不能为空");
        }
        try {
            List<Client> clients = clientMapper.selectByMicro(microUuid);
            return Result.newSuccess().withData(clients);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            throw new IllegalArgumentException("查询服务上线的集群节点出错");
        }
    }

    @Override
    public Result uploadLicense(String clientUuid, MultipartFile file) {
        if (StringUtil.isBlank(clientUuid)) {
            logger.warn("上传集群节点license，集群节点uuid为空");
            return Result.newError().withMsg("参数不能为空");
        }
        if (null == file){
            logger.info("上传集群节点license，文件为空");
            return Result.newError().withMsg("license文件不能为空");
        }
        String license;
        try {
            license = IOUtils.toString(file.getInputStream());
            if (StringUtils.isBlank(license)) {
                logger.warn("license文件无效: {}", file);
                throw new IllegalArgumentException("license文件无效");
            }
        } catch (IOException e) {
            logger.warn("读取license文件失败: {}", file);
            throw new IllegalStateException("读取license文件失败");
        }

        try {
            Client client = clientMapper.selectByUuid(clientUuid);
            if (client == null) {
                throw new IllegalArgumentException("集群节点不存在");
            }

            boolean uploadSuccess = pushLicense2Client(client.getBaseUrl(), license);
            if (uploadSuccess) {
                return Result.newSuccess();
            }

            throw new IllegalStateException("上传license失败");
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            logger.error(license + ", " + e.getLocalizedMessage(), e);
            throw new IllegalStateException("上传license失败");
        }
    }

    @Override
    public List<Client> listAll() {
        try {
            return clientMapper.selectAll();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Client> listAvailables() {
        try {
            return clientMapper.selectAll()
                    .stream()
                    .filter(client -> !Optional.ofNullable(client.getDisabled()).orElse(false))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Collections.emptyList();
    }

    @Override
    public void deployRule(String baseUrl, String microUuid, byte[] bytes) throws IOException {
		RequestBody requestBody = getMultipartBodyBuilder(microUuid)
				.addFormDataPart("type", RestConstants.MicroType.RULE.name())
				.addFormDataPart("file", microUuid + ".zip",
						RequestBody.create(MediaType.parse("application/zip"), bytes))
				.build();
        doDeploy(baseUrl, microUuid, requestBody);
    }

    @Override
    public void deployModel(String baseUrl, String microUuid, File file) throws IOException {
        RequestBody requestBody = getMultipartBodyBuilder(microUuid)
				.addFormDataPart("type",
                        file.getName().endsWith(".jar") ? RestConstants.MicroType.MODEL.name() : RestConstants.MicroType.PMML.name()
                )
				.addFormDataPart("file", file.getName(),
						RequestBody.create(MediaType.parse("application/octet-stream"), IOUtils.toByteArray(file.toURI())))
				.build();
        doDeploy(baseUrl, microUuid, requestBody);
    }

    @Override
    public void unDeploy(String baseUrl, String microUuid) throws IOException {
        String url = StringUtils.concatUrl(baseUrl, RestConstants.OpenApi.Endpoints.UNDEPLOY);
        Request request = new Request.Builder()
				.url(url)
				.post(new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("microId", microUuid).build())
				.build();
		try (Response response = httpClient.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				logger.warn("删除集群节点的服务失败, clientUrl: {}, microUuid: {}, 接口响应非200报错: {}", baseUrl, microUuid, response);
				throw new IllegalStateException("删除服务失败");
			}

			JSONObject result = JSON.parseObject(response.body().string());
			if (result.getIntValue("code") != 0) {
				logger.warn("删除集群节点的服务失败, clientUrl: {}, microUuid: {}, 集群节点返回结果: {}", baseUrl, microUuid, result);
				throw new IllegalStateException("删除服务失败");
			}

			logger.info("删除集群节点的服务成功. clientUrl: {}, microUuid: {}", baseUrl, microUuid);
		}
    }

    private MultipartBody.Builder getMultipartBodyBuilder(String microUuid) {
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("uuid", microUuid);
    }

    private void doDeploy(String baseUrl, String microUuid, RequestBody requestBody) throws IOException {
        String url = StringUtils.concatUrl(baseUrl, RestConstants.OpenApi.Endpoints.DEPLOY);
        Request request = new Request.Builder()
				.url(url)
				.post(requestBody)
				.build();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();
        long time = System.currentTimeMillis();
		try (Response response = okHttpClient.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				logger.warn("发布服务到集群节点失败, clientUrl: {}, microUuid: {}, 接口响应非200报错: {}", baseUrl, microUuid, response);
				throw new IllegalStateException("发布到集群节点失败");
			}

			JSONObject result = JSON.parseObject(response.body().string());
			if (result.getIntValue("code") != 0) {
				logger.warn("发布服务到集群节点失败, clientUrl: {}, microUuid: {}, 集群节点返回结果: {}", baseUrl, microUuid, result);
				throw new IllegalStateException("发布到集群节点失败");
			}

			logger.info("发布服务到集群节点成功. clientUrl: {}, microUuid: {}, 耗时: {}ms", baseUrl, microUuid, System.currentTimeMillis() - time);
		}
    }

    /**
     * 校验输入
     *
     * @param client
     */
    private void validateClient(Client client) {
        if (StringUtils.isBlank(client.getName()) || client.getName().length() > 20) {
            throw new IllegalArgumentException("集群节点名称不能为空并且长度不能超过20个字符");
        }
        if (StringUtils.isBlank(client.getBaseUrl())) {
            throw new IllegalArgumentException("集群节点地址不能为空");
        }
    }

    /**
     * 校验接口地址
     *
     * @param client
     */
    private String validateBaseUrl(Client client) {
        String serialNumber = requestClientSerialNumber(client.getBaseUrl());
        if (serialNumber == null) {
            // 根据集群节点地址调用获取序列号接口，未正确拿到序列号，返回前端检查地址
            throw new IllegalArgumentException("集群节点连接失败，请检查地址是否正确");
        }

        return serialNumber;
    }

    private String requestClientSerialNumber(String baseUrl) {
        String url = StringUtils.concatUrl(baseUrl, RestConstants.OpenApi.Endpoints.SERIALNUMBER);
        Request request = new Request.Builder().url(url).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JSONObject result = JSON.parseObject(response.body().string());
                if (result.getIntValue("code") == 0) {
                    return result.getString("data");
                }

                logger.error("获取集群节点序列号失败, baseUrl: {}, 接口返回: {}", baseUrl, result);
                return null;
            }

            logger.warn("获取集群节点序列号失败, baseUrl: {}, 接口报错: {}", baseUrl, response);
            return null;
        } catch (IOException e) {
            logger.warn("获取集群节点序列号失败: " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    private boolean pushLicense2Client(String baseUrl, String license) throws IOException {
        String url = StringUtils.concatUrl(baseUrl, RestConstants.OpenApi.Endpoints.UPLOAD_LICENSE);
        RequestBody formBody = new FormBody.Builder()
                .add("license", license)
                .build();
        Request request = new Request.Builder().url(url).post(formBody).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JSONObject result = JSON.parseObject(response.body().string());
                if (result.getIntValue("code") == 0) {
                    return true;
                }

                logger.warn("上传license失败, 返回结果: {}", result);
            } else {
                logger.warn("上传license失败, 接口报错: {}", response);
            }
        }
        return false;
    }
}
