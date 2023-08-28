package com.beagledata.gaea.workbench.rule.parser;

import com.alibaba.fastjson.JSON;
import com.beagledata.gaea.ruleengine.runtime.scorecard.ModelTransformScoringStrategy;
import com.beagledata.gaea.ruleengine.runtime.scorecard.MulScoringStrategy;
import com.beagledata.gaea.ruleengine.runtime.scorecard.ScoringStrategy;
import com.beagledata.gaea.ruleengine.runtime.scorecard.SumScoringStrategy;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.rule.define.Attr;
import com.beagledata.gaea.workbench.rule.define.Expression;
import com.beagledata.gaea.workbench.rule.define.FactExpression;
import com.beagledata.gaea.workbench.rule.define.Lhs;
import com.beagledata.gaea.workbench.rule.define.scorecard.ScorecardDrl;
import com.beagledata.gaea.workbench.rule.define.scorecard.ScorecardRule;
import com.beagledata.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 解析评分卡功能
 * Created by mahongfei on 2018/10/18.
 */
public class ScorecardParser extends DrlParser implements Parser {
    public ScorecardParser(Assets assets) {
        super(assets);
    }

    protected void doParse() {
        drl = new ScorecardDrl(String.valueOf(assets.getId()));
        Scorecard scorecard = JSON.parseObject(assets.getContent(), Scorecard.class);
        if (!isForVerifier()) {
            doParse(scorecard);
        } else {
            doParseForVerifier(scorecard);
        }
    }

    private void doParse(Scorecard scorecard) {
        if (scorecard.getScoringType() == null
                || scorecard.getAssign() == null
                || StringUtils.isBlank(scorecard.getAssign().getId())) {
            return;
        }

        Set<Attr> attrs = scorecard.getAttrs();
        for (Attr attr : attrs) {
            drl.addAttr(attr.getName(), attr.getValue());
        }

        drl.addRule(new ScorecardRule(getRuleNamePrefix(), scorecard.getDefaultScore())); // 添加初始化规则
        for (int i = 0; i < scorecard.getRows().size(); i++) {
            Scorecard.Row row = scorecard.getRows().get(i);
            if (row.getFact() == null || row.getCondition() == null || row.getCondition().getConstraint() == null) {
                continue;
            }
            row.getCondition().getConstraint().getConstraints().forEach(c -> c.setLeft(new Expression(row.getFact())));
            ScorecardRule rule = new ScorecardRule(getRuleNamePrefix(), i + 1, row.getScore(), row.getWeight());
            if (rule.getScore() == null || StringUtils.isBlank(row.getCondition().dump())) {
                continue;
            }
            rule.setLhs(row.getCondition());
            rule.setReasonCode(row.getReasonCode());
            rule.setReasonMsg(row.getReasonMsg());
            drl.addRule(rule);
        }

        ScorecardRule endRule = new ScorecardRule(
                getRuleNamePrefix(), -1, getScoringStrategy(scorecard.getScoringType()), scorecard.getAssign()
        );
        endRule.setReasonCodesExpr(scorecard.getReasonCodes());
        endRule.setReasonMsgsExpr(scorecard.getReasonMsgs());
        drl.addRule(endRule); // 添加计算得分规则
        ((ScorecardDrl)drl).setStrategy(getScoringStrategy(scorecard.getScoringType()));
    }

    private void doParseForVerifier(Scorecard scorecard) {
        for (int i = 0; i < scorecard.getRows().size(); i++) {
            Scorecard.Row row = scorecard.getRows().get(i);
            if (row.getFact() == null || row.getCondition() == null || row.getCondition().getConstraint() == null) {
                continue;
            }
            row.getCondition().getConstraint().getConstraints().forEach(c -> c.setLeft(new Expression(row.getFact())));
            ScorecardRule rule = new ScorecardRule(getRuleNamePrefix(), i + 1, row.getScore(), row.getWeight());
            rule.setLhs(row.getCondition());
            drl.addRule(rule);
        }
    }

    /**
     * @param scoringType
     * @return 根据计算得分类型，获取策略类
     */
    private Class<? extends ScoringStrategy> getScoringStrategy(Scorecard.ScoringType scoringType) {
        switch (scoringType) {
            case MUL: return MulScoringStrategy.class;
            case MODELTRANSFORM: return ModelTransformScoringStrategy.class;
            default: return SumScoringStrategy.class;
        }
    }

    /**
     * 评分卡模型
     */
    static final class Scorecard {
        /**
         * 属性列表
         */
        private Set<Attr> attrs = new HashSet<>();
        /**
         * 是否乘以权重
         */
        private boolean weight;
        /**
         * 得分计算类型
         */
        private ScoringType scoringType;
        /**
         * 赋值数据模型表达式
         */
        private FactExpression assign;
        /**
         * 条件数据模型id
         */
        private String factId;
        /**
         * 行
         */
        private List<Row> rows = new ArrayList<>();
        /**
         * 默认评分
         */
        private Double defaultScore;
        /**
         * 原因码数据模型表达式
         */
        private FactExpression reasonCodes;
        /**
         * 原因信息数据模型表达式
         */
        private FactExpression reasonMsgs;

        enum ScoringType {
            SUM, // 求和
            MUL, // 求积
            MODELTRANSFORM // 模型评分转换
        }

        /**
         * 行
         */
        static final class Row {
            /**
             * 数据模型字段id
             */
            private FactExpression fact;
            /**
             * 条件
             */
            private Lhs condition;
            /**
             * 得分表达式
             */
            private Expression score;
            /**
             * 当前行权重
             */
            private Double weight;
            /**
             * 原因码
             */
            private String reasonCode;
            /**
             * 原因信息
             */
            private String reasonMsg;

            public FactExpression getFact() {
                return fact;
            }

            public void setFact(FactExpression fact) {
                this.fact = fact;
            }

            public Lhs getCondition() {
                return condition;
            }

            public void setCondition(Lhs condition) {
                this.condition = condition;
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
        }

        public Set<Attr> getAttrs() {
            return attrs;
        }

        public void setAttrs(Set<Attr> attrs) {
            this.attrs = attrs;
        }

        public boolean isWeight() {
            return weight;
        }

        public void setWeight(boolean weight) {
            this.weight = weight;
        }

        public ScoringType getScoringType() {
            return scoringType;
        }

        public void setScoringType(ScoringType scoringType) {
            this.scoringType = scoringType;
        }

        public FactExpression getAssign() {
            return assign;
        }

        public void setAssign(FactExpression assign) {
            this.assign = assign;
        }

        public String getFactId() {
            return factId;
        }

        public void setFactId(String factId) {
            this.factId = factId;
        }

        public List<Row> getRows() {
            return rows;
        }

        public void setRows(List<Row> rows) {
            this.rows = rows;
        }

        public Double getDefaultScore() {
            return defaultScore;
        }

        public void setDefaultScore(Double defaultScore) {
            this.defaultScore = defaultScore;
        }

        public FactExpression getReasonCodes() {
            return reasonCodes;
        }

        public void setReasonCodes(FactExpression reasonCodes) {
            this.reasonCodes = reasonCodes;
        }

        public FactExpression getReasonMsgs() {
            return reasonMsgs;
        }

        public void setReasonMsgs(FactExpression reasonMsgs) {
            this.reasonMsgs = reasonMsgs;
        }
    }
}
