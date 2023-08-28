package com.beagledata.gaea.workbench.test.service;

import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.entity.KnowledgePackageBaseline;
import com.beagledata.gaea.workbench.mapper.KnowledgePackageBaselineMapper;
import com.beagledata.gaea.workbench.mapper.MicroRelationMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class KnowledgePackageBaselineServiceTests {
    @Autowired
    private KnowledgePackageBaselineMapper baselineMapper;
    @Autowired
    private MicroRelationMapper microRelationMapper;

    @Test
    public void selectKnowledgePackage(){
        String uuid = "ksf";
        baselineMapper.delete(uuid);
    }

    @Test
    public void offline(){
        String microUuid = "594b65cd7d3d40f2b4837c789a41ee9e";
        Integer baselineVersion = 2;
        String packageUuid = "c4b8f6e437e6468ea2ec63e17d7c7c54";
        microRelationMapper.deleteByMicroUuid(microUuid);
        baselineMapper.updateState(packageUuid, baselineVersion, Constants.BaselineStat.UNEFFECTIVE);
    }

    @Test
    public void AuditRecord(){
        KnowledgePackageBaseline knowledgePackageBaseline = new KnowledgePackageBaseline();
        knowledgePackageBaseline.setAuditReason("1");
        knowledgePackageBaseline.setState(2);
        knowledgePackageBaseline.setUuid("36fcfe44a7fd4e9ea123ce52daab1c81");

        baselineMapper.updateAuditState(knowledgePackageBaseline);
        baselineMapper.insertAuditRecord(knowledgePackageBaseline.getState(), knowledgePackageBaseline.getAuditReason(), "1", knowledgePackageBaseline.getUuid());
    }
}
