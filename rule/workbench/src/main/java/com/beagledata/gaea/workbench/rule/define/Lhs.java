package com.beagledata.gaea.workbench.rule.define;

import com.beagledata.gaea.workbench.rule.util.AssetsCache;
import com.beagledata.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Created by liulu on 2018/9/30.
 */
public class Lhs implements Dumper, RuleAware {
    /**
     * 约束条件
     */
    private Constraint constraint = new Constraint();
    /**
     * 所属规则
     */
    protected Rule rule;

    public Lhs() {
    }

    public Lhs(Constraint constraint) {
        this.constraint = constraint;
    }

    @Override
    public String dump() {
        if (rule == null) {
            setRule(new Rule());
        }

        String constraintContent = constraint.dump();
        if (constraintContent == null) {
            // 没有条件
            StringBuffer buffer = new StringBuffer();
            String loopData = "";
            StringBuffer loopBuffer = new StringBuffer();
            if (rule.isLoop()) {
                String loopElement = getLoopElement();
                loopData = String.format(" $%s_loop: %s(this != null) from $%s.%s\n",
                        loopElement,
                        loopElement,
                        rule.getLoopTarget().getFact().getClassName(),
                        rule.getLoopTarget().getLinkNames()
                );
            }
            if (rule.getFacts().isEmpty()) {
                // 没有赋值
                if (rule.isLoop()) {
                   loopBuffer.append(String.format(" $%s: %s(this != null) ;",
                           rule.getLoopTarget().getFact().getClassName(),
                           rule.getLoopTarget().getFact().getClassName()
                   ));
                   return loopBuffer.append(loopData).toString();
                } else {
                    return null;
                }
            }
            String resStr = rule.getFacts().stream()
                    .map(fact -> String.format("  $%s: %s()", fact.getClassName(), fact.getClassName()))
                    .collect(Collectors.joining("\n"));
            buffer.append(resStr).append("\n");
            if (rule.isLoop()) {
                if (!resStr.contains(rule.getLoopTarget().getFact().getClassName())) {
                    buffer.append(
                            String.format(" $%s: %s(this != null) ;\n",
                                    rule.getLoopTarget().getFact().getClassName(),
                                    rule.getLoopTarget().getFact().getClassName()
                            )
                    );
                }
                buffer.append(loopData);
            }
            return buffer.toString();
        }

        if (rule.getFacts().isEmpty()) { // 有条件没有Fact
            return "eval" + constraintContent;
        }

        int i = 0;
        StringBuilder buffer = new StringBuilder();
        for (Fact fact : rule.getFacts()) {
            if (i++ < rule.getFacts().size() - 1 || rule.isLoop()) {
                if (!rule.isForVerifier()) {
                    buffer.append(String.format("  $%s: %s(this != null)\n", fact.getClassName(), fact.getClassName()));
                }
            } else { // 所有约束条件放在最后一个Fact判断
                if (constraintContent.length() > 2 && !rule.isForVerifier()) {
                    // 解决Lhs调用函数不重新匹配问题
                    constraintContent = "(this != null && " + constraintContent + ")";
                }
                buffer.append(String.format("  $%s: %s%s", fact.getClassName(), fact.getClassName(), constraintContent));
            }
        }
        if (rule.isLoop()) {
            String loopElement = getLoopElement();
            if (loopElement != null) {
                buffer.append(
                        String.format(
                                "  $%s_loop: %s%s from $%s.%s",
                                loopElement,
                                loopElement,
                                constraintContent,
                                rule.getLoopTarget().getFact().getClassName(),
                                rule.getLoopTarget().getLinkNames()
                        )
                );
            }
        }
        return buffer.toString();
    }

    private String getLoopElement() {
        if (rule.getLoopTarget().getFact() == null) {
            rule.getLoopTarget().dump();
        }

        String subType = rule.getLoopTarget().getLastField().getSubType();
        if (StringUtils.isBlank(subType)) {
            return null;
        }

        if (subType.length() == 32) {
            Fact fact = AssetsCache.getFactByUuid(subType);
            rule.addImport(fact);
            return fact != null ? fact.getClassName() : null;
        }

        if (BigDecimal.class.getSimpleName().equals(subType)) {
            rule.addImport(Import.with(BigDecimal.class.getName()));
        } else if (Date.class.getSimpleName().equals(subType)) {
            rule.addImport(Import.with(Date.class.getName()));
        }
        return subType;
    }

    @Override
    public void setRule(Rule rule) {
        this.rule = rule;
        RuleAware.setRuleUnchecked(rule, constraint);
    }

    public Rule getRule() {
        return rule;
    }

    public Constraint getConstraint() {
        return constraint;
    }

    public void setConstraint(Constraint constraint) {
        this.constraint = constraint;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Lhs{");
        sb.append("constraint=").append(constraint);
        sb.append(", rule=").append(rule);
        sb.append('}');
        return sb.toString();
    }
}
