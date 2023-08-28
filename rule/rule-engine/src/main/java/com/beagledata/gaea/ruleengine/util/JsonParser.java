package com.beagledata.gaea.ruleengine.util;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 描述:
 * @author 周庚新
 * @date 2020-12-15
 */
public class JsonParser {
	private ReadContext readCtx;

	public JsonParser(String json) {
		this.readCtx = JsonPath.parse(json);
	}
	public <T> T read(String jsonPath, Class<T> clazz) {
		try {
			if (List.class.isAssignableFrom(clazz)) {
			    Object obj = readCtx.read(jsonPath, Objects.class);
			    if (obj instanceof Map) {
			        List list = new ArrayList();
			        list.add(obj);
			        obj = list;
			    }
			    return clazz.cast(obj);
			}
			return readCtx.read(jsonPath, clazz);
		} catch (PathNotFoundException e) {
			return null;
		}
	}
}