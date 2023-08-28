package com.beagledata.gaea.workbench.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Auther: zgx
 * @Date: 0010 2020/6/19 11:11
 * @Description: 规则与测试用例关联
 */
@Mapper
public interface TestCaseMapper {

    /**
    * 描述: 插入一条新的规则与测试用例关联记录
    * @param: [ruleUuid, caseUuid, assetsVersion]
    * @author: 周庚新
    * @date: 2020/6/19
    * @return: int
    */
    int insert(@Param("ruleUuid") String ruleUuid, @Param("caseUuid") String caseUuid, @Param("assetsVersion") int assetsVersion);

    /**
     * @Author yangyongqiang
     * @Description 删除测试案例
     * @Date 10:01 上午 2020/7/1
     * @Param [caseUuid]
     * @return int
     **/
    int delete(String caseUuid);
}
