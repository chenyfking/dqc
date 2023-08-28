package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

/**
 * Created by mahongfei on 2018/11/27.
 */
public class Folder extends BaseEntity {
     //文件夹id
    private String dirId;

    //文件夹名称
    private String dirName;

    //文件夹上级id
    private String parentId;

    //菜单类型名称
    private String categoryName;

    //创建文件或者文件夹的项目uuid
    private String projectUuid;

    //是否被锁定
    private boolean locked;

    //创建用户
    private User creator;

    public Folder(String uuid){
        super.setUuid(uuid);
    }

    public Folder(){}

    public String getDirId() {
        return dirId;
    }

    public void setDirId(String dirId) {
        this.dirId = dirId;
    }

    public String getDirName() {
        return dirName;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getProjectUuid() {
        return projectUuid;
    }

    public void setProjectUuid(String projectUuid) {
        this.projectUuid = projectUuid;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Folder{");
        sb.append("dirId='").append(dirId).append('\'');
        sb.append(", dirName='").append(dirName).append('\'');
        sb.append(", parentId='").append(parentId).append('\'');
        sb.append(", categoryName='").append(categoryName).append('\'');
        sb.append(", projectUuid='").append(projectUuid).append('\'');
        sb.append(", locked=").append(locked);
        sb.append(", creator=").append(creator);
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
