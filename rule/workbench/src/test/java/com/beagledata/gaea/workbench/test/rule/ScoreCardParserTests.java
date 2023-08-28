package com.beagledata.gaea.workbench.test.rule;

import com.beagledata.common.SpringBeanHolder;
import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.mapper.AssetsMapper;
import com.beagledata.gaea.workbench.rule.parser.ScorecardParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by mahongfei on 2018/10/16.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class ScoreCardParserTests {
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
        Assets assets = assetsMapper.selectByUuid("94cb4fd1e677494ca57cd05462326ce0");
        String rule = new ScorecardParser(assets).getDumper().dump();
        System.out.println(rule);
    }
}
