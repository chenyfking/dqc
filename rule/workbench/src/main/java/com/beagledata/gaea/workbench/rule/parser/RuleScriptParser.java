package com.beagledata.gaea.workbench.rule.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.rule.define.rulescript.RuleScriptDrl;
import com.beagledata.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 脚本式决策集
 *
 * Created by liulu on 2020/7/3.
 */
public class RuleScriptParser extends DrlParser implements Parser {
    public RuleScriptParser(Assets assets) {
        super(assets);
    }

    @Override
    protected void doParse() {
        JSONObject json = JSON.parseObject(assets.getContent());
        RuleScriptDrl drl = new RuleScriptDrl();
        drl.setId(String.valueOf(assets.getId()));
        drl.setScript(replaceRuleName(json.getString("script")));

        this.drl = drl;
    }

    private String replaceRuleName(String script) {
        Pattern ruleNameCompile = Pattern.compile("rule +\"(.*?)\"");
        Matcher ruleNameMatcher = ruleNameCompile.matcher(script);
        List<String> searchList = new ArrayList<>();
        List<String> replacementList = new ArrayList<>();
        while (ruleNameMatcher.find()) {
            String match = ruleNameMatcher.group();
            String name = ruleNameMatcher.group(1);
            searchList.add(match);
            match = match.replace(name, getRuleNamePrefix() + "-" + name);
            match += String.format("\n    @assetsUuid(\"%s\")", assets.getUuid());
            if (assets.getVersionNo() != null && assets.getVersionNo() > 0) {
                match += String.format("\n    @assetsVersion(%s)", assets.getVersionNo());
            }
            replacementList.add(match);
        }
        return StringUtils.replaceEach(script, searchList.toArray(new String[0]), replacementList.toArray(new String[0]));
    }
}
