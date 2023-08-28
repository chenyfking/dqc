package com.beagledata.gaea.workbench.rule.define;

import com.beagledata.gaea.ruleengine.util.PropertyUtils;

import java.math.BigDecimal;

/**
 * 动作对象
 *
 * Created by liulu on 2020/4/9.
 */
public class Action implements Dumper, RuleAware {
    /**
     * 动作类型
     */
    private ActionType type;
    /**
     * 动作类型为赋值的时候，等号左侧表达式
     */
    private FactExpression left;
    /**
     * 动作类型为赋值的时候，等号右侧表达式
     */
    private Expression right;
    /**
     * 动作类型为执行函数的时候，函数表达式
     */
    private FuncExpression func;
    /**
     * 外部接口表达式
     */
    private ThirdApiExpression thirdApi;
    /**
     * 模型表达式
     */
    private ModelExpression model;

    public Action() {
    }

    public Action(FactExpression left, Expression right) {
        this.type = ActionType.ASSIGN;
        this.left = left;
        this.right = right;
    }

    @Override
    public String dump() {
        if (ActionType.ASSIGN.equals(type)) {
            return dumpAssign();
        } else if (ActionType.FUNC.equals(type)) {
            return dumpOther(func);
        } else if (ActionType.THIRDAPI.equals(type)) {
            return dumpOther(thirdApi);
        } else if (ActionType.MODEL.equals(type)) {
            return dumpOther(model);
        }
        return null;
    }

    private String dumpAssign() {
        if (left == null || right == null) {
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

        if (BigDecimal.class.getSimpleName().equals(left.getJavaType())) {
            left.rule.addImport(Import.with(BigDecimal.class.getName()));
            rightContent = "new BigDecimal((" + rightContent + ") + \"\")";
        }

        if (left.getLastField() == null) {
            return String.format(
                    "  %s.setProperties($%s, %s);\n  update($%s);",
                    PropertyUtils.class.getName(),
                    left.getModifyName(),
                    rightContent,
                    left.getModifyName()
            );
        }

        if ("Map".equals(left.getJavaType()) || "Object".equals(left.getJavaType())) {
            rightContent = String.format(
                    "  %s.setProperties($%s.%s, %s);",
                    PropertyUtils.class.getName(),
                    left.getModifyName(),
                    left.getLastField().getName(),
                    rightContent
            );
        } else if ("List".equals(left.getJavaType())) {
            rightContent = String.format(
                    "  %s.setProperties($%s, \"%s\", %s);",
                    PropertyUtils.class.getName(),
                    left.getModifyName(),
                    left.getLastField().getName(),
                    rightContent
            );
        }

        String modifyContent = String.format(
                "  $%s.%s = %s;",
                left.getModifyName(),
                left.getLastField().getName(),
                rightContent
        );
        return modifyContent;
    }

    private String dumpOther(Dumper dumper) {
        if (dumper == null) {
            return null;
        }

        String content = dumper.dump();
        if (content == null) {
            return null;
        }
        return String.format("  %s;", content);
    }

    @Override
    public void setRule(Rule rule) {
        RuleAware.setRuleUnchecked(rule, left, right, func, thirdApi, model);
    }

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public FactExpression getLeft() {
        return left;
    }

    public void setLeft(FactExpression left) {
        this.left = left;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    public FuncExpression getFunc() {
        return func;
    }

    public void setFunc(FuncExpression func) {
        this.func = func;
    }

    public ThirdApiExpression getThirdApi() {
        return thirdApi;
    }

    public void setThirdApi(ThirdApiExpression thirdApi) {
        this.thirdApi = thirdApi;
    }

    public ModelExpression getModel() {
        return model;
    }

    public void setModel(ModelExpression model) {
        this.model = model;
    }
}
