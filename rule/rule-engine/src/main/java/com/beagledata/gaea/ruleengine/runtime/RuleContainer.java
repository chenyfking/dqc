package com.beagledata.gaea.ruleengine.runtime;

import com.beagledata.gaea.ruleengine.builder.AbstractRuleBuilder;
import com.beagledata.gaea.ruleengine.builder.ZipRuleBuilder;
import com.beagledata.gaea.ruleengine.exception.RuleException;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 规则容器
 * 在一个EXCUTION SERVER里一个规则容器，容器里缓存多个知识包编译实例，知识包从文件系统加载
 *
 * Created by liulu on 2020/5/8.
 */
public class RuleContainer {
    private static Logger logger = LoggerFactory.getLogger(RuleContainer.class);

    /**
     * 全局唯一的容器实例
     */
    protected static AtomicReference<RuleContainer> INSTANCE = new AtomicReference<>();
    protected static final ThreadLocal<RuleContainer> threadLocal = ThreadLocal.withInitial(() -> {
        while (true) {
            RuleContainer instance = INSTANCE.get();
            if (instance != null) {
                return instance;
            }

            instance = new RuleContainer();
            if (INSTANCE.compareAndSet(null, instance)) {
                return instance;
            }
        }
    });

    /**
     * 知识包编译实例缓存
     */
    private Map<String, AbstractRuleBuilder> builders = new ConcurrentHashMap<>();

    /**
     * 加载知识包文件，编译后缓存在容器里
     *
     * @param file 知识包文件
     * @return build id
     */
    public static String loadRule(File file) {
        try {
            AbstractRuleBuilder builder = ZipRuleBuilder.newBuilder(file);
            return loadRule(builder);
        } catch (Exception e) {
            throw new RuleException("加载知识包出错", e);
        }
    }

    protected static String loadRule(AbstractRuleBuilder builder) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            builder.buildAll();
            getCurrentRuleContainer().putBuilder(builder);
            logger.info("加载知识包成功, 知识包id: {}, 耗时：{}ms", builder.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return builder.getId();
        } catch (Exception e) {
            throw new RuleException("加载知识包出错", e);
        } finally {
            stopwatch.stop();
        }
    }

    /**
     * 根据知识包id获取知识包编译实例
     *
     * @param id
     * @return
     */
    public static AbstractRuleBuilder getRule(String id) {
        return getCurrentRuleContainer().getBuilder(id);
    }

    /**
     * 删除知识包，释放资源
     *
     * @param id
     */
    public static void removeRule(String id) {
        AbstractRuleBuilder ruleBuilder = getRule(id);
        if (ruleBuilder != null) {
            ruleBuilder.getKieContainer().dispose();
        }
    }

    private static RuleContainer getCurrentRuleContainer() {
        return threadLocal.get();
    }

    protected void putBuilder(AbstractRuleBuilder builder) {
        this.builders.put(builder.getId(), builder);
    }

    protected AbstractRuleBuilder getBuilder(String id) {
        return this.builders.get(id);
    }
}
