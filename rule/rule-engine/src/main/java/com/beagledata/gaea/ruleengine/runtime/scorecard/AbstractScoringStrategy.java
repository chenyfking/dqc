package com.beagledata.gaea.ruleengine.runtime.scorecard;

import com.beagledata.gaea.ruleengine.model.scorecard.Scorecard;

import java.util.Optional;

/**
 * Created by liulu on 2020/6/18.
 */
public abstract class AbstractScoringStrategy implements ScoringStrategy {
    /**
     * 支持默认评分
     *
     * @param scorecard
     * @return
     */
    @Override
	public Number calculate(Scorecard scorecard) {
        if (scorecard.getRows() == null || scorecard.getRows().isEmpty()) {
            // 所有条件都不满足，返回默认评分
            return Optional.ofNullable(scorecard.getDefaultScore()).orElse(0D);
        }
        return doCalculate(scorecard);
    }

    abstract Number doCalculate(Scorecard scorecard);
}
