package com.beagledata.gaea.workbench.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

/**
 * 在线账户
 *
 * Created by liulu on 2018/1/24.
 */
public class OnlineUser implements Comparable<OnlineUser> {
	/**
	 * 登录账户
	 */
	private User user;
	/**
	 * 登录时间
	 */
	private Date loginTime;
	/**
	 * 在线时长毫秒数(ms)
	 */
	@JsonIgnore
	private long onlineMillis;
	/**
	 * 在线时长字符串描述
	 */
	private String onlineTime;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	public String getOnlineTime() {
		return onlineTime;
	}

	public void setOnlineTime(String onlineTime) {
		this.onlineTime = onlineTime;
	}

	public long getOnlineMillis() {
		return onlineMillis;
	}

	public void setOnlineMillis(long onlineMillis) {
		this.onlineMillis = onlineMillis;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("OnlineUser{");
		sb.append("user=").append(user);
		sb.append(", loginTime=").append(loginTime);
		sb.append(", onlineMillis=").append(onlineMillis);
		sb.append(", onlineTime='").append(onlineTime).append('\'');
		sb.append('}');
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		OnlineUser onlineUser = (OnlineUser) obj;
		if (this.user == null || onlineUser.getUser() == null) return false;
		User thisUser = this.user;
		User user = onlineUser.getUser();
		if (thisUser.getId() == null || thisUser.getId() <= 0
				|| user.getId() == null || user.getId() <= 0)
			return false;
		if (!thisUser.getId().equals(user.getId())) return false;
		if (!thisUser.getLastLoginIP().equals(user.getLastLoginIP())) {
			return false;
		}
		if (Math.abs((thisUser.getLastLoginTime().getTime() - user.getLastLoginTime().getTime())) > 2000) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {

		return super.hashCode();
	}

	/**
	 * 比较登录时长
	 *
	 * @return 比较后结果为1:取当前对象       -1:取比较对象
	 */
	@Override
	public int compareTo(OnlineUser o) {
		if (this.loginTime == null || o.loginTime == null) return 1;
		return o.loginTime.before(this.loginTime) ? 1 : -1;
	}

}
