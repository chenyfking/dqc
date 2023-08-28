package com.beagledata.gaea.workbench.rule.define;

import com.beagledata.gaea.ruleengine.common.RuleMetaData;
import com.beagledata.gaea.workbench.entity.AiModel;
import com.beagledata.gaea.workbench.entity.ThirdApiDefinition;
import com.beagledata.util.IdUtils;
import com.beagledata.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by liulu on 2018/9/30.
 */
public class Rule implements Dumper {
    public static String TPL = "rule \"%s\"\n%s\nwhen\n%s\nthen\n%s\nend";

    /**
     * 规则名称
     */
    protected String name;
    /**
     * 规则的元数据
     */
    private Map<String, Object> metaData = new HashMap<>();
    /**
     * 属性列表
     */
    private Set<Attr> attrs = new HashSet<>();
    private Lhs lhs = new Lhs();
    private Rhs rhs = new Rhs();
    /**
     * 否则动作
     */
    private Rhs other;
    /**
     * 需要import的列表
     */
    private Set<Import> imports = new HashSet<>();
    /**
     * 用到的数据模型列表
     */
    private Set<Fact> facts = new HashSet<>();
    /**
     * 用到的函数列表
     */
    private Set<String> funcs = new HashSet<>();
    /**
     * 用到的AI模型列表
     */
    private Set<AiModel> models = new HashSet<>();
    /**
     * 用到的外部接口
     */
    private Set<ThirdApiDefinition> thirdApis = new HashSet<>();
    /**
     * 当前规则是否要记录执行
     */
    private boolean implicit;
    /**
     * 是否循环规则
     */
    private boolean loop;
    /**
     * 循环对象
     */
    private FactExpression loopTarget;
    /**
     * 是否用来校验
     */
    private boolean forVerifier;

    public Rule() {
    }

    public Rule(String name) {
        this.name = name;
    }

    @Override
    public String dump() {
        if (loop) {
            RuleAware.setRuleUnchecked(this, lhs, rhs, loopTarget);
            setMetaData(RuleMetaData.KEY_LOOP, true);
        } else {
            RuleAware.setRuleUnchecked(this, lhs, rhs, other);
        }
        if (isImplicit()) {
            setMetaData(RuleMetaData.KEY_IMPLICIT, true);
        }
        return doDump();
    }

    protected String doDump() {
        StringBuilder buffer = new StringBuilder();
        // 规则名称
        buffer.append("rule \"").append(getName()).append("\"\n");
        // 规则元数据
        Map<String, Object> metaData = getMetaData();
        if (!metaData.isEmpty()) {
            metaData.forEach((k, v) -> {
                buffer.append("@").append(k).append("(");
                if (v instanceof String) {
                    buffer.append("\"").append(v).append("\"");
                } else {
                    buffer.append(v);
                }
                buffer.append(")\n");
            });
        }
        // 规则属性
        if (!forVerifier) {
            String attrContent = parseAttr();
            if (StringUtils.isNotBlank(attrContent)) {
                buffer.append(attrContent).append("\n");
            }
        }
        String rhsContent = getRhsContent(); // 先dump rhs，把赋值的Fact添加到rule
        String lhsContent = getLhsContent();
        // 规则条件
        buffer.append("when\n");
        if (StringUtils.isNotBlank(lhsContent)) {
            buffer.append(lhsContent).append("\n");
        }
        // 规则动作
        buffer.append("then\n");
        if (StringUtils.isNotBlank(rhsContent)) {
            buffer.append(rhsContent).append("\n");
        }
        if (forVerifier) {
            // 添加一行打印语句区分相同规则
            buffer.append(String.format("  System.out.println(\"%s\");\n", IdUtils.RandomCode(32)));
        }
        buffer.append("end");
        return buffer.toString();
    }

    protected String parseAttr() {
        return attrs.stream()
                .map(attr -> attr.dump())
                .filter(dump -> StringUtils.isNotBlank(dump))
                .collect(Collectors.joining("\n")
        );
    }

    protected String getLhsContent() {
        return lhs.dump();
    }

    protected String getRhsContent() {
        return rhs.dump();
    }

    public boolean addImport(Import impt) {
        return this.imports.add(impt);
    }

    public boolean addFact(Fact fact) {
        addImport(fact);
        return this.facts.add(fact);
    }

    public boolean addFunc(String func) {
        return this.funcs.add(func);
    }

    public boolean addModel(AiModel model) {
        return this.models.add(model);
    }

    public boolean addThirdApi(ThirdApiDefinition definition) {
        return this.thirdApis.add(definition);
    }

    public void setMetaData(String key, Object value) {
        this.metaData.put(key, value);
    }

    public void addAttrs(Set<Attr> attrs) {
        this.attrs.addAll(attrs);
    }

    public void setAttr(Attr attr) {
        if (attrs.contains(attr)) {
            attrs.remove(attr);
        }
        attrs.add(attr);
    }

    public Attr getAttr(String name) {
        return attrs.stream().filter(attr -> attr.getName().equals(name)).findFirst().orElse(null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, Object> metaData) {
        this.metaData = metaData;
    }

    public Set<Attr> getAttrs() {
        return attrs;
    }

    public void setAttrs(Set<Attr> attrs) {
        this.attrs = attrs;
    }

    public Lhs getLhs() {
        return lhs;
    }

    public void setLhs(Lhs lhs) {
        this.lhs = lhs;
    }

    public Rhs getRhs() {
        return rhs;
    }

    public void setRhs(Rhs rhs) {
        this.rhs = rhs;
    }

    public Rhs getOther() {
        return other;
    }

    public void setOther(Rhs other) {
        this.other = other;
    }

    public boolean isImplicit() {
        return implicit;
    }

    public void setImplicit(boolean implicit) {
        this.implicit = implicit;
    }

    public Set<Import> getImports() {
        return imports;
    }

    public void setImports(Set<Import> imports) {
        this.imports = imports;
    }

    public Set<Fact> getFacts() {
        return facts;
    }

    public void setFacts(Set<Fact> facts) {
        this.facts = facts;
    }

    public Set<String> getFuncs() {
        return funcs;
    }

    public void setFuncs(Set<String> funcs) {
        this.funcs = funcs;
    }

    public Set<AiModel> getModels() {
        return models;
    }

    public void setModels(Set<AiModel> models) {
        this.models = models;
    }

    public Set<ThirdApiDefinition> getThirdApis() {
        return thirdApis;
    }

    public void setThirdApis(Set<ThirdApiDefinition> thirdApis) {
        this.thirdApis = thirdApis;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public FactExpression getLoopTarget() {
        return loopTarget;
    }

    public void setLoopTarget(FactExpression loopTarget) {
        this.loopTarget = loopTarget;
    }

    public boolean isForVerifier() {
        return forVerifier;
    }

    public void setForVerifier(boolean forVerifier) {
        this.forVerifier = forVerifier;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Rule{");
        sb.append("name='").append(name).append('\'');
        sb.append(", metaData=").append(metaData);
        sb.append(", attrs=").append(attrs);
        sb.append(", lhs=").append(lhs);
        sb.append(", rhs=").append(rhs);
        sb.append(", other=").append(other);
        sb.append(", imports=").append(imports);
        sb.append(", facts=").append(facts);
        sb.append(", funcs=").append(funcs);
        sb.append(", models=").append(models);
        sb.append(", thirdApis=").append(thirdApis);
        sb.append(", implicit=").append(implicit);
        sb.append(", loop=").append(loop);
        sb.append(", loopTarget=").append(loopTarget);
        sb.append(", forVerifier=").append(forVerifier);
        sb.append('}');
        return sb.toString();
    }
}
