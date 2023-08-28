package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.Role;
import com.beagledata.gaea.workbench.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author: yangyongqiang 2019/09/17
 */
@Mapper
public interface UserMapper {
	 /**
     * @author liulu
     * 2017/12/13 17:24
     */
    User selectByUsername(String username);

    /**
     * @author liulu
     * 2017/12/13 17:24
     */
    List<User> selectAll();

    /**
     * @author liulu
     * 2017/12/19 11:02
     */
    int insert(User user);

    /**
     * @author liulu
     * 2018/1/5 18:21
     */
    List<User> selectList(
            @Param("start") Integer start,
            @Param("end") Integer end,
            @Param("user") User user,
            @Param("isAdmin") boolean isAdmin,
            @Param("isOrg") boolean isOrg,
            @Param("userUuid") String userUuid,
            @Param("superAdminUsername") String superAdminUsername
    );

    /**
     * @author liulu
     * 2018/1/5 19:26
     */
    int delete(String uuid);

    /**
     * @author liulu
     * 2018/1/8 17:39
     */
    User selectByUuid(String uuid);

    /**
     *查询带机构信息的用户信息
     * @author yinrj
     * @date 2020/7/22
     */
    User selectOrgByUuid(String uuid);

    /**
     * @author liulu
     * 2018/1/8 17:51
     */
    int updateEnable(User user);

    /**
     * @author liulu
     * 2018/1/9 11:10
     */
    int update(User user);

    /**
     * 编辑用户调用
     *
     * @param user
     * @return
     */
    int updateForEdit(User user);

    /**
      *@auto: yangyongqiang
      *@Description: 查询条数
      *@Date: 2019-02-26 10:36
      **/
    int selectCount(
            @Param("user") User user,
            @Param("isAdmin") boolean isAdmin,
            @Param("isOrg") boolean isOrg,
            @Param("superAdminUsername") String superAdminUsername
    );

    /**
     * 修改用户信息
     * @author yangyongqiang
     * 2019/9/17
     */
	int updateByPrimaryKeySelective(User record);

	/**
	 * 根据参数查询用户角色列表
     * @author yangyongqiang
     * 2019/9/17
     */
	List<Role> selectUserRole(String userUuid);

    /**
     * 插入用户角色关联
     *
     * @param userUuid
     * @param roleIds
     * @return
     */
	int insertUserRoles(@Param("userUuid") String userUuid, @Param("roleIds") Collection<Integer> roleIds);

    /**
     * 删除用户角色关联
     *
     * @param userUuid
     * @param roleIds
     * @return
     */
	int deleteUserRoles(@Param("userUuid") String userUuid, @Param("roleIds") Collection<Integer> roleIds);

    /**
     * 查询项目成员
     *
     * @param start
     * @param end
     * @param user
     * @param projectUuid
     * @param superAdminUsername
     * @return
     */
    List<User> selectNotProjectMember(
            @Param("start") Integer start,
            @Param("end") Integer end,
            @Param("user") User user,
            @Param("projectUuid") String projectUuid,
            @Param("superAdminUsername") String superAdminUsername
    );

    int countNotProjectMember(
            @Param("user") User user,
            @Param("projectUuid") String projectUuid,
            @Param("superAdminUsername") String superAdminUsername
    );

    List<String> selectOrgUserUuid(String uuid);

    /**
     * 查询用户基本信息
     *
     * @param username
     * @return
     */
    User selectBaseByUsername(String username);

    /**
     * 根据username统计数量
     *
     * @param username
     * @return
     */
    int countByUsername(String username);

    /**
	 * 根据机构编号查询用户列表
     * @author yinrj
     * 2021/1/21
     */
    List<User> selectByOrgUuid(@Param("start") Integer start, @Param("end") Integer end, @Param("orgUuid") String orgUuid, @Param("keywords") String keywords);

    /**
	 * 根据机构编号查询用户数量
     * @author yinrj
     * 2021/1/21
     */
    int selectCountByOrgUuid(@Param("orgUuid") String orgUuid, @Param("keywords") String keywords);

    /**
	 * 根据角色ID查询用户列表
     * @author yinrj
     * 2021/1/22
     */
    List<User> selectByRoleId(@Param("start") Integer start, @Param("end") Integer end, @Param("roleId") Integer roleId, @Param("keywords") String keywords);

    /**
	 * 根据角色ID查询用户数量
     * @author yinrj
     * 2021/1/22
     */
    int selectCountByRoleId(@Param("roleId") Integer roleId, @Param("keywords") String keywords);

    /**
     * @Author yangyongqiang
     * @Description 根据uuid获取用户详情
     * @Date 上午10:32 2021/1/25
     **/
    Map getUserByUuid(String uuid);
}