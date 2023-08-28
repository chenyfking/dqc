package com.beagledata.gaea.workbench.rule.parser;


import com.alibaba.fastjson.JSONObject;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.rule.define.Drl;

/**
 * 解析向导式决策集
 *
 * Created by liulu on 2018/9/30.
 */
public class GuidedRuleParser extends DrlParser implements Parser {
    public GuidedRuleParser(Assets assets) {
        super(assets);
    }

    @Override
    protected void doParse() {
        drl = JSONObject.parseObject(assets.getContent(), Drl.class);
        drl.setId(String.valueOf(assets.getId()));
        if (!isForVerifier()) {
            drl.getRules().forEach(rule -> rule.setName(String.format("%s-%s", getRuleNamePrefix(), rule.getName())));
        }
    }
}
