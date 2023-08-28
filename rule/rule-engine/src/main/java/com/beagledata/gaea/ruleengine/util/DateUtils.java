package com.beagledata.gaea.ruleengine.util;

import com.beagledata.gaea.ruleengine.exception.RuleException;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

/**
 * 描述:
 * @author 周庚新
 * @date 2020-12-15
 */
public class DateUtils {
	public static Date parseDate(String str, String format) {
		try {
			return org.apache.commons.lang3.time.DateUtils.parseDate(str, format);
		} catch (ParseException e) {
			throw new RuleException(e);
		}
	}

	public static String formatDate(Date date, String format) {
		return DateFormatUtils.format(date ,format);
	}

	public static Date addMonths(Date date, int amount) {
		return org.apache.commons.lang3.time.DateUtils.addMonths(date, amount);
	}

	public static int monthsBetween(Date date1, Date date2) {
		Calendar startCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		if (date1.before(date2)) {
		    startCal.setTime(date1);
		    endCal.setTime(date2);
		} else {
			startCal.setTime(date2);
			startCal.setTime(date1);
		}

		int startYear = startCal.get(Calendar.YEAR);
		int endYear = endCal.get(Calendar.YEAR);
		int startMonth = startCal.get(Calendar.MONTH);
		int endMonth = endCal.get(Calendar.MONTH);

		return (endYear - startYear) * 12 + endMonth - startMonth;
	}

	public static int getYear() {
		return Calendar.getInstance().get(Calendar.YEAR);
	}

	public static int getMonth() {
		return Calendar.getInstance().get(Calendar.MONTH) + 1;
	}
	public static  int daysBetween(Date date1, Date date2) {
		Calendar startCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		if (date1.before(date2)) {
			startCal.setTime(date1);
			endCal.setTime(date2);
		} else {
			startCal.setTime(date2);
			startCal.setTime(date1);
		}
		LocalDate startDate = date2LocalDate(startCal.getTime());
		LocalDate endDate = date2LocalDate(endCal.getTime());
		Long until = startDate.until(endDate, ChronoUnit.DAYS);
		return until.intValue();
	}
	public static LocalDate date2LocalDate(Date date) {
		Instant instant = date.toInstant();
		ZoneId zoneId = ZoneId.systemDefault();
		return instant.atZone(zoneId).toLocalDate();
	}

	/**
	* 描述: 获取 amount 季度后（前）的季度
	* @param: [date, amount]
	* @author: 周庚新
	* @date: 2020/12/15 
	* @return: java.lang.String
	*/
	public static String getYearAndSeason (Date date, int amount) {
		Date date1 = org.apache.commons.lang3.time.DateUtils.addMonths(date, amount * 3);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date1);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int season = 0;
		switch (month) {
			case Calendar.JANUARY:
			case Calendar.FEBRUARY:
			case Calendar.MARCH:
				season = 1;
				break;
			case Calendar.APRIL:
			case Calendar.MAY:
			case Calendar.JUNE:
				season = 2;
				break;
			case Calendar.JULY:
			case Calendar.AUGUST:
			case Calendar.SEPTEMBER:
				season = 3;
				break;
			case Calendar.OCTOBER:
			case Calendar.NOVEMBER:
			case Calendar.DECEMBER:
				season = 4;
				break;
			default:
				break;
		}
		return year + "" +season;
	}
}