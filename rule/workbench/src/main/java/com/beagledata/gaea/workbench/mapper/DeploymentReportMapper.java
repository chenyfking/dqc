package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.DeploymentReport;
import com.beagledata.gaea.workbench.vo.ReportSearchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 描述:
 * @author 周庚新
 * @date 2020-11-11
 */
@Mapper
public interface DeploymentReportMapper {

	List<DeploymentReport> selectDeploymentReport(@Param(value = "start") Integer start,
												@Param(value = "end") Integer end,
												@Param(value = "reportSearchVO") ReportSearchVO reportSearchVO);

	Integer countTotal(@Param(value = "reportSearchVO") ReportSearchVO reportSearchVO);

	/**
	 * 描述: 获取预统计的最新时间
	 * @param: []
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: java.util.Date
	 */
	Date getLeastDate();

	List<DeploymentReport> preStat(@Param(value = "startDate") Date startDate, @Param(value = "endDate") Date endDate, @Param(value = "logTableMonth") String logTableMonth);

	void insertBatch(List<DeploymentReport> deploymentReports);

	void insert(DeploymentReport deploymentReport);

	DeploymentReport selectDeploymentReportDate(@Param(value = "reportSearchVO") ReportSearchVO reportSearchVO);
}