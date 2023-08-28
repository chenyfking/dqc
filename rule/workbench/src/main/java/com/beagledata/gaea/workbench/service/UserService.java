package com.beagledata.gaea.workbench.service;


import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.User;

/**
 * @author yangyongqiang 2019/09/12
 */
public interface UserService {
	/**
     * @author liulu
     * 2017/12/13 17:25
     */
    User getByUsername(String username);

    /**
     *
     * @author liulu
     * 2017/12/19 10:21
     */
    User add(User user, String roleIds);

    /**
     * 搜索账户
     *
     * @author liulu
     * 2018/1/5 18:13
     */
    Result search(int page, int pageNum, User user);

    /**
     * @author liulu
     * 2018/1/5 19:31
     */
    void delete(String uuid);

    /**
     * @author liulu
     * 2018/1/8 17:52
     */
    void enable(String uuid);

    /**
     * @author liulu
     * 2018/1/8 9:34
     */
    void disable(String uuid);

    /**
     * @author liulu
     * 2018/1/9 11:27
     */
    void login(User user);

    /**
     * 当前用户修改登录密码
     *
     * @author liulu
     * 2018/1/11 17:16
     */
    void editMyPwd(String oldPassword, String newPassword);

    /**
     * 获取所有在线登录的账户
     *
     * @author liulu
     * 2018/1/24 17:36
     */
    Result listAllOnline(int page, int pageNum, String sortField, String sortDirection);

    /**
     * 强制用户下线
     * @author chenyafeng
     * 2018/1/29 15:06
     */
    void forceLogOut(String uuid,String loginIp);

     /**
    *@Author: mahongfei
    *@description: 编辑用户
    */
    void edit(User user, String roleIds);

    Result searchNotProjectMember(int page, int pageNum, User user, String projectUuid);

    /**
     * @Author yangyongqiang
     * @Description 强制登陆（同时踢出异地登陆的账号）
     * @Date 3:38 下午 2020/11/11
     **/
    Result forceLogin();

    /**
     * @Author yangyongqiang
     * @Description 获取用户详情
     * @Date 上午10:23 2021/1/25
     **/
    Result getUserDetails(String uuid);
}
