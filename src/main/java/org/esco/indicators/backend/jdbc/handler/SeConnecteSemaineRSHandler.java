/**
 * 
 */
package org.esco.indicators.backend.jdbc.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.esco.indicators.backend.jdbc.model.CompressionKey;
import org.esco.indicators.backend.jdbc.model.SeConnectePeriode;

/**
 * ResultSetHandler which build a SeConnecteSemaine bean.
 * 
 * @author  GIP RECIA 2012 - Maxime BOSSARD.
 */
public class SeConnecteSemaineRSHandler implements ResultSetHandler<List<SeConnectePeriode>> {

	private List<SeConnectePeriode> result;

	@Override
	public List<SeConnectePeriode> handle(final ResultSet rs) throws SQLException {
		this.result = new ArrayList<SeConnectePeriode>(rs.getFetchSize());

		while (rs.next()) {
			CompressionKey ck = new CompressionKey(rs.getString("uai"), rs.getString("nomprofil"),
					rs.getDate("premierjoursemaine"), rs.getInt("nbconnexionsemaine"));
			SeConnectePeriode donnees = new SeConnectePeriode(ck);
			donnees.setUid(rs.getString("uid"));
			donnees.setMoyenne(rs.getDouble("moyennesemaine"));
			this.result.add(donnees);
		}
		rs.close();

		return this.result;
	}
}