package com.beagledata.gaea.workbench.test.service;

import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.entity.KnowledgePackage;
import com.beagledata.gaea.workbench.mapper.KnowledgePackageMapper;
import com.beagledata.gaea.workbench.service.KnowledgePackageService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 知识包测试类
 * @author chenyafeng
 * @date 2018/12/4
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class PackageServiceTests {
    @Autowired
    private KnowledgePackageService knowledgePackageService;
    @Autowired
    private KnowledgePackageMapper knowledgePackageMapper;

    @Test
    public void selectKnowledgePackage(){
        String packageUuid = "27e77c0aee4c4483b42cba0c878caf2a";
        KnowledgePackage knowledgePackage = knowledgePackageMapper.selectByUuid(packageUuid);
        Assert.assertNotNull(knowledgePackage);
        System.out.println(knowledgePackage);
    }

    @Test
    public void updateAuditStatus(){
        String packageUuid = "27e77c0aee4c4483b42cba0c878caf2a";
        KnowledgePackage knowledgePackage = new KnowledgePackage();
        knowledgePackage.setUuid(packageUuid);
       knowledgePackageMapper.update(knowledgePackage);
    }
}
