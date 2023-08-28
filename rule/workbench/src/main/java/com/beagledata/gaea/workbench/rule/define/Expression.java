package com.beagledata.gaea.workbench.rule.define;

/**
 * 表达式
 * 包括：输入表达式、数据模型表达式、常量表达式、函数表达式、模型表达式、算术表达式
 *
 * Created by liulu on 2020/3/31.
 */
public class Expression implements Dumper, RuleAware, JavaTypeGetter {
    /**
     * 表达式类型
     */
    private ExpressionType type;
    /**
     * 输入表达式
     */
    private InputExpression input;
    /**
     * 数据模型表达式
     */
    private FactExpression fact;
    /**
     * 常量表达式
     */
    private ConstantExpression constant;
    /**
     * 函数表达式
     */
    private FuncExpression func;
    /**
     * 集合对象表达式
     */
    private CollectionChildExpression collectionChild;
    /**
     * 算术表达式
     */
    private Arithmetic arith;
    /**
     * 括号
     */
    private boolean parentheses;
    /**
     * 括号后面的算法表达式
     */
    private Arithmetic parenArith;
    /**
     * 所属规则
     */
    private Rule rule;

    public Expression() {
    }

    public Expression(InputExpression input) {
        this.type = ExpressionType.INPUT;
        this.input = input;
    }

    public Expression(FactExpression fact) {
        this.type = ExpressionType.FACT;
        this.fact = fact;
    }

    @Override
    public String dump() {
        if (type == null) {
            return null;
        }

        Dumper dumper = null;
        if (ExpressionType.INPUT.equals(type)) {
            dumper = input;
        } else if (ExpressionType.FACT.equals(type)) {
            dumper = fact;
        } else if (ExpressionType.CONSTANT.equals(type)) {
            dumper = constant;
        } else if (ExpressionType.FUNC.equals(type)) {
            dumper = func;
        } else if (ExpressionType.COLLECTION_CHILD.equals(type)) {
            if (collectionChild == null) {
                collectionChild = new CollectionChildExpression();
                RuleAware.setRuleUnchecked(rule, collectionChild);
            }
            dumper = collectionChild;
        }
        if (dumper == null) {
            return null;
        }
        String content = dumper.dump();
        if (content == null) {
            return null;
        }
        if (arith != null) {
            String arithContent = arith.dump();
            if (arithContent != null) {
                content += " " + arithContent;
            }
        }
        if (parentheses && parenArith != null) {
            String parenArithContent = parenArith.dump();
            if (parenArithContent != null) {
                content = String.format("(%s) %s", content, parenArithContent);
            }
        }
        return content;
    }

    @Override
    public void setRule(Rule rule) {
        this.rule = rule;
        RuleAware.setRuleUnchecked(rule, fact, func, arith, parenArith, collectionChild);
    }

    @Override
    public String getJavaType() {
        if (ExpressionType.INPUT.equals(type)) {
            return input.getJavaType();
        } else if (ExpressionType.FACT.equals(type)) {
            return fact.getJavaType();
        } else if (ExpressionType.FUNC.equals(type)) {
            return func.getJavaType();
        } else if (ExpressionType.COLLECTION_CHILD.equals(type)) {
            return collectionChild.getJavaType();
        }
        return null;
    }

    public ExpressionType getType() {
        return type;
    }

    public void setType(ExpressionType type) {
        this.type = type;
    }

    public InputExpression getInput() {
        return input;
    }

    public void setInput(InputExpression input) {
        this.input = input;
    }

    public FactExpression getFact() {
        return fact;
    }

    public void setFact(FactExpression fact) {
        this.fact = fact;
    }

    public ConstantExpression getConstant() {
        return constant;
    }

    public void setConstant(ConstantExpression constant) {
        this.constant = constant;
    }

    public FuncExpression getFunc() {
        return func;
    }

    public void setFunc(FuncExpression func) {
        this.func = func;
    }

    public CollectionChildExpression getCollectionChild() {
        return collectionChild;
    }

    public void setCollectionChild(CollectionChildExpression collectionChild) {
        this.collectionChild = collectionChild;
    }

    public Arithmetic getArith() {
        return arith;
    }

    public void setArith(Arithmetic arith) {
        this.arith = arith;
    }

    public boolean isParentheses() {
        return parentheses;
    }

    public void setParentheses(boolean parentheses) {
        this.parentheses = parentheses;
    }

    public Arithmetic getParenArith() {
        return parenArith;
    }

    public void setParenArith(Arithmetic parenArith) {
        this.parenArith = parenArith;
    }
}
