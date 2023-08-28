package com.beagledata.gaea.workbench.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 内置角色
 *
 * Created by liulu on 2020/11/11.
 */
public class RoleSet {
    public static final Role SystemAdmin = new Role("SystemAdmin", "系统管理员", new HashSet<>(
            Arrays.asList(new Permission("所有权限", "*", "", "", ""))
    ));

    public static final Role OrgAdmin = new Role("OrgAdmin", "机构管理员", new HashSet<>(Arrays.asList(
            PermissionSet.Project.PROJECT_ADD,
            PermissionSet.Project.PROJECT_IMPORT,
            PermissionSet.Project.PROJECT_VIEW,
            PermissionSet.Project.PROJECT_EXPORT,
            PermissionSet.Project.ASSET_ADD,
            PermissionSet.Project.ASSET_EDIT,
            PermissionSet.Project.ASSET_LOCK,
            PermissionSet.Project.ASSET_DEL,
            PermissionSet.Project.PKG_ADD,
            PermissionSet.Project.PKG_EDIT,
            PermissionSet.Project.PKG_DEL,
            PermissionSet.Project.PKG_RUN,
            PermissionSet.Project.BASELINE_VIEW,
            PermissionSet.Project.BASELINE_ADD,
            PermissionSet.Project.BASELINE_AUDIT,
            PermissionSet.Project.BASELINE_DEL,
            PermissionSet.Project.BASELINE_DOWNLOAD,
            PermissionSet.Project.BASELINE_PUBLISH,
            PermissionSet.Project.FUNCTION_VIEW,
            PermissionSet.Project.FUNCTION_ADD,
            PermissionSet.Project.FUNCTION_EDIT,
            PermissionSet.Project.FUNCTION_DELETE,
            PermissionSet.Project.THIRDAPI_VIEW,
            PermissionSet.Project.THIRDAPI_ADD,
            PermissionSet.Project.THIRDAPI_EDIT,
            PermissionSet.Project.THIRDAPI_DELETE,
            PermissionSet.Micro.AIMODEL_VIEW,
            PermissionSet.Micro.AIMODEL_ADD,
            PermissionSet.Micro.AIMODEL_EDIT,
            PermissionSet.Micro.AIMODEL_DEL,
            PermissionSet.Micro.AIMODEL_DOWNLOAD,
            PermissionSet.Micro.MICRO_VIEW,
            PermissionSet.Micro.MICRO_TRACE,
            PermissionSet.Micro.MICRO_EDIT,
            PermissionSet.Micro.REPORT_VIEW,
            PermissionSet.Micro.REPORT_EXPORT,
            PermissionSet.Menu.MENU_VIEW_DECISION,
            PermissionSet.Menu.MENU_VIEW_MICROSERVICE,
            PermissionSet.Menu.MENU_VIEW_SYSTEMADMIN,
            PermissionSet.Log.VIEW,
            PermissionSet.Login.VIEW,
            PermissionSet.Login.OFFLINE
    )));

    public static final Role ModelingUser = new Role("ModelingUser", "建模人员", new HashSet<>(Arrays.asList(
            PermissionSet.Project.PROJECT_ADD,
            PermissionSet.Project.PROJECT_IMPORT,
            PermissionSet.Project.PROJECT_VIEW,
            PermissionSet.Project.PROJECT_EXPORT,
            PermissionSet.Project.ASSET_ADD,
            PermissionSet.Project.ASSET_EDIT,
            PermissionSet.Project.ASSET_LOCK,
            PermissionSet.Project.ASSET_DEL,
            PermissionSet.Project.PKG_ADD,
            PermissionSet.Project.PKG_EDIT,
            PermissionSet.Project.PKG_DEL,
            PermissionSet.Project.PKG_RUN,
            PermissionSet.Project.BASELINE_VIEW,
            PermissionSet.Project.BASELINE_ADD,
            PermissionSet.Project.BASELINE_DEL,
            PermissionSet.Project.BASELINE_DOWNLOAD,
            PermissionSet.Project.BASELINE_PUBLISH,
            PermissionSet.Project.FUNCTION_VIEW,
            PermissionSet.Project.FUNCTION_ADD,
            PermissionSet.Project.FUNCTION_EDIT,
            PermissionSet.Project.FUNCTION_DELETE,
            PermissionSet.Project.THIRDAPI_VIEW,
            PermissionSet.Project.THIRDAPI_ADD,
            PermissionSet.Project.THIRDAPI_EDIT,
            PermissionSet.Project.THIRDAPI_DELETE,
            PermissionSet.Micro.AIMODEL_VIEW,
            PermissionSet.Micro.AIMODEL_ADD,
            PermissionSet.Micro.AIMODEL_EDIT,
            PermissionSet.Micro.AIMODEL_DEL,
            PermissionSet.Micro.AIMODEL_DOWNLOAD,
            PermissionSet.Menu.MENU_VIEW_DECISION
    )));

    public static final Set<Role> CONTEXT = new HashSet<>(Arrays.asList(
       SystemAdmin, OrgAdmin, ModelingUser
    ));
}
