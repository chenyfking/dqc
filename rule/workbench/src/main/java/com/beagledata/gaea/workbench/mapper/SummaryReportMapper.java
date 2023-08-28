package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.SummaryReport;
import com.beagledata.gaea.workbench.vo.ReportSearchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by liulu on 2020/11/11.
 */
@Mapper
public interface SummaryReportMapper {
    List<SummaryReport> selectSummaryReport(
            @Param("start") Integer start,
            @Param("end") Integer end,
            @Param("reportSearchVO") ReportSearchVO reportSearchVO,
            @Param("isAdmin") boolean isAdmin,
            @Param("userUuid") String userUuid
    );

    int countTotal(
            @Param("reportSearchVO") ReportSearchVO reportSearchVO,
            @Param("isAdmin") boolean isAdmin,
            @Param("userUuid") String userUuid
    );

    /**
     * 获取汇总统计的最新日期
     *
     * @return
     */
    Date getLeastDate();

    List<SummaryReport> preStat(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("logTableMonth") String logTableMonth
    );

    int insertBatch(List<SummaryReport> summaryReports);

    SummaryReport selectSummaryReportDate(
            @Param("reportSearchVO") ReportSearchVO reportSearchVO,
            @Param("isAdmin") boolean isAdmin,
            @Param("userUuid") String userUuid
    );

    int insert(SummaryReport summaryReport);
}
