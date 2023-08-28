package com.beagledata.gaea.workbench.test.mapper;

import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.entity.KnowledgePackageBaseline;
import com.beagledata.gaea.workbench.entity.MicroDeployment;
import com.beagledata.gaea.workbench.mapper.MicroDeploymentMapper;
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
public class MicroDeploymentMapperTests {
    @Autowired
    private MicroDeploymentMapper microDeploymentMapper;

    @Test
    public void insertOrUpdate() {
        MicroDeployment microDeployment = new MicroDeployment();
        microDeployment.setUuid("231223dfwesdwef");
        microDeployment.setMicroUuid("333");
        microDeployment.setIncomingQuantity(2233);
        microDeployment.setExpiredTime(new Date());
        microDeployment.setType(MicroDeployment.DeployType.CHAMPION_CHALLENGER);

        List<MicroDeployment.Model> models = new ArrayList<>();

        MicroDeployment.Model m = new MicroDeployment.Model();
        m.setPercent(1);
        m.setPrimary(true);
        KnowledgePackageBaseline k = new KnowledgePackageBaseline();
        k.setVersionNo(1);
        k.setPackageUuid("23234234");
        m.setPkgBaseline(k);

        models.add(m);

        MicroDeployment.Model m1 = new MicroDeployment.Model();
        m1.setPercent(1);
        m1.setPrimary(true);
        KnowledgePackageBaseline k1 = new KnowledgePackageBaseline();
        k1.setVersionNo(11);
        k1.setPackageUuid("23234234sdfsd");
        m1.setPkgBaseline(k1);

        models.add(m1);

        microDeployment.setModels(models);
        microDeploymentMapper.insertOrUpdate(microDeployment);
    }


    @Test
    public void insertModel(){
        MicroDeployment microDeployment = new MicroDeployment();
        microDeployment.setUuid("231223dfwesdwef");
        microDeployment.setMicroUuid("333");
        microDeployment.setIncomingQuantity(2233);
        microDeployment.setExpiredTime(new Date());
        microDeployment.setType(MicroDeployment.DeployType.CHAMPION_CHALLENGER);

        List<MicroDeployment.Model> models = new ArrayList<>();

        MicroDeployment.Model m = new MicroDeployment.Model();
        m.setPercent(1);
        m.setPrimary(true);
        KnowledgePackageBaseline k = new KnowledgePackageBaseline();
        k.setVersionNo(1);
        k.setPackageUuid("23234234");
        m.setPkgBaseline(k);

        models.add(m);

        MicroDeployment.Model m1 = new MicroDeployment.Model();
        m1.setPercent(1);
        m1.setPrimary(true);
        KnowledgePackageBaseline k1 = new KnowledgePackageBaseline();
        k1.setVersionNo(11);
        k1.setPackageUuid("23234234sdfsd");
        m1.setPkgBaseline(k1);

        models.add(m1);

        microDeployment.setModels(models);
        microDeploymentMapper.insertModel(microDeployment);
    }
}
