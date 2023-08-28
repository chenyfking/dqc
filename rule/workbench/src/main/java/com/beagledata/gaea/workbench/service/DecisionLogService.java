package com.beagledata.gaea.workbench.service;

import com.beagledata.common.Result;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Created by liulu on 2020/11/11.
 */
public interface DecisionLogService {
    void downloadDecisionLog(
            String microUuid,
            Date startDate,
            Date endDate,
            HttpServletResponse response
    );

    Result trace(String seqNo);
}
