package com.beagledata.gaea.workbench.vo;

/**
 * 描述:
 * 服务关联令牌
 * @author 周庚新
 * @date 2020-06-11
 */
public class MicroTokenVO {
	/**
	* 描述: 令牌token值
	*/
	private String token;

	/**
	 * 描述: 服务id
	 */
	private String microUuid;

	public MicroTokenVO() {
	}

	public MicroTokenVO(String token, String microUuid) {
		this.token = token;
		this.microUuid = microUuid;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getMicroUuid() {
		return microUuid;
	}

	public void setMicroUuid(String microUuid) {
		this.microUuid = microUuid;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("MicroTokenVO{");
		sb.append("token='").append(token).append('\'');
		sb.append(", microUuid='").append(microUuid).append('\'');
		sb.append('}');
		return sb.toString();
	}
}