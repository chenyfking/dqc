package com.beagledata.gaea.workbench.service.impl;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.Org;
import com.beagledata.gaea.workbench.entity.User;
import com.beagledata.gaea.workbench.mapper.OrgMapper;
import com.beagledata.gaea.workbench.mapper.UserMapper;
import com.beagledata.gaea.workbench.service.OrgService;
import com.beagledata.util.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yangyongqiang 2020/06/01
 */
@Service
public class OrgServiceImpl extends BaseServiceImpl implements OrgService {
	@Autowired
	private OrgMapper orgMapper;
	@Autowired
	private UserMapper userMapper;

	@Override
	public Org add(Org org) {
		if (StringUtil.isBlank(org.getName()) || org.getName().length() > 40) {
			throw new IllegalArgumentException("机构名称不能为空并且长度不能超过40个字符");
		}
		org.setUuid(IdUtils.UUID());
		try {
			int rows = orgMapper.insert(org);
			if (rows > 0) {
				return org;
			}
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("机构名称不能重复");
		} catch (Exception e) {
			logger.error("添加机构失败: {}", org, e);
		}
		throw new IllegalStateException("机构添加失败");
	}

	@Override
	public Result search(Integer page, Integer pageNum, Org org) {
		String name = org.getName();
		if (StringUtils.isNotBlank(name)) {
			org.setName("%" + name + "%");
		}
		if (page == null || page <= 0) {
			page = 1;
		}
		try {
			int count = orgMapper.countTotal(org);
			if (count <= 0) {
				return Result.emptyList();
			}

			List<Org> list = orgMapper.selectList(pageNum * (page - 1), pageNum, org);
			return Result.newSuccess().withData(list).withTotal(count);
		} catch (Exception e) {
			logger.error("查询机构失败. page: {}, pageNum: {}, org: {}", page, pageNum, org, e);
			return Result.emptyList();
		}
	}

	@Override
	public Result selectAll() {
		try {
			List<Org> list = orgMapper.selectAll();
			return Result.newSuccess().withData(list);
		} catch (Exception e) {
			logger.error("查询机构失败", e);
			return Result.emptyList();
		}
	}

	@Override
	public void delete(String uuid) {
		try {
			int count = orgMapper.countUserByOrgUuid(uuid);
			if (count > 0) {
				throw new IllegalArgumentException("该机构下有用户，不能删除");
			}
			orgMapper.delete(uuid);
		} catch (Exception e) {
			logger.error("删除机构失败: {}", uuid, e);
			throw new IllegalStateException("删除机构失败");
		}
	}

	@Override
	public void edit(Org org) {
		if (StringUtil.isBlank(org.getUuid())) {
			throw new IllegalArgumentException("机构uuid不能为空");
		}
		if (StringUtil.isBlank(org.getName()) || org.getName().length() > 40) {
			throw new IllegalArgumentException("机构名称不能为空并且长度不能超过40个字符");
		}
		Org dbOrg = orgMapper.selectByUuid(org.getUuid());
		if (dbOrg == null) {
			throw new IllegalArgumentException("机构不存在");
		}
		try {
			orgMapper.update(org);
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("机构名称不能重复");
		} catch (Exception e) {
			logger.error("编辑机构失败: {}", org, e);
			throw new IllegalStateException("编辑失败");
		}
	}

	@Override
	public Result listTop50ByName(String name) {
	    try {
		    return Result.newSuccess().withData(orgMapper.selectTopNByName(name, 50));
	    } catch (Exception e) {
            logger.error("根据名称查询机构Top50失败: {}", name, e);
	        return Result.emptyList();
	    }
	}

	/**
	 *根据所属机构查询用户
	 * @author: yinrj
	 * @date: 2021/1/21
	 * @return
	 */
	@Override
	public Result selectUsers(Integer page, Integer pageNum, String orgUuid, String keywords) {
		if (StringUtils.isBlank(orgUuid)) {
			throw new IllegalArgumentException("机构uuid不能为空");
		}

		try {
			int total = userMapper.selectCountByOrgUuid(orgUuid, keywords);
			if (total > 0) {
				List<User> list = userMapper.selectByOrgUuid(pageNum * (page - 1), pageNum, orgUuid, keywords);
				return Result.newSuccess().withData(list).withTotal(total);
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), orgUuid, e);
		}
		return Result.emptyList();
	}
}
