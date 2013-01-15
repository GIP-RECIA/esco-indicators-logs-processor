/**
 * 
 */
package org.esco.indicators.backend.jdbc.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;
import org.esco.indicators.backend.jdbc.model.EstActivee;
import org.springframework.util.Assert;

/**
 * ResultSetHandler which build a SeConnecteMois bean.
 * 
 * FIXME MBD: why loop on all activations instead of return the last object ?
 * 
 * @author  GIP RECIA 2012 - Maxime BOSSARD.
 */
public class EstActiveeRSHandler implements ResultSetHandler<EstActivee> {

	private final String uid;

	/**
	 * Unique constructor.
	 * 
	 * @param uid the uid
	 */
	public EstActiveeRSHandler(final String uid) {
		super();
		Assert.hasText(uid, "Uid must be supplied !");
		this.uid = uid;
	}

	@Override
	public EstActivee handle(final ResultSet rs) throws SQLException {
		EstActivee result = null;
		if (rs.next()) {
			result = new EstActivee();
			result.setUid(this.uid);
			result.setDateDebutActivation(rs.getDate("datedebutactivation"));
			result.setDateFinActivation(rs.getDate("datefinactivation"));
		}
		rs.close();

		return result;
	}
}