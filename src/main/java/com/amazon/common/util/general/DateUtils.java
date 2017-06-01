package com.amazon.common.util.general;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {

	/***
	 * 获取当天零点
	 * 
	 * @return
	 */
	public static Date getTodayZeorHour() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 从输入的date获取该日期的下一天
	 * 
	 * @param date
	 *            输入的日期
	 * @return 输入日期的一下天
	 */
	public static Date getNextDate(Date date) {
		if (date == null)
			return null;
		return new Date(date.getTime() + 86400000);
	}
}
