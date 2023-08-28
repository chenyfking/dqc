package com.beagledata.gaea.workbench.test.service;

import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.service.TestCaseService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by liulu on 2018/1/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class TestCaseServiceTests {

    @Autowired
    private TestCaseService testCaseService;
    /**
     * 查询ai列表
     * @author chenyafeng
     * @date 2018/12/6
     */
    @Test
    public void deletde() {
        testCaseService.delete("264de68e89a0467baf77e9e45b36b66d");
    }

}
