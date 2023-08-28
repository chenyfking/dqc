package com.beagledata.gaea.workbench.entity;


import com.alibaba.fastjson.annotation.JSONField;
import com.beagledata.gaea.common.BaseEntity;
import com.beagledata.gaea.workbench.common.Constants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 描述:
 * 用户
 *
 * @author 周庚新
 * @date 2019-08-15
 */
public class User extends BaseEntity {
    private static final long serialVersionUID = -5681005556809537991L;

    /**
     * 真实姓名
     */
    private String realname;
    /**
     * 用户名（登录名）
     */
    private String username;
    /**
     * 密码
     */
    @JsonIgnore
    @JSONField(serialize = false)
    private String password;
    /**
     * 盐值
     */
    @JsonIgnore
    @JSONField(serialize = false)
    private String salt;
    /**
     * 最后登录时间
     */
    private Date lastLoginTime;
    /**
     * 是否禁用（）
     */
    private Integer disabled;
    /**
     * 权限代码集合
     */
    private Set<String> permissions = new HashSet<>();
    /**
     * 是否管理员
     */
    @JsonIgnore
    private boolean admin;
    /**
     * 登录IP
     */
    private String lastLoginIP;
    /**
     * 过期时间
     */
    @JsonFormat(pattern = Constants.DEFAULT_DATE_FORMAT, timezone = "GMT+8")
    @DateTimeFormat(pattern = Constants.DEFAULT_DATE_FORMAT)
    private Date expiredTime;
    /**
     * 所属机构
     */
    private Org org;
    /**
     * 是否被强制下线
     */
    @JsonIgnore
    @JSONField(serialize = false)
    private boolean forceLogout;
    /**
     * 最后重置密码时间
     */
    private Date lastResetPwd;

    public User() {
    }

    public User(String uuid) {
        super.setUuid(uuid);
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String uuid, String username, String password) {
        this(uuid);
        this.username = username;
        this.password = password;
    }

    public User(String uuid, String username, String password, Date lastResetPwd) {
        this(uuid);
        this.username = username;
        this.password = password;
        this.lastResetPwd = lastResetPwd;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Integer getDisabled() {
        return disabled;
    }

    public void setDisabled(Integer disabled) {
        this.disabled = disabled;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getLastLoginIP() {
        return lastLoginIP;
    }

    public void setLastLoginIP(String lastLoginIP) {
        this.lastLoginIP = lastLoginIP;
    }

    public Date getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(Date expiredTime) {
        this.expiredTime = expiredTime;
    }

    public Org getOrg() {
        return org;
    }

    public void setOrg(Org org) {
        this.org = org;
    }

    public boolean isForceLogout() {
        return forceLogout;
    }

    public void setForceLogout(boolean forceLogout) {
        this.forceLogout = forceLogout;
    }

    public Date getLastResetPwd() {
        return lastResetPwd;
    }

    public void setLastResetPwd(Date lastResetPwd) {
        this.lastResetPwd = lastResetPwd;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("User{");
        sb.append("realname='").append(realname).append('\'');
        sb.append(", username='").append(username).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", salt='").append(salt).append('\'');
        sb.append(", lastLoginTime=").append(lastLoginTime);
        sb.append(", disabled=").append(disabled);
        sb.append(", permissions=").append(permissions);
        sb.append(", admin=").append(admin);
        sb.append(", lastLoginIP='").append(lastLoginIP).append('\'');
        sb.append(", expiredTime=").append(expiredTime);
        sb.append(", org=").append(org);
        sb.append(", forceLogout=").append(forceLogout);
        sb.append(", lastResetPwd=").append(lastResetPwd);
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}