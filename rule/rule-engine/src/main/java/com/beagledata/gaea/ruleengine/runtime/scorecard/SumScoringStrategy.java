package com.beagledata.gaea.ruleengine.runtime.scorecard;

import com.beagledata.gaea.ruleengine.model.scorecard.Row;
import com.beagledata.gaea.ruleengine.model.scorecard.Scorecard;

import java.math.BigDecimal;

/**
 * Created by liulu on 2020/4/17.
 */
public class SumScoringStrategy extends AbstractScoringStrategy implements ScoringStrategy {
    @Override
    public Number doCalculate(Scorecard scorecard) {
        BigDecimal score = new BigDecimal(0);
        for (Row row : scorecard.getRows()) {
            score = score.add(new BigDecimal(String.valueOf(row.getActualScore())));
        }
        return score;
    }
}
