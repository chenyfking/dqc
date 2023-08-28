package com.beagledata.gaea.examples;

import java.io.Serializable;

/**
 * 信用等级
 *
 * Created by liulu on 2018/11/5.
 */
public class CreditRating implements Serializable {
    private static final long serialVersionUID = -4372424238615839438L;

    /**
     * 评级为
     */
    private String rank;

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CreditRating{");
        sb.append("rank='").append(rank).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
