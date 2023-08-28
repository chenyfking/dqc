package com.beagledata.gaea.ruleengine.aimodel;

import com.alibaba.fastjson.JSON;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 模型文件的元数据
 *
 * Created by liulu on 2020/5/15.
 */
public class ModelMetadata {
    private static final String PARAM_CLASS_PREFIX = "NamesHolder_";
    private static final String PARAM_CLASS_FIELD = "VALUES";

    private URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();

    /**
     * 模型文件
     */
    private File file;

    public ModelMetadata(File file) {
        this.file = file;
    }

    /**
     * 解析模型入参JSON串，格式：{name: type}
     *
     * @return
     * @throws IllegalAccessException
     * @throws IOException
     * @throws NoSuchFieldException
     * @throws DocumentException
     * @throws InvocationTargetException
     * @throws ClassNotFoundException
     */
    public String getParamsJson() throws IllegalAccessException, IOException, NoSuchFieldException, DocumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException {
        return JSON.toJSONString(getParams());
    }

    public Map<String, Class> getParams() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, IOException, DocumentException, NoSuchMethodException, InvocationTargetException {
        return extractFile();
    }

    /**
     * 获取参数默认值
     *
     * @param clazz
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public Object getParamDefaultValue(Class clazz) throws IllegalAccessException, InstantiationException {
        if (Integer.class.equals(clazz) || Long.class.equals(clazz) || Short.class.equals(clazz)) {
            return 0;
        } else if (Double.class.equals(clazz) || Float.class.equals(clazz)) {
            return 0.0;
        } else if (Boolean.class.equals(clazz)) {
            return false;
        }
        return clazz.newInstance();
    }

    public String getClassName() throws IOException {
        if (!file.getName().endsWith(".jar")) {
            throw new UnsupportedOperationException("不支持获取ClassName");
        }

        String namesHolderClassName = getNamesHolderClassName();
        return namesHolderClassName.substring(PARAM_CLASS_PREFIX.length());
    }

    private Map<String, Class> extractFile() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, IOException, DocumentException, NoSuchMethodException, InvocationTargetException {
        if (file.getName().endsWith(".jar")) {
            return extractJar();
        }
        return extractPmml();
    }

    /**
     * 从MaximAI平台jar模型中解析参数
     * jar中在特定类中存储参数字段信息，类名是 "NamesHolder_" + jarName
     * 模型参数在类中是静态的字符串数组，数组名称是 VALUES
     *
     * @throws IOException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws NoSuchFieldException
     */
    private Map<String, Class> extractJar() throws IOException, IllegalAccessException, ClassNotFoundException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException {
        // zip方式打开jar包，找到NamesHolder_前缀的参数类
        String namesHolderClassName = getNamesHolderClassName();

        loadJar();
        Class clazz = classLoader.loadClass(namesHolderClassName);
        Field paramField = clazz.getField(PARAM_CLASS_FIELD);
        paramField.setAccessible(true);
        Object obj = paramField.get(clazz);
        String[] values = (String[]) obj;
        Map<String, Class> params = new HashMap<>(values.length);
        for (String param : values) {
            params.put(param, String.class);
        }
        return params;
    }

    private Map<String, Class> extractPmml() throws DocumentException {
        Map<String, Class> params = new HashMap<>();
        Document document = new SAXReader().read(file);
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
                params.put(name, castType(type));
            }
        }
        return params;
    }

    private Class castType(String type) {
        if ("integer".equalsIgnoreCase(type)) {
            return Integer.class;
        }
        if ("double".equalsIgnoreCase(type)) {
            return Double.class;
        }
        return String.class;
    }

    private String getNamesHolderClassName() throws IOException {
        try (ZipFile zipFile = new ZipFile(file)) {
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

    private void loadJar() throws NoSuchMethodException, MalformedURLException, InvocationTargetException, IllegalAccessException {
        Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        addURL.setAccessible(true);
        addURL.invoke(classLoader, file.toURI().toURL());
    }
}
