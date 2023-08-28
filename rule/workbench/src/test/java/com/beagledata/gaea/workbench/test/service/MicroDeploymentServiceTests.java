package com.beagledata.gaea.workbench.test.service;

import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.entity.*;
import com.beagledata.gaea.workbench.service.MicroDeploymentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author yangyongqiang
 * @Date 5:09 下午 2020/7/14
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class MicroDeploymentServiceTests {
    @Autowired
    private MicroDeploymentService microDeploymentService;


    @Test
    public void listAll() {
       // System.out.println(microDeploymentService.listAll("bad4fb9029bf4530aec9a5e320563964","a22f6ec4bf224756b2f15918a225e42b"));
    }
}
