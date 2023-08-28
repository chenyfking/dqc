package com.beagledata.gaea.workbench.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by mahongfei on 2019/11/12.
 */
public class PermissionSet {
	/**
	 * 内置权限
	 */
	public static final Set<Permission> CONTEXT = new HashSet();

	static {
		Set<Permission> CONTEXT1 = new HashSet(Arrays.asList(
				User.ADMIN,
				User.VIEW,
				User.ADD,
				User.EDIT,
				User.EDIT_VALIDATION,
				User.DELETE,
				Role.ADMIN,
				Role.LIST,
				Role.ADD,
				Role.EDIT,
				Role.DELETE,
				Role.PERMISSION_LIST,
				Role.PERMISSION_EDIT,
				Role.VIEWUSER,
				Project.ADMIN,
				Project.ASSET_ADD,
				Project.ASSET_EDIT,
				Project.ASSET_DEL,
				Project.PKG_ADD,
				Project.PKG_EDIT,
				Project.PKG_DEL,
				Project.PKG_RUN,
				Project.BASELINE_ADD,
				Project.BASELINE_AUDIT,
				Project.BASELINE_DEL,
				Project.BASELINE_DOWNLOAD,
				Project.BASELINE_PUBLISH,
				Micro.ADMIN,
				Micro.AIMODEL_ADD,
				Micro.AIMODEL_EDIT,
				Micro.AIMODEL_DEL,
				Micro.AIMODEL_VIEW,
				Micro.AIMODEL_DOWNLOAD,
				Micro.CLIENT_VIEW,
				Micro.CLIENT_DEL,
				Micro.CLIENT_EDIT,
				Micro.MICRO_VIEW,
				Micro.MICRO_TRACE,
				Micro.MICRO_EDIT,
				Micro.MICRO_SHUTDOWN,
				Login.ADMIN,
				Login.VIEW,
				Login.OFFLINE,
				Log.VIEW,
				Menu.ADMIN,
				Org.ADMIN,
				Org.VIEW,
				Org.ADD,
				Org.EDIT,
				Org.DELETE,
				Org.VIEWUSER
		));
		CONTEXT.addAll(CONTEXT1);
	}

	/**
	 * 机构管理权限
	 */
	public static final class Org {
		/**
		 * 机构管理所有权限
		 */
		public static final Permission ADMIN = add("机构管理所有权限", "org", "*", "机构管理");
		public static final String CODE_ADMIN = "org:*";
		/**
		 * 机构列表
		 */
		public static final Permission VIEW = add("机构列表", "org", "view", "机构管理");
		public static final String CODE_VIEW = "org:view";
		/**
		 * 新增机构
		 */
		public static final Permission ADD = add("新增机构", "org", "add", "机构管理");
		public static final String CODE_ADD = "org:add";
		/**
		 * 编辑机构
		 */
		public static final Permission EDIT = add("编辑机构", "user", "edit", "机构管理");
		public static final String CODE_EDIT = "org:edit";
		/**
		 * 删除机构
		 */
		public static final Permission DELETE = add("删除机构", "user", "del", "机构管理");
		public static final String CODE_DELETE = "org:del";
		/**
		 * 查看用户
		 */
		public static final Permission VIEWUSER = add("查看用户", "org", "viewuser", "机构管理");
		public static final String CODE_VIEWUSER = "org:viewuser";
	}
	/**
	 * 用户管理权限
	 */
	public static final class User {
		/**
		 * 用户管理所有权限
		 */
		public static final Permission ADMIN = add("用户管理所有权限", "user", "*", "用户管理");
		public static final String CODE_ADMIN = "user:*";
		/**
		 * 用户列表
		 */
		public static final Permission VIEW = add("用户列表", "user", "view", "用户管理");
		public static final String CODE_VIEW = "user:view";
		/**
		 * 新增用户
		 */
		public static final Permission ADD = add("用户添加", "user", "add", "用户管理");
		public static final String CODE_ADD = "user:add";
		/**
		 * 编辑用户
		 */
		public static final Permission EDIT = add("编辑用户", "user", "edit", "用户管理");
		public static final String CODE_EDIT = "user:edit";
		/**
		 * 启用禁用用户
		 */
		public static final Permission EDIT_VALIDATION = add("启用禁用用户", "user", "enable", "", "用户管理");
		public static final String CODE_EDIT_VALIDATION = "user:enable";
		/**
		 * 删除用户
		 */
		public static final Permission DELETE = add("用户删除", "user", "del", "用户管理");
		public static final String CODE_DELETE = "user:del";
	}

