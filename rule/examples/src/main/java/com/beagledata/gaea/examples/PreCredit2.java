package com.beagledata.gaea.examples;

import java.io.Serializable;

/**
 * 信用历史审查结果
 *
 * Created by liulu on 2018/11/5.
 */
public class PreCredit2 implements Serializable {
    private static final long serialVersionUID = -84052109649633448L;

    /**
     * 信用历史一审结果
     */
    private String preCredit21;
    /**
     * 信用历史二审结果
     */
    private String preCredit22;
    /**
     * 信用历史三审结果
     */
    private String preCredit23;

    public String getPreCredit21() {
        return preCredit21;
    }

    public void setPreCredit21(String preCredit21) {
        this.preCredit21 = preCredit21;
    }

    public String getPreCredit22() {
        return preCredit22;
    }

    public void setPreCredit22(String preCredit22) {
        this.preCredit22 = preCredit22;
    }

    public String getPreCredit23() {
        return preCredit23;
    }

    public void setPreCredit23(String preCredit23) {
        this.preCredit23 = preCredit23;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PreCredit2{");
        sb.append("preCredit21='").append(preCredit21).append('\'');
        sb.append(", preCredit22='").append(preCredit22).append('\'');
        sb.append(", preCredit23='").append(preCredit23).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
