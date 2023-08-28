package com.beagledata.gaea.ruleengine.function;

import com.beagledata.gaea.ruleengine.annotation.FunctionMethodProperty;
import com.beagledata.gaea.ruleengine.annotation.FunctionProperty;
import com.beagledata.gaea.ruleengine.exception.RuleException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by liulu on 2018/10/15.
 */
@FunctionProperty(name = "日期函数")
public class DateFunction {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@FunctionMethodProperty(name = "解析字符串为日期", params = {"日期字符串", "格式"})
	public Date formatString(String dateStr, String pattern) {
		try {
            return DateUtils.parseDateStrictly(dateStr, pattern);
		} catch (ParseException e) {
			throw new RuleException(String.format("日期字符串[%s]或格式[%s]不正确"));
		}
	}

	@FunctionMethodProperty(name = "当前日期")
	public Date getDate() {
		return new Date();
	}

	@FunctionMethodProperty(name = "格式化日期", params = {"目标日期", "格式"})
	public String format(Date date, String pattern) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		return new SimpleDateFormat(pattern).format(date);
	}

	@FunctionMethodProperty(name = "加日期", params = {"目标日期", "年数", "月数", "天数", "小时", "分钟", "秒数"})
	public Date addDate(Date date, Integer years, Integer months, Integer days, Integer hours, Integer minutes, Integer seconds) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.YEAR, years);
		c.add(Calendar.MONTH, months);
		c.add(Calendar.DAY_OF_MONTH, days);
		c.add(Calendar.HOUR_OF_DAY, hours);
		c.add(Calendar.MINUTE, minutes);
		c.add(Calendar.SECOND, seconds);
		return c.getTime();
	}

	@FunctionMethodProperty(name = "日期加年", params = {"目标日期", "年数"})
	public Date addDateForYear(Date date, Integer years) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.YEAR, years);
		return c.getTime();
	}

	@FunctionMethodProperty(name = "日期加月", params = {"目标日期", "月数"})
	public Date addDateForMonth(Date date, Integer months) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, months);
		return c.getTime();
	}

	@FunctionMethodProperty(name = "日期加天", params = {"目标日期", "天数"})
	public Date addDateForDay(Date date, Integer days) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_MONTH, days);
		return c.getTime();
	}

	@FunctionMethodProperty(name = "日期加小时", params = {"目标日期", "小时数"})
	public Date addDateForHour(Date date, Integer hours) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.HOUR_OF_DAY, hours);
		return c.getTime();
	}

	@FunctionMethodProperty(name = "日期加分钟", params = {"目标日期", "分钟数"})
	public Date addDateForMinute(Date date, Integer minutes) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MINUTE, minutes);
		return c.getTime();
	}

	@FunctionMethodProperty(name = "日期加秒", params = {"目标日期", "秒数"})
	public Date addDateForSecond(Date date, Integer seconds) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.SECOND, seconds);
		return c.getTime();
	}

	@FunctionMethodProperty(name = "减日期", params = {"目标日期", "年数", "月数", "天数", "小时", "分钟", "秒数"})
	public Date subDate(Date date, Integer years, Integer months, Integer days, Integer hours, Integer minutes, Integer seconds) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.YEAR, -years);
		c.add(Calendar.MONTH, -months);
		c.add(Calendar.DAY_OF_MONTH, -days);
		c.add(Calendar.HOUR_OF_DAY, -hours);
		c.add(Calendar.MINUTE, -minutes);
		c.add(Calendar.SECOND, -seconds);
		return c.getTime();
	}

	@FunctionMethodProperty(name = "减日期减年", params = {"目标日期", "年数"})
	public Date subDateForYear(Date date, Integer years) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.YEAR, -years);
		return c.getTime();
	}
	
	@FunctionMethodProperty(name = "减日期减月", params = {"目标日期", "月数"})
	public Date subDateForMonth(Date date, Integer months) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, -months);
		return c.getTime();
	}

	@FunctionMethodProperty(name = "减日期减天", params = {"目标日期", "天数"})
	public Date subDateForDay(Date date, Integer days) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_MONTH, -days);
		return c.getTime();
	}

	@FunctionMethodProperty(name = "减日期减小时", params = {"目标日期", "小时"})
	public Date subDateForHour(Date date, Integer hours) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.HOUR_OF_DAY, -hours);
		return c.getTime();
	}

	@FunctionMethodProperty(name = "减日期减分钟", params = {"目标日期", "分钟"})
	public Date subDateForMinute(Date date, Integer minutes) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MINUTE, -minutes);
		return c.getTime();
	}

	@FunctionMethodProperty(name = "减日期减秒", params = {"目标日期", "秒数"})
	public Date subDateForSecond(Date date, Integer seconds) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.SECOND, -seconds);
		return c.getTime();
	}

	@FunctionMethodProperty(name = "取年份", params = {"目标日期"})
	public Integer getYear(Date date) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.YEAR);
	}

	@FunctionMethodProperty(name = "取月份", params = {"目标日期"})
	public Integer getMonth(Date date) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.MONTH) + 1;
	}

	@FunctionMethodProperty(name = "取星期", params = {"目标日期"})
	public Integer getWeek(Date date) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		Integer day = c.get(Calendar.DAY_OF_WEEK);
		day = day - 1;
		if (day == 0) {
		    day = 7;
		}
		return day;
	}

	@FunctionMethodProperty(name = "取天", params = {"目标日期"})
	public Integer getDay(Date date) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.DAY_OF_MONTH);
	}

	@FunctionMethodProperty(name = "取小时", params = {"目标日期"})
	public Integer getHour(Date date) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.HOUR_OF_DAY);
	}

	@FunctionMethodProperty(name = "取分钟", params = {"目标日期"})
	public Integer getMinute(Date date) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.MINUTE);
	}

	@FunctionMethodProperty(name = "取秒", params = {"目标日期"})
	public Integer getSecond(Date date) {
		if (date == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.SECOND);
	}

	@FunctionMethodProperty(name = "日期相减返回毫秒", params = {"日期", "减去的日期"})
	public Long dateDifMillSecond(Date d1, Date d2) {
		if (d1 == null || d2 == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(d1);
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d2);
		long a = c.getTimeInMillis();
		long b = c1.getTimeInMillis();
		return a - b;
	}

	@FunctionMethodProperty(name = "日期相减返回秒", params = {"日期", "减去的日期"})
	public Long dateDifSecond(Date d1, Date d2) {
		if (d1 == null || d2 == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(d1);
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d2);
		long a = c.getTimeInMillis();
		long b = c1.getTimeInMillis();
		return (a - b) / 1000;
	}

	@FunctionMethodProperty(name = "日期相减返回分钟", params = {"日期", "减去的日期"})
	public Long dateDifMinute(Date d1, Date d2) {
		if (d1 == null || d2 == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(d1);
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d2);
		long a = c.getTimeInMillis();
		long b = c1.getTimeInMillis();
		return (a - b) / (1000 * 60);
	}

	@FunctionMethodProperty(name = "日期相减返回小时", params = {"日期", "减去的日期"})
	public Long dateDifHour(Date d1, Date d2) {
		if (d1 == null || d2 == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(d1);
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d2);
		long a = c.getTimeInMillis();
		long b = c1.getTimeInMillis();
		return (a - b) / (1000 * 60 * 60);
	}

	@FunctionMethodProperty(name = "日期相减返回天", params = {"日期", "减去的日期"})
	public Long dateDifDay(Date d1, Date d2) {
		if (d1 == null || d2 == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(d1);
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d2);
		long a = c.getTimeInMillis();
		long b = c1.getTimeInMillis();
		return (a - b) / (1000 * 60 * 60 * 24);
	}

	@FunctionMethodProperty(name = "日期相减返回星期", params = {"日期", "减去的日期"})
	public Long dateDifWeek(Date d1, Date d2) {
		if (d1 == null || d2 == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(d1);
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d2);
		long a = c.getTimeInMillis();
		long b = c1.getTimeInMillis();
		return (a - b) / (1000 * 60 * 60 * 24 * 7);
	}

	@FunctionMethodProperty(name = "日期相减返回月", params = {"日期", "减去的日期"})
	public Integer dateDifMonth(Date d1, Date d2) {
		if (d1 == null || d2 == null) {
			throw new RuleException("日期不能为空");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(d1);
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d2);

		Integer year1 = c.get(Calendar.YEAR);
		Integer year2 = c1.get(Calendar.YEAR);

		Integer month1 = c.get(Calendar.MONTH);
		Integer month2 = c1.get(Calendar.MONTH);
		Integer result = 12 * (year1 - year2) + (month1 - month2);
		return result;
	}

	@FunctionMethodProperty(name = "根据出生日期获取年龄", params = {"出生日期"})
	public Integer getAge(Date birthDay) {
		if (birthDay == null) {
		    return 0;
		}
        Calendar cal = Calendar.getInstance();
        if (cal.before(birthDay)) { //出生日期晚于当前时间，无法计算
           return 0;
        }
        int yearNow = cal.get(Calendar.YEAR);  //当前年份
        int monthNow = cal.get(Calendar.MONTH);  //当前月份
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH); //当前日期
        cal.setTime(birthDay);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
        int age = yearNow - yearBirth;   //计算整岁数
        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth){
                    age--;//当前日期在生日之前，年龄减一
				}
            } else {
                age--; //当前月份在生日之前，年龄减一
            }
        }
		return age;
    }

	@FunctionMethodProperty(name = "是否上个月", params = {"日期字符串", "格式"})
	public Boolean isLastMonth(String date, String format) {
		if (StringUtils.isBlank(date) || StringUtils.isBlank(format)) {
		    return false;
		}
		Calendar lastMonthCal = Calendar.getInstance();
		lastMonthCal.set(Calendar.MONTH, -1);
		try {
			Date inputDate = DateUtils.parseDate(date, format);
			Calendar inputCal = Calendar.getInstance();
			inputCal.setTime(inputDate);
			return lastMonthCal.get(Calendar.MONTH) == inputCal.get(Calendar.MONTH);
		} catch (ParseException e) {
			logger.warn("执行日期函数[是否上个月]出错， 日期字符串：{}，格式：{}", date, format);
		}
		return false;
	}

	@FunctionMethodProperty(name = "根据身份证获取年龄", params = {"身份证号"})
	public Integer getAgeByIdentityCardNum(String identityCardNum) {
		try {
			Date birthDate = getBirthDate(identityCardNum);
			if (birthDate.getTime() > System.currentTimeMillis()) {
				throw new RuleException("身份证号格式不正确");
			}

			int nowYear = Calendar.getInstance().get(Calendar.YEAR);
			Calendar birthCalendar = Calendar.getInstance();
			birthCalendar.setTime(birthDate);
			int birthYear = birthCalendar.get(Calendar.YEAR);
			return nowYear - birthYear;
		} catch (Exception e) {
			throw e;
		}
	}

	@FunctionMethodProperty(name = "根据身份证获取出生日期", params = {"身份证号"})
	public Date getBirthByIdentityCardNum(String identityCardNum) {
		try {
			Date birthDate = getBirthDate(identityCardNum);
			if (birthDate.getTime() > System.currentTimeMillis()) {
				throw new RuleException("身份证号格式不正确");
			}

			return birthDate;
		} catch (Exception e) {
			throw e;
		}
	}

	private Date getBirthDate(String identityCardNum) {
    	if (StringUtils.isBlank(identityCardNum)) {
    	    throw new RuleException("身份证号不能为空");
    	}

    	boolean flag = identityCardNum.matches("\\d{15}|\\d{17}[\\dXx]");
    	if (!flag) {
			throw new RuleException("身份证号格式不正确");
    	}

    	try {
    		String birthDay = "";
    		Integer length = identityCardNum.length();
    		if (length == 15) {
    		    birthDay = "19" + identityCardNum.substring(6, 12);
    		} else if (length == 18) {
				birthDay = identityCardNum.substring(6, 14);
			} else {
				throw new RuleException("身份证号格式不正确");
			}
    		return DateUtils.parseDateStrictly(birthDay, "yyyyMMdd");
		}catch (Exception e) {
			throw new RuleException("身份证号格式不正确");
		}
	}


}
