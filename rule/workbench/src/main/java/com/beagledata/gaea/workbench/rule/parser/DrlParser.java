package com.beagledata.gaea.workbench.rule.parser;

import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.rule.define.Drl;

/**
 * Created by liulu on 2018/11/2.
 */
public abstract class DrlParser implements Parser {
    protected Drl drl;
    protected Assets assets;
    /**
     * 是否用来校验
     */
    private boolean forVerifier;
    /**
     * 规则名称前缀，区分规则唯一性（前端展示、ruleflow-group）
     */
    private String ruleNamePrefix;

    public DrlParser(Assets assets) {
        this.assets = assets;

        StringBuilder buffer = new StringBuilder();
        if (GuidedRuleParser.class.equals(this.getClass())) {
            buffer.append("向导式决策集-");
        } else if (RuleScriptParser.class.equals(this.getClass())) {
            buffer.append("脚本式决策集-");
        } else if (RuleTableParser.class.equals(this.getClass())) {
            buffer.append("决策表-");
        } else if (RuleTreeParser.class.equals(this.getClass())) {
            buffer.append("决策树-");
        } else if (ScorecardParser.class.equals(this.getClass())) {
            buffer.append("评分卡-");
        }
        buffer.append(assets.getName());
        if (assets.getVersionNo() != null && assets.getVersionNo() > 0) {
            buffer.append("_V").append(assets.getVersionNo());
        }
        this.ruleNamePrefix = buffer.toString();
    }

    @Override
    public Drl getDumper() {
        if (drl == null) {
            doParse();
        }
        drl.setForVerifier(forVerifier);
        drl.setAssets(assets);
        return drl;
    }

    abstract protected void doParse();

    protected String getRuleNamePrefix() {
        return ruleNamePrefix;
    }

    public Assets getAssets() {
        return assets;
    }

    public void setAssets(Assets assets) {
        this.assets = assets;
    }

    public boolean isForVerifier() {
        return forVerifier;
    }

    public void setForVerifier(boolean forVerifier) {
        this.forVerifier = forVerifier;
    }
}
