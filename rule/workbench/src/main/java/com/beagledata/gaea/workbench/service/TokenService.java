package com.beagledata.gaea.workbench.service;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.Token;

/**
* 描述: Token service
* @author: 周庚新
* @date: 2020/6/11
*/
public interface TokenService {
    /**
     * by yangyongqiang
     * 2020/06/11
     * 删除令牌
     */
    void delete(String uuid);

    /**
    * 描述: 新增令牌
    * @param: [name, microUuids]
    * @author: 周庚新
    * @date: 2020/6/11 
    * @return: void
    */
    void add(String name, String microUuids, boolean all);

    /**
     * by yangyongqiang
     * 2020/06/11
     */
    Result listPage(int page, int pageNum, Token token);

    /**
    * 描述: 修改令牌
    * @param: [uuid, name, token, microUuids]
    * @author: 周庚新
    * @date: 2020/6/11 
    * @return: void
    */
    void edit(String uuid, String name, String microUuids, boolean all);

    void refreshToken();
}
