package com.beagledata.gaea.workbench.service;

import com.beagledata.gaea.workbench.entity.Logs;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: LogsService 
 * @Description:  日志功能
 * @author: yinrj
 * @date 2018年8月16日 下午3:54:06
 */
public interface LogsService {
	/**
	 * @param paramsMap
	 * @return
	 */
    List<Logs> listPage(Map<String, Object> paramsMap);

    /**
	 * @param paramsMap
	 * @return
	 */
    int countTotal(Map<String, Object> paramsMap);

	/**
	 * 查询日志列表：没有数据隔离
	 * @param paramsMap
	 * @auth yinrj
	 * @date 2021/1/22
	 * @return
	 */
    List<Logs> listPageWithoutDataIsolate(Map<String, Object> paramsMap);

    /**
	 * @auth yinrj
	 * @date 2021/1/22
	 * @param paramsMap
	 * @return
	 */
    int countTotalWithoutDataIsolate(Map<String, Object> paramsMap);

    /**
	 * 查询登录日志列表
	 * @param paramsMap
	 * @auth yinrj
	 * @date 2021/1/25
	 * @return
	 */
    List<Logs> listPageOfLoginLogs(Map<String, Object> paramsMap);

    /**
	 * @auth yinrj
	 * @date 2021/1/25
	 * @param paramsMap
	 * @return
	 */
    int countTotalOfLoginLogs(Map<String, Object> paramsMap);

}