	/**
	 * 角色管理权限
	 */
	public static final class Role {
		/**
		 * 角色管理所有权限
		 */
		public static final Permission ADMIN = add("角色管理所有权限", "role", "*", "角色管理");
		public static final String CODE_ADMIN = "role:*";
		/**
		 * 角色列表
		 */
		public static final Permission LIST = add("角色列表", "role", "list", "角色管理");
		public static final String CODE_LIST = "role:list";
		/**
		 * 新增角色
		 */
		public static final Permission ADD = add("角色添加", "role", "add", "角色管理");
		public static final String CODE_ADD = "role:add";
		/**
		 * 编辑角色
		 */
		public static final Permission EDIT = add("角色编辑", "role", "edit", "角色管理");
		public static final String CODE_EDIT = "role:edit";
		/**
		 * 删除角色
		 */
		public static final Permission DELETE = add("角色删除", "role", "del", "角色管理");
		public static final String CODE_DELETE = "role:del";

		/**
		 * 角色权限列表
		 */
		public static final Permission PERMISSION_LIST = add("角色权限列表", "role", "permission.list", "角色管理");
		public static final String CODE_PERMISSION_LIST = "role.permission:list";

		/**
		 * 编辑权限
		 */
		public static final Permission PERMISSION_EDIT = add("角色权限编辑", "role", "permission.edit", "角色管理");
		public static final String CODE_PERMISSION_EDIT = "role.permission:edit";

		/**
		 * 查看用户
		 */
		public static final Permission VIEWUSER = add("查看用户", "role", "viewuser", "角色管理");
		public static final String CODE_VIEWUSER = "role:viewuser";
	}

	/**
	 * 决策建模
	 */
	public static final class Project {
		/**
		 * 决策建模所有权限
		 */
		public static final Permission ADMIN = add("决策建模所有权限", "project", "*", "决策建模");
		public static final String CODE_ADMIN = "project:*";
		/**
		 * 添加项目
		 */
		public static final Permission PROJECT_ADD = add("添加项目", "project", "add", "决策建模");
		public static final String CODE_PROJECT_ADD = "project:add";
		/**
		 * 导入项目
		 */
		public static final Permission PROJECT_IMPORT = add("导入项目", "project", "import", "决策建模");
		public static final String CODE_PROJECT_IMPORT = "project:import";
		/**
		 * 查看项目
		 */
		public static final Permission PROJECT_VIEW = add("查看项目", "project", "view", "决策建模");
		public static final String CODE_PROJECT_VIEW = "project:view";
		/**
		 * 导出项目
		 */
		public static final Permission PROJECT_EXPORT = add("导出项目", "project", "export", "决策建模");
		public static final String CODE_PROJECT_EXPORT = "project:export";
		/**
		 * 添加资源文件
		 */
		public static final Permission ASSET_ADD = add("添加资源文件", "project.asset", "add", "决策建模");
		public static final String CODE_ASSET_ADD = "project.asset:add";
		/**
		 * 编辑资源文件
		 */
		public static final Permission ASSET_EDIT = add("编辑资源文件", "project.asset", "edit", "决策建模");
		public static final String CODE_ASSET_EDIT = "project.asset:edit";
		/**
		 * 锁定资源文件
		 */
		public static final Permission ASSET_LOCK = add("锁定资源文件", "project.asset", "lock", "决策建模");
		public static final String CODE_ASSET_LOCK = "project.asset:lock";
		/**
		 * 删除资源文件
		 */
		public static final Permission ASSET_DEL = add("删除资源文件", "project.asset", "del", "决策建模");
		public static final String CODE_ASSET_DEL = "project.asset:del";

