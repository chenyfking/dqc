package com.beagledata.gaea.workbench.service;

import com.beagledata.common.Result;

/**
 * Created by liulu on 2020/11/11.
 */
public interface ReferService {
    Result selectReferBySubjectUuidAndVersion(String subjectUuid, Integer subjectVersion);
}
