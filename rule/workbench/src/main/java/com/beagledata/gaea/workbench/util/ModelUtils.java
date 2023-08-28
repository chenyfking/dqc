package com.beagledata.gaea.workbench.util;

import com.alibaba.fastjson.JSON;
import com.beagledata.gaea.common.LogManager;
import javafx.util.Pair;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 模型工具类，解析模型入参
 *
 * Created by Cyf on 2019/7/8
 **/
public class ModelUtils {
    private static Logger logger = LogManager.getLogger(ModelUtils.class);

    private static URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    private static Method addURL = null;
    private static final String PARAM_CLASS_PREFIX = "NamesHolder_";
    private static final String PARAM_CLASS_FIELD = "VALUES";

    static {
        try {
            addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);
        } catch (NoSuchMethodException ignore) {
        }
    }

    /**
     * 解析模型入参JSON串，格式：{name: type}
     *
     * @param modelFile
     * @return
     * @throws IllegalAccessException
     * @throws IOException
     * @throws NoSuchFieldException
     * @throws DocumentException
     * @throws InvocationTargetException
     * @throws ClassNotFoundException
     */
    public static String getParamsJsonFromModelFile(File modelFile) throws IllegalAccessException, IOException, NoSuchFieldException, DocumentException, InvocationTargetException, ClassNotFoundException {
        Map<String, String> paramMap = new HashMap<>();
        getParamsListFromModelFile(modelFile).forEach(p -> paramMap.put(p.getKey(), p.getValue()));
        return JSON.toJSONString(paramMap);
    }

    /**
     * 解析模型入参集合
     *
     * @param modelFile
     * @return
     * @throws InvocationTargetException
     * @throws ClassNotFoundException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IOException
     * @throws DocumentException
     */
    public static List<Pair<String, String>> getParamsListFromModelFile(File modelFile) throws InvocationTargetException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException, IOException, DocumentException {
        if (modelFile.getName().endsWith(".jar")) {
            return getParamsFromJar(modelFile);
        }
        if (modelFile.getName().endsWith(".pmml") || modelFile.getName().endsWith(".xml")) {
            return getParamsFromPmml(modelFile);
        }

        logger.warn("不支持的模型文件: {}", modelFile);
        return Collections.emptyList();
    }

    /**
     * 从MaximAI平台jar模型中解析参数
     * jar中在特定类中存储参数字段信息，类名是 "NamesHolder_" + jarName
     * 模型参数在类中是静态的字符串数组，数组名称是 VALUES
     *
     * @param jarFile
     * @return
     * @throws IOException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws NoSuchFieldException
     */
    private static List<Pair<String, String>> getParamsFromJar(File jarFile) throws IOException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchFieldException {
        // zip方式打开jar包，找到NamesHolder_前缀的参数类
        String namesHolderClassName = getNamesHolderClassName(jarFile);
        if (namesHolderClassName == null) {
            logger.warn("没有找到参数类: {}", jarFile);
            return Collections.emptyList();
        }

        addURL.invoke(classLoader, jarFile.toURI().toURL());
        Class clazz = classLoader.loadClass(namesHolderClassName);
        Field paramField = clazz.getField(PARAM_CLASS_FIELD);
        paramField.setAccessible(true);
        Object obj = paramField.get(clazz);
        String[] params = (String[])obj;
        return Arrays.stream(params).map(p -> new Pair<>(p, "string")).collect(Collectors.toList());
    }

    private static List<Pair<String, String>> getParamsFromPmml(File pmmlFile) throws DocumentException {
        List<Pair<String, String>> params = new ArrayList<>();
        Document document = new SAXReader().read(pmmlFile);
        Element root = document.getRootElement();
        List<Element> elements = root.elements() ;
        for (Element e : elements) {
            if (!e.getName().equals("DataDictionary")) {
                continue;
            }

            Element dataDic = root.element("DataDictionary");
            List<Element> items = dataDic.elements();
            if (items.isEmpty()) {
                continue;
            }

            for (Element item : items) {
                String name = item.attributeValue("name");
                String type = item.attributeValue("dataType");
                params.add(new Pair(name, type));
            }
        }
        return params;
    }

    private static String getNamesHolderClassName(File jarFile) throws IOException {
        try (ZipFile zipFile = new ZipFile(jarFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(PARAM_CLASS_PREFIX)
                        && !name.contains("$")
                        && name.endsWith(".class")) {
                    return name.substring(0, name.length() - 6);
                }
            }
        }
        return null;
    }
}