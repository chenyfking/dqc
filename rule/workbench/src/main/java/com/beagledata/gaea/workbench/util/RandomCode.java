package com.beagledata.gaea.workbench.util;

import java.util.Arrays;

/**
 * @description: 工具类，生成随机随机码字符串
 */
public class RandomCode {
	/**
	 * 随机码难度级别，Simple只包含数字，Medium包含数字和小写英文，Hard包含数字和大小写英文
	 */
	public enum SecurityCodeLevel {Simple, Medium, Hard}

	;
	public static final int NINE = 9;
	public static final int THIRTY_THREE = 33;

	/**
	 * 产生默认随机码，6位Hard级别随机码
	 * @return String 随机码
	 */
	public static String getSecurityCode() {
		return getSecurityCode(6, SecurityCodeLevel.Hard, true);
	}

	/**
	 * 产生长度和难度任意的随机码
	 * @param length 长度
	 * @param level 难度级别
	 * @param isCanRepeat 是否能够出现重复的字符，如果为true，则可能出现 5578这样包含两个5,如果为false，则不可能出现这种情况
	 * @return String 随机码
	 */
	public static String getSecurityCode(int length, SecurityCodeLevel level, boolean isCanRepeat) {

		//随机抽取len个字符
		int len = length;

		//字符集合(除去易混淆的数字0、数字1、字母l、字母o、字母O)
		char[] codes = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
				'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
				'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C',
				'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
				'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
				'W', 'X', 'Y', 'Z'};

		//根据不同的难度截取字符数组
		if (level == SecurityCodeLevel.Simple) {
			codes = Arrays.copyOfRange(codes, 0, NINE);
		} else if (level == SecurityCodeLevel.Medium) {
			codes = Arrays.copyOfRange(codes, 0, THIRTY_THREE);
		}

		//字符集合长度
		int n = codes.length;

		//存放抽取出来的字符
		char[] result = new char[len];

		//判断能否出现重复的字符
		if (isCanRepeat) {
			for (int i = 0; i < result.length; i++) {
				//索引 0 and n-1
				int r = (int) (Math.random() * n);

				//将result中的第i个元素设置为codes[r]存放的数值
				result[i] = codes[r];
			}
		} else {
			for (int i = 0; i < result.length; i++) {
				//索引 0 and n-1
				int r = (int) (Math.random() * n);

				//将result中的第i个元素设置为codes[r]存放的数值
				result[i] = codes[r];

				//必须确保不会再次抽取到那个字符，因为所有抽取的字符必须不相同。
				//因此，这里用数组中的最后一个字符改写codes[r]，并将n减1
				codes[r] = codes[n - 1];
				n--;
			}
		}

		return String.valueOf(result);
	}
}
