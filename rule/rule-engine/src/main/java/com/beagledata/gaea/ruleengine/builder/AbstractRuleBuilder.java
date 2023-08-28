package com.beagledata.gaea.ruleengine.builder;

import com.beagledata.gaea.ruleengine.aimodel.Invoker;
import com.beagledata.gaea.ruleengine.common.Constants;
import com.beagledata.gaea.ruleengine.exception.RuleException;
import com.beagledata.gaea.ruleengine.model.RuleFactModel;
import com.beagledata.gaea.ruleengine.thirdapi.ThirdApi;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.KieFileSystemImpl;
import org.drools.core.common.ProjectClassLoader;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.runtime.KieContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Created by liulu on 2019/1/4.
 */
public abstract class AbstractRuleBuilder {
    private static  Logger logger = LoggerFactory.getLogger(AbstractRuleBuilder.class);
    /**
     * 打包的时候模型文件存放的目录
     */
    protected static final String DIR_MODEL = "lib/model/";
    /**
     * 打包的时候函数jar包存放的目录
     */
    protected static final String DIR_FUNC = "lib/func/";

    protected KieServices ks;
    protected KieFileSystem kfs;
    protected KieContainer kc;
    protected KieBuilder kb;

    protected String id;

    protected Set<RuleFactModel> ruleFacts = new HashSet<>();
    protected Set<String> globals = new HashSet<>();
    protected Set<String> flowProcessIds = new HashSet<>();
    protected Set<String> modelNames = new HashSet<>();

    protected Map<String, Invoker> modelInvokers = new HashMap<>();
    protected Map<String, ThirdApi> thirdApis = new HashMap<>();

    protected boolean isFlow;

    protected ClassLoader classLoader = ProjectClassLoader.createProjectClassLoader(
            new URLClassLoader(new URL[] {}, ProjectClassLoader.findParentClassLoader())
    );

    static {
        System.setProperty("drools.dateformat", Constants.DEFAULT_DROOLS_DATE_FORMAT);
        try {
            //开启ProjectClassLoader 的并行加载，解决类加载死锁问题
            Class parallelLoadersClazz = Class.forName(ClassLoader.class.getName() + "$ParallelLoaders");
            Method method = parallelLoadersClazz.getDeclaredMethod("register", Class.class);
            method.setAccessible(true);

            method.invoke(parallelLoadersClazz, ProjectClassLoader.class);
            Class defaultInternalTypesClassLoaderClazz = Class.forName(ProjectClassLoader.class.getName() + "$DefaultInternalTypesClassLoader");
            method.invoke(parallelLoadersClazz, defaultInternalTypesClassLoaderClazz);
            method.setAccessible(false);
            logger.info("ProjectClassLoader成功开启并行加载机制");
        } catch (Exception e) {
            logger.info("ProjectClassLoader开启并行加载机制失败", e);
        }
    }

    protected AbstractRuleBuilder() {
        ks = KieServices.Factory.get();
        kfs = ks.newKieFileSystem();
    }

    public void buildAll() throws Exception {
        build();

        ReleaseId releaseId = ks.newReleaseId(
                "com.beagledata.gaea",
                "rule",
                "1.0.0"
        );
        kfs.generateAndWritePomXML(releaseId);
        kb = ks.newKieBuilder(kfs, classLoader).buildAll();

        Results results = kb.getResults();
        if (results.hasMessages(Message.Level.ERROR)) {
            throw new RuleException("规则构建失败", results);
        }

        kc = ks.newKieContainer(releaseId, classLoader);
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        return kc.getClassLoader().loadClass(name);
    }

    public RuleFactModel getFact(String factId) {
        return ruleFacts.stream().filter(f -> f.getId().equals(factId)).findFirst().orElse(null);
    }

    public Object newObject(String factId) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        RuleFactModel fact = getFact(factId);
        if (fact == null) {
            throw new RuleException("数据模型找不到: " + factId);
        }

        Class clazz = loadClass(fact.getClassName());
        return clazz.newInstance();
    }

    public Invoker getModelInvoker(String fileName) {
        return modelInvokers.get(fileName);
    }

    public ThirdApi getThirdApi(String apiId) {
        return thirdApis.get(apiId);
    }

    protected MemoryFileSystem getMemoryFileSystem() {
        KieFileSystemImpl kfsi = (KieFileSystemImpl) kfs;
        return kfsi.asMemoryFileSystem();
    }

    public abstract void build() throws Exception;

    public Set<String> getGlobals() {
        return globals;
    }

    public Set<String> getFlowProcessIds() {
        return flowProcessIds;
    }

    public Set<String> getModelNames() {
        return modelNames;
    }

    public KieContainer getKieContainer() {
        return kc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isFlow() {
        return isFlow;
    }

    public void setFlow(boolean flow) {
        isFlow = flow;
    }

    public Set<RuleFactModel> getRuleFacts() {
        return ruleFacts;
    }
}
