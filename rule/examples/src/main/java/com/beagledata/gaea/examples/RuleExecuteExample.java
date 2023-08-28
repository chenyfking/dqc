package com.beagledata.gaea.examples;

import com.beagledata.gaea.ruleengine.runtime.RuleContainer;
import com.beagledata.gaea.ruleengine.runtime.RuleEngine;
import com.beagledata.gaea.ruleengine.runtime.RuleEngineFactory;

import java.io.File;

/**
 * 代码调用规则示例
 *
 * Created by liulu on 2020/5/8.
 */
public class RuleExecuteExample {
    public static void main(String[] args) {
        RuleContainer.loadRule(new File("D:/Desktop/pkg1022.zip")); // 加载文件系统上的知识包文件

        RuleEngine ruleEngine = RuleEngineFactory.getRuleEngine("1022"); // 填写知识包id, 获取规则引擎实例
        ruleEngine.put("3741", "name", "张三"); // 设置参数值, (数据模型id, 字段名称, 字段值)
        try {
            ruleEngine.execute(); // 调用知识包
        } catch (Exception e) {
            e.printStackTrace();
        }
        String welcome = ruleEngine.getString("3741", "welcome"); // 获取参数值, (数据模型id, 字段名称)
        System.out.println("欢迎词: " + welcome);
    }
}
