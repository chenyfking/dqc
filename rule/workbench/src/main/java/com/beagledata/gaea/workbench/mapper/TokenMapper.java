package com.beagledata.gaea.workbench.mapper;


import com.beagledata.gaea.workbench.entity.Token;
import com.beagledata.gaea.workbench.vo.MicroTokenVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 描述: 令牌mapper
* @author: 周庚新
* @date: 2020/6/11
*/
@Mapper
public interface TokenMapper {
    /**
     * by yangyongqiang
     * 2020/06/11
     * 删除令牌
     */
    int delete(String uuid);

    /**
     * by yangyongqiang
     * 2020/06/11
     * 删除令牌服务关联
     */
    void deleteMicroToken(String token);

	/**
	 * 描述: 新增令牌
	 * @param: [record]
	 * @author: 周庚新
	 * @date: 2020/6/11
	 * @return: int
	 */
	void insert(Token record);
	
    /**
     * by yangyongqiang
     * 2020/06/11
     * 查询令牌
     */
    List<Token> selectList(@Param("start") Integer start, @Param("limit") Integer limit, @Param("token") Token token);

	/**
	 * 根据名称查询token
	 *
	 * @param name
	 * @return
	 */
    Token selectByName(String name);

	/**
	 * @param token
	 * @return
	 */
	int update(Token token);

    /**
     * by yangyongqiang
     * 2020/06/11
     * 查询令牌条数
     */
    int countTotal(Token token);

    /**
    * 描述: 批量新增 令牌关联 服务
    * @param: [microTokens]
    * @author: 周庚新
    * @date: 2020/6/11 
    * @return: void
    */
	void insertBatchMicroToken(List<MicroTokenVO> microTokenVOS);

	/**
	 * @author liulu
	 * 2020/11/18 10:07
	 */
	Token selectByUuid(String uuid);
}