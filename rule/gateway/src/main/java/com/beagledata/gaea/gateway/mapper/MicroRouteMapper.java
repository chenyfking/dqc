package com.beagledata.gaea.gateway.mapper;

import com.beagledata.gaea.gateway.entity.MicroRoute;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created by liulu on 2020/6/29.
 */
@Mapper
public interface MicroRouteMapper {
    /**
     * @author liulu
     * 2020/6/29 18:10
     */
    List<MicroRoute> selectAll();
    /**
    * 描述: 查询所有可用节点的baseurl
    * @param: []
    * @author: 周庚新
    * @date: 2020/11/6 
    * @return: java.util.List<com.beagledata.gaea.gateway.entity.MicroRoute>
    */
    List<MicroRoute> selectAllBaseUrl();
    
}
