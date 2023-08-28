package com.beagledata.gaea.ruleengine.function;

import com.beagledata.gaea.ruleengine.annotation.FunctionProperty;
import com.beagledata.gaea.ruleengine.annotation.FunctionMethodProperty;

import java.util.Set;

/**
 * Created by liulu on 2019/5/13.
 */
@FunctionProperty(name = "Set函数")
public class SetFunction {
    @FunctionMethodProperty(name = "求Set大小", params = {"集合"})
    public Integer size(Set set) {
        return set == null ? 0 : set.size();
    }

    @FunctionMethodProperty(name = "向Set中添加对象", params = {"集合", "对象"})
    public void add(Set set, Object obj) {
        if (set != null) {
            set.add(obj);
        }
    }

    @FunctionMethodProperty(name = "从Set中删除对象", params = {"集合", "对象"})
    public void remove(Set set, Object obj) {
        if (set != null) {
            set.remove(obj);
        }
    }
}
