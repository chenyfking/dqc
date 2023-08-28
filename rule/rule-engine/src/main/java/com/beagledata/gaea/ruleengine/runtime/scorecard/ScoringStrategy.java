package com.beagledata.gaea.ruleengine.runtime.scorecard;

import com.beagledata.gaea.ruleengine.model.scorecard.Scorecard;

/**
 * Created by liulu on 2020/4/17.
 */
public interface ScoringStrategy {
    Number calculate(Scorecard scorecard);
}
