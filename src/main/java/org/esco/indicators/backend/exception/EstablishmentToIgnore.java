/**
 * 
 */
package org.esco.indicators.backend.exception;

/**
 * Should be throw when an Establishment must be ignored from calculation.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 *
 */
public class EstablishmentToIgnore extends Exception {

	/** SVUID. */
	private static final long serialVersionUID = -1236873521386818630L;

	/** {@inheritDoc} */
	public EstablishmentToIgnore() {
		super();
	}

	/** {@inheritDoc} */
	public EstablishmentToIgnore(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

	/** {@inheritDoc} */
	public EstablishmentToIgnore(final String arg0) {
		super(arg0);
	}

	/** {@inheritDoc} */
	public EstablishmentToIgnore(final Throwable arg0) {
		super(arg0);
	}



}
