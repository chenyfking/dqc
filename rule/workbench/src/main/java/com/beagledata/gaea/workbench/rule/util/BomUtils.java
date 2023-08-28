package com.beagledata.gaea.workbench.rule.util;

import com.beagledata.common.SpringBeanHolder;
import com.beagledata.gaea.workbench.common.AssetsType;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.mapper.AssetsMapper;
import com.beagledata.gaea.workbench.rule.define.Constant;
import com.beagledata.gaea.workbench.rule.define.Fact;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liulu on 2018/12/11.
 */
public class BomUtils {
    private static AssetsMapper assetsMapper = SpringBeanHolder.getBean(AssetsMapper.class);
    private static Map<String, Fact> factCache = new HashMap<>();
    private static Map<Integer, Fact> factIdCache = new HashMap<>();
    private static Map<String, Constant> constantCache = new HashMap<>();

    public static Fact getFactByUuid(String uuid) {
        if (StringUtils.isBlank(uuid))
            return null;
        Fact fact = factCache.get(uuid);
        if (fact == null) {
            Assets assets = assetsMapper.selectByUuid(uuid);
            if (assets != null && AssetsType.FACT.equals(assets.getType())) {
                fact = Fact.fromAssets(assets);
                factCache.put(uuid, fact);
            }
        }
        return fact;
    }

    public static Constant getConstantByUuid(String uuid) {
        if (StringUtils.isBlank(uuid))
            return null;
        Constant constant = constantCache.get(uuid);
        if (constant == null) {
            Assets assets = assetsMapper.selectByUuid(uuid);
            if (assets != null && AssetsType.CONSTANT.equals(assets.getType())) {
                constant = Constant.fromAssets(assets);
                constantCache.put(uuid, constant);
            }
        }
        return constant;
    }

    public static Fact getFactById(int id) {
        if (id <= 0)
            return null;
        Fact fact  = factIdCache.get(id);
        if (fact == null) {
            Assets assets = assetsMapper.selectById(id);
            if (assets != null && AssetsType.FACT.equals(assets.getType())) {
                fact = Fact.fromAssets(assets);
                factIdCache.put(id, fact);
            }
        }
        return fact;
    }

    public static void removeFact(String uuid) {
        if (null != uuid) {
            factCache.put(uuid, null);
        }
    }

    public static void removeConstant(String uuid) {
        if (null != uuid) {
            constantCache.put(uuid, null);
        }
    }
}
