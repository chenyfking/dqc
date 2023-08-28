package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.DecisionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 描述:
 * 决策日志mapper
 * @author zgx
 * @date 2020-11-11
 */
@Mapper
public interface DecisionLogMapper {
	List<DecisionLog> selectPages(@Param("start") Integer start,
								  @Param("end") Integer end,
								  @Param("microUuid") String microUuid,
								  @Param(value = "startDate") Date startDate,
								  @Param(value = "endDate") Date endDate,
								  @Param("tableMonth") String tableMonth);

	DecisionLog selectStateAndMicroUuid(DecisionLog decisionLog);

	DecisionLog selectTrace(DecisionLog decisionLog);

}
