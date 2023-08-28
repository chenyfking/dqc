package com.beagledata.gaea.workbench.rule.define.scorecard;

import com.beagledata.gaea.ruleengine.common.RuleMetaData;
import com.beagledata.gaea.ruleengine.runtime.scorecard.ScoringStrategy;
import com.beagledata.gaea.workbench.rule.define.Expression;
import com.beagledata.gaea.workbench.rule.define.FactExpression;
import com.beagledata.gaea.workbench.rule.define.Rule;
import com.beagledata.gaea.workbench.rule.define.RuleAware;
import com.beagledata.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评分卡的规则对象
 *
 * Created by liulu on 2020/4/17.
 */
public class ScorecardRule extends Rule {
    /**
     * 行号
     */
    private int rowNumber;
    /**
     * 分值表达式
     */
    private Expression score;
    /**
     * 行权重
     */
    private Double weight;
    /**
     * 得分计算策略
     */
    private Class<? extends ScoringStrategy> strategy;
    /**
     * 得分赋值对象
     */
    private FactExpression assign;
    /**
     * 默认评分
     */
    private Double defaultScore;
    /**
     * 原因码
     */
    private String reasonCode;
    /**
     * 原因信息
     */
    private String reasonMsg;
    /**
     * 原因码赋值对象
     */
    private FactExpression reasonCodesExpr;
    /**
     * 原因信息赋值对象
     */
    private FactExpression reasonMsgsExpr;

    public ScorecardRule(String name) {
        super(name);
    }

    public ScorecardRule(String name, Double defaultScore) {
        super(name);
        this.defaultScore = defaultScore;
    }

    public ScorecardRule(String name, int rowNumber, Class<? extends ScoringStrategy> strategy, FactExpression assign) {
        super(name);
        this.rowNumber = rowNumber;
        this.strategy = strategy;
        this.assign = assign;
    }

    public ScorecardRule(String name, int rowNumber, Expression score, Double weight) {
        super(name);
        this.rowNumber = rowNumber;
        this.score = score;
        this.weight = weight;
    }

    @Override
    public String dump() {
        if (rowNumber == 0 || rowNumber == -1) {
            if (!isForVerifier()) {
                setMetaData(RuleMetaData.KEY_IMPLICIT, true);
            } else {
                return null;
            }
        }
        RuleAware.setRuleUnchecked(this, getLhs(), score, assign, reasonCodesExpr, reasonMsgsExpr);
        if (rowNumber == -1) {
            assign.dump();
            if (reasonCodesExpr != null) {
                reasonCodesExpr.dump();
            }
            if (reasonMsgsExpr != null) {
                reasonMsgsExpr.dump();
            }
        }
        return doDump();
    }

    protected String getLhsContent() {
        if (rowNumber == 0) {
            return null;
        }

        if (rowNumber == -1) {
            Set<String> classNames = new HashSet<>();
            classNames.add(assign.getFact().getClassName());
            if (reasonCodesExpr != null && reasonCodesExpr.getId() != null) {
                classNames.add(reasonCodesExpr.getFact().getClassName());
            }
            if (reasonMsgsExpr != null && reasonMsgsExpr.getId() != null) {
                classNames.add(reasonMsgsExpr.getFact().getClassName());
            }
            List<String> contents = new ArrayList<>();
            classNames.forEach(classname -> {
                contents.add(String.format("  $%s: %s()", classname, classname));
            });
            contents.add(String.format("  $scorecard: ScorecardImpl(name == \"%s\")", name));
            return contents.stream().collect(Collectors.joining("\n"));
        }

        StringBuilder lhsBuffer = new StringBuilder();
        String content = getLhs().dump();
        if (content != null) {
            lhsBuffer.append(content);
        }
        if (!isForVerifier()) {
            lhsBuffer.append(String.format("\n  $scorecard: ScorecardImpl(name == \"%s\")", name));
        }
        return lhsBuffer.toString();
    }

