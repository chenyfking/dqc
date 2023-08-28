package com.beagledata.gaea.batch;

import com.beagledata.gaea.batch.configs.ExitCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 描述:
 * 停止应用并返回退出码
 *
 * @author 周庚新
 * @date 2020-11-04
 */
@Component
public class ShutDownManager {
	@Autowired
	private ApplicationContext ctx;
	
	/**
	* 描述: 正常退出
	* @param: []
	* @author: 周庚新
	* @date: 2020/11/4 
	* @return: void
	*/
	public void shutdown(){
		shutdown(ExitCode.SUCCESS);
	}
	/**
	* 描述: 异常退出
	* @param: [exitCode]
	* @author: 周庚新
	* @date: 2020/11/4 
	* @return: void
	*/
	public void shutdown(int exitCode){
		int code = SpringApplication.exit(ctx, () -> exitCode);
		System.exit(code);
	}
}