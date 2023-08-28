package com.beagledata.gaea.workbench.test.service;

import com.beagledata.gaea.ruleengine.runtime.RuleContainer;
import com.beagledata.gaea.ruleengine.runtime.RuleEngine;
import com.beagledata.gaea.ruleengine.runtime.RuleEngineFactory;
import com.beagledata.gaea.ruleengine.util.PackageUtils;
import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.entity.KnowledgePackage;
import com.beagledata.gaea.workbench.mapper.KnowledgePackageMapper;
import com.beagledata.gaea.workbench.rule.builder.KnowledgePackageRuleBuilder;
import com.beagledata.gaea.workbench.rule.define.Fact;
import com.beagledata.gaea.workbench.rule.define.Field;
import com.beagledata.gaea.workbench.service.KnowledgePackageService;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.io.*;
import java.util.List;
import java.util.Set;

/**
 * 知识包测试
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = TestApplication.class)
public class KnowledgePkgServiceTests {

//    @Autowired
    private KnowledgePackageMapper knowledgePackageMapper;

    /**
     * 调用知识包
     */
    @Test
    public void testPkg() throws IOException {
        String pkguuid = "5ae6f6abee384e38ab7d3d63957f811f";
        KnowledgePackage pkg = knowledgePackageMapper.selectByUuid(pkguuid);
        List<Assets> assetsList = knowledgePackageMapper.selectKnowledgePackageAssets(pkg);
        pkg.setAssetsList(assetsList);
        KnowledgePackageRuleBuilder builder = KnowledgePackageRuleBuilder.newBuilder(pkg);
        Set<Fact> facts = builder.getFacts();

        File file = new File("C:\\Users\\Cyf\\Desktop\\pkg1045.zip");
        RuleContainer.loadRule(file);
        RuleEngine ruleEngine = RuleEngineFactory.getRuleEngine("1045");
        for (Fact fact : facts) {
            String factId = String.valueOf(fact.getId());
            List<Field> fields = fact.getFields();
            int i=0;
            for (Field field : fields) {
                i++;
                ruleEngine.put(factId, field.getName(), String.valueOf(i)); //入参 测试value填写i
            }
        }
        ruleEngine.put("3796", "f_qKtFlX", "1");
        try {
            ruleEngine.execute();    //执行知识包
        } catch (Exception e) {
            e.printStackTrace();
        }
        Integer result = ruleEngine.getInt("3796", "f_qKtFlX");//获取结果
        System.out.println("result: " + result);
    }

    @Test
    public void pkgDecode() throws IOException {
        String filePath = "C:\\Users\\Cyf\\Desktop\\pkg1045.zip";
        File file = new File(filePath);
        File decodeFile = PackageUtils.getDecodeZip(file);
        Assert.notNull(decodeFile, "解密后的临时文件为空");
        String fileName = file.getName();
        File dest = new File(file.getParent(), fileName.substring(0, fileName.lastIndexOf(".")).concat("(unencode).zip"));
        dest.createNewFile();
        System.out.println("解密后的文件为: " + dest.getAbsolutePath());
        FileUtils.copyFile(decodeFile, dest);
        decodeFile.deleteOnExit();
    }

}
