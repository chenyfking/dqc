package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

/**
 * 服务使用到的文件，
 * 知识包生成服务时，将使用到的文件同步到服务相关的文件表中，
 * 调用服务使用这些文件，与知识包及决策引擎隔离
 * Created by Chenyafeng on 2018/12/19.
 */
public class MicroAssets extends BaseEntity {
    private static final long serialVersionUID = 3725005744286099039L;

    /**
     * 服务uuid
     */
    private String microId;
    /**
     * 知识包中文件的uuid
     */
    private String assetsId;
    /**
     * 文件名
     */
    private String name;
    /**
     * 文件类型
     */
    private String type;
    /**
     * 文件内容
     */
    private String content;

    public String getMicroId() {
        return microId;
    }

    public void setMicroId(String microId) {
        this.microId = microId;
    }

    public String getAssetsId() {
        return assetsId;
    }

    public void setAssetsId(String assetsId) {
        this.assetsId = assetsId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "MicroAssets{" +
                "microId='" + microId + '\'' +
                ", assetsId='" + assetsId + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                "} " + super.toString();
    }
}
