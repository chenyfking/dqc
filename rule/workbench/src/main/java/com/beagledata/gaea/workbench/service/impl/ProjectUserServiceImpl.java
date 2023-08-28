package com.beagledata.gaea.workbench.service.impl;


import com.beagledata.gaea.workbench.entity.Project;
import com.beagledata.gaea.workbench.entity.ProjectUser;
import com.beagledata.gaea.workbench.entity.User;
import com.beagledata.gaea.workbench.mapper.ProjectMapper;
import com.beagledata.gaea.workbench.mapper.ProjectUserMapper;
import com.beagledata.gaea.workbench.service.ProjectUserService;
import com.beagledata.gaea.workbench.util.UserHolder;
import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cyf on 2019/12/13
 **/
@Service
public class ProjectUserServiceImpl implements ProjectUserService {
	private static final Logger logger = LoggerFactory.getLogger(ProjectUserServiceImpl.class);

	@Autowired
	private ProjectUserMapper projectUserMapper;
	@Autowired
	private ProjectMapper projectMapper;

	@Override
	public List<User> selectUserByProject(String projectUuid) {
		if (StringUtil.isBlank(projectUuid)) {
			logger.info("查询项目关联的用户, projectUuid为空");
			throw new IllegalArgumentException("参数异常");
		}
		try {
			return projectUserMapper.selectUserByPrj(projectUuid);
		} catch (Exception e) {
			logger.info("查询项目关联的用户出错, projectUuid: {}", projectUuid);
			logger.error(e.getLocalizedMessage(), e);
			throw new IllegalArgumentException("查询项目关联的用户出错");
		}
	}

	@Override
	public void batchRelation(String projectUuid, String userUuids) {
		if (StringUtil.isBlank(projectUuid)) {
			logger.info("查询项目关联的用户, projectUuid为空");
			throw new IllegalArgumentException("参数异常");
		}
		try {
			Project project = projectMapper.selectByUuid(projectUuid);
			if (project == null) {
				logger.info("查询项目为空, projectUuid: {}", projectUuid);
				throw new IllegalArgumentException("查询项目不存在");
			}
			projectUserMapper.deleteByProject(project.getUuid(), UserHolder.currentUserUuid());
			if (StringUtil.isBlank(userUuids)) {
				return;
			}
			String[] ids = userUuids.split(",");
			if (ids == null || ids.length < 1) {
				return;
			}
			List<ProjectUser> list = new ArrayList<>(ids.length + 1);
			for (String userUuid : ids) {
				list.add(new ProjectUser(project.getUuid(), userUuid));
			}

			projectUserMapper.batchInsert(list);
		} catch (Exception e) {
			logger.error("项目关联用户出错, projectUuid: {}, userUuids: {}", projectUuid, userUuids, e);
			throw new IllegalStateException("项目关联用户出错");
		}
	}
}