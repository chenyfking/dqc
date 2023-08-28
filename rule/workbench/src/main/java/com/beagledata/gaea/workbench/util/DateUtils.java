package com.beagledata.gaea.workbench.util;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 描述:
 * 日期工具类
 * @author 周庚新
 * @date 2020-11-11
 */
public class DateUtils {

	/**
	* 描述: 两个日期之间的天数 （java8api）
	* @param: [start, end]
	* @author: 周庚新
	* @date: 2020/11/11
	* @return: int
	*/
	public static int durationDays(Date start, Date end) {
		LocalDate endDate = date2LocalDate(end);
		LocalDate startDate = date2LocalDate(start);
		Long until = startDate.until(endDate, ChronoUnit.DAYS);
		return until.intValue();
	}

	/**
	 * 描述: 两个日期之间的分钟 （java8api）
	 * @param: [start, end]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: int
	 */
	public static int durationMinutes(Date start, Date end) {
		LocalDateTime endDate = date2LocalDateTime(end);
		LocalDateTime startDate = date2LocalDateTime(start);
		Long until = startDate.until(endDate, ChronoUnit.MINUTES);
		return until.intValue();
	}

	/**
	* 描述:  Date 转换为 LocalDate
	* @param: [date]
	* @author: 周庚新
	* @date: 2020/11/11
	* @return: java.time.LocalDate
	*/
	public static LocalDate date2LocalDate(Date date) {
		Instant instant = date.toInstant();
		ZoneId zoneId = ZoneId.systemDefault();
		LocalDate localDate = instant.atZone(zoneId).toLocalDate();
		return localDate;
	}

	/**
	* 描述: Date 转换为 LocalDateTime
	* @param: [date]
	* @author: 周庚新
	* @date: 2020/11/11
	* @return: java.time.LocalDateTime
	*/
	public static LocalDateTime date2LocalDateTime(Date date) {
		Instant instant = date.toInstant();
		ZoneId zoneId = ZoneId.systemDefault();
		LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
		return localDateTime;
	}
}