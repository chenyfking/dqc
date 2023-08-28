package com.beagledata.gaea.workbench.vo;

import java.util.List;

/**
 * 描述:
 * 基线版本比较
 * @author 周庚新
 * @date 2020-07-06
 */
public class BaselineCompareVO {
	/**
	 * 唯一标示
	 */
	private String uuid;
	/**
	* 资源文件类型
	*/
	private String typeName;
	/**
	 * 该类型下的所有文件
	 */
	private List<BaselineCompareChildrenVO> children;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public List<BaselineCompareChildrenVO> getChildren() {
		return children;
	}

	public void setChildren(List<BaselineCompareChildrenVO> children) {
		this.children = children;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("BaselineCompareVO{");
		sb.append("uuid='").append(uuid).append('\'');
		sb.append(", typeName='").append(typeName).append('\'');
		sb.append(", children=").append(children);
		sb.append('}');
		return sb.toString();
	}
}