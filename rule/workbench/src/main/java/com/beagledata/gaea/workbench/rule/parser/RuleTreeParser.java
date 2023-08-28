package com.beagledata.gaea.workbench.rule.parser;

import com.alibaba.fastjson.JSON;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.rule.define.*;

import java.util.*;

/**
 * 解析决策树
 * Created by liulu on 2018/10/18.
 *
 * enabled 是否可用
 * activation-group: 同组中一个规则被触发，其他规则不执行
 * date-effective 达到指定时间触发 ,System.setProperty("drools.dateformat", "yyyy-MM-dd HH:mm");
 * date-expires 规则过期时间
 * lock-on-active ： 控制当前的规则只会被执行一次
 * no-loop ： fact更新后是否再次执行规则 默认false
 */
public class RuleTreeParser extends DrlParser implements Parser {
    private RuleTree ruleTree;
    private Stack<RuleTree.Node> stack = new Stack<>();

    public RuleTreeParser(Assets assets) {
        super(assets);
    }

    protected void doParse() {
        drl = new Drl(String.valueOf(assets.getId()));
        ruleTree = JSON.parseObject(assets.getContent(), RuleTree.class);
        traversal(ruleTree.getTree());
    }

    /**
     * 遍历树节点，把每条根节点到叶子结点的路径压入栈，连接整个路径上的节点组成一条规则
     * 算法逻辑：深度遍历树，每个节点压入栈，弹出非父节点，确保栈内节点前后父子关系，遍历到叶子结点，处理整个栈组成一条规则
     *
     * @param parent 父节点
     * @author liulu
     * 2020/4/15 10:52
     */
    private void traversal(RuleTree.Node parent) {
        pushStack(parent);

        if (parent.getChildren().isEmpty()) {
            handleStack();
            return;
        }

        parent.getChildren().forEach(node -> {
            node.setParent(parent);
            traversal(node);
        });
    }

    private void pushStack(RuleTree.Node node) {
        if (stack.isEmpty()) {
            stack.push(node);
            return;
        }

        while (!stack.peek().equals(node.getParent())) { // 如果顶部节点不是父节点，循环弹出，直到父节点
            stack.pop();
        }
        stack.push(node);
    }

    /**
     * 处理从根节点到叶子节点的一条路径
     */
    private void handleStack() {
        int ruleId = drl.getRules().size() + 1;
        Rule rule = new Rule(String.format("%s-%s", getRuleNamePrefix(), ruleId));
        Constraint constraint = new Constraint(Conjunction.AND);
        Expression left = null;
        for (RuleTree.Node node : stack) {
            if (RuleTree.Node.Type.FACT.equals(node.getNodeType()) || node.getNodeType() == null) {
                left = new Expression(node.getFact());
            } else if (RuleTree.Node.Type.CONDITION.equals(node.getNodeType()) && node.getCondition() != null) {
                if (!node.getCondition().isOther()) {
                    Constraint tmpConstraint = new Constraint();
                    tmpConstraint.setLeft(left);
                    tmpConstraint.setOp(node.getCondition().getOp());
                    tmpConstraint.setRight(node.getCondition().getRight());
                    constraint.addConstraint(tmpConstraint);

                    RuleTree.Node factNode = getFactNodeOfCondition(node);
                    List<Constraint> constraints = factNode.getConstraintMap().get(ruleId);
                    if (constraints == null) {
                        constraints = new ArrayList<>();
                        factNode.getConstraintMap().put(ruleId, constraints);
                    }

                    Constraint elseConstraint = getElseConstraint(node);
                    if (elseConstraint != null) {
                        // ELSE分支在当前节点之前
                        Constraint siblingConstraint;
                        if (constraints.isEmpty()) {
                            siblingConstraint = new Constraint(Conjunction.AND);
                            elseConstraint.addConstraint(siblingConstraint);
                        } else {
                            siblingConstraint = elseConstraint.getConstraints().get(elseConstraint.getConstraints().size() - 1);
                        }
                        siblingConstraint.addConstraint(tmpConstraint);

                        List<Constraint> elseChildren = elseConstraint.getConstraints();
                        if (elseChildren.size() > 1) {
                            if (equals(elseChildren.get(elseChildren.size() - 2).getConstraints(),
                                    elseChildren.get(elseChildren.size() - 1).getConstraints())) {
                                elseChildren.remove(elseChildren.size() - 1);
                            }
                        }
                    }
                    constraints.add(tmpConstraint);
                } else {
                    Constraint elseConstraint = new Constraint(Conjunction.NOT);
                    removeDuplicateConstraintMap(node.getParent().getConstraintMap()).forEach(list -> {
                        Constraint siblingConstraint = new Constraint(Conjunction.AND);
                        siblingConstraint.addConstraints(list);
                        elseConstraint.addConstraint(siblingConstraint);
                    });
                    constraint.addConstraint(elseConstraint);
                    node.setElseConstraint(elseConstraint);
                }
            } else if (RuleTree.Node.Type.ACTION.equals(node.getNodeType())) {
                rule.setRhs(node.getRhs());
            }
        }
        rule.setAttrs(ruleTree.getAttrs());
        rule.setLhs(new Lhs(constraint));
        drl.addRule(rule);
    }

