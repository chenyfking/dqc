package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

/**
 * Created by Cyf on 2020/6/12
 * 规则模板
 **/
public class AssetsTemplate extends BaseEntity {
    private static final long serialVersionUID = 6562335257904632982L;

    /**
     * 模板名称
     */
    private String name;
    /**
     * 模板内容
     */
    private String content;
    /**
     * 模板类型
     */
    private String type;
    /**
     * 所属项目
     */
    private String projectUuid;
    /**
     * 创建人
     */
    private User creator;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProjectUuid() {
        return projectUuid;
    }

    public void setProjectUuid(String projectUuid) {
        this.projectUuid = projectUuid;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AssetsModel{");
        sb.append("name='").append(name).append('\'');
        sb.append(", content='").append(content).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", projectUuid='").append(projectUuid).append('\'');
        sb.append(", creator=").append(creator);
        sb.append('}');
        return sb.toString();
    }
}