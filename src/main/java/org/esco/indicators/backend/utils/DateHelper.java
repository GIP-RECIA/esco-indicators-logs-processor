/**
 * 
 */
package org.esco.indicators.backend.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 *
 */
public abstract class DateHelper {

	/**
	 * Build the first day of the week.
	 * 
	 * @param date in the week
	 * @return the first day of the week
	 */
	public static Date getFirstDayOfWeek(final Date date) {
		final Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.clear(Calendar.MILLISECOND);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.HOUR);
		cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());

		return cal.getTime();
	}

	/**
	 * Build the first day of the month.
	 * 
	 * @param date in the month
	 * @return the first day of the month
	 */
	public static Date getFirstDayOfMonth(final Date date) {
		final Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.clear(Calendar.MILLISECOND);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.HOUR);
		cal.set(Calendar.DAY_OF_MONTH, 1);

		return cal.getTime();
	}

}
