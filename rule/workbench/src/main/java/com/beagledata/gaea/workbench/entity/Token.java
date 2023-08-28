package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * 描述:
 *
 * @author: 周庚新
 * @date: 2020/6/10
 */
public class Token extends BaseEntity {
	/**
	 * 令牌名称
	 */
	private String name;
	/**
	 * token串
	 */
	private String token;

	/**
	 * 创建人uuid
	 */
	private String creatorUuid;
	/**
	 * 创建人真实姓名
	 */
	private String creatorName;
	/**
	* 是否关联全部服务
	*/
	private boolean all;
	/**
	 * 关联的服务uuid列表
	 */
	private Set<String> microUuids = new HashSet<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getCreatorUuid() {
		return creatorUuid;
	}

	public void setCreatorUuid(String creatorUuid) {
		this.creatorUuid = creatorUuid;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}

	public boolean isAll() {
		return all;
	}

	public void setAll(boolean all) {
		this.all = all;
	}

	public Set<String> getMicroUuids() {
		return microUuids;
	}

	public void setMicroUuids(Set<String> microUuids) {
		this.microUuids = microUuids;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Token{");
		sb.append("name='").append(name).append('\'');
		sb.append(", token='").append(token).append('\'');
		sb.append(", creatorUuid='").append(creatorUuid).append('\'');
		sb.append(", creatorName='").append(creatorName).append('\'');
		sb.append(", all=").append(all);
		sb.append(", microUuids=").append(microUuids);
		sb.append('}');
		sb.append(" ").append(super.toString());
		return sb.toString();
	}
}