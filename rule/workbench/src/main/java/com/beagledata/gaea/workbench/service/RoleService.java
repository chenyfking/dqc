package com.beagledata.gaea.workbench.service;

import com.beagledata.common.Result;

/**
 * @Auther: yinrj
 * @Date: 0022 2021/1/22 09:44
 * @Description:
 */
public interface RoleService {
    /**
	 *根据所属机构查询用户
	 * @author: yinrj
	 * @date: 2021/1/22
	 * @return
	 */
	Result selectUsers(Integer page, Integer pageNum, Integer roleId, String keywords);
}
