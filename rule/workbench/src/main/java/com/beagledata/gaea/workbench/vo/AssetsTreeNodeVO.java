package com.beagledata.gaea.workbench.vo;

import com.beagledata.gaea.workbench.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 描述: 决策建模左侧树节点
 *
 * @author: 周庚新
 * @date: 2020/6/23
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetsTreeNodeVO {
	/**
	 * 资源uuid
	 */
	private String uuid;
	/**
	 * 名称
	 */
	private String label;
	/**
	 * 是否叶子节点 （文件）
	 */
	private boolean leaf;
	/**
	 * 文件夹是否锁定
	 */
	private boolean locked;
	/**
	 * 正在编辑的人员姓名
	 */
	private User editor;
	/**
	 * 文件类型
	 */
	private String type;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isLeaf() {
		return leaf;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public User getEditor() {
		return editor;
	}

	public void setEditor(User editor) {
		this.editor = editor;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("AssetsTreeNodeVO{");
		sb.append("uuid='").append(uuid).append('\'');
		sb.append(", label='").append(label).append('\'');
		sb.append(", leaf=").append(leaf);
		sb.append(", locked=").append(locked);
		sb.append(", editor=").append(editor);
		sb.append(", type='").append(type).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
    