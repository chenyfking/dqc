package com.beagledata.gaea.workbench.test.service;

import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.common.ResourceResolver;
import com.beagledata.gaea.workbench.entity.OnlineUser;
import com.beagledata.gaea.workbench.entity.User;
import org.apache.shiro.util.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.ExpiringSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.test.context.junit4.SpringRunner;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by Cyf on 2020/5/21
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class SessionTests {
    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ResourceResolver resourceResolver;




    @Test
    public void testRedisRepository() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Assert.notNull(sessionRepository, "sessionRepository is null ...");
        if (sessionRepository instanceof RedisOperationsSessionRepository) {
            System.out.println("redis repository ...");
            RedisOperationsSessionRepository repository = (RedisOperationsSessionRepository)sessionRepository;
            Class outer = repository.getClass();
            Field templateField = outer.getDeclaredField("sessionRedisOperations");
            templateField.setAccessible(true);
            RedisTemplate template = (RedisTemplate)templateField.get(repository);

            HashOperations ops = template.opsForHash();
            Set<String> keys = template.keys("spring:session:sessions:*");
            Set<String> keys1 = template.keys("spring:session:sessions:expires:*");
            Set<String> expiresKeys = new HashSet<>();

            System.out.println("================");
            System.out.println("spring:session:sessions:");
            keys.forEach(k -> System.out.println(k));
            System.out.println("spring:session:sessions:expires:*");
            keys1.forEach(k -> System.out.println(k));
            System.out.println("================");

            keys1.forEach(k -> expiresKeys.add(k.replace("expires:", "")));
            for (String key : keys) {
                if (key.contains("expires")) {
                    continue;
                }
                if (!expiresKeys.contains(key)) {
                    template.delete(key);
                    continue;
                }
                //用户信息
                User user = (User) ops.get(key, "sessionAttr:user");
                if (null == user) {
                    continue;
                }
                System.out.println(user.toString());
            }
            System.out.println("...");
        } else {
            System.out.println("sessionRepository is orther Repository ... ");
        }
    }

    @Test
    public void testMapSessionRepository() throws NoSuchFieldException, IllegalAccessException {
        Assert.notNull(sessionRepository, "sessionRepository is null ...");
        if (sessionRepository instanceof MapSessionRepository) {
            System.out.println("map repository ...");
            MapSessionRepository repository = (MapSessionRepository)sessionRepository;
            Class clazz = repository.getClass();
            Field sessionsField = clazz.getDeclaredField("sessions");
            sessionsField.setAccessible(true);
            Map<String, ExpiringSession> sessions=  (Map)sessionsField.get(repository);
            for (Map.Entry<String, ExpiringSession> entry : sessions.entrySet()) {
                String key = entry.getKey();
                ExpiringSession es = entry.getValue();
                User user = (User)es.getAttribute("user");
                System.out.println(user.toString());
            }
        } else {
            System.out.println("sessionRepository is orther Repository ... ");
        }
    }
}