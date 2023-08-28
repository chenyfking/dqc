package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.ThirdApiDefinition;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created by liulu on 2020/6/14.
 */
@Mapper
public interface ThirdApiDefinitionMapper {
    /**
     * @param definition
     * @return
     */
    int insert(ThirdApiDefinition definition);

    /**
     * @return
     */
    List<ThirdApiDefinition> selectAll();

    /**
     * @param definition
     * @return
     */
    int update(ThirdApiDefinition definition);

    /**
     * @param uuid
     * @return
     */
    int delete(String uuid);

    /**
     * @return
     */
    List<ThirdApiDefinition> selectForModeling();

    /**
     * @param uuid
     * @return
     */
    ThirdApiDefinition selectByUuid(String uuid);

    /**
    * 描述: 根据名称获取uuid
    * @param: [name]
    * @author: 周庚新
    * @date: 2020/7/21
    * @return: java.lang.String
    *
    */
    ThirdApiDefinition selectUuidByName(String name);

    ThirdApiDefinition selectOnUploadBaseline(ThirdApiDefinition apiDefinition);

    int updateOnUploadBaseline(ThirdApiDefinition apiDefinition);
}