		/**
		 * 添加知识包
		 */
		public static final Permission PKG_ADD = add("添加知识包", "project.pkg", "add", "决策建模");
		public static final String CODE_PKG_ADD = "project.pkg:add";
		/**
		 * 编辑知识包
		 */
		public static final Permission PKG_EDIT = add("编辑知识包", "project.pkg", "edit", "决策建模");
		public static final String CODE_PKG_EDIT = "project.pkg:edit";
		/**
		 * 删除知识包
		 */
		public static final Permission PKG_DEL = add("删除知识包", "project.pkg", "del", "决策建模");
		public static final String CODE_PKG_DEL = "project.pkg:del";
		/**
		 * 仿真测试
		 */
		public static final Permission PKG_RUN = add("仿真测试", "project.pkg", "run", "决策建模");
		public static final String CODE_PKG_RUN = "project.pkg:run";
		/**
		 * 查看基线
		 */
		public static final Permission BASELINE_VIEW = add("查看基线", "project.pkg.baseline", "view", "决策建模");
		public static final String CODE_BASELINE_VIEW = "project.pkg.baseline:view";
		/**
		 * 新增基线
		 */
		public static final Permission BASELINE_ADD = add("新增基线", "project.pkg.baseline", "add", "决策建模");
		public static final String CODE_BASELINE_ADD = "project.pkg.baseline:add";
		/**
		 * 下载基线
		 */
		public static final Permission BASELINE_DOWNLOAD = add("下载基线", "project.pkg.baseline", "download", "决策建模");
		public static final String CODE_BASELINE_DOWNLOAD = "project.pkg.baseline:download";
		/**
		 * 发布基线
		 */
		public static final Permission BASELINE_PUBLISH = add("发布基线", "project.pkg.baseline", "publish", "决策建模");
		public static final String CODE_BASELINE_PUBLISH = "project.pkg.baseline:publish";
		/**
		 * 审核基线
		 */
		public static final Permission BASELINE_AUDIT = add("审核基线", "project.pkg.baseline", "audit", "决策建模");
		public static final String CODE_BASELINE_AUDIT = "project.pkg.baseline:audit";
		/**
		 * 废除基线
		 */
		public static final Permission BASELINE_DEL = add("废除基线", "project.pkg.baseline", "del", "决策建模");
		public static final String CODE_BASELINE_DEL = "project.pkg.baseline:del";
		/**
		 * 查看函数库
		 */
		public static final Permission FUNCTION_VIEW = add("查看函数库", "function", "view", "决策建模");
		public static final String CODE_FUNCTION_VIEW = "function:view";
		/**
		 * 添加函数
		 */
		public static final Permission FUNCTION_ADD = add("添加函数", "function", "add", "决策建模");
		public static final String CODE_FUNCTION_ADD = "function:add";
		/**
		 * 编辑函数
		 */
		public static final Permission FUNCTION_EDIT = add("编辑函数", "function", "edit", "决策建模");
		public static final String CODE_FUNCTION_EDIT = "function:edit";
		/**
		 * 删除函数
		 */
		public static final Permission FUNCTION_DELETE = add("删除函数", "function", "delete", "决策建模");
		public static final String CODE_FUNCTION_DELETE = "function:delete";
		/**
		 * 查看外部接口
		 */
		public static final Permission THIRDAPI_VIEW = add("查看外部接口", "thirdapi", "view", "决策建模");
		public static final String CODE_THIRDAPI_VIEW = "thirdapi:view";
		/**
		 * 添加外部接口
		 */
		public static final Permission THIRDAPI_ADD = add("添加外部接口", "thirdapi", "add", "决策建模");
		public static final String CODE_THIRDAPI_ADD = "thirdapi:add";
		/**
		 * 编辑外部接口
		 */
		public static final Permission THIRDAPI_EDIT = add("编辑外部接口", "thirdapi", "edit", "决策建模");
		public static final String CODE_THIRDAPI_EDIT = "thirdapi:edit";
		/**
		 * 删除外部接口
		 */
		public static final Permission THIRDAPI_DELETE = add("删除外部接口", "thirdapi", "delete", "决策建模");
		public static final String CODE_THIRDAPI_DELETE = "thirdapi:delete";
	}

