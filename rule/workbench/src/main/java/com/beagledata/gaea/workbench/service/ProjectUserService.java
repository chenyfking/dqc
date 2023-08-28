package com.beagledata.gaea.workbench.service;

import com.beagledata.gaea.workbench.entity.User;

import java.util.List;

/**
 * 项目用户关联
 * Created by Cyf on 2019/12/13
 **/
public interface ProjectUserService {
	/**
	 * 查看项目关联的用户 过滤项目创建者和管理员
	 */
	List<User> selectUserByProject(String projectUuid);

	/**
	 * 项目批量关联用户
	 *
	 * @param projectUuid
	 * @param userUuids
	 */
	void batchRelation(String projectUuid, String userUuids);
}