package com.beagledata.gaea.workbench.rule.util;

import com.beagledata.common.SpringBeanHolder;
import com.beagledata.gaea.workbench.entity.AiModel;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.entity.FunctionDefinition;
import com.beagledata.gaea.workbench.entity.ThirdApiDefinition;
import com.beagledata.gaea.workbench.mapper.AiModelMapper;
import com.beagledata.gaea.workbench.mapper.AssetsMapper;
import com.beagledata.gaea.workbench.rule.define.Constant;
import com.beagledata.gaea.workbench.rule.define.Fact;
import com.beagledata.gaea.workbench.service.FunctionDefinitionService;
import com.beagledata.gaea.workbench.service.ThirdApiDefinitionService;
import com.beagledata.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liulu on 2020/4/8.
 */
public class AssetsCache {
    private static AssetsMapper assetsMapper = SpringBeanHolder.getBean(AssetsMapper.class);
    private static AiModelMapper aiModelMapper = SpringBeanHolder.getBean(AiModelMapper.class);
    private static FunctionDefinitionService functionDefinitionService = SpringBeanHolder.getBean(FunctionDefinitionService.class);
    private static ThirdApiDefinitionService thirdApiDefinitionService = SpringBeanHolder.getBean(ThirdApiDefinitionService.class);

    private static ThreadLocal<Map<String, Map<String, Optional<Object>>>> threadLocal
            = ThreadLocal.withInitial(() -> new ConcurrentHashMap<>());

    public static Fact getFactByUuid(String uuid) {
        Map<String, Optional<Object>> factCache = getCache("fact");
        Optional<Object> optional = factCache.get(uuid);
        if (optional == null) {
            optional = doGetFact(uuid);
            factCache.put(uuid, optional);
        }
        return (Fact) optional.orElse(null);
    }

    public static Fact getFactById(String id) {
        Map<String, Optional<Object>> factCache = getCache("fact");
        for (Optional<Object> fact : factCache.values()) {
            // 先直接在缓存里找，找到相同id的fact，直接返回
            if (fact.isPresent() && String.valueOf(((Fact) fact.get()).getId()).equals(id)) {
                return (Fact) fact.get();
            }
        }
        return getFactByUuid(id);
    }

    private static Map<String, Optional<Object>> getCache(String cacheName) {
        Map<String, Map<String, Optional<Object>>> cacheMap = threadLocal.get();
        Map<String, Optional<Object>> cache = cacheMap.get(cacheName);
        if (cache == null) {
            cache = new ConcurrentHashMap<>();
            cacheMap.put(cacheName, cache);
        }
        return cache;
    }

    private static Optional<Object> doGetFact(String key) {
        Assets assets;
        if (StringUtils.isBlank(key)) {
            return Optional.empty();
        }
        if (key.length() == 32) {
            assets = assetsMapper.selectByUuid(key);
        } else {
            assets = assetsMapper.selectById(Integer.parseInt(key));
        }

        if (assets != null) {
            return Optional.of(Fact.fromAssets(assets));
        }
        return Optional.empty();
    }

    public static Constant getConstantByUuid(String uuid) {
        Map<String, Optional<Object>> constantCache = getCache("constant");
        Optional<Object> optional = constantCache.get(uuid);
        if (optional == null) {
            optional = doGetConstant(uuid);
            constantCache.put(uuid, optional);
        }
        return (Constant) optional.orElse(null);
    }

    private static Optional<Object> doGetConstant(String uuid) {
        Assets assets = assetsMapper.selectByUuid(uuid);
        if (assets != null) {
            return Optional.of(Constant.fromAssets(assets));
        }
        return Optional.empty();
    }

    public static AiModel getAiModelByUuid(String uuid) {
        Map<String, Optional<Object>> modelCache = getCache("aimodel");
        Optional<Object> optional = modelCache.get(uuid);
        if (optional == null) {
            optional = doGetAiModel(uuid);
            modelCache.put(uuid, optional);
        }
        return (AiModel) optional.orElse(null);
    }
    
    private static Optional<Object> doGetAiModel(String uuid) {
        return Optional.ofNullable(aiModelMapper.selectByUuid(uuid));
    }

    public static FunctionDefinition getFuncByName(String name) {
        Map<String, Optional<Object>> funcCache = getCache("func");
        Optional<Object> optional = funcCache.get(name);
        if (optional == null) {
            optional = doGetFunc(name);
            funcCache.put(name, optional);
        }
        return (FunctionDefinition) optional.orElse(null);
    }

    public static FunctionDefinition getFuncByClassName(String name) {
        Map<String, Optional<Object>> funcCache = getCache("func");
        Optional<Object> optional = funcCache.get(name);
        if (optional == null) {
            optional = doGetFuncByClassName(name);
            funcCache.put(name, optional);
        }
        return (FunctionDefinition) optional.orElse(null);
    }

    private static Optional<Object> doGetFunc(String name) {
        return Optional.ofNullable(functionDefinitionService.getByName(name));
    }
    private static Optional<Object> doGetFuncByClassName(String className) {
        return Optional.ofNullable(functionDefinitionService.getByClassName(className));
    }

    public static ThirdApiDefinition getThirdApiByUuid(String uuid) {
        Map<String, Optional<Object>> apiCache = getCache("thirdapi");
        Optional<Object> optional = apiCache.get(uuid);
        if (optional == null) {
            optional = doGetThirdApi(uuid);
            apiCache.put(uuid, optional);
        }
        return (ThirdApiDefinition) optional.orElse(null);
    }

    private static Optional<Object> doGetThirdApi(String uuid) {
        return Optional.ofNullable(thirdApiDefinitionService.getByUuid(uuid));
    }

    public static void clear() {
        threadLocal.remove();
    }
}
