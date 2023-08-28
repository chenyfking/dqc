package com.beagledata.gaea.workbench.rule.parser;

import com.beagledata.gaea.workbench.common.AssetsType;
import com.beagledata.gaea.workbench.entity.Assets;

/**
 * Created by liulu on 2020/4/21.
 */
public class ParserFactory {
    public static Parser getParser(Assets assets) {
        if (AssetsType.GUIDED_RULE.equals(assets.getType())) {
            return new GuidedRuleParser(assets);
        } else if (AssetsType.RULE_TABLE.equals(assets.getType())) {
            return new RuleTableParser(assets);
        } else if (AssetsType.RULE_TREE.equals(assets.getType())) {
            return new RuleTreeParser(assets);
        } else if (AssetsType.SCORECARD.equals(assets.getType())) {
            return new ScorecardParser(assets);
        } else if (AssetsType.RULE_FLOW.equals(assets.getType())) {
            return new FlowParser(assets);
        } else if (AssetsType.RULE_SCRIPT.equals(assets.getType())) {
            return new RuleScriptParser(assets);
        }
        return null;
    }
}
