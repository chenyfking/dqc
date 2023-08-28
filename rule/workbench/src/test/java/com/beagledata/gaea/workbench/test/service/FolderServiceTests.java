package com.beagledata.gaea.workbench.test.service;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.entity.Folder;
import com.beagledata.gaea.workbench.service.FolderService;
import com.beagledata.util.IdUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by mahongfei on 2018/12/6.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class FolderServiceTests {
    @Autowired
    private FolderService folderService;

    @Test
    public void addFolder(){
        Folder folder = new Folder();
        folder.setUuid(IdUtils.UUID());
        folder.setDirId("1");
        folder.setDirName("lalla");
        folder.setParentId("0");
        folder.setCategoryName("fact");
        folder.setProjectUuid("d4eec4d1764d46f18fb12b3bc103cc24");
        folderService.addFolder(folder);
        assertThat(folder).isNotNull();
    }

    @Test
    public void deleteFolder() {
        folderService.deleteFolder("7ba0b1465b7a44baac8310f199192a3f");
    }

    @Test
    public void editFolder() {
        Folder folder = new Folder();
        folder.setUuid("3d675e01122d45a68b0d9f6679fe536d");
        folder.setDirName("lallal");
        folderService.editFolder(folder);
        assertThat(folder).isNotNull();
    }

    @Test
    public void listAll() {
        List<Object> list = folderService.listAll("d4eec4d1764d46f18fb12b3bc103cc24");
        assertThat(list).isNotNull();
    }

    @Test
    public void lockFolder() {
        String uuid = "7ba0b1465b7a44baac8310f199192a3f";
        Boolean isLock = true;
        Result result = folderService.lockFolder(uuid, isLock);
        System.out.println(result);
    }
}
