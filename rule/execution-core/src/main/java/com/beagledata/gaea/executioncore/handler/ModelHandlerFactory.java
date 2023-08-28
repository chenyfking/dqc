package com.beagledata.gaea.executioncore.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liulu on 2020/5/15.
 */
public class ModelHandlerFactory {
    private static Map<String, ModelHandler> handlers = new ConcurrentHashMap<>();

    public static ModelHandler get(String id) {
        return handlers.get(id);
    }

    public static void register(String id, ModelHandler handler) {
        handlers.put(id, handler);
    }

    public static void remove(String id) {
        handlers.remove(id);
    }
}
