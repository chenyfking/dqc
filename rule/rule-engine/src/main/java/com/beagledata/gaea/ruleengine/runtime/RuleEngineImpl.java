package com.beagledata.gaea.ruleengine.runtime;

import com.alibaba.fastjson.JSON;
import com.beagledata.gaea.ruleengine.aimodel.AiModelHandler;
import com.beagledata.gaea.ruleengine.builder.AbstractRuleBuilder;
import com.beagledata.gaea.ruleengine.exception.RuleException;
import com.beagledata.gaea.ruleengine.function.jdbc.JdbcTemplateProxy;
import com.beagledata.gaea.ruleengine.function.jdbc.JdbcTemplateProxyImpl;
import com.beagledata.gaea.ruleengine.model.RuleFactModel;
import com.beagledata.gaea.ruleengine.runtime.flow.PercentageDecisionHandler;
import com.beagledata.gaea.ruleengine.runtime.flow.TraceProcessEventListener;
import com.beagledata.gaea.ruleengine.thirdapi.ThirdApiHandler;
import com.beagledata.gaea.ruleengine.util.PropertyUtils;
import com.beagledata.util.EncodeUtils;
import com.google.common.base.Stopwatch;
import org.drools.core.SessionConfiguration;
import org.drools.core.common.InternalAgenda;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.common.PhreakWorkingMemoryFactory;
import org.drools.core.common.WorkingMemoryFactory;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.drools.core.spi.FactHandleFactory;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by liulu on 2020/5/8.
 */
public class RuleEngineImpl implements RuleEngine {
    private static Logger logger = LoggerFactory.getLogger(RuleEngineImpl.class);

    /**
     * 知识包编译实例
     */
    private AbstractRuleBuilder builder;
    /**
     * 每个数据模型对象要赋值的字段
     */
    private Map<String, RuleFact> facts = new HashMap<>();
    /**
     * 执行结果
     */
    private ExecutionResult executionResult;

    static {
        //替换kieSession的fireAllRule方法，使用默认调用的DefaultAgendaFilter
        WorkingMemoryFactory workingMemoryFactory = new WorkingMemoryFactory() {
            @Override
            public InternalWorkingMemory createWorkingMemory(long id, InternalKnowledgeBase kBase, SessionConfiguration config, Environment environment) {
                InternalWorkingMemory cachedWm = kBase.getCachedSession(config, environment);
                if (cachedWm != null) {
                    return cachedWm;
                }
                if (kBase.hasUnits()) {
                    throw  new IllegalStateException("Cannot create a KieSession against a KieBase with units");
                }
                return new StatefulKnowledgeSessionImpl(id, kBase, true, config, environment){
                    @Override
                    public int fireAllRules() {
                        ExecutionResult executionResult = (ExecutionResult) this.getGlobal("$ExecutionResult");
                        if (executionResult == null) {
                            executionResult =  new ExecutionResult();
                        }
                        return this.fireAllRules(new DefaultAgendaFilter(executionResult));
                    }
                };
            }

            @Override
            public InternalWorkingMemory createWorkingMemory(long id, InternalKnowledgeBase kBase, FactHandleFactory handleFactory, long propagationContext, SessionConfiguration config, InternalAgenda agenda, Environment environment) {
                if (kBase.hasUnits()) {
                    throw  new IllegalStateException("Cannot create a KieSession against a KieBase with units");
                }
                return new StatefulKnowledgeSessionImpl(id, kBase, true, config, environment){
                    @Override
                    public int fireAllRules() {
                        ExecutionResult executionResult = (ExecutionResult) this.getGlobal("$ExecutionResult");
                        if (executionResult == null) {
                            executionResult =  new ExecutionResult();
                        }
                        return this.fireAllRules(new DefaultAgendaFilter(executionResult));
                    }
                };
            }
        };
        try {
            //替换PhreakWorkingMemoryFactory 里的INSTANCE 属性
            Field field = PhreakWorkingMemoryFactory.class.getDeclaredField("INSTANCE");
            field.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            modifiersField.setAccessible(false);

            field.set(null, workingMemoryFactory);
            field.setAccessible(false);

        } catch (Exception e){
            logger.error("替换KieSession的fireAllRules方法失败", e);
            throw new RuleException("替换KieSession的fireAllRules方法失败");
        }
    }
    public RuleEngineImpl() {
    }

    public RuleEngineImpl(String id) {
        builder = RuleContainer.getRule(id);
        if (builder == null) {
            throw new RuleException(String.format("知识包[%s]找不到", id));
        }

        collectObjects();
    }

