package com.beagledata.gaea.executionserver.controller;

import com.beagledata.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述:
 * 健康检测
 *
 * @author 周庚新
 * @date 2020-11-06
 */
@RestController
public class HealthController {
	@GetMapping("health")
	public Result health(){
		return Result.SUCCESS;
	}
}