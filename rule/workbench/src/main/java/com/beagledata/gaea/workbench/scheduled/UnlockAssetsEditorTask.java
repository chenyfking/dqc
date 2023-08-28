package com.beagledata.gaea.workbench.scheduled;

import com.beagledata.common.SpringBeanHolder;
import com.beagledata.gaea.common.task.AbstractTask;
import com.beagledata.gaea.workbench.service.AssetsService;

/**
 * 解锁所有资源文件的编辑者
 *
 * Created by liulu on 2020/11/11.
 */
public class UnlockAssetsEditorTask extends AbstractTask {
    private AssetsService assetsService = SpringBeanHolder.getBean(AssetsService.class);

    @Override
    public String desc() {
        return "解锁所有资源文件的编辑者";
    }

    @Override
    public void run() {
        assetsService.updateEditor(null, false, null);
    }
}
