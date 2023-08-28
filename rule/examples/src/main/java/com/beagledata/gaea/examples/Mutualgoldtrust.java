package com.beagledata.gaea.examples;

import java.io.Serializable;

/**
 * 互金内部信审结果
 *
 * Created by liulu on 2018/11/5.
 */
public class Mutualgoldtrust implements Serializable {
    private static final long serialVersionUID = -4051923269636611944L;

    /**
     * 互金一审结果
     */
    private String mutualGoldTrust1;
    /**
     * 互金二审结果
     */
    private String mutualGoldTrust2;

    public String getMutualGoldTrust1() {
        return mutualGoldTrust1;
    }

    public void setMutualGoldTrust1(String mutualGoldTrust1) {
        this.mutualGoldTrust1 = mutualGoldTrust1;
    }

    public String getMutualGoldTrust2() {
        return mutualGoldTrust2;
    }

    public void setMutualGoldTrust2(String mutualGoldTrust2) {
        this.mutualGoldTrust2 = mutualGoldTrust2;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Mutualgoldtrust{");
        sb.append("mutualGoldTrust1='").append(mutualGoldTrust1).append('\'');
        sb.append(", mutualGoldTrust2='").append(mutualGoldTrust2).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
