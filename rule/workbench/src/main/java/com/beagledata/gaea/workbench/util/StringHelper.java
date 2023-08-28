package com.beagledata.gaea.workbench.util;

import com.beagledata.gaea.ruleengine.exception.RuleException;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvel2.PropertyAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {

	private static final String HELPERSPACE = " ";
	private static final String HELPERDOT = ".";
	private static final String CATEGORY_PRE = "com.";
	private static final String MODEL_DOT = ".model.";
	private static final Pattern pattern = Pattern.compile("error binding property: (.*) \\(value <<(.*)>>::(.*)\\)");

	/**
	 * 中文转汉语拼音
	 * @param chinese
	 * @param isPure true:只保留中文和英文   false:保留中英文和其他字符
	 * @return 拼音全部小写, 拼音之间以空格分隔
	 */
	public static String chinesePaseSpell(String chinese, boolean isPure) {
		StringBuilder result = new StringBuilder();
		if (StringUtils.isNotBlank(chinese)) {
			HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
			defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
			defaultFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
			for (int i = 0; i < chinese.length(); i++) {
				char c = chinese.charAt(i);
				try {
					if (String.valueOf(c).matches("[\u4e00-\u9fa5]+")) {// 如果字符是中文,则将中文转为汉语拼音
						result.append(PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat)[0]).append(HELPERSPACE);
					} else if (String.valueOf(c).matches("[a-zA-Z]")) {//如果字符是英文，则直接追加
						result.append(String.valueOf(c));
					} else {// 如果字符不是中英文,则进行判断
						result.append(isPure ? "" : String.valueOf(c));
					}
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			}
		}
		return result.toString().trim();
	}

	/**
	 * 根据空格分割字符串，返回每个单词首字母拼接的字符串
	 */
	public static String getFirstCharBySpace(String str) {
		if (StringUtils.isBlank(str)) {
			return str;
		}
		StringBuilder result = new StringBuilder();
		String[] words = str.split(HELPERSPACE);
		for (String word : words) {
			if (StringUtils.isBlank(word)) {
				continue;
			}
			result.append(word.charAt(0));
		}
		return result.toString();
	}

	//拼接异常信息
	public static String translateMessage(Exception e) {
		String message = null;
		Throwable rootExpetion = null;
		List<Throwable> stack = new ArrayList<>();
		List<String> ruleExceptionMsgs = new ArrayList<>();
		getStack(e, stack, ruleExceptionMsgs);

		if (!ruleExceptionMsgs.isEmpty()) {
			return "测试失败[" + StringUtils.join(ruleExceptionMsgs, ",") + "]";
		}

		for (Throwable t : stack) {
			if (t instanceof PropertyAccessException) {
				PropertyAccessException pae = (PropertyAccessException) t;
				message = pae.getMessage();
				break;
			}
		}
		rootExpetion = stack.get(stack.size() - 1);
		if (message == null) {
			message = rootExpetion.getLocalizedMessage();
		}
		if (message == null) {
			return "测试失败[内部执行错误]";
		}
		StringBuilder builder = new StringBuilder("测试失败[");
		if (message.contains("incompatible types in statement")) {
			builder.append("规则中存在不兼容类型的比较或其他操作,请检查数据类型");
		} else if (message.contains("evaluating constraint")) {
			if (message.contains("startsWith")) {
				builder.append("开始于 和 不开始于 条件判断取值不能为空,请检查是否填写相关数据的参数");
			} else if (message.contains("endsWith")) {
				builder.append("结束于 和 不结束于 条件判断取值不能为空,请检查是否填写相关数据的参数");
			} else if (message.contains(".length()")) {
				builder.append("长度判断取值不能为空,请检查是否填写相关数据的参数");
			} else if (message.contains("$AiModelHandler.handle")) {
				builder.append("请检查AI模型是否存在,或模型参数是否填写正确");
			} else {
				builder.append("规则条件判断处取值不能为空,请检查是否填写相关数据的参数");
			}
		} else if (message.contains("argument type mismatch")) {
			builder.append("数据类型异常,请检查是否修改数据的类型，或更新当前所执行的资源");
		} else if (message.contains("Comparison operation requires compatible types")) {
			builder.append("进行比较操作时数据的类型不同，请检查规则文件中数据的类型");
		} else if (message.contains("Division by zero") || message.contains("Division undefined")) {
			builder.append("不能除以0");
		}  else if (message.contains("You have an error in your SQL syntax")) {
			builder.append(String.format("SQL在'%s'处存在语法错误", message.substring(message.indexOf('\'') + 1, message.lastIndexOf('\''))));
		} else if (rootExpetion instanceof IndexOutOfBoundsException) {
			builder.append("数组下标越界");
		} else {
			String matchMsg = match(message);
			if (matchMsg != null) {
				builder.append(matchMsg);
			} else {
				builder.append("内部执行错误");
			}
		}
		builder.append("]");
		return builder.toString();
	}

	private static String match(String message) {
		Matcher matcher = pattern.matcher(message);
		if (matcher.find()) {
			return String.format("\"%s\"赋值给\"%s\"错误", matcher.group(2), matcher.group(1));
		}
		return null;
	}

	private static void getStack(Throwable e, List<Throwable> stack, List<String> ruleExceptionMsgs) {
		stack.add(e);
		Throwable c = e.getCause();
		if (c != null) {
		    if (c instanceof RuleException) {
		        String ruleMsg = c.getLocalizedMessage();
		        if (StringUtils.isNotBlank(ruleMsg)) {
		            ruleExceptionMsgs.add(ruleMsg);
		        }
		    }
		    getStack(c, stack, ruleExceptionMsgs);
		}
	}
}
