package com.beagledata.gaea.ruleengine.util;

import org.json.XML;

/**
 * 描述:
 * xml工具类
 *
 * @author 周庚新
 * @date 2020-09-16
 */
public class XmlUtils {

	public static String toJson(String xml) {
		return XML.toJSONObject(xml).toString();
	}
}