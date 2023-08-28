package com.beagledata.gaea.workbench.rule.parser;

import com.beagledata.gaea.workbench.rule.define.Dumper;

/**
 * 解析前端json，生成drools的规则文件
 *
 * Created by liulu on 2018/9/30.
 */
public interface Parser<T extends Dumper> {
    /**
     * @author liulu
     * 2018/10/16 下午 12:16
     */
    T getDumper();
}
