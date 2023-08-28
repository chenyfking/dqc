package com.beagledata.gaea.workbench.service.impl;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.entity.OnlineUser;
import com.beagledata.gaea.workbench.entity.User;
import com.beagledata.gaea.workbench.mapper.UserMapper;
import com.beagledata.gaea.workbench.service.UserService;
import com.beagledata.gaea.workbench.util.UserHolder;
import com.beagledata.gaea.workbench.util.UserSessionManager;
import com.beagledata.security.util.PasswordUtils;
import com.beagledata.util.IdUtils;
import com.beagledata.util.StringUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yangyongqiang 2019/09/12
 */
@Service
public class UserServiceImpl extends BaseServiceImpl implements UserService {
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private UserSessionManager userSessionManager;

	@Override
	public User getByUsername(String username) {
		try {
			return userMapper.selectByUsername(username);
		} catch (Exception e) {
			logger.error(username + e.getLocalizedMessage(), e);
		}
		return null;
	}

	@Override
	@Transactional
	public User add(User user, String roleIds) {
		if (StringUtils.isBlank(user.getRealname()) || user.getRealname().length() > 20) {
			throw new IllegalArgumentException("真实姓名不能为空并且长度不能超过20个字符");
		}
		if (StringUtils.isBlank(user.getUsername()) || user.getUsername().length() > 20) {
			throw new IllegalArgumentException("用户名不能为空并且长度不能超过20个字符");
		} else {
			int count = userMapper.countByUsername(user.getUsername());
			if (count > 0) {
				throw new IllegalArgumentException("用户名不能重复");
			}
		}
		if (!user.getUsername().matches("[a-zA-Z0-9]+")) {
			throw new IllegalArgumentException("用户名只支持英文字母或数字");
		}
		if (StringUtils.isBlank(user.getPassword()) || user.getPassword().length() < 6) {
			throw new IllegalArgumentException("密码不能为空并且长度不能少于6个字符");
		}
		if (user.getOrg() == null || StringUtils.isBlank(user.getOrg().getUuid())) {
			throw new IllegalArgumentException("所属机构不能为空");
		}

		String[] pas = PasswordUtils.sign(user.getPassword());
		user.setPassword(pas[0]);
		user.setSalt(pas[1]);
		user.setUuid(IdUtils.UUID());
		try {
			int rows = userMapper.insert(user);
			if (rows < 1) {
				throw new IllegalStateException("添加失败");
			}

			if (StringUtils.isNotBlank(roleIds)) {
				userMapper.insertUserRoles(
						user.getUuid(),
						Arrays.stream(roleIds.split(","))
								.map(id -> Integer.parseInt(id))
								.collect(Collectors.toSet())
				);
			}
			return user;
		} catch (IllegalStateException e) {
			throw e;
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("用户名不能重复");
		} catch (Exception e) {
			logger.error("添加用户失败. user: {}, roleIds: {}", user, roleIds, e);
		}
		throw new IllegalStateException("添加失败");
	}

	@Override
	public Result search(int page, int pageNum, User user) {
		if (StringUtils.isNotBlank(user.getUsername())) {
			user.setUsername("%" + user.getUsername() + "%");
		}

		try {
			boolean isAdmin = UserHolder.hasAdminPermission();
			boolean isOrg = UserHolder.isOrgAdmin();
			List<User> list;
			if (page != -1) {
				list = userMapper.selectList(pageNum * (page - 1), pageNum, user, isAdmin, isOrg, UserHolder.currentUserUuid(), Constants.User.SUPERADMIN_USERNAME);
			} else {
				list = userMapper.selectList(null, null, user, isAdmin, isOrg, UserHolder.currentUserUuid(), Constants.User.SUPERADMIN_USERNAME);
			}
			if (list == null) {
				return Result.newSuccess().withData(Collections.emptyList()).withTotal(0);
			} else {
				return Result.newSuccess().withData(list).withTotal(userMapper.selectCount(user, isAdmin, isOrg, Constants.User.SUPERADMIN_USERNAME));
			}
		} catch (Exception e) {
			logger.error("查询用户列表失败. page: {}, pageNum: {}, user: {}", page, pageNum, user, e);
			throw new IllegalStateException("查询失败");
		}
	}

