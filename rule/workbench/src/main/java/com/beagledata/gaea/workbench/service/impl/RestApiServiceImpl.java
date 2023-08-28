package com.beagledata.gaea.workbench.service.impl;

import com.beagledata.gaea.ruleengine.util.SafelyFiles;
import com.beagledata.gaea.workbench.common.ResourceResolver;
import com.beagledata.gaea.workbench.entity.Client;
import com.beagledata.gaea.workbench.entity.MicroDeployment;
import com.beagledata.gaea.workbench.mapper.ClientMapper;
import com.beagledata.gaea.workbench.mapper.MicroDeploymentMapper;
import com.beagledata.gaea.workbench.mapper.MicroMapper;
import com.beagledata.gaea.workbench.service.ClientService;
import com.beagledata.gaea.workbench.service.MicroDeploymentService;
import com.beagledata.gaea.workbench.service.MicroRouteService;
import com.beagledata.gaea.workbench.service.RestApiService;
import com.beagledata.util.IdUtils;
import com.beagledata.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * Created by liulu on 2020/5/13.
 */
@Service
public class RestApiServiceImpl implements RestApiService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ClientMapper clientMapper;
    @Autowired
    private MicroMapper microMapper;
    @Autowired
    private ClientService clientService;
    @Autowired
    private MicroRouteService microRouteService;
    @Autowired
    private ResourceResolver resourceResolver;
    @Autowired
    private MicroDeploymentService microDeploymentService;
    @Autowired
    private MicroDeploymentMapper microDeploymentMapper;

    @Override
    public void registerClient(Client client) {
        if (StringUtils.isBlank(client.getBaseUrl())) {
            throw new IllegalArgumentException("集群节点地址为空");
        }
        if (client.getBaseUrl().length() > 200) {
            throw new IllegalArgumentException("集群节点地址超过200个字符");
        }

        if (client.getName() == null) {
            client.setName(client.getBaseUrl());
        }

        try {
            Client cli = clientMapper.selectByUrl(client.getBaseUrl());
             // 未注册过
            if (null == cli) {
                client.setUuid(IdUtils.UUID());
                try {
                    clientMapper.insert(client);
                } catch (DuplicateKeyException e) {
                    logger.warn("当前地址已注册:%s", client);
                }
            } else {
                client.setUuid(cli.getUuid());
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    logger.info("开始向集群节点推送服务: {}", client.getBaseUrl());
                    deployMicro2Client(client);
                    logger.info("结束向集群节点推送服务: {}", client.getBaseUrl());
                }
            }).start();
        } catch (Exception e) {
            logger.error("集群节点注册失败: {}", client.getBaseUrl(), e);
            throw new IllegalStateException("集群节点注册失败");
        }
    }

    @Override
	public byte[] getMicroPackage(String microUuid) {
        if (StringUtils.isBlank(microUuid)) {
            throw new IllegalArgumentException("服务uuid不能为空");
        }

        byte[] microBytes = null;
        File microFile = SafelyFiles.newFile(resourceResolver.getDeployPath(), microUuid + ".zip");
        try {
            if (microFile.exists()){
                microBytes = FileUtils.readFileToByteArray(microFile);
            } else {
                List<MicroDeployment> list = microDeploymentMapper.selectByMicroUuid(microUuid, 0, 1);
                if (!list.isEmpty()) {
                    microBytes = microDeploymentService.getMicroPackage(list.get(0));
                }
            }
        } catch (Exception e) {
            logger.error("获取服务包失败. microUuid: {}", microUuid, e);
        }
        return microBytes;
    }

    private void deployMicro2Client(Client client) {
        //查询已经上线的服务列表，推送到集群节点
        List<String> onlineMicros = microMapper.selectOnlineMicro();
        for (String microUuid : onlineMicros) {
            File microFile = SafelyFiles.newFile(resourceResolver.getDeployPath(), microUuid + ".zip");
            byte[] microBytes = null;
            try {
                if (microFile.exists()) {
                    microBytes = FileUtils.readFileToByteArray(microFile);
                } else {
                    List<MicroDeployment> list = microDeploymentMapper.selectByMicroUuid(microUuid, 0, 1);
                    if (list.isEmpty()) {
                        continue;
                    }

                    microBytes = microDeploymentService.getMicroPackage(list.get(0));
                }
                clientService.deployRule(client.getBaseUrl(), microUuid, microBytes);
                //上线成功一个则保存一个集群节点和服务的路由记录
                microRouteService.saveMicroRoute(client.getUuid(), microUuid);
            } catch (Exception e) {
                logger.warn("推送服务到集群节点失败. microUuid: {}, clienUrl: {}", microUuid, client.getBaseUrl(), e);
            }
        }
    }
}
