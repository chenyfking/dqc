package com.beagledata.gaea.workbench.rule.define;

/**
 * 算术
 *
 * Created by liulu on 2020/4/8.
 */
public class Arithmetic implements Dumper, RuleAware {
    /**
     * 算术类型
     */
    private ArithmeticType type;
    /**
     * 右侧表达式
     */
    private Expression right;
    /**
     * 是否括号
     */
    private boolean parentheses;
    /**
     * 算术表达式
     */
    private Arithmetic arith;

    public Arithmetic() {
    }

    public Arithmetic(ArithmeticType type, Expression right) {
        this.type = type;
        this.right = right;
    }

    @Override
    public String dump() {
        if (type == null) {
            return null;
        }
        String rightContent = right.dump();
        if (rightContent == null) {
            return null;
        }
        if (parentheses) {
            if (arith != null) {
                return String.format("%s (%s) %s", type.getSymbol(), rightContent, arith.dump());
            }
            return String.format("%s (%s)", type.getSymbol(), rightContent);
        }
        return String.format("%s %s", type.getSymbol(), rightContent);
    }

    @Override
    public void setRule(Rule rule) {
        RuleAware.setRuleUnchecked(rule, right, arith);
    }

    public ArithmeticType getType() {
        return type;
    }

    public void setType(ArithmeticType type) {
        this.type = type;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    public boolean isParentheses() {
        return parentheses;
    }

    public void setParentheses(boolean parentheses) {
        this.parentheses = parentheses;
    }

    public Arithmetic getArith() {
        return arith;
    }

    public void setArith(Arithmetic arith) {
        this.arith = arith;
    }
}
