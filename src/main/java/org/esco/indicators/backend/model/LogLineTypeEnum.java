/**
 * 
 */
package org.esco.indicators.backend.model;

import org.esco.indicators.backend.exception.LogLineToIgnore;

/**
 * Enumeration of all stat logs event type supported.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 *
 */
public enum LogLineTypeEnum {
	/** Login. */
	LI,

	/** Logout. */
	LO,

	/** Session start. */
	SSTART,

	/** Session stop. */
	SSTOP,

	/** Service use. */
	CTARG,

	/** External service use. */
	CCALL_EXT;

	/**
	 * Load a LineEventTypeEnum based on the log line event type row code.
	 * 
	 * @param eventTypeCode the event type row code
	 * @return the corresponding LineEventTypeEnum
	 * @throws LogLineToIgnore if code not recognized
	 */
	public static LogLineTypeEnum fromLogLineEventTypeRow(final String eventTypeCode) throws LogLineToIgnore {
		if (eventTypeCode != null) {
			for (LogLineTypeEnum item : LogLineTypeEnum.values()) {
				if (item.name().equals(eventTypeCode)) {
					return item;
				}
			}
		}

		throw new LogLineToIgnore(String.format("Statistique event type code: [%s] is not recognized !", eventTypeCode));
	}
}
