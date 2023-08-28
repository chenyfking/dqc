package com.beagledata.gaea.workbench.rule.builder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beagledata.common.SpringBeanHolder;
import com.beagledata.gaea.ruleengine.aimodel.AiModelHandler;
import com.beagledata.gaea.ruleengine.aimodel.JarInvoker;
import com.beagledata.gaea.ruleengine.aimodel.PmmlInvoker;
import com.beagledata.gaea.ruleengine.builder.AbstractRuleBuilder;
import com.beagledata.gaea.ruleengine.exception.RuleException;
import com.beagledata.gaea.ruleengine.model.RuleFactModel;
import com.beagledata.gaea.ruleengine.thirdapi.ThirdApi;
import com.beagledata.gaea.ruleengine.thirdapi.ThirdApiHandler;
import com.beagledata.gaea.ruleengine.util.ClassUtils;
import com.beagledata.gaea.workbench.common.ResourceResolver;
import com.beagledata.gaea.workbench.entity.*;
import com.beagledata.gaea.workbench.rule.define.Drl;
import com.beagledata.gaea.workbench.rule.define.Fact;
import com.beagledata.gaea.workbench.rule.define.Flow;
import com.beagledata.gaea.workbench.rule.define.Import;
import com.beagledata.gaea.workbench.rule.parser.DrlParser;
import com.beagledata.gaea.workbench.rule.parser.FlowParser;
import com.beagledata.gaea.workbench.rule.parser.Parser;
import com.beagledata.gaea.workbench.rule.parser.ParserFactory;
import com.beagledata.gaea.workbench.rule.util.AssetsCache;
import com.beagledata.gaea.workbench.service.FunctionDefinitionService;
import com.beagledata.gaea.workbench.util.JavaCodeGenerator;
import com.beagledata.util.StringUtils;
import com.bigdata.bdtm.exceptions.ModelPredictException;
import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by liulu on 2019/1/4.
 */
public class KnowledgePackageRuleBuilder extends AbstractRuleBuilder {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ResourceResolver resourceResolver = SpringBeanHolder.getBean(ResourceResolver.class);
    private FunctionDefinitionService functionDefinitionService = SpringBeanHolder.getBean(FunctionDefinitionService.class);

    private KnowledgePackage pkg;

    /**
     * 知识包需要import的列表
     */
    private Set<Import> imports = new HashSet<>();
    /**
     * 知识包用到的数据模型
     */
    private Set<Fact> facts = new HashSet<>();
    /**
     * 知识包用到的AI模型
     */
    private Set<AiModel> models = new HashSet<>();
    /**
     * 知识包用到的外部函数Jar
     */
    private Set<File> funcFiles = new HashSet<>();
    /**
     * 用到的外部接口
     */
    private Set<ThirdApiDefinition> thirdApiDefinitions = new HashSet<>();
    /**
     * 依赖的资源文件集合
     */
    private Set<Assets> assetsSet = new HashSet<>();
    /**
     * 依赖的函数集合
     */
    private Set<FunctionDefinition> funcSet = new HashSet<>();
    /**
     * 依赖的AI模型集合
     */
    private Set<AiModel> aiModelSet = new HashSet<>();

    public static KnowledgePackageRuleBuilder newBuilder(KnowledgePackage pkg) {
        return new KnowledgePackageRuleBuilder(pkg);
    }

    private KnowledgePackageRuleBuilder(KnowledgePackage pkg) {
        super();
        this.pkg = pkg;
    }

    @Override
    public void build() throws Exception {
        buildId();
        buildKieModuleModel();
        buildAssets();
        buildFacts();
        buildFuncs();
        buildModel();
        buildThirdApi();
    }

