package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.Org;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: yangyongqiang 2020/06/01
 */
@Mapper
public interface OrgMapper {
    /**
     * by yangyongqiang
     * 2020/6/1
     */
    int insert(Org org);

    /**
     * @author liulu
     * 2018/1/5 18:21
     */
    List<Org> selectList(@Param("start") Integer start, @Param("end") Integer end, @Param("org") Org org);

    /**
     * @author yangyongqiang
     * 2020/6/2
     */
    List<Org> selectAll();

    /**
     * @author liulu
     * 2018/1/5 19:26
     */
    int delete(String uuid);

    /**
     * 查询机构下的用户数量
     *
     * @param uuid
     * @return
     */
    int countUserByOrgUuid(String uuid);

    /**
     * by yangyongqiang
     * 2020/6/1
     */
    Org selectByUuid(String uuid);

    /**
     * by yangyongqiang
     * 2020/6/1
     */
    int update(Org org);

    /**
    * 描述: 查询条数
    * @param: [org]
    * @author: 周庚新
    * @date: 2020/6/1
    * @return: int
    *
    */
    int countTotal(Org org);

    /**
     * 根据关键词查询TopN机构
     *
     * @param name
     * @return
     */
    List<Org> selectTopNByName(@Param("name") String name, @Param("limit") int limit);
}