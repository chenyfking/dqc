package com.beagledata.gaea.workbench.test.service;

import com.alibaba.fastjson.JSONObject;
import com.beagledata.gaea.ruleengine.aimodel.AiModelHandler;
import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.entity.AiModel;
import com.beagledata.gaea.workbench.mapper.AiModelMapper;
import com.beagledata.gaea.workbench.service.AiModelService;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * Created by liulu on 2018/1/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class AiModelServiceTests {

    @Autowired
    private AiModelService aiModelService;
    @Autowired
    private AiModelMapper aiModelMapper;
    /**
     * 查询ai列表
     * @author chenyafeng
     * @date 2018/12/6
     */
    @Test
    public void selectList() {
        List<AiModel> list = aiModelMapper.selectAll(0,10,true,new AiModel(), null, null);
        System.out.println(list.toString());
    }

    /**
     * 模型预测
     */
    @Test
    public void modelPredict() throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
        String fileDir = "C:\\Users\\Cyf\\Desktop";
        String fileName = "LinearRegressionBoston_2.pmml";
        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Method addURL =URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        addURL.setAccessible(true);
        File modelFile = new File(fileDir, fileName);
        if (modelFile.exists()) {
            if (fileName.endsWith(".jar")) {
                addURL.invoke(classLoader, modelFile.toURI().toURL());
            } else {
                FileUtils.copyFile(
                        modelFile,
                        new File(this.getClass().getClassLoader().getResource("").getFile(), fileName)
                );
            }
        }
        AiModelHandler handler = new AiModelHandler();
        JSONObject obj = new JSONObject();
        obj.put("CRIM", "0.00632");
        obj.put("ZN", "18");
        obj.put("INDUS", "2.31");
        obj.put("CHAS", "0");
        obj.put("NOX", "0.538");
        obj.put("RM", "6.575");
        obj.put("AGE", "65.2");
        obj.put("DIS", "4.09");
        obj.put("RAD", "1");
        obj.put("TAX", "296");
        obj.put("PTRATIO", "15.3");
        obj.put("B", "396.9");
        obj.put("LSTAT", "4.98");
        obj.put("MEDV", "24");
        obj.put("result", "30.00821269");
//        String result = handler.handle(obj, fileName);
//        System.out.println("预测结果是: "+result);
    }
}
