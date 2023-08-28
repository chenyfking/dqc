package com.beagledata.gaea.examples;

import java.io.Serializable;

/**
 * 司法名单审批结果
 *
 * Created by liulu on 2018/11/5.
 */
public class PreLaw2 implements Serializable {
    private static final long serialVersionUID = 5133304117757335742L;

    /**
     * 司法一审结果
     */
    private String preLaw21;
    /**
     * 司法二审结果
     */
    private String preLaw22;

    public String getPreLaw21() {
        return preLaw21;
    }

    public void setPreLaw21(String preLaw21) {
        this.preLaw21 = preLaw21;
    }

    public String getPreLaw22() {
        return preLaw22;
    }

    public void setPreLaw22(String preLaw22) {
        this.preLaw22 = preLaw22;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PreLaw2{");
        sb.append("preLaw21='").append(preLaw21).append('\'');
        sb.append(", preLaw22='").append(preLaw22).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
