package com.beagledata.gaea.workbench.rule.define;

import com.beagledata.gaea.workbench.entity.AiModel;
import com.beagledata.gaea.workbench.rule.util.AssetsCache;

/**
 * AI模型表达式
 *
 * Created by liulu on 2020/4/8.
 */
public class ModelExpression implements Dumper, RuleAware {
    /**
     * AI模型id
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
        if (input == null || output == null) {
            return null;
        }

        AiModel model = AssetsCache.getAiModelByUuid(id);
        if (model == null) {
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

        rule.addModel(model);
        return String.format("$AiModelHandler.handle(\"%s\", %s, %s)", model.getJarName(), inputContent, outputContent);
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
