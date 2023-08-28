package com.beagledata.gaea.examples;

import java.io.Serializable;

/**
 * 评分结果
 *
 * Created by liulu on 2018/11/5.
 */
public class ScoreResult implements Serializable {
    private static final long serialVersionUID = -2660131993387869924L;

    /**
     * 分数为
     */
    private Integer score_result;

    public Integer getScore_result() {
        return score_result;
    }

    public void setScore_result(Integer score_result) {
        this.score_result = score_result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ScoreResult{");
        sb.append("score_result='").append(score_result).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
