package com.beagledata.gaea.ruleengine.runtime;

import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.Match;

/**
 * 描述:
 * 规则执行拦截器
 *
 * @author 周庚新
 * @date 2020-09-17
 */
public class DefaultAgendaFilter implements AgendaFilter {

	private ExecutionResult executionResult;
	public DefaultAgendaFilter(ExecutionResult executionResult) {
		this.executionResult = executionResult;
	}

	@Override
	public boolean accept(Match match) {
		return executionResult.addRule(match);
	}
}