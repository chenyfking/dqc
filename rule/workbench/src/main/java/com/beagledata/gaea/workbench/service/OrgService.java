package com.beagledata.gaea.workbench.service;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.Org;

/**
 * 描述: 机构管理service
 *
 * @param:
 * @author: 周庚新
 * @date: 2020/6/1
 * @return:
 */
public interface OrgService {

	/**
	 * by yangyongqiang
	 * 2020/6/1
	 */
	Org add(Org Org);

	/**
	 * 描述: 搜索机构
	 *
	 * @param: [page, pageNum, org]
	 * @author: 周庚新
	 * @date: 2020/6/1
	 * @return: com.beagledata.common.Result
	 */
	Result search(Integer page, Integer pageNum, Org org);

	/**
	 * 描述: 删除机构
	 *
	 * @param: [uuid]
	 * @author: 周庚新
	 * @date: 2020/6/1
	 * @return: void
	 */
	void delete(String uuid);

	/**
	 * by yangyongqiang
	 * 2020/6/1
	 */
	void edit(Org org);

	/**
	 * by yangyongqiang
	 * 2020/6/2
	 */
	Result selectAll();

	/**
	 * 根据名称查询机构
	 *
	 * @param name
     * @return
	 */
	Result listTop50ByName(String name);

	/**
	 *根据所属机构查询用户
	 * @author: yinrj
	 * @date: 2021/1/21
	 * @return
	 */
	Result selectUsers(Integer page, Integer pageNum, String orgUuid, String keywords);
}
