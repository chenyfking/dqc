package com.beagledata.gaea.executioncore.handler;

import com.beagledata.gaea.executioncore.domain.ApiDoc;
import com.beagledata.gaea.executioncore.domain.Input;
import com.beagledata.gaea.executioncore.domain.Output;

/**
 * Created by liulu on 2020/5/15.
 */
public interface ModelHandler {
    /**
     * 执行模型
     *
     * @param input 接口输入
     * @return 执行结果
     */
    Output handle(Input input);

    /**
     * @return 获取调用文档
     */
    ApiDoc getApiDoc();
}
