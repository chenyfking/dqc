package com.beagledata.gaea.executionserver.service;

import com.beagledata.common.Result;
import com.beagledata.gaea.executioncore.domain.Input;
import com.beagledata.gaea.executioncore.domain.Output;

import java.io.InputStream;

/**
 * 接口调用知识包
 * Created by Chenyafeng on 2018/12/28.
 */
public interface ExecuteService {
    /**
     * 执行规则
     *
     * @param input 输入
     * @return
     */
    Output execute(Input input);

    /**
     * 上传license
     */
    Result uploadLicense(InputStream inputStream);

    /**
    * 描述: 异步请求
    * @param: [input]
    * @author: 周庚新
    * @date: 2020/11/10 
    * @return: com.beagledata.gaea.executioncore.domain.Output
    */
	Output executeAsync(Input input);

	/**
	* 描述: 查询异步请求结果
	* @param: [o]
	* @author: 周庚新
	* @date: 2020/11/10 
	* @return: com.beagledata.common.Result
	*/
	Result query(String o);
}
