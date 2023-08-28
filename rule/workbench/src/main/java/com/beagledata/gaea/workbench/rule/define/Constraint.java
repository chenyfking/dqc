package com.beagledata.gaea.workbench.rule.define;

import com.beagledata.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A constraint is an expression that returns true or false
 *
 * Created by liulu on 2020/3/31.
 */
public class Constraint implements Dumper, RuleAware {
    /**
     * 左侧表达式
     */
    private Expression left;
    /**
     * 运算符
     */
    private Operator op;
    /**
     * 右侧表达式
     */
    private Expression right;
    /**
     * 连接符
     */
    private Conjunction conjunction;
    /**
     * 子表达式
     */
    private List<Constraint> constraints = new ArrayList<>();
    /**
     * 规则
     */
    private Rule rule;

    private boolean not;

    public Constraint() {
    }

    public Constraint(Conjunction conjunction) {
        this.conjunction = conjunction;
    }

    public Constraint(Expression left, Operator op, Expression right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public String dump() {

        if (Conjunction.NOT.equals(conjunction)) {
            setNot(true);
        }

        String dump;
        if (conjunction == null) {
            dump = visitConstraint();
        } else {
            dump = visitJoinConstraint();
        }
        if (not) {
            setNot(false);
        }
        return dump;
    }

    @Override
    public void setRule(Rule rule) {
        RuleAware.setRuleUnchecked(rule, left, right);
        this.rule = rule;
        if (!constraints.isEmpty()) {
            constraints.forEach(constraint -> RuleAware.setRuleUnchecked(rule, constraint));
        }
    }

    private String visitConstraint() {
        if (left == null || op == null || right == null) {
            return null;
        }
        String leftContent = left.dump();
        if (leftContent == null) {
            return null;
        }
        String rightContent = right.dump();
        if (rightContent == null) {
            return null;
        }

        if (BigDecimal.class.getSimpleName().equals(left.getJavaType())
                && left.getArith() == null
                && BigDecimal.class.getSimpleName().equals(right.getJavaType())
                && right.getArith() == null) {
            if (!BigDecimal.class.getSimpleName().equals(left.getJavaType())) {
                leftContent = String.format("new BigDecimal(%s)", leftContent);
            }
            if (!BigDecimal.class.getSimpleName().equals(right.getJavaType())) {
                rightContent = String.format("new BigDecimal(%s)", rightContent);
            }
            rule.addImport(Import.with(BigDecimal.class.getName()));
            return addNotNull(castBigDecimalConstraint(leftContent, rightContent));
        }

        // 根据操作符判断是否需要添加整体括号
        if (needParentheses()) {
            if ((leftContent.startsWith("\"") && leftContent.endsWith("\""))
                || leftContent.contains("+")
                || (left.getArith() != null && left.getArith().getType() != null && left.getArith().getType().equals(ArithmeticType.SUB))
                || leftContent.contains("*")
                || leftContent.contains("/")) {
                leftContent = "(" + leftContent + ")";
            }
            if ((rightContent.startsWith("\"") && rightContent.endsWith("\""))
                || rightContent.contains("+")
                || (right.getArith() != null && right.getArith().getType() != null && right.getArith().getType().equals(ArithmeticType.SUB))
                || rightContent.contains("*")
                || rightContent.contains("/")) {
                rightContent = "(" + rightContent + ")";
            }
        }

        return addNotNull(String.format("%s %s %s", leftContent, op.getSymbol(), rightContent));
    }

    private String addNotNull(String result) {
        if (ExpressionType.FACT.equals(left.getType())) {
            FactExpression leftFact = left.getFact();
            if (leftFact != null && leftFact.getFact() != null) {
                List<Field> fields = leftFact.getFields();
                Field leftField = null;
                if (fields != null && leftFact.getFieldId() != null) {
                    for (Field field : fields) {
                        if (leftFact.getFieldId().equals(field.getId())) {
                            leftField = field;
                            break;
                        }
                    }
                }

                if (leftField != null && !"Derive".equals(leftField.getType().name()) && StringUtils.isNotBlank(leftField.getName())) {
                    String fieldName = leftField.getName();
                    result = String.format("(($Fact_%s.%s != null) &&  (%s))", leftFact.getFact().getId(), fieldName, result);
                }
            }
        }

        if (ExpressionType.FACT.equals(right.getType())) {
            FactExpression rightFact = right.getFact();
            if (rightFact != null && rightFact.getFact() != null) {
                List<Field> fields = rightFact.getFields();
                Field rightField = null;
                if (fields != null && rightFact.getFieldId() != null) {
                    for (Field field : fields) {
                        if (rightFact.getFieldId().equals(field.getId())) {
                            rightField = field;
                            break;
                        }
                    }
                }
                if (rightField != null && !"Derive".equals(rightField.getType().name()) &&  StringUtils.isNotBlank(rightField.getName())) {
                    String fieldName = rightField.getName();
                    result = String.format("(($Fact_%s.%s != null) && (%s))", rightFact.getFact().getId(), fieldName, result);
                }
            }
        }
        return result;
    }

    private boolean needParentheses() {
        switch (op) {
            case GT:
                return false;
            case GTE:
                return false;
            case LT:
                return false;
            case LTE:
                return false;
            case EQ:
                return false;
            case NEQ:
                return false;
            default:
                return true;
        }
    }

    private String castBigDecimalConstraint(String leftContent, String rightContent) {
        switch (op) {
            case GT:
                return String.format("%s.compareTo(%s) > 0", leftContent, rightContent);
            case GTE:
                return String.format("%s.compareTo(%s) >= 0", leftContent, rightContent);
            case LT:
                return String.format("%s.compareTo(%s) < 0", leftContent, rightContent);
            case LTE:
                return String.format("%s.compareTo(%s) <= 0", leftContent, rightContent);
            case EQ:
                return String.format("%s.compareTo(%s) == 0", leftContent, rightContent);
            case NEQ:
                return String.format("%s.compareTo(%s) != 0", leftContent, rightContent);
            default:
                return null;
        }
    }

    private String visitJoinConstraint() {
        String constraintContent = constraints.stream()
                .map(constraint -> constraint.dump())
                .filter(content -> content != null)
                .collect(Collectors.joining( " " + getConjunctionText() + " "));
        if (StringUtils.isNotBlank(constraintContent)) {
            return "(" + constraintContent + ")";
        }
        return null;
    }

    private String getConjunctionText() {
        if (not) {
            if (Conjunction.AND.equals(conjunction)) {
                return Conjunction.OR.getSymbol();
            }
            return Conjunction.AND.getSymbol();
        }
        return conjunction.getSymbol();
    }

    /**
    * 描述: 取非操作，翻转比较符
    * @param: [not]
    * @author: 周庚新
    * @date: 2020/12/16
    * @return: void
    */
    public void setNot(boolean not) {
        this.not = not;
        if (!this.constraints.isEmpty()) {
            this.constraints.forEach(c -> c.setNot(not));
        }
    }
    private String getOpText() {
        if (not) {
            switch (op) {
                case GT:
                    return Operator.LTE.getSymbol();
                case GTE:
                    return Operator.LT.getSymbol();
                case LT:
                    return Operator.GTE.getSymbol();
                case LTE:
                    return Operator.GT.getSymbol();
                case EQ:
                    return Operator.NEQ.getSymbol();
                case NEQ:
                    return Operator.EQ.getSymbol();
                case MATCHES:
                    return Operator.NOT_MATCHES.getSymbol();
                case NOT_MATCHES:
                    return Operator.CONTAINS.getSymbol();
                case CONTAINS:
                    return Operator.NOT_CONTAINS.getSymbol();
                case NOT_CONTAINS:
                    return Operator.CONTAINS.getSymbol();
                case STR_STARTSWITH:
                    return Operator.STR_NOT_STARTSWITH.getSymbol();
                case STR_NOT_STARTSWITH:
                    return Operator.STR_STARTSWITH.getSymbol();
                case STR_ENDSWITH:
                    return Operator.STR_NOT_ENDSWITH.getSymbol();
                case STR_NOT_ENDSWITH:
                    return Operator.STR_ENDSWITH.getSymbol();
                case STR_LENGTH:
                    return Operator.STR_NOT_LENGTH.getSymbol();
                case STR_NOT_LENGTH:
                    return Operator.STR_LENGTH.getSymbol();
                default:
                    return op.getSymbol();
            }
        }
        return op.getSymbol();
    }

    public void addConstraint(Constraint constraint) {
        this.constraints.add(constraint);
    }

    public void addConstraints(Collection<Constraint> constraints) {
        this.constraints.addAll(constraints);
    }

    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public Operator getOp() {
        return op;
    }

    public void setOp(Operator op) {
        this.op = op;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    public Conjunction getConjunction() {
        return conjunction;
    }

    public void setConjunction(Conjunction conjunction) {
        this.conjunction = conjunction;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Constraint{");
        sb.append("left=").append(left);
        sb.append(", op=").append(op);
        sb.append(", right=").append(right);
        sb.append(", conjunction=").append(conjunction);
        sb.append(", constraints=").append(constraints);
        sb.append('}');
        return sb.toString();
    }
}
