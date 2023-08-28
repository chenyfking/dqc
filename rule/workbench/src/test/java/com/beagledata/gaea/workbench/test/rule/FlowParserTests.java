package com.beagledata.gaea.workbench.test.rule;

import com.beagledata.common.SpringBeanHolder;
import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.mapper.AssetsMapper;
import com.beagledata.gaea.workbench.rule.parser.FlowParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by liulu on 2018/11/2.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class FlowParserTests {
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
        Assets assets = assetsMapper.selectByUuid("5bb341e995454d24bc9ffacd59a8c2b1");
        String flow = new FlowParser(assets).getDumper().dump();
        System.out.println(flow);
    }
}
