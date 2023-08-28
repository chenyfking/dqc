package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.ProjectUser;
import com.beagledata.gaea.workbench.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by cyf on 2019/12/13.
 */
@Mapper
public interface ProjectUserMapper {
	/**
	 * 新增
	 */
	void insert(@Param("projectUser") ProjectUser projectUser);

	/**
	 * 批量新增
	 */
	void batchInsert(List<ProjectUser> list);

	/**
	 * 查询项目关联的用户
	 */
	List<User> selectUserByPrj(@Param("projectUuid") String projectUuid);


	/**
	 * 查询
	 */
	List<ProjectUser> select(@Param("projectUser") ProjectUser projectUser);

	/**
	 * 根据用户删除
	 */
	void deleteByUser(@Param("userId") int userId);

	/**
	 * 根据项目删除
	 */
	void deleteByProject(@Param("projectUuid") String projectUuid, @Param("userUuid") String userUuid);

}
