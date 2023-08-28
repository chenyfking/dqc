package com.beagledata.gaea.workbench.service;

/**
 * 描述: 测试案例service
 *
 * @param:
 * @author: 周庚新
 * @date: 2020/6/22
 * @return:
 */
public interface TestCaseService {

	/**
     *测试案例绑定规则
     * @author yinrj
     * 2020/6/22 16:21
     */
	void add(String ruleUuid, String caseUuid, Integer assetsVersion);

	/**
	 * @Author yangyongqiang
	 * @Description 删除测试案例
	 * @Date 9:58 上午 2020/7/1
	 * @Param [caseUuid]
	 * @return void
	 **/
	void delete(String caseUuid);

}
