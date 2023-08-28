package com.beagledata.gaea.ruleengine.runtime.flow;

import com.beagledata.gaea.ruleengine.runtime.ExecutionResult;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;

/**
 * 描述:
 * 记录流程节点
 * @author 周庚新
 * @date 2020-09-16
 */
public class TraceProcessEventListener extends DefaultProcessEventListener {

	@Override
	public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
		//监听触发节点事件，记录节点id
		ExecutionResult executionResult = (ExecutionResult) event.getKieRuntime().getGlobal("$ExecutionResult");
		ProcessInstance processInstance = event.getProcessInstance();
		NodeInstance nodeInstance = event.getNodeInstance();
		String nodeUniqueId = (String) nodeInstance.getNode().getMetaData().get("UniqueId");
		int nodeKey = Integer.parseInt(nodeUniqueId.substring(nodeUniqueId.lastIndexOf("-")));
		executionResult.appendNodeKey(processInstance.getProcessId(), nodeKey);
	}
}