    @Override
    public void put(String id, String name, Object value) {
        RuleFact fact = getFact(id);
        if (fact != null) {
            try {
                if (value instanceof Map) {
                    value = JSON.toJSONString(value);
                }
                PropertyUtils.setProperty(fact.getObj(), name, value);
            } catch (NoSuchFieldException e) {
                logger.warn("设置数据模型的字段值出错, 字段不存在. id: {}, name: {}, value: {}", id, name, value);
            } catch (Exception e) {
                logger.error("设置数据模型的字段值出错. id: {}, name: {}, value: {}", id, name, value, e);
                throw new RuleException("参数[" + id + "." + name + "]错误");
            }
        }
    }

    @Override
    public void put(String name, Object value) {
        for (Map.Entry<String, RuleFact> me : facts.entrySet()) {
            if (me.getValue().getFields().containsKey(name)) {
                try {
                    if (value instanceof Map) {
                        value = JSON.toJSONString(value);
                    }
                    PropertyUtils.setProperty(me.getValue().getObj(), name, value);
                } catch (Exception e) {
                    logger.error("设置数据模型的字段值出错, id: {}, name: {}, value: {}", me.getValue().getId(), name, value, e);
                    throw new RuleException("参数[" + me.getValue().getId() + "." + name + "]错误");
                }
                break;
            }
        }
    }

    private RuleFact getFact(String id) {
        RuleFact fact = facts.get(id);
        if (fact == null) {
            RuleFactModel factModel = builder.getFact(id);
            if (factModel != null) {
                fact = new RuleFact(id, factModel.getClassName());
                facts.put(id, fact);
            }
        }
        return fact;
    }

    @Override
    public ExecutionResult execute() throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        KieSession kieSession = builder.getKieContainer().newKieSession();
        initFunctions(kieSession); // 初始化内置函数
        insertObjects(kieSession); // 插入对象到workmemory
        fireAllRules(kieSession); // 执行所有规则
        executionResult.setExecutionTime(stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return executionResult;
    }

    @Override
    public Object getObject(String id) {
        RuleFact fact = getFact(id);
        if (fact == null) {
            return null;
        }
        return doGetObject(fact);
    }

    private Object doGetObject(RuleFact fact) {
        try {
            Object obj = fact.getObj();
            if (obj == null) {
                obj = builder.newObject(fact.getId());
                fact.setObj(obj);
            }
            return obj;
        } catch (Exception e) {
            logger.warn("获取数据模型实例失败: [{}]", fact.getId());
            logger.warn(e.getLocalizedMessage(), e);
        }
        return null;
    }

    @Override
    public List<Object> getObjects() {
        List<Object> objs = new ArrayList<>(facts.size());
        facts.forEach((k, v) -> objs.add(doGetObject(v)));
        return objs;
    }

    @Override
    public Map<String, Object> getObjectMap() {
        Map<String, Object> map = new HashMap<>();
        facts.forEach((k, v) -> map.put(k, doGetObject(v)));
        return map;
    }

    @Override
    public String getString(String id, String name) {
        try {
            return get(id, name, String.class);
        } catch (Exception e) {
            logger.warn("获取数据模型String类型字段值出错, id: [{}], name: [{}]", id, name);
            logger.warn(e.getLocalizedMessage(), e);
        }
        return null;
    }

    @Override
    public int getInt(String id, String name) {
        try {
            return get(id, name, Integer.class);
        } catch (Exception e) {
            logger.warn("获取数据模型int类型字段值出错, id: [{}], name: [{}]", id, name);
            logger.warn(e.getLocalizedMessage(), e);
        }
        return 0;
    }

    @Override
    public double getDouble(String id, String name) {
        try {
            return get(id, name, Double.class);
        } catch (Exception e) {
            logger.warn("获取数据模型double类型字段值出错, id: [{}], name: [{}]", id, name);
            logger.warn(e.getLocalizedMessage(), e);
        }
        return 0;
    }

    @Override
    public long getLong(String id, String name) {
        try {
            return get(id, name, Long.class);
        } catch (Exception e) {
            logger.warn("获取数据模型long类型字段值出错, id: [{}], name: [{}]", id, name);
            logger.warn(e.getLocalizedMessage(), e);
        }
        return 0;
    }

    @Override
    public BigDecimal getBigDecimal(String id, String name) {
        try {
            return get(id, name, BigDecimal.class);
        } catch (Exception e) {
            logger.warn("获取数据模型BigDecimal类型字段值出错, id: [{}], name: [{}]", id, name);
            logger.warn(e.getLocalizedMessage(), e);
        }
        return new BigDecimal(0);
    }

    @Override
    public boolean getBoolean(String id, String name) {
        try {
            return get(id, name, Boolean.class);
        } catch (Exception e) {
            logger.warn("获取数据模型boolean类型字段值出错, id: [{}], name: [{}]", id, name);
            logger.warn(e.getLocalizedMessage(), e);
        }
        return false;
    }

