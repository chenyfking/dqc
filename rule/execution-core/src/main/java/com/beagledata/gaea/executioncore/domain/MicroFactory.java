package com.beagledata.gaea.executioncore.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liulu on 2020/7/14.
 */
public class MicroFactory {
    private static final Map<String, Micro> micros = new ConcurrentHashMap<>();

    public static Micro get(String id) {
        return micros.get(id);
    }

    public static void register(Micro micro) {
        micros.put(micro.getId(), micro);
    }

    public static void remove(String id) {
        micros.remove(id);
    }
}
