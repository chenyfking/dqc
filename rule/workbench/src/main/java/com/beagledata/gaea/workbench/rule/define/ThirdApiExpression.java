package com.beagledata.gaea.workbench.rule.define;

import com.beagledata.common.SpringBeanHolder;
import com.beagledata.gaea.workbench.entity.ThirdApiDefinition;
import com.beagledata.gaea.workbench.rule.util.AssetsCache;
import com.beagledata.gaea.workbench.service.ThirdApiDefinitionService;
import com.beagledata.util.StringUtils;

/**
 * 外部接口表达式
 *
 * Created by liulu on 2020/6/16.
 */
public class ThirdApiExpression implements Dumper, RuleAware {
    private static ThirdApiDefinitionService thirdApiDefinitionService = SpringBeanHolder.getBean(ThirdApiDefinitionService.class);

    /**
     * 外部接口uuid
     */
    private String id;
    /**
     * 输入表达式
     */
    private FactExpression input;
    /**
     * 输出表达式
     */
    private FactExpression output;
    /**
     * 所属规则
     */
    private Rule rule;

    @Override
    public String dump() {
        if (StringUtils.isBlank(id) || input == null || output == null) {
            return null;
        }

        ThirdApiDefinition definition = AssetsCache.getThirdApiByUuid(id);
        if (definition == null) {
            return null;
        }
        String inputContent = input.dump();
        if (inputContent == null) {
            return null;
        }
        String outputContent = output.dump();
        if (outputContent == null) {
            return null;
        }
        rule.addThirdApi(definition);
        return String.format(
                "$ThirdApiHandler.handle(\"%s\", %s, %s);\n  update($%s);",
                id,
                inputContent,
                outputContent,
                output.getFact().getClassName()
        );
    }

    @Override
    public void setRule(Rule rule) {
        this.rule = rule;
        RuleAware.setRuleUnchecked(rule, input, output);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FactExpression getInput() {
        return input;
    }

    public void setInput(FactExpression input) {
        this.input = input;
    }

    public FactExpression getOutput() {
        return output;
    }

    public void setOutput(FactExpression output) {
        this.output = output;
    }
}
