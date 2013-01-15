/**
 * 
 */
package org.esco.indicators.backend.exception;

/**
 * Should be throw when a log line must be ignored from calculation.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 *
 */
public class LogLineToIgnore extends Exception {

	/** SVUID. */
	private static final long serialVersionUID = -1236873521386818630L;

	/** {@inheritDoc} */
	public LogLineToIgnore() {
		super();
	}

	/** {@inheritDoc} */
	public LogLineToIgnore(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

	/** {@inheritDoc} */
	public LogLineToIgnore(final String arg0) {
		super(arg0);
	}

	/** {@inheritDoc} */
	public LogLineToIgnore(final Throwable arg0) {
		super(arg0);
	}



}