	/**
	 * 决策服务
	 */
	public static final class Micro {
		/**
		 * 决策服务所有权限
		 */
		public static final Permission ADMIN = add("决策服务所有权限", "microservice", "*", "决策服务");
		public static final String CODE_ADMIN = "microservice:*";
		/**
		 * 查看服务
		 */
		public static final Permission MICRO_VIEW = add("查看服务", "microservice", "view", "决策服务");
		public static final String CODE_MICRO_VIEW = "microservice:view";
		/**
		 * 查询轨迹
		 */
		public static final Permission MICRO_TRACE = add("查询轨迹", "microservice", "trace", "决策服务");
		public static final String CODE_MICRO_TRACE = "microservice:trace";
		/**
		 * 导入基线
		 */
		public static final Permission MICRO_IMPORTBASELINE = add("导入基线", "microservice", "importbaseline", "决策服务");
		public static final String CODE_MICRO_IMPORTBASELINE = "microservice:importbaseline";
		/**
		 * 生效上线
		 */
		public static final Permission MICRO_ONLINE = add("生效上线", "microservice", "online", "决策服务");
		public static final String CODE_MICRO_ONLINE = "microservice:online";
		/**
		 * 编辑服务
		 */
		public static final Permission MICRO_EDIT = add("编辑服务", "microservice", "edit", "决策服务");
		public static final String CODE_MICRO_EDIT = "microservice:edit";
		/**
		 * 查看调用文档
		 */
		public static final Permission MICRO_APIDOC = add("查看调用文档", "microservice", "apidoc", "决策服务");
		public static final String CODE_MICRO_APIDOC = "microservice:apidoc";
		/**
		 * 查看上线节点
		 */
		public static final Permission MICRO_CLIENT = add("查看上线节点", "microservice", "client", "决策服务");
		public static final String CODE_MICRO_CLIENT = "microservice:client";
		/**
		 * 停用服务
		 */
		public static final Permission MICRO_SHUTDOWN = add("停用服务", "microservice", "shutdown", "决策服务");
		public static final String CODE_MICRO_SHUTDOWN = "microservice:shutdown";

		/**
		 * 查看AI模型
		 */
		public static final Permission AIMODEL_VIEW = add("查看AI模型", "aimodel", "view", "决策服务");
		public static final String CODE_AIMODEL_VIEW = "aimodel:view";
		/**
		 * 添加AI模型
		 */
		public static final Permission AIMODEL_ADD = add("添加AI模型", "aimodel", "add", "决策服务");
		public static final String CODE_AIMODEL_ADD = "aimodel:add";
		/**
		 * 编辑AI模型
		 */
		public static final Permission AIMODEL_EDIT = add("编辑AI模型", "aimodel", "edit", "决策服务");
		public static final String CODE_AIMODEL_EDIT = "aimodel:edit";
		/**
		 * 删除AI模型
		 */
		public static final Permission AIMODEL_DEL = add("删除AI模型", "aimodel", "del", "决策服务");
		public static final String CODE_AIMODEL_DEL = "aimodel:del";
		/**
		 * 下载AI模型
		 */
		public static final Permission AIMODEL_DOWNLOAD = add("下载AI模型", "aimodel", "download", "决策服务");
		public static final String CODE_AIMODEL_DOWNLOAD = "aimodel:download";

