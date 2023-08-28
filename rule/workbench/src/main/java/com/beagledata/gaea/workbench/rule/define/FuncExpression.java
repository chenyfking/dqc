package com.beagledata.gaea.workbench.rule.define;

import com.beagledata.common.SpringBeanHolder;
import com.beagledata.gaea.workbench.entity.FunctionDefinition;
import com.beagledata.gaea.workbench.rule.util.AssetsCache;
import com.beagledata.gaea.workbench.service.FunctionDefinitionService;
import com.beagledata.util.EncodeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 函数表达式
 *
 * Created by liulu on 2020/4/8.
 */
public class FuncExpression implements Dumper, RuleAware, JavaTypeGetter {
    private static FunctionDefinitionService functionDefinitionService = SpringBeanHolder.getBean(FunctionDefinitionService.class);

    /**
     * 函数名称
     */
    private String name;
    /**
     * 函数方法
     */
    private String method;
    /**
     * 参数列表
     */
    private List<Param> params = new ArrayList<>();
    /**
     * 所属规则
     */
    private Rule rule;
    /**
     * 函数
     */
    private FunctionDefinition.Method funcMethod;

    @Override
    public String dump() {
        FunctionDefinition function = AssetsCache.getFuncByName(name);
        if (function == null) {
            return null;
        }
        funcMethod = function.getMethodByName(method);
        if (funcMethod == null) {
            return null;
        }
       List<String> paramsDump = params.stream().map(param -> param.dump()).filter(dump-> dump!=null).collect(Collectors.toList());
        if (params.size() != paramsDump.size()) {
            return null;
        }
        rule.addFunc(function.getClassName());
        return String.format(
                "$%s.%s(%s)",
                EncodeUtils.encodeMD516(function.getClassName()), // 解决函数集类名重名问题
                funcMethod.getDeclare(),
                StringUtils.join(paramsDump, ",")
        );
    }

    @Override
    public void setRule(Rule rule) {
        this.rule = rule;
        params.forEach(param -> param.setRule(rule));
    }

    @Override
    public String getJavaType() {
        if (funcMethod == null) {
            return null;
        }
        return funcMethod.getReturnType();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<Param> getParams() {
        return params;
    }

    public void setParams(List<Param> params) {
        this.params = params;
    }

    /**
     * 参数
     */
    public static class Param implements Dumper, RuleAware {
        /**
         * 参数名称
         */
        private String name;
        /**
         * 参数类型
         */
        private String type;
        /**
         * 值表达式
         */
        private Expression value;

        @Override
        public String dump() {
            return value != null ? value.dump() : null;
        }

        @Override
        public void setRule(Rule rule) {
            RuleAware.setRuleUnchecked(rule, value);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Expression getValue() {
            return value;
        }

        public void setValue(Expression value) {
            this.value = value;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Param{");
            sb.append("name='").append(name).append('\'');
            sb.append(", type='").append(type).append('\'');
            sb.append(", value=").append(value);
            sb.append('}');
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FuncExpression{");
        sb.append("name='").append(name).append('\'');
        sb.append(", method='").append(method).append('\'');
        sb.append(", params=").append(params);
        sb.append('}');
        return sb.toString();
    }
}
