package com.beagledata.gaea.workbench.test.rule;

import com.beagledata.common.SpringBeanHolder;
import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.mapper.AssetsMapper;
import com.beagledata.gaea.workbench.rule.parser.RuleTreeParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 决策树测试
 * @author chenyafeng
 * @date 2018/10/24
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class RuleTreeParserTests {
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
        Assets assets = assetsMapper.selectByUuid("a9dae3bae0634e70b40f41a7a9b07c26");
        String rule = new RuleTreeParser(assets).getDumper().dump();
        System.out.println(rule);
    }
}
