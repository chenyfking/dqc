package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

/**
 * Created by mahongfei on 2018/9/18.
 */
public class Project extends BaseEntity {
	private static final long serialVersionUID = 2942535653069026004L;

	/**
	 * 项目名称
	 */
	private String name;
	/**
	 * 项目描述
	 */
	private String description;
	/**
	 * 创建用户
	 */
	private User creator;

	private boolean userEdit;
	/**
	 * 待审核基线个数
	 */
	private Integer auditCount;

	public Project() {
	}

	public Project(String uuid) {
		super.setUuid(uuid);
	}

	public Project(String name, String description) {
		this.setName(name);
		this.setDescription(description);
	}

	public Project(String uuid, String name, String description) {
		super.setUuid(uuid);
		this.setName(name);
		this.setDescription(description);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

    public boolean isUserEdit() {
        return userEdit;
    }

    public void setUserEdit(boolean userEdit) {
        this.userEdit = userEdit;
    }

	public Integer getAuditCount() {
		return auditCount;
	}

	public void setAuditCount(Integer auditCount) {
		this.auditCount = auditCount;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Project{");
		sb.append("name='").append(name).append('\'');
		sb.append(", description='").append(description).append('\'');
		sb.append(", creator=").append(creator);
		sb.append(", userEdit=").append(userEdit);
		sb.append(", auditCount=").append(auditCount);
		sb.append('}');
		sb.append(" ").append(super.toString());
		return sb.toString();
	}
}
