package com.beagledata.gaea.workbench.test.service;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.entity.Project;
import com.beagledata.gaea.workbench.mapper.ProjectMapper;
import com.beagledata.gaea.workbench.service.ProjectService;
import com.beagledata.util.IdUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by mahongfei on 2018/9/18.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class ProjectServiceTests {
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectMapper projectMapper;

    @Test
    public void addProject(){
        Project project = new Project();
        project.setUuid(IdUtils.UUID());
        project.setName("youyou");
        project.setDescription("asdafasfsdf");
        projectService.addProject(project);
        assertThat(project).isNotNull();
    }

    @Test
    public void deleteProject() {
        projectService.deleteProject("2");
    }

    @Test
    public void editProject() {
        Project project = new Project();
        project.setUuid("2");
        project.setName("啦啦");
        project.setDescription("asadgafgwG");
        projectService.editProject(project);
        assertThat(project).isNotNull();
    }

    @Test
    public void listAll() {
        Result result = projectService.listPage(1,10, "df", null, null);
        assertThat(result).isNotNull();
    }

    @Test
    public void projectDetails() {
        projectService.projectDetails("2");
    }

    @Test
    public void projectImport() {
        String zipPath = "C:\\Users\\Administrator\\Desktop\\保险计费 (10).zip";
        //projectService.projectImport();
    }

    @Test
    public void testTransaction() throws Exception {
        Project project = new Project();
        project.setUuid("2");
        project.setName("啦啦dfsfa");
        project.setDescription("asadgafgwG");
        projectService.addProject(project);
        System.out.println("方法结束");
        assertThat(project).isNotNull();
    }
}
