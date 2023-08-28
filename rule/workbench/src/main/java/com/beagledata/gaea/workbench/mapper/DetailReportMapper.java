package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.DetailReport;
import com.beagledata.gaea.workbench.vo.ReportSearchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 描述:
 * 详细报表
 * @author zgx
 * @date 2020-11-11
 */
@Mapper
public interface DetailReportMapper {
	List<DetailReport> selectDetailReport(@Param(value = "start") Integer start,
											@Param(value = "end") Integer end,
											@Param(value = "reportSearchVO") ReportSearchVO reportSearchVO,
											@Param(value = "isAdmin") boolean isAdmin,
											@Param(value = "userUuid") String userUuid);

	Integer countTotal(@Param(value = "reportSearchVO") ReportSearchVO reportSearchVO,
					   @Param(value = "isAdmin") boolean isAdmin,
					   @Param(value = "userUuid") String userUuid);

	Date getLeastDate();

	List<DetailReport> preStat(@Param(value = "startDate") Date startDate, @Param(value = "endDate") Date endDate, @Param(value = "logTableMonth") String logTableMonth);

	void insertBatch(List<DetailReport> detailReports);

	void insert(DetailReport detailReport);

	DetailReport selectDetailReportDate(@Param(value = "reportSearchVO") ReportSearchVO reportSearchVO,
										  @Param(value = "isAdmin") boolean isAdmin,
										  @Param(value = "userUuid") String userUuid
	);
}
