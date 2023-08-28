package com.beagledata.gaea.workbench.config.session;

import com.beagledata.gaea.workbench.entity.OnlineUser;
import com.beagledata.gaea.workbench.entity.User;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Chenyafeng on 2020/5/21
 * Spring-Session操作
 **/
public interface SessionService {
    String FORCELOGOUT = "FORCELOGOUT:";

    /**
     * 获得在线用户
     */
    List<OnlineUser> getOnlineUsers(String sortField, String sortDirection);

    /**
     * 清除用户在session中的数据
     */
    void clearSession(String key, User user);

    /**
     * 判断用户session是否有效
     */
    boolean isEffective(String uuid);

    User getUserByUuid(String userUuid);

    /**
     * 强制用户下线
     */
    void forceLogOut(String uuid, String loginIp);

    /**
     * 判断用户是否被强制下线
     */
    boolean isForceLogOut(String userName, String loginIp);

    /**
    * 描述: 强制用户下线
    * @param: [uuid, sessionId]
    * @author: 周庚新
    * @date: 2020/11/11
    * @return: void
    */
    void forceLogOutOtherPlace(String uuid, String sessionId);
    /**
     * 描述: 强制用户下线
     * @param: [uuid, sessionId]
     * @author: 周庚新
     * @date: 2020/11/11
     * @return: void
     */
    void forceLogOutCurrent(String sessionId);
    /**
    * 描述: 判断用户是否在异地登录
    * @param: [uuid]
    * @author: 周庚新
    * @date: 2020/11/11
    * @return: boolean
    */
    boolean hasLoged(String uuid);
    /**
    * 描述: 判断当前sessionid是否已登录
    * @param: [uuid, sessionId]
    * @author: 周庚新
    * @date: 2020/11/11
    * @return: boolean
    */
    boolean hasLoged(String uuid, String sessionId);

    /**
     * 计算在线时长字符串
     */
    default String getOnlineTimeStr(long dateDiff) {
        StringBuilder builder = new StringBuilder();
        long day = 0, hour = 0, min = 0, sec = 0;
        day = dateDiff / (24 * 60 * 60 * 1000);
        hour = (dateDiff / (60 * 60 * 1000) - day * 24);
        min = ((dateDiff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        sec = ((dateDiff / 1000) - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        if (day > 0) {
            builder.append(day).append("天");
        }
        if (hour > 0) {
            builder.append(hour).append("小时");
        }
        if (min > 0) {
            builder.append(min).append("分");
        }
        builder.append(sec).append("秒");
        return builder.toString();
    }

    /**
     * 向集合添加在线用户
     */
    default void addOnlineUserToList(List<OnlineUser> onlineList, OnlineUser onlineUser) {
        if (!onlineList.contains(onlineUser)) {//统计到新用户
            onlineList.add(onlineUser);
        } else { //用户已经在list中
            int index = onlineList.indexOf(onlineUser);
            OnlineUser oldUser = onlineList.get(index);//获得list中旧数据
            if (oldUser.compareTo(onlineUser) < 0) {//登录时间晚于list中旧数据的登录时间,替换新数据
                onlineList.set(index, onlineUser);
            }
        }
    }

    /**
     * 集合排序
     * 默认降序, 按照 loginTime(登录时间)
     * Comparator.comparing 结果为升序
     */
    default List<OnlineUser> sort(List<OnlineUser> list, String sortField, String sortDirection) {
        if ("asc".equals(sortDirection)) {
            list.sort(
                    "onlineTime".equals(sortField) ? Comparator.comparing(OnlineUser::getOnlineMillis) : Comparator.comparing(OnlineUser::getLoginTime)
            );
            return list;
        }

        list.sort(
                "onlineTime".equals(sortField) ? Comparator.comparing(OnlineUser::getOnlineMillis).reversed() : Comparator.comparing(OnlineUser::getLoginTime).reversed()
        );
        return list;
    }


}