    public byte[] getBytes() throws IOException {
        MemoryFileSystem mfs = getMemoryFileSystem();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            writeSrc(zip, mfs);
            writeModel(zip);
            writeFuncJar(zip);
            writeConf(zip);
            zip.closeEntry();
        }
        return baos.toByteArray();
    }

    private void writeSrc(ZipOutputStream zip, MemoryFileSystem mfs) throws IOException {
        Map<String, byte[]> map = mfs.getMap();
        for (Map.Entry<String, byte[]> me : map.entrySet()) {
            ZipEntry entry = new ZipEntry(me.getKey());
            zip.putNextEntry(entry);
            zip.write(me.getValue());
        }
    }

    private void writeConf(ZipOutputStream zip) throws IOException {
        JSONObject conf = new JSONObject();
        conf.put("id", id);
        conf.put("facts", ruleFacts);
        conf.put("globals", globals);
        conf.put("modelNames", modelNames);
        conf.put("flowProcessIds", flowProcessIds);
        conf.put("thirdApis", thirdApis);

        KnowledgePackage pkg = new KnowledgePackage();
        pkg.setName(getPkg().getName());
        pkg.setDescription(getPkg().getDescription());
        pkg.setBaselineVersion(getPkg().getBaselineVersion());
        pkg.setProjectUuid(getPkg().getProjectUuid());
        pkg.setUuid(getPkg().getUuid());
        conf.put("knowledgepackage", pkg);

        ZipEntry entry = new ZipEntry("default.conf");
        zip.putNextEntry(entry);
        zip.write(conf.toJSONString().getBytes());
    }

    private void writeModel(ZipOutputStream zip) throws IOException {
        for (AiModel model : models) {
            File file = new File(resourceResolver.getModelPath(), model.getJarName());
            writeFile(zip, file, DIR_MODEL);
        }
    }

    private void writeFuncJar(ZipOutputStream zip) throws IOException {
        for (File funcFile : funcFiles) {
            writeFile(zip, funcFile, DIR_FUNC);
        }
    }

    private void writeFile(ZipOutputStream zip, File file, String dir) throws IOException {
        ZipEntry entry = new ZipEntry(dir + file.getName());
        zip.putNextEntry(entry);
        zip.write(FileUtils.readFileToByteArray(file));
    }

    private void buildId() {
        if (id == null) {
            id = String.valueOf(pkg.getId());
        }
    }

    private void buildKieModuleModel() {
        KieModuleModel kmm = ks.newKieModuleModel();
        KieBaseModel baseModel = kmm.newKieBaseModel("defaultKieBase").setDefault(true);
        baseModel.newKieSessionModel("defaultKieSession").setDefault(true);
        kfs.writeKModuleXML(kmm.toXML());
    }

    public void buildAssets() {
        for (Assets assets : pkg.getAvailableAssetsList()) {
            buildAssets(assets);
        }
        AssetsCache.clear();
    }

    private void buildAssets(Assets assets) {
        Parser parser = ParserFactory.getParser(assets);
        if (parser instanceof DrlParser) {
            Drl drl = ((DrlParser) parser).getDumper();
            buildDrl(drl);
        } else {
            Flow flow = ((FlowParser) parser).getDumper();
            buildFlow(flow);
        }
    }

    private void buildDrl(Drl drl) {
        String packageName = drl.getPackageName().replace(".", "/");
        String path = String.format("src/main/resources/%s/%s.drl", packageName, drl.getId());
        String drlContent = drl.dump();
       // System.out.println(String.format("----------------------------\n%s\n----------------------------", drlContent));
        kfs.write(path, drlContent);

        imports.addAll(drl.getImports());
        facts.addAll(drl.getFacts());
        globals.addAll(drl.getGlobals());
        models.addAll(drl.getModels());
        thirdApiDefinitions.addAll(drl.getThirdApis());
        for (AiModel model : drl.getModels()) {
            modelNames.add(model.getModelName());
        }

        addAssets(drl.getAssets());
    }

    private void buildFlow(Flow flow) {
        String path = String.format("src/main/resources/%s/%s.bpmn2", flow.getPackageName(), flow.getProcessId());
        kfs.write(path, flow.dump());

        flow.getDrls().forEach(drl -> buildDrl(drl));
        flow.getFlows().forEach(subFlow -> buildFlow(subFlow));

        imports.addAll(flow.getImports());
        facts.addAll(flow.getImports());
        globals.addAll(flow.getGlobals());
        models.addAll(flow.getModels());
        for (AiModel model : flow.getModels()) {
            modelNames.add(model.getModelName());
            globals.add(AiModelHandler.class.getName());
        }

        if (!flow.isSubFlow()) {
            flowProcessIds.add(flow.getProcessId());
        }

        addAssets(flow.getAssets());
    }

    private void buildFacts() {
        imports.forEach(impt -> {
            if (impt.getClass() == Fact.class) {
                Fact fact = (Fact) impt;
                ruleFacts.add(new RuleFactModel(fact.getIdentity(), fact.getImport()));
                writeFact(fact);
            }
        });
    }

    private void writeFact(Fact fact) {
        new JavaCodeGenerator(kfs, fact).gen();
    }

    private void buildFuncs() {
        for (String className : globals) {
            if (ThirdApiHandler.class.getName().equals(className)
                    || AiModelHandler.class.getName().equals(className)) {
                continue;
            }
            if (functionDefinitionService.isBuildIn(className)) {
                continue;
            }

            FunctionDefinition fd = functionDefinitionService.getByClassName(className);
            if (fd == null) {
                logger.error("编译失败: 自定义函数找不到. className: {}", className);
                throw new RuleException("编译失败: 自定义函数找不到");
            }
            if (StringUtils.isNotBlank(fd.getContent())) {
                kfs.write(String.format("src/main/java/%s.java", className.replace(".", "/")), fd.getContent());
            } else {
                File funcFile = new File(resourceResolver.getFunctionPath(), fd.getFileUuid() + ".jar");
                if (!funcFile.exists()) {
                    logger.error("编译失败: 自定义函数找不到. className: {}, funcFile: {}", className, funcFile);
                    throw new RuleException("编译失败: 自定义函数找不到");
                }

                funcFiles.add(funcFile);

                URLClassLoader urlClassLoader = (URLClassLoader) classLoader.getParent();
                try {
                    ClassUtils.loadJar(urlClassLoader, funcFile);
                } catch (Exception e) {
                    logger.error("编译函数失败. className: {}, funcFile: {}", className, funcFile, e);
                    throw new RuleException("编译失败: 自定义函数找不到");
                }
            }

            addFunc(fd);
        }
    }

    private void buildModel() throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException, ModelPredictException, JAXBException, SAXException, DocumentException, NoSuchFieldException, ClassNotFoundException {
        Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        addURL.setAccessible(true);
        for (AiModel model : models) {
            String fileName = model.getJarName();
            File file = new File(resourceResolver.getModelPath(), fileName);
            if (fileName.endsWith(".jar")) {
                JarInvoker invoker = JarInvoker.newInstance(file);
                modelInvokers.put(fileName, invoker);
            } else {
                PmmlInvoker invoker = PmmlInvoker.newInstance(file);
                modelInvokers.put(fileName, invoker);
            }
        }
    }

    private void buildThirdApi() {
        for (ThirdApiDefinition definition : thirdApiDefinitions) {
            ThirdApi api = new ThirdApi();
            api.setId(definition.getUuid());
            api.setUrl(definition.getUrl());
            api.setMethod(definition.getMethod().name());
            api.setJsonType(ThirdApiDefinition.RequestContentType.JSON.equals(definition.getRequestContentType()));
            api.setResponseJsonPath(definition.getResponseJsonPath());
            if (StringUtils.isNotBlank(definition.getHeadersJson())) {
                JSONArray array = JSON.parseArray(definition.getHeadersJson());
                if (!array.isEmpty()) {
                    for (int i = 0; i < array.size(); i++) {
                        JSONObject json = array.getJSONObject(i);
                        api.getHeaders().put(json.getString("name"), json.getString("value"));
                    }
                }
            }
            thirdApis.put(api.getId(), api);
        }
    }

    private void addAssets(Assets assets) {
        if (assets != null) {
            for (Assets assetsInSet : assetsSet) {
                if (Objects.equals(assetsInSet.getId(), assets.getId())
                    && Objects.equals(assetsInSet.getVersionNo(), assets.getVersionNo())) {
                    return;
                }
            }
            assetsSet.add(assets);
        }
    }

    private void addFunc(FunctionDefinition func) {
        funcSet.add(func);
    }

    private void addAiModel(AiModel aiModel) {
        aiModelSet.add(aiModel);
    }

    public Set<Fact> getFacts() {
        return facts;
    }

    public KnowledgePackage getPkg() {
        return pkg;
    }

    public Set<ThirdApiDefinition> getThirdApiDefinitions() {
        return thirdApiDefinitions;
    }

    public Set<Assets> getAssetsSet() {
        return assetsSet;
    }

    public Set<FunctionDefinition> getFuncSet() {
        return funcSet;
    }

    public Set<AiModel> getAiModelSet() {
        return aiModelSet;
    }
}
