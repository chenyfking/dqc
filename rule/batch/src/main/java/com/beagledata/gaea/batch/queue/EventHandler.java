package com.beagledata.gaea.batch.queue;

import java.util.Map;

/**
 * @author zgx
 * @date 2020-11-04
 */
public interface EventHandler {
	void onEvent(Map<String, Object> input) throws Exception;
}
