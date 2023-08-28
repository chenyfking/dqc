package com.beagledata.gaea.examples;

import java.io.Serializable;

/**
 * 不通过原因
 *
 * Created by liulu on 2018/11/5.
 */
public class Reason implements Serializable {
    private static final long serialVersionUID = -5043400325070919229L;

    /**
     * 描述
     */
    private String describe;

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Reason{");
        sb.append("describe='").append(describe).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
