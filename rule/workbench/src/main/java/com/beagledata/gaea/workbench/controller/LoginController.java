package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.common.LogManager;
import com.beagledata.gaea.workbench.config.annotaion.RestLogAnnotation;
import com.beagledata.gaea.workbench.entity.User;
import com.beagledata.gaea.workbench.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by liulu on 2017/12/6.
 */
@RestController
public class LoginController {
    private static Logger logger = LogManager.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    /**
     * Ajax登录
     *
     * @author liulu
     * 2017/12/13 17:23
     */
    @PostMapping("login")
    @RestLogAnnotation(describe = "用户登录")
    public Result login(String username, String password, boolean rememberMe) {
        logger.info("license校验成功");
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username, password, rememberMe);
        logger.info("get token");
        User user = null;
        try {
            subject.login(token);
            logger.info("subject.login after");
            if (subject.isAuthenticated()) {
                logger.info("subject.isAuthenticated()");
                User user1 = userService.getByUsername(username);
                if (user1 != null && user1.getExpiredTime() != null) {
                    Date expireDate = user1.getExpiredTime();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                    Date time = df.parse(df.format(new Date()));
                    if (expireDate.before(time)) {
                        throw new IllegalStateException("该账户已过期");
                    }
                }

                userService.login(new User(username, null));
                //user = userService.setPermissionCodes((User) subject.getPrincipal());
                logger.info("setPermission end");
                subject.getSession().setAttribute("user", user);
                logger.info("set session end");
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(),"用户登录出错",e);
            return Result.newError();
        }
        return Result.newSuccess().withData(user);
    }

    /**
     * 登出
     *
     * @author liulu
     * 2017/12/15 9:49
     */
    @PostMapping("logout")
    @RestLogAnnotation(describe = "用户登出")
    public Result logout() {
        SecurityUtils.getSubject().logout();
        return Result.newSuccess();
    }

    /**
     * 前端调用查询session状态
     *
     * @author liulu
     * 2019/4/13 12:08
     */
    @GetMapping("session")
    public Result session() {
        return Result.newSuccess();
    }

    @PostMapping("forceLogin")
    public Result forceLogin() {
        return userService.forceLogin();
    }
}
