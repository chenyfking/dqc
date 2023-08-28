package com.beagledata.gaea.workbench.service.impl;

import com.beagledata.gaea.common.RedisConstants;
import com.beagledata.gaea.workbench.mapper.MicroRelationMapper;
import com.beagledata.gaea.workbench.service.MicroRouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @Auther: yinrj
 * @Date: 0021 2020/7/21 11:11
 * @Description: 服务路由
 */
@Service
public class MicroRouteServiceImpl implements MicroRouteService {
    private static Logger logger = LoggerFactory.getLogger(MicroRouteServiceImpl.class);

    @Autowired
	private MicroRelationMapper microRelationMapper;
    @Autowired
	private StringRedisTemplate stringRedisTemplate;

    /**
     * 保存服务路由
     * @author yinrj
     * @date 2020/7/21
     */
    @Override
    public void saveMicroRoute(String clientUuid, String microUuid) {
        try {
			// 保存服务集群节点关联
            microRelationMapper.insertOrUpdateMicroClientRoute(clientUuid, microUuid);
		} catch (Exception e) {
			logger.error("保存服务路由失败. clientUuid: {}, microUuid: {}", clientUuid, microUuid, e);
			throw new IllegalStateException("保存服务路由失败");
		}
    }

    /**
     * 刷新服务路由变动提示到Redis
     * @author yinrj
     * @date 2020/7/21
     */
    @Override
    public void refreshMicroRouteToRedis() {
        stringRedisTemplate.convertAndSend(RedisConstants.Gateway.CHANNEL_REFRESH_KEY, RedisConstants.Gateway.CHANNEL_REFRESH_ROUTE); // 发布刷新路由消息
    }

    @Override
    public void deleteMicroRoute(String clientUuid, String microUuid) {
        try {
            microRelationMapper.deleteByMicroAndClient(microUuid, clientUuid);
        } catch (Exception e) {
            logger.error("删除服务路由失败. clientUuid: {}, microUuid: {}", clientUuid, microUuid);
            throw new IllegalStateException("删除服务路由失败");
        }
    }
}
