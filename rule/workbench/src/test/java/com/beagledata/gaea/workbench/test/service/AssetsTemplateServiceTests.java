package com.beagledata.gaea.workbench.test.service;

import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.common.AssetsType;
import com.beagledata.gaea.workbench.entity.AssetsTemplate;
import com.beagledata.gaea.workbench.entity.User;
import com.beagledata.gaea.workbench.mapper.AssetsTemplateMapper;
import com.beagledata.gaea.workbench.mapper.ProjectMapper;
import com.beagledata.gaea.workbench.mapper.UserMapper;
import com.beagledata.util.IdUtils;
import org.apache.shiro.util.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * 规则模板
 * Created by Chenyafeng on 2020/6/12
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class AssetsTemplateServiceTests {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private AssetsTemplateMapper mapper;
    String projectUuid = "26ddd052d32040b38eead2ffd7646a71";

    @Test
    public void insert(){
        AssetsTemplate template = new AssetsTemplate();
        template.setUuid(IdUtils.UUID());
        template.setName("abc");
        User user = userMapper.selectByUsername("admin");
        template.setCreator(user);
        template.setProjectUuid(projectUuid);
        template.setType(AssetsType.RULE_TABLE);
        mapper.insert(template);
    }

    @Test
    public void updateContent(){
        String templateUuid = "36299ecacad040cfbe47b1f444779c3b";
        String content = "{fdasfdsafdasfdasfdsafds}";
        mapper.updateContentByUuid(templateUuid, content);
    }

    @Test
    public void selectByUuid(){
        String uuid = "36299ecacad040cfbe47b1f444779c3b";
        System.out.println(mapper.selectByUuid(uuid));
    }

    @Test
    public void delete(){
        String uuid = "36299ecacad040cfbe47b1f444779c3b";
        mapper.delete(uuid);
    }

     @Test
    public void selectByParams(){
         AssetsTemplate template = new AssetsTemplate();
         template.setType(AssetsType.GUIDED_RULE);
         template.setProjectUuid(projectUuid);
         List<AssetsTemplate> list = mapper.selectByParams(template);
         Assert.notEmpty(list, "查询结果为空");
         list.stream().forEach(System.out::println);
     }

}
