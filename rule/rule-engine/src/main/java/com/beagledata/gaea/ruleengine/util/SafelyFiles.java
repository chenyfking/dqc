package com.beagledata.gaea.ruleengine.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述:
 * 解決path manipulation
 *
 * @author 周庚新
 * @date 2020-11-02
 */
public class SafelyFiles {

	/**
	 * 路径字符白名单
	 */
	private static Map<String, String> PATH_CHAR_WHITE_LIST = new HashMap<>();
	/**
	 * 路径目录白名单
	 */
	private final static String[] VALID_DIR = new String[]{"/gaea/data", "D:\\gaea\\data"};

	static {
		PATH_CHAR_WHITE_LIST.put("a", "a");
		PATH_CHAR_WHITE_LIST.put("b", "b");
		PATH_CHAR_WHITE_LIST.put("c", "c");
		PATH_CHAR_WHITE_LIST.put("d", "d");
		PATH_CHAR_WHITE_LIST.put("e", "e");
		PATH_CHAR_WHITE_LIST.put("f", "f");
		PATH_CHAR_WHITE_LIST.put("g", "g");
		PATH_CHAR_WHITE_LIST.put("h", "h");
		PATH_CHAR_WHITE_LIST.put("i", "i");
		PATH_CHAR_WHITE_LIST.put("j", "j");
		PATH_CHAR_WHITE_LIST.put("k", "k");
		PATH_CHAR_WHITE_LIST.put("l", "l");
		PATH_CHAR_WHITE_LIST.put("m", "m");
		PATH_CHAR_WHITE_LIST.put("n", "n");
		PATH_CHAR_WHITE_LIST.put("o", "o");
		PATH_CHAR_WHITE_LIST.put("p", "p");
		PATH_CHAR_WHITE_LIST.put("q", "q");
		PATH_CHAR_WHITE_LIST.put("r", "r");
		PATH_CHAR_WHITE_LIST.put("s", "s");
		PATH_CHAR_WHITE_LIST.put("t", "t");
		PATH_CHAR_WHITE_LIST.put("u", "u");
		PATH_CHAR_WHITE_LIST.put("v", "v");
		PATH_CHAR_WHITE_LIST.put("w", "w");
		PATH_CHAR_WHITE_LIST.put("x", "x");
		PATH_CHAR_WHITE_LIST.put("y", "y");
		PATH_CHAR_WHITE_LIST.put("z", "z");

		PATH_CHAR_WHITE_LIST.put("A", "A");
		PATH_CHAR_WHITE_LIST.put("B", "B");
		PATH_CHAR_WHITE_LIST.put("C", "C");
		PATH_CHAR_WHITE_LIST.put("D", "D");
		PATH_CHAR_WHITE_LIST.put("E", "E");
		PATH_CHAR_WHITE_LIST.put("F", "F");
		PATH_CHAR_WHITE_LIST.put("G", "G");
		PATH_CHAR_WHITE_LIST.put("H", "H");
		PATH_CHAR_WHITE_LIST.put("I", "I");
		PATH_CHAR_WHITE_LIST.put("J", "J");
		PATH_CHAR_WHITE_LIST.put("K", "K");
		PATH_CHAR_WHITE_LIST.put("L", "L");
		PATH_CHAR_WHITE_LIST.put("M", "M");
		PATH_CHAR_WHITE_LIST.put("N", "N");
		PATH_CHAR_WHITE_LIST.put("O", "O");
		PATH_CHAR_WHITE_LIST.put("P", "P");
		PATH_CHAR_WHITE_LIST.put("Q", "Q");
		PATH_CHAR_WHITE_LIST.put("R", "R");
		PATH_CHAR_WHITE_LIST.put("S", "S");
		PATH_CHAR_WHITE_LIST.put("T", "T");
		PATH_CHAR_WHITE_LIST.put("U", "U");
		PATH_CHAR_WHITE_LIST.put("V", "V");
		PATH_CHAR_WHITE_LIST.put("W", "W");
		PATH_CHAR_WHITE_LIST.put("X", "X");
		PATH_CHAR_WHITE_LIST.put("Y", "Y");
		PATH_CHAR_WHITE_LIST.put("Z", "Z");

		PATH_CHAR_WHITE_LIST.put("0", "0");
		PATH_CHAR_WHITE_LIST.put("1", "1");
		PATH_CHAR_WHITE_LIST.put("2", "2");
		PATH_CHAR_WHITE_LIST.put("3", "3");
		PATH_CHAR_WHITE_LIST.put("4", "4");
		PATH_CHAR_WHITE_LIST.put("5", "5");
		PATH_CHAR_WHITE_LIST.put("6", "6");
		PATH_CHAR_WHITE_LIST.put("7", "7");
		PATH_CHAR_WHITE_LIST.put("8", "8");
		PATH_CHAR_WHITE_LIST.put("9", "9");

		PATH_CHAR_WHITE_LIST.put("-", "-");
		PATH_CHAR_WHITE_LIST.put("_", "_");
		PATH_CHAR_WHITE_LIST.put(".", ".");
		PATH_CHAR_WHITE_LIST.put(":", ":");
		PATH_CHAR_WHITE_LIST.put(" ", " ");
		PATH_CHAR_WHITE_LIST.put(File.separator, File.separator);
	}

	public static File newFile(String filePath) {
		return cleanPath(new File(filePath));
	}

	public static File newFile(File parent, String name) {
		return cleanPath(new File(parent, name));
	}

	public static File newFile(String parent, String name) {
		return cleanPath(new File(parent, name));
	}

	private static File cleanPath(File file) {
//		String filePath = file.getAbsolutePath();
//		checkDir(filePath);
//		filePath = cleanPath(filePath);
//		return new File(filePath);
		return file;
	}

	/**
	 * 描述: 检查文件路径是否在规定目录
	 *
	 * @param: [filePath]
	 * @author: 周庚新
	 * @date: 2020/11/2
	 * @return: void
	 */
	private static void checkDir(String filePath) {
		for (String validDir : VALID_DIR) {
			if (filePath.startsWith(validDir)) {
				return;
			}
		}
		throw new FilePathInvalidException(filePath);
	}

	private static String cleanPath(String filePath) {
		StringBuilder builder = new StringBuilder();
		int pathLength = filePath.length();
		for (int i = 0; i < pathLength; i++) {
			char curChar = filePath.charAt(i);
			String validChar = PATH_CHAR_WHITE_LIST.get(String.valueOf(curChar));
			if (validChar == null) {
				continue;
			}

			if (i == pathLength - 1) {
				builder.append(curChar);
			} else {
				char nextChar = filePath.charAt(i + 1);
				if (curChar != '.' || (curChar == '.' && nextChar != '.')) {
					builder.append(curChar);
				}
			}
		}
		return builder.toString();
	}


	public static class FilePathInvalidException extends RuntimeException {
		private static final long serialVersionUID = -8104719948937481351L;

		public FilePathInvalidException() {
			super();
		}

		public FilePathInvalidException(String message) {
			super(message);
		}

		public FilePathInvalidException(Throwable cause) {
			super(cause);
		}

		public FilePathInvalidException(String message, Throwable cause) {
			super(message, cause);
		}

	}

}