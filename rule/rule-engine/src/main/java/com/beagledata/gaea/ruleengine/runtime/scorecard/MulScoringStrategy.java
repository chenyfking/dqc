package com.beagledata.gaea.ruleengine.runtime.scorecard;

import com.beagledata.gaea.ruleengine.model.scorecard.Row;
import com.beagledata.gaea.ruleengine.model.scorecard.Scorecard;

import java.math.BigDecimal;

/**
 * Created by liulu on 2020/4/17.
 */
public class MulScoringStrategy extends AbstractScoringStrategy implements ScoringStrategy {
    @Override
    public Number doCalculate(Scorecard scorecard) {
        BigDecimal score = new BigDecimal(1);
        for (Row row : scorecard.getRows()) {
            score = score.multiply(new BigDecimal(String.valueOf(row.getActualScore())));
        }
        return score;
    }
}