    protected String getRhsContent() {
        if (rowNumber == 0) {
            if (defaultScore == null) {
                return String.format("  insert(new ScorecardImpl(\"%s\"))", name);
            } else {
                return String.format("  insert(new ScorecardImpl(\"%s\", %s))", name, defaultScore);
            }
        }

        if (rowNumber == -1) {
            Map<String, Set<String>> modifiers = new HashMap<>();
            Set<String> set = new HashSet<>();
            set.add(String.format(
                    "    %s = new %s().calculate($scorecard)",
                    assign.getLastField().getName(), strategy.getSimpleName()
            ));
            modifiers.put(assign.getModifyName(), set);

            if (reasonCodesExpr != null && reasonCodesExpr.getId() != null) {
                set = modifiers.get(reasonCodesExpr.getModifyName());
                if (set == null) {
                    set = new HashSet<>();
                    modifiers.put(reasonCodesExpr.getModifyName(), set);
                }
                set.add(String.format(
                        "    %s = $scorecard.getReasonCodes()",
                        reasonCodesExpr.getLastField().getName()
                ));
            }
            if (reasonMsgsExpr != null && reasonMsgsExpr.getId() != null) {
                set = modifiers.get(reasonMsgsExpr.getModifyName());
                if (set == null) {
                    set = new HashSet<>();
                    modifiers.put(reasonMsgsExpr.getModifyName(), set);
                }
                set.add(String.format(
                        "    %s = $scorecard.getReasonMsgs()",
                        reasonMsgsExpr.getLastField().getName()
                ));
            }

            List<String> contents = new ArrayList<>();
            modifiers.forEach((k, v) -> {
                contents.add(String.format("  modify($%s) {\n%s\n  }", k, v.stream().collect(Collectors.joining(",\n"))));
            });
            return contents.stream().collect(Collectors.joining("\n"));
        }

        if (score == null) {
            return null;
        }

        StringBuilder rhsBuffer = new StringBuilder();
        rhsBuffer.append("  $scorecard.addRow(new RowImpl(");
        rhsBuffer.append(rowNumber).append(", ");
        rhsBuffer.append(score.dump());
        if (weight != null) {
            rhsBuffer.append(", ").append(weight);
        }
        if (StringUtils.isNotBlank(reasonCode)) {
            rhsBuffer.append(", \"").append(reasonCode).append("\"");
        } else {
            rhsBuffer.append(", null");
        }
        if (StringUtils.isNotBlank(reasonMsg)) {
            rhsBuffer.append(", \"").append(reasonMsg).append("\"");
        } else {
            rhsBuffer.append(", null");
        }
        rhsBuffer.append("))");
        return rhsBuffer.toString();
    }

    public String getName() {
        if (rowNumber == -1) {
            return String.format("%s-计算得分", name);
        }
        if (rowNumber > 0) {
            return String.format("%s-第%s行", name, rowNumber);
        }
        return String.format("%s-初始化", name);
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public Expression getScore() {
        return score;
    }

    public void setScore(Expression score) {
        this.score = score;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Class<? extends ScoringStrategy> getStrategy() {
        return strategy;
    }

    public void setStrategy(Class<? extends ScoringStrategy> strategy) {
        this.strategy = strategy;
    }

    public FactExpression getAssign() {
        return assign;
    }

    public void setAssign(FactExpression assign) {
        this.assign = assign;
    }

    public Double getDefaultScore() {
        return defaultScore;
    }

    public void setDefaultScore(Double defaultScore) {
        this.defaultScore = defaultScore;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getReasonMsg() {
        return reasonMsg;
    }

    public void setReasonMsg(String reasonMsg) {
        this.reasonMsg = reasonMsg;
    }

    public FactExpression getReasonCodesExpr() {
        return reasonCodesExpr;
    }

    public void setReasonCodesExpr(FactExpression reasonCodesExpr) {
        this.reasonCodesExpr = reasonCodesExpr;
    }

    public FactExpression getReasonMsgsExpr() {
        return reasonMsgsExpr;
    }

    public void setReasonMsgsExpr(FactExpression reasonMsgsExpr) {
        this.reasonMsgsExpr = reasonMsgsExpr;
    }
}
