package com.beagledata.gaea.workbench.rule.parser;

import com.alibaba.fastjson.JSON;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.rule.define.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *@auto: yangyongqiang
 *@Description:决策表
 *@Date: 2018-10-30 13:54
 **/
public class RuleTableParser extends DrlParser implements Parser {
    public RuleTableParser(Assets assets) {
        super(assets);
    }

    @Override
    protected void doParse() {
        drl = new Drl(String.valueOf(assets.getId()));

        RuleTable ruleTable = JSON.parseObject(assets.getContent(), RuleTable.class);
        for (int i = 0; i < ruleTable.getRows().size(); i++) {
            List<RuleTable.Column> columns = ruleTable.getRows().get(i);
            Constraint constraint = new Constraint(Conjunction.AND);
            Rhs rhs = new Rhs();
            Set<Attr> attrs = new HashSet<>();
            for (int j = 0; j < columns.size(); j++) {
                RuleTable.Column column = columns.get(j);
                RuleTable.Header header = ruleTable.getHeaders().get(j);
                if (RuleTable.Header.Type.ATTR.equals(header.getType())) {
                    if (StringUtils.isNotBlank(header.getName())) {
                        attrs.add(new Attr(header.getName(), column.getValue()));
                    }
                } else if (RuleTable.Header.Type.CONDITION.equals(header.getType())) {
                    Constraint columnConstraint = column.getConstraint();
                    if (columnConstraint != null && !columnConstraint.getConstraints().isEmpty()) {
                        columnConstraint.getConstraints().forEach(c -> c.setLeft(new Expression(header.getFact())));
                        constraint.addConstraint(columnConstraint);
                    }
                } else if (RuleTable.Header.Type.ASSIGN.equals(header.getType())) {
                    rhs.addAction(new Action(header.getFact(), column.getRight()));
                }
            }
            Rule rule = new Rule(String.format("%s-第%s行", getRuleNamePrefix(), i + 1));
            rule.setAttrs(attrs);
            rule.setLhs(new Lhs(constraint));
            rule.setRhs(rhs);
            drl.addRule(rule);
        }
    }

    /**
     * 决策表模型
     */
    static final class RuleTable {
        /**
         * 属性列表
         */
        private Set<Attr> attrs = new HashSet<>();
        /**
         * 表头
         */
        private List<Header> headers = new ArrayList<>();
        /**
         * 行
         */
        private List<List<Column>> rows = new ArrayList<>();

        static final class Header {
            /**
             * 表头类型
             */
            private Type type;
            /**
             * 数据模型表达式
             */
            private FactExpression fact;
            /**
             * 属性列的属性名称
             */
            private String name;

            enum Type {
                ATTR, // 属性列
                CONDITION, // 条件列
                ASSIGN // 赋值列
            }

            public Type getType() {
                return type;
            }

            public void setType(Type type) {
                this.type = type;
            }

            public FactExpression getFact() {
                return fact;
            }

            public void setFact(FactExpression fact) {
                this.fact = fact;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }

        /**
         * 列
         */
        static final class Column {
            /**
             * 属性列，当前单元格属性值
             */
            private String value;
            /**
             * 条件列，当前单元格的条件
             */
            private Constraint constraint;
            /**
             * 赋值列，当前单元格赋值右侧表达式
             */
            private Expression right;

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            public Constraint getConstraint() {
                return constraint;
            }

            public void setConstraint(Constraint constraint) {
                this.constraint = constraint;
            }

            public Expression getRight() {
                return right;
            }

            public void setRight(Expression right) {
                this.right = right;
            }
        }

        public Set<Attr> getAttrs() {
            return attrs;
        }

        public void setAttrs(Set<Attr> attrs) {
            this.attrs = attrs;
        }

        public List<Header> getHeaders() {
            return headers;
        }

        public void setHeaders(List<Header> headers) {
            this.headers = headers;
        }

        public List<List<Column>> getRows() {
            return rows;
        }

        public void setRows(List<List<Column>> rows) {
            this.rows = rows;
        }
    }
}
