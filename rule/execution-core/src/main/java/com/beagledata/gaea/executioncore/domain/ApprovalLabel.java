package com.beagledata.gaea.executioncore.domain;

/**
 * 描述:
 * 审批服务标志
 *
 * @author 周庚新
 * @date 2020-10-14
 */
public class ApprovalLabel {
	/**
	* fact id
	*/
	private String id;

	/**
	 * 审批字段
	 */
	private String field;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ApprovalLabel{");
		sb.append("id='").append(id).append('\'');
		sb.append(", field='").append(field).append('\'');
		sb.append('}');
		return sb.toString();
	}
}