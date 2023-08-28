package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * @auto: yangyongqiang
 * @Description:资源类
 * @time: 2018-09-18 16:28
 **/

public class Assets extends BaseEntity {
    private static final long serialVersionUID = 4452388250671661579L;

    /**
     * 文件名称
     **/
    private String name;
    /**
     * 英文名称
     */
    private String enName;
    /**
     * 文件描述
     **/
    private String description;
    /**
     * 文件内容
     **/
    private String content;
    /**
     * 文件类型
     **/
    private String type;
    /**
     * 所属项目uuid
     **/
    private String projectUuid;
    /**
     * 项目名称
     **/
    private String projectName;
    /**
     * 创建用户
     */
    private User creator;
    /**
     * 版本id
     */
    private Integer versionId;
    /**
     * 版本号
     */
    private Integer versionNo;
    /**
     * 版本描述
     */
    private String versionDesc;
    /**
     * 是否被锁定
     */
    private boolean lock;
    /**
     * 文件的上级目录
     */
    private String dirParentId;
    /**
     * 文件锁定人（用户的uuid）
     */
    private User locker;
    /**
     * 锁定时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date lockTime;
    /**
     * 正在编辑的人员姓名
     */
    private User editor;
    /**
     * 正在编辑的状态开始编辑的时间
     */
    private Date editTime;

    public Assets() {
    }

    public Assets(String uuid) {
        super.setUuid(uuid);
    }

    public User getLocker() {
        return locker;
    }

    public void setLocker(User locker) {
        this.locker = locker;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public boolean isLocked() {
        return lock;
    }

    public void setLocked(boolean lock) {
        this.lock = lock;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Integer getVersionId() {
        return versionId;
    }

    public void setVersionId(Integer versionId) {
        this.versionId = versionId;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public String getVersionDesc() {
        return versionDesc;
    }

    public void setVersionDesc(String versionDesc) {
        this.versionDesc = versionDesc;
    }

    public String getDirParentId() {
        return dirParentId;
    }

    public void setDirParentId(String dirParentId) {
        this.dirParentId = dirParentId;
    }

    public Date getLockTime() {
        return lockTime;
    }

    public void setLockTime(Date lockTime) {
        this.lockTime = lockTime;
    }

    public User getEditor() {
        return editor;
    }

    public void setEditor(User editor) {
        this.editor = editor;
    }

    public Date getEditTime() {
        return editTime;
    }

    public void setEditTime(Date editTime) {
        this.editTime = editTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Assets{");
        sb.append("name='").append(name).append('\'');
        sb.append(", enName='").append(enName).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", content='").append(content).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", projectUuid='").append(projectUuid).append('\'');
        sb.append(", projectName='").append(projectName).append('\'');
        sb.append(", creator=").append(creator);
        sb.append(", versionId=").append(versionId);
        sb.append(", versionNo=").append(versionNo);
        sb.append(", versionDesc='").append(versionDesc).append('\'');
        sb.append(", lock=").append(lock);
        sb.append(", dirParentId='").append(dirParentId).append('\'');
        sb.append(", locker=").append(locker);
        sb.append(", lockTime=").append(lockTime);
        sb.append(", editor=").append(editor);
        sb.append(", editTime=").append(editTime);
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
    