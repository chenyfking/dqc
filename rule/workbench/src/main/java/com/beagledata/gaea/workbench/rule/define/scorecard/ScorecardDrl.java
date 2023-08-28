package com.beagledata.gaea.workbench.rule.define.scorecard;

import com.beagledata.gaea.ruleengine.model.scorecard.RowImpl;
import com.beagledata.gaea.ruleengine.model.scorecard.ScorecardImpl;
import com.beagledata.gaea.ruleengine.runtime.scorecard.ScoringStrategy;
import com.beagledata.gaea.workbench.rule.define.Drl;
import com.beagledata.gaea.workbench.rule.define.Import;

/**
 * 评分卡的Drl
 *
 * Created by liulu on 2020/4/21.
 */
public class ScorecardDrl extends Drl {
    private Class<? extends ScoringStrategy> strategy;

    public ScorecardDrl(String id) {
        super(id);
    }

    /**
     * @return 添加ScorecardImpl、RowImpl、ScoringStrategy的import
     */
    protected String visitImports() {
        addImport(Import.with(ScorecardImpl.class.getName()));
        addImport(Import.with(RowImpl.class.getName()));
        if (strategy != null) {
            addImport(Import.with(strategy.getName()));
        }
        return super.visitImports();
    }

    public Class<? extends ScoringStrategy> getStrategy() {
        return strategy;
    }

    public void setStrategy(Class<? extends ScoringStrategy> strategy) {
        this.strategy = strategy;
    }
}
