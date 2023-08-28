package com.beagledata.gaea.workbench.service;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.Client;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * yangyongqiang  2019/06/11
 */
public interface ClientService {
    /**
     * 增加
     */
    void add(Client client);
    /**
     * 删除
     */
    void delete(String uuid);
    /**
     * 改动
     */
    void update(Client client);

    /**
     * 启用集群节点
     *
     * @param uuid
     */
    void enable(String uuid);

    /**
     * 禁用集群节点
     *
     * @param uuid
     */
    void disable(String uuid);

    /**
     * 获得集群节点上正在运行的服务
     */
    Result getRunningMicro(String uuid);

    /**
     * 获得某服务上线的集群节点
     * Created by Chenyafeng on 2019/6/24
     */
    Result getClientsByMicro(String microUuid);

    /**
     * 向集群节点上传license文件
     * Created by Chenyafeng on 2019/12/17
     */
    Result uploadLicense(String clientUuid, MultipartFile file);

    /**
     * @return 获取所有集群节点
     */
    List<Client> listAll();

    /**
     * @return 获取所有可以发布的集群节点
     */
    List<Client> listAvailables();

    /**
     * 发布规则服务到集群节点
     *
     * @param baseUrl 集群节点连接地址
     * @param microUuid 服务uuid
     * @param bytes 规则文件字节数组
     */
    void deployRule(String baseUrl, String microUuid, byte[] bytes) throws IOException;

    /**
     * 发布模型服务到集群节点
     *
     * @param baseUrl 集群节点连接地址
     * @param microUuid 服务uuid
     * @param file 模型文件
     * @throws IOException
     */
    void deployModel(String baseUrl, String microUuid, File file) throws IOException;

    /**
     * 删除服务
     *
     * @param baseUrl
     * @param microUuid
     */
    void unDeploy(String baseUrl, String microUuid) throws IOException;
}
