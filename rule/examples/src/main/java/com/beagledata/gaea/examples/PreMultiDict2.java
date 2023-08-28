package com.beagledata.gaea.examples;

import java.io.Serializable;

/**
 * 多头信贷审查结果
 *
 * Created by liulu on 2018/11/5.
 */
public class PreMultiDict2 implements Serializable {
    private static final long serialVersionUID = -4623929833658346927L;

    /**
     * 多头借贷一审结果
     */
    private String preMultiDict21;

    public String getPreMultiDict21() {
        return preMultiDict21;
    }

    public void setPreMultiDict21(String preMultiDict21) {
        this.preMultiDict21 = preMultiDict21;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PreMultiDict2{");
        sb.append("preMultiDict21='").append(preMultiDict21).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
