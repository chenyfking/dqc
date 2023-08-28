package com.beagledata.gaea.ruleengine.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.beagledata.gaea.ruleengine.common.Constants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

/**
 * Created by liulu on 2019/5/21.
 */
public class PropertyUtils {
    public static Map<String, Object> getProperties(Object bean) {
        if (bean instanceof Map) {
            return (Map) bean;
        }

        Map<String, Object> fieldMap = new HashMap<>();
        for (Field field : bean.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(bean);
                if (value != null) {
                    if (value instanceof BigDecimal) {
                        BigDecimal bd = (BigDecimal) value;
                        if (bd.scale() > Constants.BIGDECIMAL_SCALE) {
                            value = bd.setScale(Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND);
                        }
                    }
                    fieldMap.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
            }
            field.setAccessible(false);
        }
        return fieldMap;
    }

    /**
     * 过滤JsonIgnore注解的属性
     *
     * @param bean
     * @param jsonIgnore
     * @return
     * @throws IllegalAccessException
     */
    public static Map<String, Object> getProperties(Object bean, boolean jsonIgnore) {
        if (bean instanceof Map) {
            return (Map) bean;
        }

        if (!jsonIgnore) {
            return getProperties(bean);
        }

        Map<String, Object> fieldMap = new HashMap<>();
        for (Field field : bean.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (!field.isAnnotationPresent(JsonIgnore.class)) {
                try {
                    Object value = field.get(bean);
                    if (value != null) {
                        fieldMap.put(field.getName(), value);
                    }
                } catch (IllegalAccessException e) {
                }
            }
        }
        return fieldMap;
    }

    public static Object getProperty(Object bean, String name) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        PropertyDescriptor pd = new PropertyDescriptor(name, bean.getClass());
        Method readMethod = pd.getReadMethod(); //获得读方法
        return readMethod.invoke(bean);
    }

    public static <T> T getProperty(Object bean, String name, Class<T> clazz) throws NoSuchFieldException, IllegalAccessException {
        Field field = getField(bean, name);
        Object value = null;
        try {
            value = field.get(bean);
        } catch (IllegalAccessException e) {
        }
        return clazz.cast(value);
    }

    public static Object setProperties(Object oldBean, Object newBean) {
        if (newBean == null) {
           return oldBean;
        }
        String json;
        if (newBean instanceof String) {
            json = (String) newBean;
        } else {
            json = JSON.toJSONString(newBean);
        }
        return setProperties(oldBean, json);
    }

    public static Object setProperties(Object bean, String json) {
        if (StringUtils.isBlank(json)) {
            return bean;
        }
        DefaultJSONParser parser = new DefaultJSONParser(json);
        try {
            parser.parseObject(bean);
            return bean;
        } finally {
            parser.close();
        }
    }

    /**
    * 描述: 设置list字段值
    * @param: [bean, listFieldName, listValue]
    * @author: 周庚新
    * @date: 2020/9/16 
    * @return: java.lang.Object
    * 
    */
    public static Object setProperties(Object bean, String listFieldName, List listValue) {
        if (listValue == null) {
            return new ArrayList<>();
        }
        try {
            Field field = getField(bean, listFieldName);
            return getListValue(field, JSON.toJSONString(listValue));
        } catch (NoSuchFieldException e){

        }
        return new ArrayList<>();
    }

    /**
     * 支持String类型和Object类型的值
     *
     * @param bean
     * @param name
     * @param value
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws ParseException
     */
    public static void setProperty(Object bean, String name, Object value) throws NoSuchFieldException, IllegalAccessException, ParseException {

        Field field = getField(bean, name);
        if (value == null) {
            field.set(bean, null);
            return;
        }
        Class typeClass = field.getType();
        if (value instanceof String) {
            String strValue = (String) value;
            if (StringUtils.isBlank(strValue)) {
                return;
            }

            if (int.class.equals(typeClass) || Integer.class.equals(typeClass)) {
                value = Integer.valueOf(strValue);
            } else if (double.class.equals(typeClass) || Double.class.equals(typeClass)) {
                value = Double.valueOf(strValue);
            }  else if (long.class.equals(typeClass) || Long.class.equals(typeClass)) {
                value = Long.valueOf(strValue);
            } else if (BigDecimal.class.equals(typeClass)) {
                value = new BigDecimal(strValue);
            } else if (boolean.class.equals(typeClass) || Boolean.class.equals(typeClass)) {
                value = Boolean.valueOf(strValue);
            } else if (Date.class.equals(typeClass)) {
                if (strValue.contains("-")) {
                    value = DateUtils.parseDate(strValue, Constants.DEFAULT_DROOLS_DATE_FORMAT);
                } else {
                    value = new Date(Long.parseLong(strValue));
                }
            } else if (List.class.equals(typeClass)) {
                value = getListValue(field, strValue);
            } else if (Set.class.equals(typeClass)) {
                value = getSetValue(field, strValue);
            } else if (typeClass.getSimpleName().startsWith("Fact_")) {
                value = JSON.parseObject(strValue, typeClass);
            }
        } else if ( BigDecimal.class.equals(typeClass) && !BigDecimal.class.equals(value.getClass())) {
            //字段是BigDecimal类型，但参数不是，强制转换
            value = new BigDecimal(String.valueOf(value));
        } else if (List.class.isAssignableFrom(typeClass)) {
            value = getListValue(field, JSON.toJSONString(value));
        } else if (Set.class.isAssignableFrom(typeClass)) {
            value = getSetValue(field, JSON.toJSONString(value));
        }

        field.set(bean, value);
    }

    private static Field getField(Object bean, String name) throws NoSuchFieldException {
        Field field = bean.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static List<?> getListValue(Field field, String value) {
        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
        Class elementClass = (Class) genericType.getActualTypeArguments()[0];
        return JSON.parseArray(value, elementClass);
    }

    private static Set<?> getSetValue(Field field, String value) {
        return new HashSet<>(getListValue(field, value));
    }
}
