package com.beagledata.gaea.workbench.rule.define.rulescript;

import com.beagledata.common.SpringBeanHolder;
import com.beagledata.gaea.ruleengine.util.DateUtils;
import com.beagledata.gaea.ruleengine.util.JsonParser;
import com.beagledata.gaea.workbench.entity.FunctionDefinition;
import com.beagledata.gaea.workbench.entity.ThirdApiDefinition;
import com.beagledata.gaea.workbench.mapper.ThirdApiDefinitionMapper;
import com.beagledata.gaea.workbench.rule.define.Drl;
import com.beagledata.gaea.workbench.rule.define.Fact;
import com.beagledata.gaea.workbench.rule.util.AssetsCache;
import com.beagledata.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liulu on 2020/7/4.
 */
public class RuleScriptDrl extends Drl {

    /**
     * 规则脚本
     */
    private String script;

    @Override
    public String dump() {
        StringBuilder buffer = new StringBuilder(script);
        // 优先替换
        Pattern compile = Pattern.compile("外部接口:\\((.*),.*,.*?\\)");
        Matcher matcher = compile.matcher(script);
        ThirdApiDefinitionMapper mapper = SpringBeanHolder.getBean(ThirdApiDefinitionMapper.class);
        while (matcher.find()) {
            String match = new String(matcher.group());
            String name = matcher.group(1);
            ThirdApiDefinition thirdApiDefinition  = mapper.selectUuidByName(name);
            thirdApis.add(thirdApiDefinition);
            match = match.replace(name,"\""+thirdApiDefinition.getUuid()+"\"");
            match = match.replace("外部接口:","$ThirdApiHandler.handle");
            buffer.replace(matcher.start(), matcher.end(), match);
        }
        script = buffer.toString();
        buffer = new StringBuilder(script);

        compile = Pattern.compile(Fact.DEFAULT_PACKAGE + ".Fact_(\\d+) *\\([\\s\\S]*?\\)");
        matcher = compile.matcher(script);
        while (matcher.find()) {
            //找到lhs的fact
            Fact fact = AssetsCache.getFactById(matcher.group(1));
            if (fact != null) {
                addFact(fact);
                addImport(fact);
            }
        }

        compile = Pattern.compile("import[\\s]+?" + Fact.DEFAULT_PACKAGE + ".Fact_(\\d+)");
        matcher = compile.matcher(script);
        while (matcher.find()) {
            //找到import的fact
            Fact fact = AssetsCache.getFactById(matcher.group(1));
            if (fact != null) {
                addImport(fact);
            }
        }

        compile = Pattern.compile("import[\\s]+([A-Za-z0-9\\._$]+);?");
        matcher = compile.matcher(script);
        while (matcher.find()) {
            //找到import的自定义函数
            String className = matcher.group(1);
            if (StringUtils.isBlank(className)) {
                continue;
            }
            if (className.matches(Fact.DEFAULT_PACKAGE + ".Fact_\\d+")) {
                continue;
            }
            if (className.startsWith("java.")) {
                continue;
            }
            if (JsonParser.class.getName().equals(className)
                    || DateUtils.class.getName().equals(className)) {
                continue;
            }
            FunctionDefinition fd = AssetsCache.getFuncByClassName(className);
            if (fd == null) {
                continue;
            }
            globals.add(className);
        }

        buffer.insert(0, String.format("package %s\n\n%s\n\n%s\n\n", packageName, visitGlobals(), visitAttrs()));
        return buffer.toString();
    }

    public void setScript(String script) {
        this.script = script;
    }
}

