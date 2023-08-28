package com.beagledata.gaea.workbench.test.service;

import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.entity.KnowledgePackage;
import com.beagledata.gaea.workbench.entity.Micro;
import com.beagledata.gaea.workbench.entity.MicroType;
import com.beagledata.gaea.workbench.mapper.MicroMapper;
import com.beagledata.gaea.workbench.mapper.MicroRelationMapper;
import com.beagledata.gaea.workbench.service.MicroService;
import com.beagledata.util.IdUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * 微服务测试类
 * @author chenyafeng
 * @date 2018/12/3
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class MicroServiceTests {
    @Autowired
    private MicroMapper microMapper;
    @Autowired
    private MicroService microService;
    @Autowired
    private MicroRelationMapper microRelationMapper;

    @Test
    public void refuse() {
        KnowledgePackage knowledgePackage = new KnowledgePackage();
        knowledgePackage.setPackageUuid("86d3ff8d013b47f698e5a775d4625134");
        microService.refusePass(knowledgePackage);
    }

    @Test
    public void insert() {
        Micro micro = new Micro();
        micro.setUuid(IdUtils.UUID());
        micro.setName("测试微服务type111");
        micro.setType(new MicroType("3edcvfr6yhn"));
        micro.setPackageUuid("111");
        microMapper.insert(micro);
    }

    @Test
    public void selectByUuid() {
        String uuid = "1a60a7640222490eb81aa5aa2dfb1fcb";
        Micro micro = microMapper.selectByUuid(uuid);
        System.out.println(micro);
    }

    @Test
    public void update() {
        String uuid = "0182c3e6f55f424eadde6f187ddb69c0";
        Micro micro = microMapper.selectByUuid(uuid);
        micro.setPackageUuid("333");
        micro.setType(new MicroType("1qazxsw23edc"));
        micro.setName("测试3");
        microMapper.update(micro);
    }

    @Test
    public void delete() {
        String uuid = "0182c3e6f55f424eadde6f187ddb69c0";
        microMapper.delete(uuid);
    }

    @Test
    public void updateEnable() {
        String uuid = "0182c3e6f55f424eadde6f187ddb69c0";
        microMapper.updateEnable(uuid,false);
    }

    @Test
    public void insertType() {
        MicroType type = new MicroType();
        type.setUuid("fda");
        type.setName("测试添加");
        microMapper.insertType(type);
    }


    @Test
    public void deleteMicroModels() {
        String microId = "efd83ca6067c47ff862d7f2f7f47c5be";
        List<String> modelIds = new ArrayList<>();
        modelIds.add("32d7bd12fe3b43d4962c4c0b2d12ae04");
        modelIds.add("2abc36c99bab483b8d3fb273e04f2892");
        microRelationMapper.deleteModels(microId);
    }
}
