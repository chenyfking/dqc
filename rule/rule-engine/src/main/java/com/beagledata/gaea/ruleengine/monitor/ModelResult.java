package com.beagledata.gaea.ruleengine.monitor;


/**
 * Area under the Curve,用来度量分类模型好坏的一个标准
 * 即ROC 曲线下方的那部分面积的大小
 * Created by Cyf on 2019/7/4
 **/
public class ModelResult implements Comparable<ModelResult>{

    private String positive;    //二分类模型正类，key1为 p1 或 p1.0 或 yes
    private String negative;    //二分类模型负类，key2为 p0 或 p0.0 或 no
    private String y;   //二分类模型 的label(即用户输入的y) 为 1/0 或1.0/0.0 或 yes/no
    private Double score;   //对应二分类模型预测结果中 key1对应的值，即p1或p1.0,或yes对应的值

    public boolean isPositive() {
        return y.equals(positive);
    }

    public String getY() {
        return y;
    }

    public double getScore() {
        return score;
    }

    public void setY(String y) {
        this.y = y;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getPositive() {
        return positive;
    }

    public void setPositive(String positive) {
        this.positive = positive;
    }

    public String getNegative() {
        return negative;
    }

    public void setNegative(String negative) {
        this.negative = negative;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ModelResult{");
        sb.append("positive='").append(positive).append('\'');
        sb.append(", negative='").append(negative).append('\'');
        sb.append(", y='").append(y).append('\'');
        sb.append(", score=").append(score);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(ModelResult o) {
        if (null == o ) {   //降序
            return -1;
        }
        if (this.score == null && o.score == null) {
            return 0;
        }
        if (this.score == null) {
            return 1;
        }
        if (o.score == null) {
            return -1;
        }
        if ((this.score - o.score) < 0) {
            return 1;
        } else if ((this.score - o.score) == 0) {
            return 0;
        } else {
            return -1;
        }
    }

}