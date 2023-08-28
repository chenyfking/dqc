package com.beagledata.gaea.workbench.rule.define;

import com.beagledata.gaea.ruleengine.aimodel.AiModelHandler;
import com.beagledata.gaea.ruleengine.runtime.ExecutionResult;
import com.beagledata.gaea.ruleengine.thirdapi.ThirdApiHandler;
import com.beagledata.gaea.workbench.entity.AiModel;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.entity.ThirdApiDefinition;
import com.beagledata.util.EncodeUtils;
import com.beagledata.util.IdUtils;
import com.beagledata.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by liulu on 2018/10/16.
 */
public class Drl implements Dumper {
    private static String TPL = "package %s\n\n%s\n\n%s\n\n%s\n\n%s";

    public static final String DEFAULT_PACKAGE = "com.beagledata.gaea.rule";
    public static final String DEFAULT_PACKAGE_PATH = DEFAULT_PACKAGE.replace(".", "/");

    /**
     * 用作文件名称
     */
    private String id;
    /**
     * drl文件所在包
     */
    protected String packageName = DEFAULT_PACKAGE;
    /**
     * 需要import的列表
     */
    protected Set<Import> imports = new HashSet<>();
    /**
     * drl用到的数据模型列表
     */
    protected Set<Fact> facts = new HashSet<>();
    /**
     * drl用到的函数列表
     */
    protected Set<String> globals = new HashSet<>(Arrays.asList(ExecutionResult.class.getName()));
    /**
     * drl全局属性列表
     */
    protected Set<Attr> attrs = new HashSet<>(Arrays.asList(
            new Attr("dialect", "mvel"),
            new Attr("no-loop", "true")
    ));
    /**
     * drl规则列表
     */
    protected List<Rule> rules = new ArrayList<>();
    /**
     * drl用到的AI模型列表
     */
    protected Set<AiModel> models = new HashSet<>();
    /**
     * 用到的外部接口
     */
    protected Set<ThirdApiDefinition> thirdApis = new HashSet<>();
    /**
     * 是否用来校验
     */
    private boolean forVerifier;
    /**
     * 对应的资源文件
     */
    private Assets assets;

    public Drl() {
    }

    public Drl(String id) {
        this.id = id;
    }

    @Override
    public String dump() {
        String ruleContent = visitRules();
        if (!forVerifier) {
            return String.format(TPL, packageName, visitImports(), visitGlobals(), visitAttrs(), ruleContent);
        }
        return String.format("package %s\n\n%s", packageName, ruleContent);
    }

    /**
     * @return 循环所有规则用到的数据模型，去重
     */
    protected String visitImports() {
        rules.forEach(rule -> {
            facts.addAll(rule.getFacts());
            imports.addAll(rule.getImports());
        });
        return imports.stream().map(imp -> "import " + imp.getImport()).collect(Collectors.joining("\n"));
    }

    /**
     * @return 循环所有规则用到的函数，去重
     */
    protected String visitGlobals() {
        rules.forEach(rule -> globals.addAll(rule.getFuncs()));
        String globalContent = globals.stream().map(func -> {
            if (ExecutionResult.class.getName().equals(func)
                || AiModelHandler.class.getName().equals(func)
                || ThirdApiHandler.class.getName().equals(func)) {
                return String.format("global %s $%s", func, func.substring(func.lastIndexOf('.') + 1));
            } else {
                return String.format("global %s $%s", func, EncodeUtils.encodeMD516(func));
            }
        }).collect(Collectors.joining("\n"));
        if (rules.stream().anyMatch(rule -> !rule.getModels().isEmpty())) {
            rules.forEach(rule -> models.addAll(rule.getModels()));
            globals.add(AiModelHandler.class.getName());
            globalContent += "\nglobal com.beagledata.gaea.ruleengine.aimodel.AiModelHandler $AiModelHandler";
        }
        rules.stream().forEach(rule -> thirdApis.addAll(rule.getThirdApis()));
        if (!thirdApis.isEmpty()) {
            globals.add(ThirdApiHandler.class.getName());
            globalContent += "\nglobal com.beagledata.gaea.ruleengine.thirdapi.ThirdApiHandler $ThirdApiHandler";
        }
        return globalContent;
    }

    protected String visitAttrs() {
        return attrs.stream()
                .map(attr -> attr.dump())
                .filter(dump -> StringUtils.isNotBlank(dump))
                .collect(Collectors.joining("\n")
        );
    }

    protected String visitRules() {
        if (!forVerifier) {
            for (int i = 0; i < rules.size(); i++) {
                Rule rule = rules.get(i);
                if (rule.getOther() == null) {
                    continue;
                }
                Rule elseRule = new Rule(rule.getName() + "-ELSE分支");
                elseRule.addAttrs(rule.getAttrs());
                elseRule.setRhs(rule.getOther());
                elseRule.dump();
                if (!elseRule.getRhs().isDoAction()) {
                    continue;
                }
                Attr activationGroupAttr = rule.getAttr(Attr.ACTIVATION_GROUP);
                if (activationGroupAttr == null) {
                    activationGroupAttr = new Attr(Attr.ACTIVATION_GROUP, IdUtils.RandomCode(10));
                    rule.setAttr(activationGroupAttr);
                    elseRule.setAttr(activationGroupAttr);
                }
                // 追加在当前规则后面
                rules.add(++i, elseRule);
            }
        }

        return rules.stream().map(rule -> {
            rule.setForVerifier(forVerifier);
            rule.setMetaData("assetsUuid", assets.getUuid());
            if (assets.getVersionNo() != null && assets.getVersionNo() > 0) {
                rule.setMetaData("assetsVersion", assets.getVersionNo());
            }
            return rule.dump();
        }).filter(content -> StringUtils.isNotBlank(content)).collect(Collectors.joining("\n\n"));
    }

    public void addAttr(String name, String value) {
        this.attrs.add(new Attr(name, value));
    }

    public void addImport(Import impt) {
        this.imports.add(impt);
    }

    public void addFact(Fact fact) {
        this.facts.add(fact);
    }

    public void addRule(Rule rule) {
        this.rules.add(rule);
    }

    public void addModel(AiModel model) {
        this.models.add(model);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
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

    public Set<String> getGlobals() {
        return globals;
    }

    public void setGlobals(Set<String> globals) {
        this.globals = globals;
    }

    public Set<Attr> getAttrs() {
        return attrs;
    }

    public void setAttrs(Set<Attr> attrs) {
        this.attrs = attrs;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
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

    public void setForVerifier(boolean forVerifier) {
        this.forVerifier = forVerifier;
    }

    public Assets getAssets() {
        return assets;
    }

    public void setAssets(Assets assets) {
        this.assets = assets;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Drl{");
        sb.append("id='").append(id).append('\'');
        sb.append(", packageName='").append(packageName).append('\'');
        sb.append(", imports=").append(imports);
        sb.append(", facts=").append(facts);
        sb.append(", globals=").append(globals);
        sb.append(", attrs=").append(attrs);
        sb.append(", rules=").append(rules);
        sb.append(", models=").append(models);
        sb.append(", thirdApis=").append(thirdApis);
        sb.append(", forVerifier=").append(forVerifier);
        sb.append(", assets=").append(assets);
        sb.append('}');
        return sb.toString();
    }
}
