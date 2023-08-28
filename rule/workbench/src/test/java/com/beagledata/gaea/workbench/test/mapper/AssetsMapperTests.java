package com.beagledata.gaea.workbench.test.mapper;

import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.mapper.AssetsMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
  *@auto: yangyongqiang
  *@Description: 资源文件测试类
  *@Date: 2018-09-19 8:56
  **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class AssetsMapperTests {
    @Autowired
    private AssetsMapper assetsMapper;

    @Test
    public void selectAssetsByProjectUuid(){
        List<String> type = new ArrayList<>();
        type.add("fact");
        type.add("constant");
        List<Assets> assetsByProjectUuid = assetsMapper.selectAssetsByProjectUuid("b83d82e9b5d14dfba98f271e2548ad03", type);
        for (Assets as:assetsByProjectUuid) {
            System.out.println(as.toString());
        }
    }
}
