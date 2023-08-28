package com.beagledata.gaea.ruleengine.function;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.beagledata.gaea.ruleengine.annotation.FunctionMethodProperty;
import com.beagledata.gaea.ruleengine.annotation.FunctionProperty;

import java.util.List;

/**
 * Json函数
 */
@FunctionProperty(name = "序列化函数")
public class JsonFunction {

    @FunctionMethodProperty(name = "对象转为字符串", params = {"对象"})
    public String convert(Object object) {
        if (object == null) {
            return "";
        }
        return JSON.toJSONString(object, SerializerFeature.WriteMapNullValue);
    }

    @FunctionMethodProperty(name = "向字符串集合添加序列化对象", params = {"集合", "对象"})
    public void addToList(List list, Object object) {
        if (list != null && object != null) {
            list.add(JSON.toJSONString(object, SerializerFeature.WriteMapNullValue));
        }
    }

}