	@Override
	public Result searchNotProjectMember(int page, int pageNum, User user, String projectUuid) {
		if (StringUtils.isNotBlank(user.getRealname())){
			String name = user.getRealname();
			user.setRealname("%" + name + "%");
		}

		try {
			int count = userMapper.countNotProjectMember(user, projectUuid, Constants.User.SUPERADMIN_USERNAME);
			if (count <= 0) {
				return Result.emptyList();
			}

			List<User> list = userMapper.selectNotProjectMember(pageNum * (page - 1), pageNum, user, projectUuid, Constants.User.SUPERADMIN_USERNAME);
			return Result.newSuccess().withData(list).withTotal(count);
		} catch (Exception e) {
			logger.error("查询非项目成员失败. page: {}, pageNum: {}, user: {}, projectUuid: {}", page, pageNum, user, projectUuid, e);
			throw new IllegalArgumentException("项目获取错误");
		}
	}

	@Override
	public void delete(String uuid) {
		try {
			userMapper.delete(uuid);
		} catch (Exception e) {
			logger.error(uuid + e.getLocalizedMessage(), e);
			throw new IllegalStateException("删除账户失败");
		}
	}

	@Override
	public void enable(String uuid) {
		try {
			editEnable(uuid, 0);
		} catch (Exception e) {
			logger.error(uuid + e.getLocalizedMessage(), e);
			throw new IllegalStateException("账户启用失败");
		}
	}

	@Override
	public void disable(String uuid) {
		try {
			editEnable(uuid, 1);
		} catch (Exception e) {
			logger.error(uuid + e.getLocalizedMessage(), e);
			throw new IllegalStateException("账户禁用失败");
		}
	}

