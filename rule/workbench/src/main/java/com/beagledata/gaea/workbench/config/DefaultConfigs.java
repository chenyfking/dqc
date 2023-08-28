package com.beagledata.gaea.workbench.config;

import com.beagledata.gaea.common.BaseConfigs;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by liulu on 2019/7/23.
 */

@Component
@ConfigurationProperties(prefix = "config")
public class DefaultConfigs extends BaseConfigs {
    /**
     * 最大允许密码输入错误次数
     */
    private Integer loginRetryNumber;
    /**
     * 允许输入错误次数的时间间隔(分钟)
     */
    private Integer loginRetryTime;
    /**
     * 锁定时间（小时）
     */
    private Integer loginLockTime;
    /**
     * 密码修改时间间隔（天）
     */
    private Integer pwdExpiredTime;

    /**
     * dqc登录页面url
     */
    private String dqcurl;

    /**
     * 是否开启dqc会话控制
     * @return
     */
    private String dqcsession;

    public Integer getLoginRetryNumber() {
        return loginRetryNumber;
    }

    public void setLoginRetryNumber(Integer loginRetryNumber) {
        this.loginRetryNumber = loginRetryNumber;
    }

    public Integer getLoginRetryTime() {
        return loginRetryTime;
    }

    public void setLoginRetryTime(Integer loginRetryTime) {
        this.loginRetryTime = loginRetryTime;
    }

    public Integer getLoginLockTime() {
        return loginLockTime;
    }

    public void setLoginLockTime(Integer loginLockTime) {
        this.loginLockTime = loginLockTime;
    }

    public Integer getPwdExpiredTime() {
        return pwdExpiredTime;
    }

    public void setPwdExpiredTime(Integer pwdExpiredTime) {
        this.pwdExpiredTime = pwdExpiredTime;
    }

    public String getDqcurl() {
        return dqcurl;
    }

    public void setDqcurl(String dqcurl) {
        this.dqcurl = dqcurl;
    }

    public String getDqcsession() {
        return dqcsession;
    }

    public void setDqcsession(String dqcsession) {
        this.dqcsession = dqcsession;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DefaultConfigs{");
        sb.append("loginRetryNumber=").append(loginRetryNumber);
        sb.append(", loginRetryTime=").append(loginRetryTime);
        sb.append(", loginLockTime=").append(loginLockTime);
        sb.append(", pwdExpiredTime=").append(pwdExpiredTime);
        sb.append(", dqcsession=").append(dqcsession);
        sb.append(", dqcurl=").append(dqcurl);
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
