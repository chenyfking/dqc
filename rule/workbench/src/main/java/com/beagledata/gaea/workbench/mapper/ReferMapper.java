package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.AiModel;
import com.beagledata.gaea.workbench.entity.FunctionDefinition;
import com.beagledata.gaea.workbench.entity.Refer;
import com.beagledata.gaea.workbench.vo.AssetsReferenceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by liulu on 2020/11/11.
 */
@Mapper
public interface ReferMapper {
    /**
     * 添加引用
     *
     * @param refer
     * @return
     */
    int insert(Refer refer);

    /**
     * 删除引用
     *
     * @param refer
     * @return
     */
    int delete(Refer refer);

    /**
     * 批量添加引用
     *
     * @param refers
     * @return
     */
    int insertBatch(Collection<Refer> refers);

    /**
     * 删除回收站时删除引用关系
     *
     * @param projectUuid
     * @param assetsUuids
     * @param tplUuids
     * @return
     */
    int deleteFromRecycle(
            @Param("projectUuid") String projectUuid,
            @Param("assetsUuids") Collection<String> assetsUuids,
            @Param("tplUuids") Collection<String> tplUuids
    );

    /**
     * 统计资源被引用的个数（测试案例、决策流、知识包）
     *
     * @param subjectUuid
     * @return
     */
    int countBySubjectUuid(String subjectUuid);

    /**
     * 根据uuid和版本号统计资源被引用的个数（测试案例、决策流、知识包）
     *
     * @param subjectUuid
     * @param subjectVersion
     * @return
     */
    int countBySubjectUuidAndSubjectVersion(
            @Param("subjectUuid") String subjectUuid,
            @Param("subjectVersion") Integer subjectVersion
    );

    /**
     * 统计资源被引用的个数（测试案例、决策流、知识包）
     *
     * @param subjectUuid
     * @param subjectChild
     * @return
     */
    int countBySubjectUuidAndChild(
            @Param("subjectUuid") String subjectUuid,
            @Param("subjectChild") String subjectChild
    );

    /**
     * 查询引用结果
     *
     * @param subjectUuid
     * @param subjectVersion
     * @return
     */
    List<AssetsReferenceVO> selectReferBySubjectUuidAndVersion(
            @Param("subjectUuid") String subjectUuid,
            @Param("subjectVersion") Integer subjectVersion
    );

    /**
     * 根据引用uuid查询被引用的资源uuid
     *
     * @param referUuids
     * @return
     */
    List<String> selectSubjectUuidByReferUuid(Collection<String> referUuids);

    Set<AiModel> selectAiModelByReferUuid(Set<String> uuids);

    List<FunctionDefinition> selectFunctionByReferUuids(Set<String> uuids);
}
