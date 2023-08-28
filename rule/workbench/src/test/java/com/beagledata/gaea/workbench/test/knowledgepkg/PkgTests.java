package com.beagledata.gaea.workbench.test.knowledgepkg;

import com.beagledata.gaea.ruleengine.runtime.RuleContainer;
import com.beagledata.gaea.ruleengine.runtime.RuleEngine;
import com.beagledata.gaea.ruleengine.runtime.RuleEngineFactory;
import com.beagledata.gaea.ruleengine.util.PackageUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.List;

/**
 * Created by Cyf on 2021/1/14
 **/
public class PkgTests {
    public static void main(String[] args) {
        try {
            testPkg();
//            decode("C:\\Users\\Cyf\\Desktop\\pkg\\pkg1432.zip");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testPkg() throws Exception {
        String filePath = "C:\\Users\\Cyf\\Desktop\\pkg\\pkg1432.zip";
        File file = new File(filePath);
        RuleContainer.loadRule(file);
        RuleEngine ruleEngine = RuleEngineFactory.getRuleEngine("1432");

        ruleEngine.put("persion", "age", "30");    //age
        ruleEngine.put("persion", "birth", "1990-01-01 00:00:00");

        ruleEngine.put("persion", "xinzi", "20000"); //cc

        ruleEngine.execute();

        Object object = ruleEngine.getObject("persion", "xinzi");

        System.out.println( "=============");
    }

    //解密知识包
    public static void decode(String path) throws IOException {
        File pkgFile = new File(path);
        byte[] decrypt = PackageUtils.decrypt(FileUtils.readFileToByteArray(pkgFile));
        File decodeFile = new File(pkgFile.getParent(), "decode_"+pkgFile.getName());
        OutputStream os = new FileOutputStream(decodeFile);
        os.write(decrypt);
        os.flush();
        os.close();
    }
}