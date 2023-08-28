package com.beagledata.gaea.ruleengine.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by liulu on 2020/7/24.
 */
public class ClassUtils {
    private static Method addURL;

    static {
        try {
            addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);
        } catch (NoSuchMethodException ignore) {
        }
    }

    /**
     * 加载jar包到ClassLoader
     *
     * @param classLoader
     * @param jarFile
     * @throws MalformedURLException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void loadJar(ClassLoader classLoader, File jarFile) throws MalformedURLException, InvocationTargetException, IllegalAccessException {
        addURL.invoke(classLoader, jarFile.toURI().toURL());
    }
}
