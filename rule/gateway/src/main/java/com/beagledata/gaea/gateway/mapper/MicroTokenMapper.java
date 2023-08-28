package com.beagledata.gaea.gateway.mapper;

import com.beagledata.gaea.gateway.entity.MicroToken;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created by liulu on 2020/6/29.
 */
@Mapper
public interface MicroTokenMapper {
    /**
     * @author liulu
     * 2020/6/29 20:48
     */
    List<MicroToken> selectAll();
}
