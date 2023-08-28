package com.beagledata.gaea.workbench.service.impl;

import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.entity.Refer;
import com.beagledata.gaea.workbench.mapper.AssetsMapper;
import com.beagledata.gaea.workbench.mapper.ReferMapper;
import com.beagledata.gaea.workbench.service.TestCaseService;
import com.beagledata.util.StringUtils;
import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 描述: 测试案例service
 *
 * @param:
 * @author: 周庚新
 * @date: 2020/6/22
 * @return:
 */
@Service
public class TestCaseServiceImpl implements TestCaseService {
	private static Logger logger = LoggerFactory.getLogger(TestCaseServiceImpl.class);

	@Autowired
	private AssetsMapper assetsMapper;
	@Autowired
	private ReferMapper referMapper;

	/**
     *测试案例绑定规则
     * @author yinrj
     * 2020/6/22 16:21
     */
	@Override
	@Transactional
	public void add(String ruleUuid, String caseUuid, Integer assetsVersion) {
		if (StringUtils.isBlank(ruleUuid) || StringUtils.isBlank(caseUuid) ||assetsVersion == null) {
			throw new IllegalArgumentException("参数缺失！");
		}
		try {
			Refer deleteRefer = new Refer();
			deleteRefer.setReferType("testcase");
			deleteRefer.setReferUuid(caseUuid);
			referMapper.delete(deleteRefer);
			Refer refer = new Refer(ruleUuid, null, null, assetsVersion, caseUuid, "testcase", null);
			referMapper.insert(refer);
		} catch (Exception e) {
			logger.error("测试案例绑定规则失败！参数ruleUuid:{}, caseUuid:{}", ruleUuid, caseUuid);
			logger.error(e.getLocalizedMessage(), e);
			throw new IllegalStateException("测试案例绑定规则失败!");
		}

	}

	/**
	 * @Author yangyongqiang
	 * @Description 删除测试案例
	 * @Date 10:04 上午 2020/7/1
	 * @Param [caseUuid]
	 * @return void
	 **/
	@Override
	@Transactional
	public void delete(String caseUuid) {
		if (StringUtil.isBlank(caseUuid)) {
			throw new IllegalArgumentException("测试uuid不能为空");
		}

		Assets assets = assetsMapper.selectByUuid(caseUuid);
		if (assets == null) {
			throw new IllegalArgumentException("资源文件不存在");
		}
		if (assets.isLocked()) {
			throw new IllegalArgumentException("文件被锁定，不能删除");
		}

		try {
			assetsMapper.deleteByUuid(caseUuid);
			Refer refer = new Refer();
			refer.setReferType("testcase");
			refer.setReferUuid(caseUuid);
			referMapper.delete(refer);
		} catch (Exception e) {
			logger.error(caseUuid + e.getLocalizedMessage(), e);
			throw new IllegalStateException("删除失败");
		}
	}
}
