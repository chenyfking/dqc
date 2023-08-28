package com.beagledata.gaea.ruleengine.util;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 描述:
 * bean 工具类
 *
 * @author 周庚新
 * @date 2020-09-15
 */
public class BeanUtils {

	public static Object copy(Object obj){
		if (isPrimitiveType(obj)) {
		    return obj;
		}
		return JSON.parseObject(JSON.toJSONString(obj), obj.getClass());
	}

	/**
	* 描述: 判断对象是否为基本类型 包括 String Date
	* @param: [obj]
	* @author: 周庚新
	* @date: 2020/9/15
	* @return: boolean
	*
	*/
	private static boolean isPrimitiveType(Object obj) {
		Class objClass = obj.getClass();
		if (Integer.class.equals(objClass)) {
		    return true;
		}
		if (Double.class.equals(objClass)) {
		    return true;
		}
		if (Long.class.equals(objClass)) {
		    return true;
		}
		if (BigDecimal.class.equals(objClass)) {
		    return true;
		}
		if (Date.class.equals(objClass)) {
		    return true;
		}
		if (String.class.equals(objClass)) {
		    return true;
		}
		return  false;
	}
}