    private List<List<Constraint>> removeDuplicateConstraintMap(Map<Integer, List<Constraint>> map) {
        List<List<Constraint>> list = new ArrayList<>();
        for (List<Constraint> list1 : map.values()) {
            boolean exists = false;
            for (List<Constraint> list2 : list) {
                if (equals(list1, list2)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                list.add(list1);
            }
        }
        return list;
    }

    private boolean equals(List<Constraint> list1, List<Constraint> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }

        for (int i = 0; i < list1.size(); i++) {
            if (!equals(list1.get(i), list2.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean equals(Constraint c1, Constraint c2) {
        if (c1.getLeft() != null && c2.getLeft() != null) {
            return Objects.equals(c1.getLeft().getFact(), c2.getLeft().getFact())
                    && Objects.equals(c1.getOp(), c2.getOp())
                    && Objects.equals(c1.getRight(), c2.getRight());
        }
        return false;
    }

    /**
     * 获取条件节点对应的模型节点
     *
     * @param conditionNode
     * @return
     */
    private RuleTree.Node getFactNodeOfCondition(RuleTree.Node conditionNode) {
        RuleTree.Node parentNode = conditionNode.getParent();
        if (RuleTree.Node.Type.FACT.equals(parentNode.getNodeType()) || parentNode.getNodeType() == null) {
            return parentNode;
        }
        return getFactNodeOfCondition(parentNode);
    }

    /**
     * 获取同级ELSE分支条件节点约束
     *
     * @param conditionNode
     * @return
     */
    private Constraint getElseConstraint(RuleTree.Node conditionNode) {
        RuleTree.Node parentNode = getFactNodeOfCondition(conditionNode);
        for (RuleTree.Node node : parentNode.getChildren()) {
            if (node.getElseConstraint() != null) {
                return node.getElseConstraint();
            }
        }
        return null;
    }

    /**
     * 决策树模型
     */
    static final class RuleTree {
        /**
         * 属性列表
         */
        private Set<Attr> attrs = new HashSet<>();
        /**
         * 根节点
         */
        private Node tree;

        public Set<Attr> getAttrs() {
            return attrs;
        }

        public void setAttrs(Set<Attr> attrs) {
            this.attrs = attrs;
        }

        public Node getTree() {
            return tree;
        }

        public void setTree(Node tree) {
            this.tree = tree;
        }

        /**
         * 节点
         */
        static final class Node {
            /**
             * 节点类型
             */
            private Type nodeType;
            /**
             * 数据模型表达式
             */
            private FactExpression fact;
            /**
             * 条件
             */
            private Condition condition;
            /**
             * 动作集合
             */
            private Rhs rhs;
            /**
             * 子节点集合
             */
            private List<Node> children = new ArrayList<>();
            /**
             * 父节点
             */
            private Node parent;
            /**
             * 模型节点下多个分支条件的约束集合
             */
            private Map<Integer, List<Constraint>> constraintMap = new HashMap<>();
            /**
             * ELSE分支约束
             */
            private Constraint elseConstraint;

            public Type getNodeType() {
                return nodeType;
            }

            public void setNodeType(Type nodeType) {
                this.nodeType = nodeType;
            }

            public FactExpression getFact() {
                return fact;
            }

            public void setFact(FactExpression fact) {
                this.fact = fact;
            }

            public Condition getCondition() {
                return condition;
            }

            public void setCondition(Condition condition) {
                this.condition = condition;
            }

            public Rhs getRhs() {
                return rhs;
            }

            public void setRhs(Rhs rhs) {
                this.rhs = rhs;
            }

            public List<Node> getChildren() {
                return children;
            }

            public void setChildren(List<Node> children) {
                this.children = children;
            }

            public Node getParent() {
                return parent;
            }

            public void setParent(Node parent) {
                this.parent = parent;
            }

            public Map<Integer, List<Constraint>> getConstraintMap() {
                return constraintMap;
            }

            public void setConstraintMap(Map<Integer, List<Constraint>> constraintMap) {
                this.constraintMap = constraintMap;
            }

            public Constraint getElseConstraint() {
                return elseConstraint;
            }

            public void setElseConstraint(Constraint elseConstraint) {
                this.elseConstraint = elseConstraint;
            }

            enum Type {
                FACT, // 数据模型节点
                CONDITION, // 条件节点
                ACTION // 动作节点
            }

            /**
             * 条件
             */
            static final class Condition {
                /**
                 * 操作符
                 */
                private Operator op;
                /**
                 * 右侧表达式
                 */
                private Expression right;
                /**
                 * ELSE分支
                 */
                private boolean other;

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

                public boolean isOther() {
                    return other;
                }

                public void setOther(boolean other) {
                    this.other = other;
                }

                @Override
                public String toString() {
                    return JSON.toJSONString(this);
                }
            }

            @Override
            public String toString() {
                return JSON.toJSONString(this);
            }
        }
    }
}
