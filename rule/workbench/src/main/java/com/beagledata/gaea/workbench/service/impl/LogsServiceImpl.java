package com.beagledata.gaea.workbench.service.impl;

import com.beagledata.gaea.workbench.entity.Logs;
import com.beagledata.gaea.workbench.mapper.LogsMapper;
import com.beagledata.gaea.workbench.service.LogsService;
import com.beagledata.gaea.workbench.util.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: LogsServiceImpl
 * @Description:  日志功能
 * @author: yinrj
 * @date 2018年8月16日 下午4:38:05
 */
@Service
public class LogsServiceImpl extends BaseServiceImpl implements LogsService {
    @Autowired
    private LogsMapper logsMapper;

    @Override
    public List<Logs> listPage(@RequestParam Map<String, Object> params){
        try {
            params.put("isAdmin", UserHolder.hasAdminPermission());
            params.put("isOrgAdmin", UserHolder.isOrgAdmin());
            params.put("userUuid", UserHolder.currentUserUuid());
            return logsMapper.selectPage(params);
        } catch (Exception e) {
            logger.error("查询操作日志列表失败: {}", params, e);
        }
        return Collections.emptyList();
    }

    @Override
    public int countTotal(Map<String, Object> params) {
            params.put("isAdmin", UserHolder.hasAdminPermission());
            params.put("isOrgAdmin", UserHolder.isOrgAdmin());
            params.put("userUuid", UserHolder.currentUserUuid());
            return logsMapper.countTotal(params);
    }

    @Override
    public List<Logs> listPageWithoutDataIsolate(Map<String, Object> paramsMap) {
        try {
            return logsMapper.selectPageWithoutDataIsolate(paramsMap);
        } catch (Exception e) {
            logger.error("查询操作日志列表失败: {}", paramsMap, e);
        }
        return Collections.emptyList();
    }

    @Override
    public int countTotalWithoutDataIsolate(Map<String, Object> paramsMap) {
        return logsMapper.countTotalWithoutDataIsolate(paramsMap);
    }

    @Override
    public List<Logs> listPageOfLoginLogs(Map<String, Object> paramsMap) {
        try {
            return logsMapper.selectPageWithoutDataIsolate(paramsMap);
        } catch (Exception e) {
            logger.error("查询邓丽日志列表失败: {}", paramsMap, e);
        }
        return Collections.emptyList();
    }

    @Override
    public int countTotalOfLoginLogs(Map<String, Object> paramsMap) {
        return logsMapper.countTotalWithoutDataIsolate(paramsMap);
    }
}
