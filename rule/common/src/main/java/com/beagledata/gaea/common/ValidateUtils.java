package com.beagledata.gaea.common;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * 描述: 文件路径过滤
 *
 * @author 周庚新
 * @date 2020-10-14
 */
public class ValidateUtils {

	public static final List<String> FILENAME_INVALIDATE_LIST = Arrays.asList(new String[]{"..", "../", "..\\", " "});

	public static boolean isValidateFileName(String fileName) {
		for (String str : FILENAME_INVALIDATE_LIST) {
			if (fileName.contains(str)) {
			    return false;
			}
		}
		return true;
	}
}