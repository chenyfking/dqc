package com.beagledata.gaea.workbench.test;

import com.beagledata.gaea.workbench.entity.OnlineUser;
import com.beagledata.security.util.PasswordUtils;
import com.beagledata.util.StringUtils;

import java.util.*;

/**
 * Created by Cyf on 2019/12/23
 **/
public class Test {
    public static void main(String[] args) {
//        String password = "123456";
//        String[] passwords = PasswordUtils.sign(password);
//        System.out.println(passwords[0]);
//        System.out.println(passwords[1]);
//        System.out.println(passwords[0].equals(PasswordUtils.sign(password, passwords[1])));
        System.out.println(Double.class.getSimpleName());
        System.out.println(String.class.getName());
        System.out.println(String.class.getCanonicalName());
        System.out.println(String.class.getTypeName());
        StringUtils.isBlank("");
        testReplace();
    }

    private static void testReplace() {
        String s = "abc\"";
        String replace = s.replace("\"", "\\\"");
        System.out.println(replace);
    }

    public static void testSort() {
        List<OnlineUser> list = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            OnlineUser user = new OnlineUser();
            user.setOnlineMillis(i);
            list.add(user);
        }
        Collections.shuffle(list);
        System.out.println("排序前: ");
        list.stream().forEach(user ->
            System.out.print(user.getOnlineMillis()+",")
        );
        list.sort(Comparator.comparing(OnlineUser::getOnlineMillis).reversed());
        System.out.println();
        System.out.println("==========");
        System.out.println("排序后: ");
        list.stream().forEach(user ->
            System.out.print(user.getOnlineMillis()+",")
        );
    }
}