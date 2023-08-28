package com.beagledata.gaea.workbench.listener;

import com.beagledata.gaea.workbench.entity.Role;
import com.beagledata.gaea.workbench.entity.RoleSet;
import com.beagledata.gaea.workbench.entity.User;
import com.beagledata.gaea.workbench.mapper.PermissionMapper;
import com.beagledata.gaea.workbench.mapper.UserMapper;
import com.beagledata.security.util.PasswordUtils;
import com.beagledata.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import com.beagledata.gaea.workbench.service.ProjectService;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by liulu on 2020/11/11.
 */
public class InitRoleListener implements SpringApplicationRunListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private PermissionMapper permissionMapper;
    private UserMapper userMapper;

    public InitRoleListener(SpringApplication application, String[] args) {
    }

    @Override
    public void starting() {
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment configurableEnvironment) {
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext configurableApplicationContext) {
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext configurableApplicationContext) {
    }

    @Override
    public void finished(ConfigurableApplicationContext configurableApplicationContext, Throwable throwable) {
        permissionMapper = configurableApplicationContext.getBean(PermissionMapper.class);
        userMapper = configurableApplicationContext.getBean(UserMapper.class);

        initRole(RoleSet.SystemAdmin);
        initRole(RoleSet.OrgAdmin);
        initRole(RoleSet.ModelingUser);
        initAdminUser();

//        logger.info("开始初始化数据质量工具关联项目");
//        ProjectService projectService = configurableApplicationContext.getBean(ProjectService.class);
//        if (projectService.initDqcProject()) {
//            logger.info("数据质量工具关联项目初始化完成");
//        } else {
//            logger.warn("数据质量工具关联项目初始化失败,请检查日志,或手动创建项目,并配置到数据质量工具项目中");
//        }
    }

    private void initRole(Role role) {
        try {
            Integer roleId = permissionMapper.selectSystemRoleIdByCode(role.getCode());
            if (roleId == null) {
                permissionMapper.insertRoleAndPermissions(role);
                logger.info("初始化角色及关联权限成功: {}", role.getName());
            }
        } catch (Exception e) {
            logger.error("初始化角色及关联权限失败: {}", role.getName(), e);
            throw new IllegalStateException("初始化角色及关联权限失败");
        }
    }

    private void initAdminUser() {
        try {
            User user = userMapper.selectBaseByUsername("admin");
            if (user == null) {
                Integer roleId = permissionMapper.selectSystemRoleIdByCode("SystemAdmin");
                if (roleId != null) {
                    user = new User();
                    user.setUsername("admin");
                    user.setRealname("管理员");
                    String[] pwd = PasswordUtils.sign("admin");
                    user.setPassword(pwd[0]);
                    user.setSalt(pwd[1]);
                    user.setUuid(IdUtils.UUID());
                    userMapper.insert(user);

                    userMapper.insertUserRoles(user.getUuid(), Stream.of(roleId).collect(Collectors.toSet()));
                    logger.info("初始化管理员用户admin成功");
                }
            }
        } catch (Exception e) {
            logger.error("初始化管理员用户admin失败", e);
            throw new IllegalStateException("初始化管理员用户admin失败");
        }
    }
}
