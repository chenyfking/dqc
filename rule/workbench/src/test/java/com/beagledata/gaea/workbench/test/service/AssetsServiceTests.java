package com.beagledata.gaea.workbench.test.service;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.service.AssetsService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
  *@auto: yangyongqiang
  *@Description: 资源文件测试类
  *@Date: 2018-09-19 8:56
  **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class AssetsServiceTests {
    @Autowired
    private AssetsService assetsService;

    @Test
    public void addAssets(){
        Assets assets = new Assets();
        assets.setProjectUuid("d4eec4d1764d46f18fb12b3bc103cc24");
        assets.setName("ttta");
        assets.setContent("tttb");
        assets.setDescription("tttc");
        assets.setType("fact");
        assetsService.addAssets(assets);
        assertThat(assets.getUuid()).isNotNull();
    }

    @Test
    public void editAssets(){
        Assets assets = new Assets();
        assets.setUuid("1");
        assets.setDescription("名称");
        assetsService.editAssets(assets);
    }

    @Test
    public void savecontent(){
        Assets assets = new Assets();
        assets.setUuid("1");
        assets.setContent("名称");
        assetsService.saveContent(assets);
    }

    @Test
    public void delete(){
        assetsService.delete("540645698b8841d182848c485ce1d400");
    }

     @Test
    public void listOfParams(){
        int page = 1;
        int pageNum = 10;
        Map params = new HashMap();
        params.put("projectUuid","5ab2f3e9805140b5be5fab3eed85539d");
        params.put("name","测试");
        params.put("type","测试");
        List<Assets> list = assetsService.listOfParams(page,pageNum,params);
        Assert.assertNotNull("查无内容！",list);

    }

    @Test
    public void selectTypeGroupByProjectUuid(){
       Set<String> list = assetsService.listTypeGroupByProjectUuid("56439b2b193c4e40bd5bd21459f4ead1");
       Assert.assertNotNull("查无内容！",list);

    }

    @Test
    public void testSelectByUuid() {
        String uuid = "76070396ec2f424691fc6bc5d37b6364";
        System.out.println(assetsService.selectAssets(uuid, 0));
    }

    @Test
    public void testLock() {
        String uuid = "540645698b8841d182848c485ce1d400";
        Boolean islock = false;
        Result result = assetsService.lockAssets(uuid,islock);
        System.out.println(result);
    }
}
