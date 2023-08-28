package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.common.RestConstants;
import com.beagledata.gaea.workbench.entity.Client;
import com.beagledata.gaea.workbench.service.RestApiService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 对外提供REST API
 *
 * Created by liulu on 2020/5/13.
 */
@RestController
public class RestApiController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestApiService restApiService;

    /**
     * 注册集群节点
     *
     * @param client
     * @return
     */
    @PostMapping(RestConstants.Workbench.Endpoints.REGISTER_CLIENT)
    public Result registerClient(Client client) {
        restApiService.registerClient(client);
        return Result.newSuccess();
    }

    /**
     * 跑批远程调用获取服务zip包
     * @author yinrj
     * @param microUuid
     * @date 2020/7/23
     */
    @PostMapping(RestConstants.Workbench.Endpoints.GET_MICRO_PACKAGE)
    public void getMicroPackage(String microUuid, HttpServletResponse response) {
        byte[] microBytes = restApiService.getMicroPackage(microUuid);
        if (microBytes == null) {
            throw new IllegalStateException(String.format("获取服务包失败: {}", microUuid));
        }

        OutputStream out = null;
        try {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + microUuid + ".zip");
            out = response.getOutputStream();
            out.write(microBytes);
            out.flush();
        } catch (IOException e) {
            logger.error("获取服务包失败: {}", microUuid, e);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }
}
