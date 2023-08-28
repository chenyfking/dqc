package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.ApprovalReport;
import com.beagledata.gaea.workbench.vo.ReportSearchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 描述:
 * 审批报表mapper
 * @author 周庚新
 * @date 2020-11-11
 */
@Mapper
public interface ApprovalReportMapper {
	List<ApprovalReport> selectApprovalReport(@Param(value = "start") Integer start,
											  @Param(value = "end") Integer end,
											  @Param(value = "reportSearchVO") ReportSearchVO reportSearchVO,
											  @Param(value = "isAdmin") boolean isAdmin,
											  @Param(value = "userUuid") String userUuid);

	Integer countTotal(@Param(value = "reportSearchVO") ReportSearchVO reportSearchVO,
					   @Param(value = "isAdmin") boolean isAdmin,
					   @Param(value = "userUuid") String userUuid);

	/**
	 * 描述: 获取预统计的最新时间
	 * @param: []
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: java.util.Date
	 */
	Date getLeastDate();

	List<ApprovalReport> preStat(@Param(value = "startDate") Date startDate, @Param(value = "endDate") Date endDate, @Param(value = "logTableMonth") String logTableMonth);

	void insertBatch(List<ApprovalReport> approvalReports);

	void insert(ApprovalReport approvalReport);

	ApprovalReport selectApprovalReportDate(@Param(value = "reportSearchVO") ReportSearchVO reportSearchVO,
											@Param(value = "isAdmin") boolean isAdmin,
											@Param(value = "userUuid") String userUuid
	);
}