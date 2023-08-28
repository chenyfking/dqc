package com.beagledata.gaea.workbench.rule.define;

import com.alibaba.fastjson.annotation.JSONField;
import com.beagledata.gaea.ruleengine.runtime.ExecutionResult;
import com.beagledata.gaea.workbench.entity.AiModel;
import com.beagledata.gaea.workbench.entity.Assets;
import org.jbpm.bpmn2.xml.XmlBPMNProcessDumper;
import org.jbpm.ruleflow.core.RuleFlowProcess;

import java.util.*;

/**
 * Created by liulu on 2018/11/1.
 */
public class Flow implements Dumper {
    public static final String DEFAULT_PACKAGE_PATH = "com/beagledata/gaea/deploy/flow";

    /**
     * 资源文件id
     */
    private int id;
    /**
     * 'flow' + id
     */
    private String processId;
    /**
     * bpmn2文件所在包
     */
    private String packageName = DEFAULT_PACKAGE_PATH;
    /**
     * 规则流读取的规则所在包
     */
    private String rulePackage;
    /**
     * 脚本节点用到的数据模型
     */
    @JSONField(serialize = false)
    private Set<Fact> imports = new HashSet<>();
    /**
     * 脚本节点用到的函数
     */
    @JSONField(serialize = false)
    private Set<String> globals = new HashSet<>(Arrays.asList(ExecutionResult.class.getName()));
    /**
     * 脚本节点用到的AI模型
     */
    @JSONField(serialize = false)
    private Set<AiModel> models = new HashSet<>();
    /**
     * 规则节点和脚本节点生成的drl列表
     */
    private List<Drl> drls = new ArrayList<>();
    /**
     * 子决策流
     */
    private List<Flow> flows = new ArrayList<>();
    /**
     * jbpm2 process
     */
    private RuleFlowProcess jbpmProcess;
    /**
     * 是否子决策流
     */
    private boolean subFlow;
    /**
     * 对应的资源文件
     */
    private Assets assets;

    public void addDrl(Drl drl) {
        this.drls.add(drl);
    }

    public void addFlow(Flow flow) {
        this.flows.add(flow);
    }

    public void addImport(Fact fact) {
        this.imports.add(fact);
    }

    public void addImports(Set<Fact> facts) {
        this.imports.addAll(facts);
    }

    public void addGlobals(Set<String> globals) {
        this.globals.addAll(globals);
    }

    public void addModel(AiModel model) {
        this.models.add(model);
    }

    public void addModels(Set<AiModel> models) {
        this.models.addAll(models);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProcessId() {
        if (processId == null) {
            processId = String.format("flow%s", id);
        }
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getRulePackage() {
        return rulePackage;
    }

    public void setRulePackage(String rulePackage) {
        this.rulePackage = rulePackage;
    }

    public Set<Fact> getImports() {
        return imports;
    }

    public void setImports(Set<Fact> imports) {
        this.imports = imports;
    }

    public Set<String> getGlobals() {
        return globals;
    }

    public void setGlobals(Set<String> globals) {
        this.globals = globals;
    }

    public Set<AiModel> getModels() {
        return models;
    }

    public void setModels(Set<AiModel> models) {
        this.models = models;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<Drl> getDrls() {
        return drls;
    }

    public void setDrls(List<Drl> drls) {
        this.drls = drls;
    }

    public List<Flow> getFlows() {
        return flows;
    }

    public void setFlows(List<Flow> flows) {
        this.flows = flows;
    }

    public RuleFlowProcess getJbpmProcess() {
        return jbpmProcess;
    }

    public void setJbpmProcess(RuleFlowProcess jbpmProcess) {
        this.jbpmProcess = jbpmProcess;
    }

    public boolean isSubFlow() {
        return subFlow;
    }

    public void setSubFlow(boolean subFlow) {
        this.subFlow = subFlow;
    }

    public Assets getAssets() {
        return assets;
    }

    public void setAssets(Assets assets) {
        this.assets = assets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Flow)) return false;

        Flow flow = (Flow) o;

        return getId() == flow.getId();
    }

    @Override
    public int hashCode() {
        return getId();
    }

    @Override
    public String dump() {
        return XmlBPMNProcessDumper.INSTANCE.dump(jbpmProcess, XmlBPMNProcessDumper.NO_META_DATA);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Flow{");
        sb.append("id=").append(id);
        sb.append(", processId='").append(processId).append('\'');
        sb.append(", packageName='").append(packageName).append('\'');
        sb.append(", rulePackage='").append(rulePackage).append('\'');
        sb.append(", imports=").append(imports);
        sb.append(", globals=").append(globals);
        sb.append(", models=").append(models);
        sb.append(", drls=").append(drls);
        sb.append(", flows=").append(flows);
        sb.append(", jbpmProcess=").append(jbpmProcess);
        sb.append(", subFlow=").append(subFlow);
        sb.append(", assets=").append(assets);
        sb.append('}');
        return sb.toString();
    }
}
