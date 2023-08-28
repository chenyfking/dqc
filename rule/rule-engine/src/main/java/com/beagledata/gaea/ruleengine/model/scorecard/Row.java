package com.beagledata.gaea.ruleengine.model.scorecard;

/**
 * Created by liulu on 2020/4/17.
 */
public interface Row {
    int getRowNumber();
    Double getScore();
    Double getWeight();
    Double getActualScore();
    String getReasonCode();
    String getReasonMsg();
}