	@Override
	public void login(User user) {
		if (StringUtils.isBlank(user.getUsername()) && StringUtils.isBlank(user.getUuid())) {
			return;
		}

		try {
			User user1 = new User();
			user1.setUsername(user.getUsername());
			user1.setUuid(user.getUuid());
			user1.setLastLoginTime(new Date());
			userMapper.update(user1);
		} catch (Exception e) {
			logger.error(user + e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void editMyPwd(String oldPassword, String newPassword) {
		if (StringUtils.isBlank(oldPassword)) {
			throw new IllegalArgumentException("登录密码不能为空");
		}
		if (StringUtils.isBlank(newPassword) || newPassword.length() < 6) {
			throw new IllegalArgumentException("新密码长度至少六位");
		}

		User user;
		try {
			user = userMapper.selectByUsername(UserHolder.currentUserName());
		} catch (Exception e) {
			logger.error("修改密码失败. oldPassword: {}, newPassword: {}", oldPassword, newPassword, e);
			throw new IllegalStateException("修改失败");
		}

		if (user == null) {
			logger.error("修改密码失败，用户不存在. oldPassword: {}, newPassword: {}", oldPassword, newPassword);
			throw new IllegalStateException("修改失败");
		}

		String oldPasswordSign = PasswordUtils.sign(oldPassword, user.getSalt());
		if (!user.getPassword().equals(oldPasswordSign)) {
			throw new IllegalArgumentException("旧密码错误");
		}
		if (newPassword.equals(oldPassword)) {
			throw new IllegalArgumentException("新密码与旧密码一致");
		}

		try {
			String[] pas = PasswordUtils.sign(newPassword);
			user.setPassword(pas[0]);
			user.setSalt(pas[1]);
			user.setLastResetPwd(new Date());
			userMapper.update(user);
		} catch (Exception e) {
			logger.error("修改密码失败. oldPassword: {}, newPassword: {}", oldPassword, newPassword, e);
			throw new IllegalStateException("修改失败");
		}
	}

	@Override
	public Result listAllOnline(int page, int pageNum, String sortField, String sortDirection) {
		try {
			//接受过滤完的参数
			List<OnlineUser> onlineList = new ArrayList<OnlineUser>();
			boolean isAdmin = UserHolder.hasAdminPermission();
			boolean isOrg = UserHolder.isOrgAdmin();
			List<OnlineUser> onlineLists = userSessionManager.getOnlineUsers(sortField, sortDirection);
			//判断是否是系统管理员
			if (!isAdmin) {
				List<String> userUuid = new ArrayList<String>();
				//判断是否是机构管理员
				if (isOrg) {
					userUuid = userMapper.selectOrgUserUuid(UserHolder.currentUserUuid());
				} else {
					userUuid.add(UserHolder.currentUserUuid());
				}
				for (OnlineUser o : onlineList) {
					if (userUuid.contains(o.getUser().getUuid())) {
						onlineList.add(o);
					}
				}
			} else {
				onlineList.addAll(onlineLists);
			}

			int newPage = (page - 1) * pageNum;
			int newPageNum = pageNum * page;
			if (newPageNum > onlineList.size()) {
				newPageNum = onlineList.size();
			}
			return Result.newSuccess().withData(onlineList.subList(newPage, newPageNum)).withTotal(onlineList.size());
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new IllegalArgumentException("查询出错");
		}
	}

	@Override
	public void forceLogOut(String uuid, String loginIp) {
		if (StringUtils.isBlank(uuid)) {
			throw new IllegalArgumentException("用户请求数据为空!");
		}
		try {
			userSessionManager.forceLogOut(uuid, loginIp);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e, "强制用户下线出错");
		}

	}

	private void editEnable(String uuid, Integer enable) {
		User user = new User();
		user.setUuid(uuid);
		user.setDisabled(enable);
		userMapper.updateEnable(user);
	}

	@Override
	public void edit(User user, String roleIds) {
		if (StringUtils.isBlank(user.getUuid())) {
			throw new IllegalArgumentException("用户uuid不能为空");
		}
		if (StringUtils.isBlank(user.getRealname()) || user.getRealname().length() > 20) {
			throw new IllegalArgumentException("真实姓名不能为空并且长度不能超过20个字符");
		}
		if (user.getOrg() == null || StringUtils.isBlank(user.getOrg().getUuid())) {
			throw new IllegalArgumentException("所属机构不能为空");
		}
		if (StringUtils.isNotBlank(user.getPassword())) {
			if (user.getPassword().length() < 6) {
				throw new IllegalArgumentException("密码长度不能少于6个字符");
			}
			String[] pas = PasswordUtils.sign(user.getPassword());
			user.setPassword(pas[0]);
			user.setSalt(pas[1]);
		}

		try {
			int rows = userMapper.updateForEdit(user);
			if (rows < 1) {
				throw new IllegalStateException("编辑失败");
			}

			Set<Integer> oldRoleIds = userMapper.selectUserRole(user.getUuid()).stream()
					.map(role -> role.getId()).collect(Collectors.toSet());
			if (StringUtils.isBlank(roleIds)) {
				userMapper.deleteUserRoles(user.getUuid(), oldRoleIds);
			} else {
				Set<Integer> newRoleIds = Arrays.stream(roleIds.split(","))
						.map(id -> Integer.parseInt(id)).collect(Collectors.toSet());
				Collection<Integer> deleteRoleIds = ListUtils.removeAll(oldRoleIds, newRoleIds);
				Collection<Integer> insertRoleIds = ListUtils.removeAll(newRoleIds, oldRoleIds);
				if (!deleteRoleIds.isEmpty()) {
					userMapper.deleteUserRoles(user.getUuid(), deleteRoleIds);
				}
				if (!insertRoleIds.isEmpty()) {
					userMapper.insertUserRoles(user.getUuid(), insertRoleIds);
				}
			}
		} catch (IllegalStateException e) {
			throw e;
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("用户名不能重复");
		} catch (Exception e) {
			logger.error("编辑用户失败. user: {}, roleIds: {}", user, roleIds, e);
			throw new IllegalStateException("编辑失败");
		}
	}

	/**
	 * @Author yangyongqiang
	 * @Description 强制异地登录退出
	 * @Date 7:23 下午 2020/11/11
	 **/
	@Override
	public Result forceLogin() {
		//强制异地登录退出
		Session session = SecurityUtils.getSubject().getSession();
		if (session == null) {
			throw new IllegalArgumentException("请现从正常登录");
		}
		User user = (User)session.getAttribute(Constants.SESSION_KEY_USER);
		if (user == null) {
			throw new IllegalArgumentException("请现从正常登录入口登录");
		}
		userSessionManager.forceLogOutOtherPlace(user.getUuid(), session.getId().toString());
		return Result.newSuccess().withData(user);
	}

	/**
	 * @Author yangyongqiang
	 * @Description 获取用户详情
	 * @Date 上午10:23 2021/1/25
	 **/
	@Override
	public Result getUserDetails(String uuid) {
		if (StringUtils.isBlank(uuid)) {
			throw new IllegalArgumentException("用户uuid不能为空");
		}

		try {
			return Result.newSuccess().withData(userMapper.getUserByUuid(uuid));
		} catch (Exception e) {
			logger.error("查询用户详情失败 uuid: {} ", uuid, e);
			throw new IllegalStateException("查询用户详情失败");
		}
	}
}
