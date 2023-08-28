package com.beagledata.gaea.workbench.vo;

import com.beagledata.gaea.workbench.rule.verifer.VerifierType;

import java.util.Arrays;

/**
 * 描述:
 * 校验入参类
 * @author 周庚新
 * @date 2020-07-13
 */
public class VerifierVO {
	/**
	 * 规则文件uuid
	 */
	private String uuid;
	/**
	 * 规则文件版本号
	 */
	private Integer versionNo;
	/**
	 * 校验类型集合
	 */
	private VerifierType[] types;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Integer getVersionNo() {
		return versionNo;
	}

	public void setVersionNo(Integer versionNo) {
		this.versionNo = versionNo;
	}

	public VerifierType[] getTypes() {
		return types;
	}

	public void setTypes(VerifierType[] types) {
		this.types = types;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("VerifierVO{");
		sb.append("uuid='").append(uuid).append('\'');
		sb.append(", versionNo=").append(versionNo);
		sb.append(", types=").append(Arrays.toString(types));
		sb.append('}');
		return sb.toString();
	}
}