package com.beagledata.gaea.examples;

import java.io.Serializable;

/**
 * 授信额度
 *
 * Created by liulu on 2018/11/5.
 */
public class CreditExtension implements Serializable {
    private static final long serialVersionUID = 5695368756964116392L;

    /**
     * 最大授额上限
     */
    private String uppe_limit_of_quota;

    public String getUppe_limit_of_quota() {
        return uppe_limit_of_quota;
    }

    public void setUppe_limit_of_quota(String uppe_limit_of_quota) {
        this.uppe_limit_of_quota = uppe_limit_of_quota;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CreditExtension{");
        sb.append("uppe_limit_of_quota='").append(uppe_limit_of_quota).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
