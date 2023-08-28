package com.beagledata.gaea.workbench.service;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.Project;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by liulu on 2017/12/29.
 */
public interface ProjectService {
    /**
    *@Author:mahongfei
    *@description:添加项目
    */
    Map<String,String> addProject(Project project);

    /**
    *@Author:mahongfei
    *@description:删除项目
    */
    Result deleteProject(String uuid);

    /**
    *@Author:mahongfei
    *@description:编辑项目
    */
    void editProject(Project project);

    /**
    *@Author:mahongfei
     * sortfield 排序字段(更新时间: updateTime, 创建时间: createTime),非必填
    *sortDirection	升序：asc; 降序：desc
    *@description:项目列表
    */
    Result listPage(int page, int pageNum, String name, String sortfield, String sortDirection);

    /**
    *@Author:mahongfei
    *@description:项目详情
    */
    Project projectDetails(String uuid);

    /**
     * 导入项目zip文件
     *
     * @author liulu
     * 2020/4/27 18:24
     */
    Result importProject(byte[] bytes);

    /**
     * 导出项目
     *
     * @author liulu
     * 2020/4/27 18:19
     */
    void export(String uuid, HttpServletResponse response);

    /**
	 *根据用户查询项目
	 * @author: yinrj
	 * @date: 2021/1/26
	 * @return
	 */
    Result listProjectsByUser(Integer page, Integer pageNum, String userUuid, String projectName, String sortField, String sortDirection);

    /**
     * 初始化数据质量关联项目
     * dataquality
     */
    boolean initDqcProject();
}

