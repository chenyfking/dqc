package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

/**
 * 项目用户关联(用户对项目的数据权限)
 * Created by Cyf on 2019/12/12
 **/
public class ProjectUser extends BaseEntity {
    private static final long serialVersionUID = -7541434262938833257L;

    /**
     * 项目uuid
     */
    private String projectUuid;

    /**
     * 用户uuid
     */
    private String userUuid;

    public ProjectUser() {
    }

    public String getProjectUuid() {
        return projectUuid;
    }

    public void setProjectUuid(String projectUuid) {
        this.projectUuid = projectUuid;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public ProjectUser(String projectUuid, String userUuid) {
        this.projectUuid = projectUuid;
        this.userUuid = userUuid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ProjectUser{");
        sb.append("projectUuid='").append(projectUuid).append('\'');
        sb.append(", userUuid='").append(userUuid).append('\'');
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}