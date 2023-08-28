package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.Logs;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: LogsMapper
 * @Description: 日志Mapper
 * @author: yinrj
 * @date 2018年8月16日 下午4:39:00
 */
@Mapper
public interface LogsMapper {
	int insert(Logs logs);

	List<Logs> selectPage(Map<String, Object> params);

	int countTotal(Map<String, Object> params);

	List<Logs> selectPageWithoutDataIsolate(Map<String, Object> params);

	int countTotalWithoutDataIsolate(Map<String, Object> params);
}
