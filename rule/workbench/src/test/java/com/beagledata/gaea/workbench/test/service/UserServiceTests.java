package com.beagledata.gaea.workbench.test.service;

import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.entity.User;
import com.beagledata.gaea.workbench.entity.Role;
import com.beagledata.gaea.workbench.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by liulu on 2017/12/19.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class UserServiceTests {
    @Autowired
    private UserService userService;

    @Test
    public void testAdd() {
        User user = new User("test2", "123456");
        user.setAdmin(true);
        System.out.println(user);
    }

    @Test
    public void testSearch() {
        User searchUser = new User();
        searchUser.setUsername("admin");
        /*List<User> userList = userService.search(1, 10, searchUser);
        for (User user : userList) {
            System.out.println(user);
        }*/
    }

    @Test
    public void testDelete() {
        userService.delete("ac66f065e1ca44b0b56100f5d3b70153");
    }

    @Test
    public void testSetRole() {
        User user = new User("6f4f537965934735881aa285cee4aeff");
        List<Role> roleList = new ArrayList<>();
        roleList.add(new Role("84c6840e737f44488faf494965c6175c"));
        roleList.add(new Role("a1f29d1e76df407c8986ffcd3fc9f2ac"));
//        user.setRoles(roleList);
//        userService.setRole(user);
    }

    @Test
    public void testSelectByUserName() {
        String userName = "admin";
        User user = userService.getByUsername(userName);
        System.out.println(user);
//        Assert.assertNotNull(user.getPermissions());
//        for (Permission permission : user.getPermissions()) {
//            System.out.println(permission);
//        }
    }

    @Test
    public void edit(){
        User user = new User();
        user.setUuid("c42b7ba2476f40ecae63ee31a5732a5b");
        user.setRealname("aaa");
        userService.edit(user, null);
        assertThat(user).isNotNull();
    }

    @Test
    public void getuser(){
        userService.getUserDetails("84f7954070554eacb32e6873f1881b56");
    }

    @Test
    public void selectList() {
        User user = new User();
        user.setId(1);
        /*List<User> list = userService.search(1, 10, user);
        assertThat(list).isNotNull();
        System.out.println(list);*/
    }
}