		/**
		 * 查看集群节点
		 */
		public static final Permission CLIENT_VIEW = add("查看集群节点", "client", "view", "决策服务");
		public static final String CODE_CLIENT_VIEW = "client:view";
		/**
		 * 编辑集群节点
		 */
		public static final Permission CLIENT_EDIT = add("编辑集群节点", "client", "edit", "决策服务");
		public static final String CODE_CLIENT_EDIT = "client:edit";
		/**
		 * 删除集群节点
		 */
		public static final Permission CLIENT_DEL = add("删除集群节点", "client", "del", "决策服务");
		public static final String CODE_CLIENT_DEL = "client:del";

		/**
		 * 查看报表
		 */
		public static final Permission REPORT_VIEW = add("查看报表", "report", "view", "决策服务");
		public static final String CODE_REPORT_VIEW = "report:view";
		/**
		 * 导出报表
		 */
		public static final Permission REPORT_EXPORT = add("查看报表", "report", "export", "决策服务");
		public static final String CODE_REPORT_EXPORT = "report:export";

		/**
		 * 查看令牌
		 */
		public static final Permission TOKEN_VIEW = add("查看令牌", "token", "view", "决策服务");
		public static final String CODE_TOKEN_VIEW = "token:view";
		/**
		 * 添加令牌
		 */
		public static final Permission TOKEN_ADD = add("添加令牌", "token", "add", "决策服务");
		public static final String CODE_TOKEN_ADD = "token:add";
		/**
		 * 编辑令牌
		 */
		public static final Permission TOKEN_EDIT = add("编辑令牌", "token", "edit", "决策服务");
		public static final String CODE_TOKEN_EDIT = "token:edit";
		/**
		 * 删除令牌
		 */
		public static final Permission TOKEN_DEL = add("删除令牌", "token", "del", "决策服务");
		public static final String CODE_TOKEN_DEL = "token:del";
	}

	/**
	 * 登录信息
	 */
	public static final class Login {
		/**
		 * 登录信息所有权限
		 */
		public static final Permission ADMIN = add("登录信息所有权限", "login", "*", "登录信息");
		public static final String CODE_ADMIN = "login:*";

		/**
		 * 查看登录信息
		 */
		public static final Permission VIEW = add("查看登录信息", "login", "view", "登录信息");
		public static final String CODE_VIEW = "login:view";

		/**
		 * 强制下线
		 */
		public static final Permission OFFLINE = add("强制下线", "login", "offline", "登录信息");
		public static final String CODE_OFFLINE = "login:offline";
	}

	/**
	 * 操作日志
	 */
	public static final class Log {

		public static final Permission VIEW = add("查看日志", "log", "view", "操作日志");
		public static final String CODE_VIEW = "log:view";
	}

	/**
	 * 菜单
	 */
	public static final class Menu {
		/**
		 * 菜单所有权限
		 */
		public static final Permission ADMIN = add("菜单所有权限", "menu", "*", "菜单");
		public static final String CODE_ADMIN = "menu:*";
		/**
		 * 决策建模
		 */
		public static final Permission MENU_VIEW_DECISION = add("决策建模", "menu", "view", "decision", "菜单");
		public static final String CODE_MENU_VIEW_DECISION = "menu:view:decision";
		/**
		 * 决策服务
		 */
		public static final Permission MENU_VIEW_MICROSERVICE = add("决策服务", "menu", "view", "microservice", "菜单");
		public static final String CODE_MENU_VIEW_MICROSERVICE = "menu:view:microservice";
		/**
		 * 系统管理
		 */
		public static final Permission MENU_VIEW_SYSTEMADMIN = add("系统管理", "menu", "view", "systemadmin", "菜单");
		public static final String CODE_MENU_VIEW_SYSTEMADMIN = "menu:view:systemadmin";
	}

	/**
	 * 添加权限
	 */
	public static final Permission add(String name, String resource, String action, String module) {
		Permission permission = new Permission(name, resource, action, "", module);
		CONTEXT.add(permission);
		return permission;
	}

	public static final Permission add(String name, String resource, String action, String identity, String module) {
		Permission permission = new Permission(name, resource, action, identity, module);
		CONTEXT.add(permission);
		return permission;
	}
}
