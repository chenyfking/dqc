package com.beagledata.gaea.workbench.service.impl;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.User;
import com.beagledata.gaea.workbench.mapper.UserMapper;
import com.beagledata.gaea.workbench.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Auther: yinrj
 * @Date: 0022 2021/1/22 09:46
 * @Description:
 */
@Service
public class RoleServiceImpl implements RoleService {
    private static Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);
    @Autowired
    private UserMapper userMapper;

    @Override
    public Result selectUsers(Integer page, Integer pageNum, Integer roleId, String keywords) {
        if (roleId == null) {
			throw new IllegalArgumentException("角色ID不能为空");
		}

		try {
			int total = userMapper.selectCountByRoleId(roleId, keywords);
			if (total > 0) {
				List<User> list = userMapper.selectByRoleId(pageNum * (page - 1), pageNum, roleId, keywords);
				return Result.newSuccess().withData(list).withTotal(total);
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), roleId, e);
		}
		return Result.emptyList();
    }
}
