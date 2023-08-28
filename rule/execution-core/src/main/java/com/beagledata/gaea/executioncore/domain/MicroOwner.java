package com.beagledata.gaea.executioncore.domain;

/**
 * 服务所有者
 *
 * Created by liulu on 2020/7/22.
 */
public class MicroOwner {
    /**
     * 机构uuid
     */
    private String orgUuid;
    /**
     * 机构名称
     */
	private String orgName;
    /**
     * 用户uuid
     */
    private String userUuid;
    /**
     * 用户名称
     */
    private String userName;

    public String getOrgUuid() {
        return orgUuid;
    }

    public void setOrgUuid(String orgUuid) {
        this.orgUuid = orgUuid;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MicroOwner{");
        sb.append("orgUuid='").append(orgUuid).append('\'');
        sb.append(", orgName='").append(orgName).append('\'');
        sb.append(", userUuid='").append(userUuid).append('\'');
        sb.append(", userName='").append(userName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
