package com.beagledata.gaea.workbench.listener;

import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.entity.User;
import com.beagledata.gaea.workbench.service.AssetsService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.session.Session;
import org.springframework.session.events.SessionExpiredEvent;
import org.springframework.stereotype.Component;

/**
 * Created by liulu on 2020/11/11.
 */
@Component
public class SessionExpiredListener implements ApplicationListener<SessionExpiredEvent> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AssetsService assetsService;

    @Override
    public void onApplicationEvent(SessionExpiredEvent event) {
        Session session = event.getSession();
        if (session == null) {
            return;
        }

        User user = session.getAttribute(Constants.SESSION_KEY_USER);
        if (user == null) {
            return;
        }

        logger.info(
                "用户[{}]session过期，解锁TA所有在编辑的资源文件",
                StringUtils.isNotBlank(user.getRealname()) ? user.getRealname() : user.getUsername()
        );
        assetsService.updateEditor(null, false, user.getUuid());
    }
}
