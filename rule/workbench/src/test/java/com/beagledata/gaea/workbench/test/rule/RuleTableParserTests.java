package com.beagledata.gaea.workbench.test.rule;

import com.beagledata.common.SpringBeanHolder;
import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.mapper.AssetsMapper;
import com.beagledata.gaea.workbench.rule.parser.RuleTableParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *@auto: yangyongqiang
 *@Description:决策树测试类
 *@Date: 2018-10-18 17:00
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class RuleTableParserTests {
    @Autowired
    private ApplicationContext ctx;
    @Autowired
    private AssetsMapper assetsMapper;

    @Before
    public void init() {
        SpringBeanHolder.initApplicationContext(ctx);
    }

    @Test
    public void test() {
        Assets assets = assetsMapper.selectByUuid("3d12f0df831d41c794fa0bf799792224");
        System.out.println(new RuleTableParser(assets).getDumper().dump());
    }
}
