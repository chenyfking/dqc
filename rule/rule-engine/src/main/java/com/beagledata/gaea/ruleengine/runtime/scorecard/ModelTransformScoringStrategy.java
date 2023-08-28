package com.beagledata.gaea.ruleengine.runtime.scorecard;

import com.beagledata.gaea.ruleengine.model.scorecard.Scorecard;

import java.math.BigDecimal;

/**
 * Created by liulu on 2020/4/17.
 */
public class ModelTransformScoringStrategy extends SumScoringStrategy implements ScoringStrategy {
    @Override
    public Number calculate(Scorecard scorecard) {
        Number data = super.calculate(scorecard);
        double sumScore = 0;
        if (data instanceof BigDecimal) {
            sumScore = data.doubleValue();
        } else {
            sumScore =  (Double) data;
        }
        double pred = Math.exp(sumScore) / (1 + Math.exp(sumScore));
        double score = Math.log(pred / (1 - pred)) * 35 / Math.log(2) + 800 - 35 * (Math.log(10) / Math.log(2));
        return score;
    }
}
