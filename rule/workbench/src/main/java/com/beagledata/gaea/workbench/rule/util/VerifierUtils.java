package com.beagledata.gaea.workbench.rule.util;

import com.beagledata.gaea.common.LogManager;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.rule.define.*;
import com.beagledata.gaea.workbench.rule.parser.DrlParser;
import com.beagledata.gaea.workbench.rule.parser.FlowParser;
import com.beagledata.gaea.workbench.rule.parser.Parser;
import com.beagledata.gaea.workbench.rule.parser.ParserFactory;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述:
 * 规则验证工具类
 *
 * @author 周庚新
 * @date 2020-07-08
 */
public class VerifierUtils {
	private static Logger logger = LogManager.getLogger(VerifierUtils.class);

	/**
	* 描述: 描述: 获取一个rule所有的Constraint
	* @param: [constraint, constraints, arith] 初始表达式，返回集合，是否包含 运算表达式的 条件
	* @author: 周庚新
	* @date: 2020/7/21
	* @return: void
	*
	*/
	public static void getAllConstraint(Constraint constraint, List<Constraint> constraints, boolean arith) {
		if (arith) {
			if (constraint.getLeft() != null || constraint.getRight() != null) {
				constraints.add(constraint);
			}
		}else {
			if (constraint.getLeft() != null && constraint.getRight() != null) {
				//todo 具有算术表达式的条件不算在内
				if (constraint.getLeft().getArith() != null || constraint.getRight().getArith() != null) {
					return;
				}
				constraints.add(constraint);
			}
		}
		List<Constraint> constraintsList = constraint.getConstraints();
		if (CollectionUtils.isNotEmpty(constraintsList)) {
			constraintsList.forEach(constraint1 -> getAllConstraint(constraint1, constraints, arith));
		}
	}

}