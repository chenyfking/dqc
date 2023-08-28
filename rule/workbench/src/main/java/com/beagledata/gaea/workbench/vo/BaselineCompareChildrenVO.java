package com.beagledata.gaea.workbench.vo;

import com.beagledata.gaea.workbench.entity.Assets;

import java.util.Objects;

/**
 * 描述:
 * 基线版本比较
 *
 * @author 周庚新
 * @date 2020-07-06
 */
public class BaselineCompareChildrenVO {

	/**
	 * 资源文件uuid
	 */
	private String uuid;
	/**
	 * 资源文件类型
	 */
	private String type;
	/**
	 * 资源文件名称
	 */
	private String name;

	/**
	 * 基线版本选择1 的 资源文件版本号
	 */
	private Integer baselineV1;
	/**
	 * 基线版本选择2 的 资源文件版本号
	 */
	private Integer baselineV2;
	/**
	 * 两个基线选择资源文件版本是否一致，
	 */
	private boolean same;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getBaselineV1() {
		return baselineV1;
	}

	public void setBaselineV1(Integer baselineV1) {
		this.baselineV1 = baselineV1;
	}

	public Integer getBaselineV2() {
		return baselineV2;
	}

	public void setBaselineV2(Integer baselineV2) {
		this.baselineV2 = baselineV2;
	}

	public boolean isSame() {
		return same;
	}

	public void setSame(boolean same) {
		this.same = same;
	}
}