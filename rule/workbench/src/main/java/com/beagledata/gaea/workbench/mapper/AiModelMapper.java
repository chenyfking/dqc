package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.AiModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * Created by liulu on 2018/1/17.
 */
@Mapper
public interface AiModelMapper {
    /**
     * @author liulu
     * 2018/1/17 11:39
     */
    int insert(AiModel aiModel);

    /**
     * @author liulu
     * 2018/1/17 13:54
     */
    int update(AiModel aiModel);
    
    /**
     * @author liulu
     * 2018/1/17 13:56
     */
    int updateEnable(AiModel aiModel);

    /**
     * @author liulu
     * 2018/1/17 14:12
     */
    int delete(String uuid);

    /**
     * @author liulu
     * 2018/1/17 14:34
     */
    List<AiModel> selectAll(@Param(value = "start")Integer start,
                            @Param(value = "end")Integer end,
                            @Param(value = "isAdmin") boolean isAdmin,
                            @Param(value ="aiModel") AiModel aiModel,
                            @Param("sortField")String sortField,
                            @Param("sortDirection")String sortDirection);

    /**
     * 统计分页的total
     * @author chenyafeng
     * @date 2018/12/6
     */
    Integer countTotal(@Param(value = "start") Integer start,
                       @Param(value = "end") Integer end,
                       @Param(value = "isAdmin") boolean isAdmin,
                       @Param(value = "aiModel") AiModel aiModel);

    /**
     * 根据uuid查询model_name
     */
    AiModel selectByUuid(@Param(value = "uuid") String uuid);

    /**
     * 根据uuid查询jar_name
     */
    String selectJarNameByUuid(@Param(value = "uuid") String uuid);

    /**
     * @author liulu
     * 2018/6/25 下午 05:07
     */
    AiModel selectByModelName(String modelName);

    AiModel selectModelByUuid(String uuid);

    /**
     * 根据model_name 或 jar_name 统计
     * @author chenyafeng
     * @date 2018/9/29
     */
    List<AiModel> selectByModelNameOrJarName(@Param("modelName")String modelName, @Param("jarName")String jarName);

    /**
     * 根据ai模型名称批量查询
     * @author chenyafeng
     * @date 2018/12/20
     */
    List<AiModel> selectByModelNames(@Param("list") Collection<String> list);

    /**
     * 查询服务关联的AI模型列表
     *
     * @param microUuid
     * @return
     */
    List<AiModel> selectByMicro(String microUuid);

    /**
     * 描述: 导入基线查询是否存在
     * @param: [aiModel]
     * @author: 周庚新
     * @date: 2020/11/11
     * @return: com.beagledata.gaea.workbench.entity.AiModel
     */
    AiModel selectOnUploadBaseline(AiModel aiModel);

    /**
     * 描述: 导入基线，更新
     * @param: [aiModel]
     * @author: 周庚新
     * @date: 2020/11/11
     * @return: void
     */
    void updateOnUploadBaseline(AiModel aiModel);
}
