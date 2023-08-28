package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.KnowledgePackage;
import com.beagledata.gaea.workbench.entity.KnowledgePackageBaseline;
import com.beagledata.gaea.workbench.vo.BaselineCompareChildrenVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Auther: yinrj
 * @Date: 0028 2020/6/28 16:31
 * @Description: 知识包基线
 */
@Mapper
public interface KnowledgePackageBaselineMapper {

	/**
	 * 新增知识包基线
	 * @author yinrj
	 * @date 2020/6/28
	 */
	int insert(KnowledgePackageBaseline baseline);

	/**
	 * 新增知识包基线资源
	 * @author yinrj
	 * @date 2020/6/28
	 */
	int insertBaselineAssets(@Param("packageUuid") String packageUuid, @Param("baselineVersion") int baselineVersion, @Param("array") List<KnowledgePackage> array);

	/**
	 * 查询知识包基线
	 * @author yinrj
	 * @date 2020/6/28
	 */
	KnowledgePackageBaseline selectById(int id);

	/**
	 * 查询知识包基线
	 * @author yinrj
	 * @date 2020/6/28
	 */
	KnowledgePackageBaseline selectByUuid(String uuid);

	/**
	 * @return void
	 * @Author yangyongqiang
	 * @Description 删除知识包基线
	 * @Date 9:48 上午 2020/7/1
	 * @Param [uuid]
	 **/
	void delete(String uuid);

	/**
	 * 查询知识包列表
	 * @author yinrj
	 * @date 2020/6/28
	 */
	List<KnowledgePackageBaseline> selectListByPage(@Param("packageUuid") String packageUuid, @Param("start") int start, @Param("limit") int limit);

	/**
	 * 查询知识包列表总数
	 * @author yinrj
	 * @date 2020/6/28
	 */
	int selectCountByPackage(@Param("packageUuid") String packageUuid);

	/**
	 * 更新知识包基线状态
	 * @author yinrj
	 * @date 2020/7/1
	 */
	void updateState(@Param("packageUuid") String packageUuid, @Param("baselineVersion") Integer baselineVersion, @Param("state") int state);

	/**
	 * 更新知识包基线状态
	 * @author yinrj
	 * @date 2020/7/1
	 */
	void updateStateByNewState(@Param("packageUuid") String packageUuid, @Param("newState") Integer newState, @Param("oldState") int oldState);

	/**
	 * 上线服务
	 * @author yinrj
	 * @date 2020/7/8
	 */
	void online(@Param("packageUuid") String packageUuid, @Param("baselineVersion") Integer baselineVersion);

	/**
	 * @Author yangyongqiang
	 * @Description 查询决策服务列表
	 * @Date 7:37 下午 2020/7/2
	 **/
	List<KnowledgePackageBaseline> selectListMicroBaselineByPage(@Param("packageUuid") String packageUuid, @Param("start") Integer start, @Param("limit") Integer limit);

	/**
	 * @Author yangyongqiang
	 * @Description 查询决策服务数量
	 * @Date 7:37 下午 2020/7/2
	 **/
	int selectCountMicroBaselineByPackage(@Param("packageUuid") String packageUuid);

	/**
	 * @Author yangyongqiang
	 * @Description 审核记录
	 * @Date 11:04 上午 2020/7/6
	 **/
	void insertAuditRecord(@Param("state") Integer state, @Param("auditReason") String auditReason, @Param("auditorUuid") String auditorUuid, @Param("baselineUuid") String baselineUuid);

	/**
	 * @Author yangyongqiang
	 * @Description 更改审核状态
	 * @Date 11:40 上午 2020/7/6
	 **/
	void updateAuditState(KnowledgePackageBaseline knowledgePackageBaseline);

	/**
	 * 描述: 基线版本比较
	 * @param: [packageUuid, microUuid, baselineVersion]
	 * @author: 周庚新
	 * @date: 2020/7/6
	 * @return: com.beagledata.common.Result
	 */
	List<BaselineCompareChildrenVO> selectCompare(@Param("packageUuid") String packageUuid, @Param("baselineV1") Integer baselineV1, @Param("baselineV2") Integer baselineV2);

	/**
	 * @Author yangyongqiang
	 * @Description 更具项目uuid查询知识包基线
	 * @Date 7:56 下午 2020/7/23
	 **/
	List<KnowledgePackageBaseline> selectBaselineByPackageUuid(@Param("packageUuid") String packageUuid);

	/**
	 * @Author yangyongqiang
	 * @Description 根据指定的一个状态查询基线
	 * @Date 6:37 下午 2020/7/15
	 **/
	List<KnowledgePackageBaseline> selectListMicroBaselineBystate(@Param("packageUuid") String packageUuid, @Param("state") int state);

	/**
	 * 描述: 批量插入知识包基线
	 * @param: [knowledgePackageBaselines]
	 * @author: 周庚新
	 * @date: 2020/7/24
	 * @return: void
	 */
	void insertBatch(List<KnowledgePackageBaseline> knowledgePackageBaselines);

	/**
	 * 描述: 批量插入知识包基线关联资源文件
	 * @param: [knowledgePackages]
	 * @author: 周庚新
	 * @date: 2020/7/24
	 * @return: void
	 */
	void insertBaselineAssetsBatch(List<KnowledgePackage> knowledgePackages);

	KnowledgePackageBaseline selectByPackageUuidAndBaselineVersion(@Param("packageUuid")String packageUuid,@Param("baselineVersion")Integer baselineVersion);
	/**
	 * 描述: 导入基线查询是否存在
	 * @param: [aiModel]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: com.beagledata.gaea.workbench.entity.AiModel
	 */
	KnowledgePackageBaseline selectOnUploadBaseline(KnowledgePackageBaseline baseline);

	/**
	 * 描述: 导入基线，更新
	 * @param: [aiModel]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: void
	 */
	void updateOnUploadBaseline(KnowledgePackageBaseline baseline);

	/**
	 * 根据服务uuid查询待生效、已生效基线列表
	 *
	 * @param microUuid
	 * @return
	 */
	List<KnowledgePackageBaseline> selectByMicro(String microUuid);
}
