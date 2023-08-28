package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.Permission;
import com.beagledata.gaea.workbench.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * Created by liulu on 2018/1/9.
 */
@Mapper
public interface PermissionMapper {

    /**
     * 查询角色权限列表
     * @author yinrj
     * 2019/9/16 16:20
     */
    List<Permission> selectByRoleId(@Param("roleId") int roleId, @Param("start") Integer start, @Param("limit") Integer limit);

    /**
     * 初始化系统管理员、机构管理员、建模人员角色
     *
     * @param role
     * @return
     */
    int insertRoleAndPermissions(Role role);

    /**
     * 根据角色code查询角色关联的权限
     *
     * @param roleCode
     * @return
     */
    Set<Permission> selectAllByRoleCode(@Param("roleCode") String roleCode);

    int deleteRoleAndPermissions(@Param("set") Set<Permission> set, @Param("roleCode") String roleCode);

    Integer selectSystemRoleIdByCode(@Param("code") String code);
}
