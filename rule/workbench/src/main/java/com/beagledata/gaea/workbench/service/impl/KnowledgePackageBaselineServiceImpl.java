package com.beagledata.gaea.workbench.service.impl;

import com.alibaba.fastjson.JSON;
import com.beagledata.common.Result;
import com.beagledata.gaea.common.LogManager;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.entity.KnowledgePackage;
import com.beagledata.gaea.workbench.entity.KnowledgePackageBaseline;
import com.beagledata.gaea.workbench.entity.Micro;
import com.beagledata.gaea.workbench.entity.Refer;
import com.beagledata.gaea.workbench.mapper.KnowledgePackageBaselineMapper;
import com.beagledata.gaea.workbench.mapper.KnowledgePackageMapper;
import com.beagledata.gaea.workbench.mapper.MicroMapper;
import com.beagledata.gaea.workbench.mapper.ReferMapper;
import com.beagledata.gaea.workbench.service.KnowledgePackageBaselineService;
import com.beagledata.gaea.workbench.util.UserHolder;
import com.beagledata.gaea.workbench.vo.BaselineCompareChildrenVO;
import com.beagledata.gaea.workbench.vo.BaselineCompareVO;
import com.beagledata.util.IdUtils;
import com.beagledata.util.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Auther: yinrj
 * @Date: 0028 2020/6/28 16:55
 * @Description: 知识包基线
 */
@Service
public class KnowledgePackageBaselineServiceImpl implements KnowledgePackageBaselineService {
    private static Logger logger = LogManager.getLogger(KnowledgePackageBaselineServiceImpl.class);

    @Autowired
    private KnowledgePackageBaselineMapper baselineMapper;
    @Autowired
	private KnowledgePackageMapper knowledgePackageMapper;
    @Autowired
    private MicroMapper microMapper;
    @Autowired
    private ReferMapper referMapper;