    @Override
    public Date getDate(String id, String name) {
        try {
            return get(id, name, Date.class);
        } catch (Exception e) {
            logger.warn("获取数据模型Date类型字段值出错, id: [{}], name: [{}]", id, name);
            logger.warn(e.getLocalizedMessage(), e);
        }
        return null;
    }

    @Override
    public Object getObject(String id, String name) {
        try {
            return get(id, name);
        } catch (Exception e) {
            logger.warn("获取数据模型Object类型字段值出错, id: [{}], name: [{}]", id, name);
            logger.warn(e.getLocalizedMessage(), e);
        }
        return null;
    }

    @Override
    public List<String> getFiredRules() {
        if (executionResult == null) {
            return Collections.emptyList();
        }

        return executionResult.getFireRules();
    }

    private Object get(String id, String name) throws IllegalAccessException, IntrospectionException, InvocationTargetException {
        Object obj = getObject(id);
        if (obj != null) {
            return PropertyUtils.getProperty(obj, name);
        }
        return null;
    }

    private <T> T get(String id, String name, Class clazz) throws NoSuchFieldException, IllegalAccessException {
        Object obj = getObject(id);
        if (obj != null) {
            return (T) PropertyUtils.getProperty(obj, name, clazz);
        }
        return null;
    }

    private void fireAllRules(KieSession kieSession) throws Exception {
        try {
            RuleContext.setCurrentContext(new RuleContext(builder));
            if (!builder.getFlowProcessIds().isEmpty()) {
                kieSession.addEventListener(new TraceProcessEventListener());
                for (String flow : builder.getFlowProcessIds()) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("PercentageDecisionHandler", new PercentageDecisionHandler());
                    kieSession.startProcess(flow, params);
                }
            }
            kieSession.fireAllRules();
            executionResult = (ExecutionResult) kieSession.getGlobal("$ExecutionResult");
            if (executionResult == null) {
                executionResult = (ExecutionResult) builder.loadClass(ExecutionResult.class.getName()).newInstance();
            }
        } finally {
            kieSession.dispose();
            RuleContext.removeCurrentContext();
        }
    }

    public RuleException getRuleException(Throwable cause){
        if (cause instanceof  InvocationTargetException) {
            InvocationTargetException exception = (InvocationTargetException) cause;
            Throwable taThroeable = exception.getTargetException();
            if (taThroeable instanceof RuleException) {
                return (RuleException) taThroeable;
            } else {
                return null;
            }
        } else {
            Throwable throwable = cause.getCause();
            if (throwable == null) {
                return null;
            } else {
                return getRuleException(throwable);
            }
        }
    }

    private void initFunctions(KieSession kieSession) {
        try {
            for (String func : builder.getGlobals()) {
                Class funcClass = builder.loadClass(func);
                Object funcObj = funcClass.newInstance();
                injectProxy(funcObj);
                if (ExecutionResult.class.getName().equals(func)
                        || AiModelHandler.class.getName().equals(func)
                        || ThirdApiHandler.class.getName().equals(func)) {
                    kieSession.setGlobal("$" + funcClass.getSimpleName(), funcObj);
                } else {
                    kieSession.setGlobal("$" + EncodeUtils.encodeMD516(func), funcObj);
                }
            }
        } catch (Exception e) {
            logger.error("初始化内置函数出错", e);
            throw new RuleException("初始化内置函数出错");
        }
    }

    /**
    * 描述: 自定义函数注入代理类
    * @param: [funcObj]
    * @author: 周庚新
    * @date: 2020/9/16
    * @return: void
    *
    */
    private void injectProxy(Object funcObj) throws IllegalArgumentException, IllegalAccessException {
        for (Field field : funcObj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (JdbcTemplateProxy.class.equals(field.getType())) {
                field.set(funcObj, new JdbcTemplateProxyImpl(RuleContext.getDataSource()));
            }
        }
    }

    private void insertObjects(KieSession kieSession) throws IllegalAccessException {
        for (Map.Entry<String, RuleFact> me : facts.entrySet()) {
            insertObject(kieSession, me.getValue().getObj());
        }
    }

    private void insertObject(KieSession kieSession, Object obj) throws IllegalAccessException {
        kieSession.insert(obj);
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getType().getSimpleName().startsWith("Fact_")) {
                insertObject(kieSession, field.get(obj));
            }
        }
    }

    private void collectObjects() {
        for (RuleFactModel factModel : builder.getRuleFacts()) {
            try {
                RuleFact fact = getFact(factModel.getId());
                doGetObject(fact);
            } catch (Exception e) {
                logger.warn("初始化数据模型出错: [{}]", factModel.getId());
                logger.warn(e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    public ExecutionResult getExecutionResult() {
        return executionResult;
    }
}
