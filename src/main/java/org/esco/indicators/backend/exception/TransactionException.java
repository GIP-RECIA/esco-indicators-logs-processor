/**
 * 
 */
package org.esco.indicators.backend.exception;

/**
 * Throw when a transaction problem occured.
 * The transaction need to be rolled back.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 *
 */
public class TransactionException extends Exception {

	/** SVUID.  */
	private static final long serialVersionUID = 131298363642379565L;

	public TransactionException() {
		super();
	}

	public TransactionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public TransactionException(final String message) {
		super(message);
	}

	public TransactionException(final Throwable cause) {
		super(cause);
	}

}