    /**
     * 新增基线
     * @auther yinrj
     * @date 2020/6/28
     * @param packageUuid
     * @param assets
     */
    @Override
	@Transactional
    public Result add(String packageUuid, String assets) {
        if (StringUtils.isBlank(packageUuid) || StringUtils.isBlank(assets)) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        List<KnowledgePackage> array = null;
        try {
             array = JSON.parseArray(assets, KnowledgePackage.class);
            if (array == null) {
                throw new IllegalArgumentException("参数不能为空json串！");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("参数不是json格式！");
        }
        KnowledgePackageBaseline baseline = new KnowledgePackageBaseline();
        baseline.setPackageUuid(packageUuid);
        baseline.setUuid(IdUtils.UUID());
        baseline.setCreator(UserHolder.currentUser());
        baseline.setState(Constants.BaselineStat.UNPUBLISHED);
        try {
            baselineMapper.insert(baseline);
            KnowledgePackageBaseline kpbl = baselineMapper.selectById(baseline.getId());
            baselineMapper.insertBaselineAssets(packageUuid, kpbl.getVersionNo(), array);

            List<Refer> refers = new ArrayList<>();
			for (KnowledgePackage pkg : array) {
				Refer refer = new Refer();
				refer.setSubjectUuid(pkg.getAssetsUuid());
				refer.setSubjectVersion(pkg.getAssetsVersion());
				refer.setReferUuid(packageUuid);
				refer.setReferVersion(kpbl.getVersionNo());
				refer.setReferType("pkg");
				refers.add(refer);
			}
            referMapper.insertBatch(refers);
        } catch (Exception e) {
            logger.error("保存知识包基线失败. packageUuid: {}, assets: {}", packageUuid, assets, e);
            throw new IllegalArgumentException("保存知识包基线失败！");
        }
        return Result.newSuccess();
    }

    /**
     * @Author yangyongqiang
     * @Description 删除知识包基线
     * @Date 9:34 上午 2020/7/1
     * @Param [uuid]
     * @return void
     **/
    @Override
	@Transactional
    public void delete(String uuid) {
        if (StringUtils.isBlank(uuid)) {
            throw new IllegalArgumentException("uuid不能为空！");
        }

        try {
            baselineMapper.delete(uuid);

            KnowledgePackageBaseline baseline = baselineMapper.selectByUuid(uuid);
            Refer refer = new Refer();
			refer.setReferType("pkg");
			refer.setReferUuid(baseline.getPackageUuid());
			refer.setReferVersion(baseline.getVersionNo());
            referMapper.delete(refer);
        } catch (Exception e) {
        	logger.error("删除知识包基线失败: {}", uuid, e);
            throw new IllegalStateException("删除知识包基线失败");
        }
    }

    /**
     * 基线列表查询
     * @auther yinrj
     * @date 2020/6/29
     * @param packageUuid
     */
    @Override
    public List<KnowledgePackageBaseline> listByPackageUuid(int page, int pageNum, String packageUuid) {
        if (StringUtils.isBlank(packageUuid)) {
            throw new IllegalArgumentException("参数缺失！");
        }

        try {
            return baselineMapper.selectListByPage(packageUuid, pageNum * (page-1), pageNum);
        } catch (Exception e) {
            logger.error("查询基线列表失败. packageUuid: {}", packageUuid, e);
        }
        return Collections.emptyList();
    }

    @Override
    public int getCountByPackageUuid(String packageUuid) {
        return baselineMapper.selectCountByPackage(packageUuid);
    }

    /**
     * @Author yangyongqiang
     * @Description 决策服务查询
     * @Date 7:33 下午 2020/7/2
     **/
    @Override
    public List<KnowledgePackageBaseline> listMicroBaselineByPackageUuid(int page, int pageNum, String packageUuid) {
        if (StringUtils.isBlank(packageUuid)) {
            throw new IllegalArgumentException("参数缺失！");
        }
        List<KnowledgePackageBaseline> list = null;
        try {
            list = baselineMapper.selectListMicroBaselineByPage(packageUuid, pageNum * (page-1), pageNum);
            return list;
        } catch (Exception e) {
            logger.error(String.format("查询基线列表失败，packageUuid=%s", packageUuid));
            logger.error(e.getLocalizedMessage(), e);
        }
        return Collections.emptyList();
    }

    /**
     * @Author yangyongqiang
     * @Description 决策服务查询数量
     * @Date 7:33 下午 2020/7/2
     **/
    @Override
    public int getCountMicroBaselineByPackageUuid(String packageUuid) {
        return baselineMapper.selectCountMicroBaselineByPackage(packageUuid);
    }

    /**
     * 发布基线生成服务
     * @author yinrj
     * @date 2020/7/1
     */
	@Override
	public void publish(String packageUuid, Integer baselineVersion) {
		if (StringUtils.isBlank(packageUuid) || baselineVersion == null) {
			throw new IllegalArgumentException("参数缺失！");
		}

		try {
			baselineMapper.updateState(packageUuid, baselineVersion, Constants.BaselineStat.AUDIT);
		} catch (Exception e) {
			logger.error("知识包基线发布失败. packageUuid: {}, baselineVersion: {}", packageUuid, baselineVersion, e);
			throw new IllegalStateException("发布失败");
		}
	}

    /**
    * 描述: 基线版本比较
    * @param: [packageUuid, baselineV1, baselineV2]
    * @author: 周庚新
    * @date: 2020/7/6
    * @return: com.beagledata.common.Result
    *
    */
	@Override
	public Result compare(String packageUuid, Integer baselineV1, Integer baselineV2) {
		if(StringUtils.isBlank(packageUuid) || baselineV1 ==null || baselineV2 == null){
			throw new IllegalArgumentException("参数缺失！");
		}

		List<BaselineCompareChildrenVO> vos = baselineMapper.selectCompare(packageUuid, baselineV1, baselineV2);
		if (vos.isEmpty()) {
			return Result.emptyList();
		}

		Map<String, List<BaselineCompareChildrenVO>> map = new HashMap<>();
		for (BaselineCompareChildrenVO vo : vos) {
		    String type = vo.getType();
			List<BaselineCompareChildrenVO> baselineCompareChildrenVoList = map.get(type);
		    if (baselineCompareChildrenVoList == null) {
				baselineCompareChildrenVoList = new ArrayList<>();
		    }

			baselineCompareChildrenVoList.add(vo);
			map.put(type, baselineCompareChildrenVoList);
		}
		if (map.isEmpty()) {
		    return Result.emptyList();
		}

		List<BaselineCompareVO> baselineCompareVOs = new ArrayList<>();
		for (Map.Entry<String, List<BaselineCompareChildrenVO>> entry : map.entrySet()) {
			BaselineCompareVO baselineCompareVO = new BaselineCompareVO();
			baselineCompareVO.setUuid(entry.getKey());
			baselineCompareVO.setTypeName(entry.getKey());
			baselineCompareVO.setChildren(entry.getValue());
			baselineCompareVOs.add(baselineCompareVO);
		}
		return Result.newSuccess().withData(baselineCompareVOs);
	}

	/**
	 * @Author yangyongqiang
	 * @Description 知识包发布审核
	 * @Date 10:32 上午 2020/7/6
	 * Integer auditState, String auditReason
	 **/
	@Override
	@Transactional
	public void auditBaseline(KnowledgePackageBaseline baseline) {
		if (baseline == null) {
			throw new IllegalArgumentException("参数有误！");
		}
		if (baseline.getState() == null) {
			throw new IllegalArgumentException("审批状态参数有误！");
		}
		if (StringUtils.isBlank(baseline.getUuid())) {
			throw new IllegalArgumentException("知识包基线参数有误！");
		}

		try {
			if (Constants.BaselineStat.UNEFFECTIVE == baseline.getState()) {
				if (StringUtils.isBlank(baseline.getPackageUuid())) {
					throw new IllegalArgumentException("基线知识包uuid不能为空");
				}
				KnowledgePackage pkg = knowledgePackageMapper.selectByUuid(baseline.getPackageUuid());
				if (pkg == null) {
					throw new IllegalArgumentException("基线知识包不存在");
				}

				if (StringUtils.isBlank(pkg.getMicroUuid())) {
					Micro micro = new Micro();
					micro.setProjectUuid(pkg.getProjectUuid());
					micro.setPackageUuid(pkg.getUuid());
					micro.setUuid(IdUtils.UUID());
					micro.setName(pkg.getName());
					micro.setDescription(pkg.getDescription());

					KnowledgePackage updatePkg = new KnowledgePackage();
					updatePkg.setUuid(pkg.getUuid());
					updatePkg.setMicroUuid(micro.getUuid());

					microMapper.insert(micro);
					knowledgePackageMapper.update(updatePkg);
				}
			}

			baselineMapper.updateAuditState(baseline);
			baselineMapper.insertAuditRecord(
					baseline.getState(),
					baseline.getAuditReason(),
					UserHolder.currentUserUuid(),
					baseline.getUuid()
			);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			logger.error("审核基线失败: {}", baseline, e);
			throw new IllegalStateException("审核基线失败");
		}
	}

	/**
	 * @Author yangyongqiang
	 * @Description 查询微服务部署相关的基线列表数据
	 * @Date 6:37 下午 2020/7/16
	 **/
	@Override
	public List<KnowledgePackageBaseline> selectListMicroBaseline (String packageUuid){
		if (StringUtils.isBlank(packageUuid)) {
			throw new IllegalArgumentException("参数缺失！");
		}
		List<KnowledgePackageBaseline> list = null;
		try {
			list = baselineMapper.selectListMicroBaselineByPage(packageUuid, null, null);
			return list;
		} catch (Exception e) {
			logger.error(String.format("查询基线列表失败，packageUuid=%s", packageUuid));
			logger.error(e.getLocalizedMessage(), e);
		}
		return Collections.emptyList();
	}
}
