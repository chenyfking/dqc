package com.beagledata.gaea.workbench.rule.define;

import com.beagledata.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by liulu on 2018/9/30.
 */
public class Rhs implements Dumper, RuleAware {
    /**
     * 动作列表
     */
    private List<Action> actions = new ArrayList<>();
    /**
     * 所属规则
     */
    protected Rule rule;
    /**
     * 执行相关动作
     */
    private boolean doAction;

    @Override
    public String dump() {
        StringBuilder buffer = new StringBuilder();

        String actionContent = actions.stream().map(action -> action.dump())
                .filter(content -> content != null)
                .collect(Collectors.joining("\n"));
        if (StringUtils.isNotBlank(actionContent)) {
            buffer.append(actionContent);
            updateFacts(buffer, actionContent);
            doAction = true;
        }

        return buffer.toString();
    }

    private void updateFacts(StringBuilder buffer, String actionContent) {
        if (rule.isForVerifier()) {
            return;
        }

        Set<String> updateFacts = new HashSet<>();
        Pattern pattern = Pattern.compile("modify\\((\\$Fact_.*)\\) \\{");
        Matcher matcher = pattern.matcher(actionContent);
        while (matcher.find()) {
            String modifyName = matcher.group(1);
            int dotIndex = modifyName.indexOf('.');
            if (dotIndex != -1) {
                updateFacts.add(modifyName.substring(0, dotIndex));
            }
        }

        // 函数里的Fact也update
        Pattern factPattern = Pattern.compile("\\$Fact_\\d+\\.");
        Matcher factMatcher = factPattern.matcher(actionContent);
        while (factMatcher.find()) {
            String updateName = factMatcher.group();
            int dotIndex = updateName.indexOf('.');
            if (dotIndex != -1) {
                updateFacts.add(updateName.substring(0, dotIndex));
            }
        }

        //模型调用里不带.的Fact也update
        Pattern factNoDotPattern = Pattern.compile("\\$Fact_\\d+");
        Matcher factNoDotMatcher = factNoDotPattern.matcher(actionContent);
        while (factNoDotMatcher.find()) {
            String updateName = factNoDotMatcher.group();
            updateFacts.add(updateName);
        }

        if (!updateFacts.isEmpty()) {
            for (String uf : updateFacts) {
                buffer.append("\n  update(").append(uf).append(");");
            }
        }
    }

    @Override
    public void setRule(Rule rule) {
        this.rule = rule;
        actions.forEach(action -> action.setRule(rule));
    }

    public void addAction(Action action) {
        this.actions.add(action);
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public boolean isDoAction() {
        return doAction;
    }

    public void setDoAction(boolean doAction) {
        this.doAction = doAction;
    }
}
