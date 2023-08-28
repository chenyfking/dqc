package com.beagledata.gaea.ruleengine.model.scorecard;

import java.util.List;

/**
 * Created by liulu on 2020/4/17.
 */
public interface Scorecard {
    String getName();
    Double getDefaultScore();
    List<Row> getRows();
    String getReasonCodes();
    String getReasonMsgs();
}
