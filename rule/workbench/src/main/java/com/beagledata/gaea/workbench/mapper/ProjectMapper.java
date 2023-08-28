package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * Created by mahongfei on 2018/9/18.
 */
@Mapper
public interface ProjectMapper {
    /**
     *@Author: mahongfei
     *@description: 添加项目
     */
    int insert(Project project);

    /**
     * @Author: mahongfei
     * @description: 删除项目
     */
    int delete(Project project);

     /**
      *@Author: mahongfei
      *@description: 编辑项目
      */
    int update(Project project);

     /**
      *@Author: mahongfei
      *@description: 项目列表
      */
    List<Project> selectPage(@Param("start") Integer start, @Param("end") Integer end,
                             @Param("name") String name,
                             @Param("userUuid") String userUuid,
                             @Param("isAdmin") boolean isAdmin,
                             @Param("sortField") String sortField,
                             @Param("sortDirection") String sortDirection,
                             @Param("isOrg") boolean isOrg);

    /**
     *@Author: mahongfei
     *@description: 统计个数
     */
    int count(
            @Param("name") String name,
            @Param("userUuid") String userUuid,
            @Param("isAdmin") boolean isAdmin,
            @Param("isOrg") boolean isOrg
    );

    /**
     *@Author: mahongfei
     *@description: 项目详情
     */
    Project selectByUuid(String uuid);

    Project selectByUuidForUser(
            @Param("uuid") String uuid,
            @Param("userUuid") String userUuid,
            @Param("isAdmin") boolean isAdmin,
            @Param("isOrg") boolean isOrg
    );

    /**
     * 统计项目生效服务格式
     *
     * @param uuid
     * @return
     */
    int countEffectiveService(String uuid);

    /**
     * 删除项目关联的服务
     *
     * @param uuid
     * @return
     */
    int deleteProjectService(String uuid);

    /**
     * 根据uuid查询，不管是否被删除
     *
     * @param project
     * @return
     */
    Project selectOnUploadBaseline(Project project);

    /**
     * 导入基线更新名称和删除状态
     *
     * @param project
     */
    void updateOnUploadBaseline(Project project);

    /**
	 * 根据用户编号查询项目列表
     * @author yinrj
     * 2021/1/26
     */
    List<Project> selectByUserUuid(@Param("start") Integer start, @Param("end") Integer end, @Param("userUuid") String userUuid, @Param("projectName") String projectName,
                                   @Param("sortField") String sortField, @Param("sortDirection") String sortDirection);

    int selectCountByUserUuid(@Param("userUuid") String userUuid, @Param("projectName") String projectName);

    Project selectByProjectName(@Param("projectName")String projectName);
}
