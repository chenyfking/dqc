package com.beagledata.gaea.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liulu on 2017/11/9.
 */
public class LogManager {
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}
