package com.beagledata.gaea.executionserver.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.common.RestConstants;
import com.beagledata.gaea.executioncore.domain.Input;
import com.beagledata.gaea.executioncore.domain.Output;
import com.beagledata.gaea.executionserver.service.ExecuteService;
import com.beagledata.util.IdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

/**
 * 执行规则接口
 *
 * Created by liulu on 2020/5/14.
 */
@RestController
public class ExecuteController {
    @Autowired
    private ExecuteService executeService;

    /**
     * 同步请求服务
     *
     * @param uuid
     * @param data
     * @return
     */
    @PostMapping(RestConstants.OpenApi.Endpoints.EXECUTE_SYNC + "/{uuid}")
    public Result execute(@PathVariable String uuid, @RequestBody Map<String, Object> data) {
        Input input = initInput(uuid, data, false);
        Output output = executeService.execute(input);
        return Result.newSuccess().withData(output);
    }

    /**
    * 描述: 异步请求服务
    * @param: [uuid, data]
    * @author: 周庚新
    * @date: 2020/11/9 
    * @return: com.beagledata.common.Result
    */
    @PostMapping(RestConstants.OpenApi.Endpoints.EXECUTE_ASYNC + "/{uuid}")
    public Result executeAsync(@PathVariable String uuid, @RequestBody Map<String, Object> data) {
        Input input = initInput(uuid, data, true);
        Output output = executeService.executeAsync(input);
        return Result.newSuccess().withData(output);
    }

    /**
    * 描述: 获取异步执行的结果
    * @param: [data]
    * @author: 周庚新
    * @date: 2020/11/9 
    * @return: com.beagledata.common.Result
    */
    @PostMapping(RestConstants.OpenApi.Endpoints.QUERY + "/{uuid}")
    public Result executeAsync(@RequestBody Map<String, String> data) {
        return executeService.query(data.get(RestConstants.OpenApi.Endpoints.SERIAL_KEY));
    }
    
    /**
    * 描述: 初始化 input
    * @param: [uuid, data, async]
    * @author: 周庚新
    * @date: 2020/11/9
    * @return: com.beagledata.gaea.executioncore.domain.Input
    */
    private Input initInput(String uuid, Map<String, Object> data, boolean async){
        Input input = new Input();
        input.setService(uuid);
        if (data != null) {
            input.getData().putAll(data);
        }
        input.setUuid(IdUtils.UUID());
        input.setAsync(async);
        input.setReqDate(new Date());
        return input;
    }

}
