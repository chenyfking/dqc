package com.beagledata.gaea.workbench.rule.verifer;

/**
* 描述: 规则校验类型工厂
* @author: 周庚新
* @date: 2020/11/17
*/
public class RuleVerifierFactory {
	public static AbstractRuleVerifier get(VerifierType type) {
		if (VerifierType.MISSING_RANGE.equals(type)) {
			return new MissingRangeRuleVerifier();
		} else if (VerifierType.EQUIVALENT.equals(type)) {
			return new EquivalentRuleVerifier();
		} else if (VerifierType.OVERLAP.equals(type)) {
			return new OverlapRuleVerifier();
		} else if (VerifierType.INCOHERENT.equals(type)) {
			return new IncoherentRuleVerifier();
		} else if (VerifierType.ALWAYS_TRUE.equals(type)) {
			return new AlwaysTrueRuleVerifier();
		}
		throw new IllegalArgumentException("不支持的校验器");
	}
}