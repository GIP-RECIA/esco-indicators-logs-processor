/**
 * 
 */
package org.esco.indicators.backend.jdbc.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.dbutils.ResultSetHandler;
import org.esco.indicators.backend.jdbc.model.ACommeProfil;

/**
 * ResultSetHandler which build a ACommeProfil bean.
 * 
 * @author  GIP RECIA 2012 - Maxime BOSSARD.
 */
public class ACommeProfilRSHandler implements ResultSetHandler<Set<ACommeProfil>> {

	private Set<ACommeProfil> result;

	@Override
	public Set<ACommeProfil> handle(final ResultSet rs) throws SQLException {
		this.result = new HashSet<ACommeProfil>(rs.getFetchSize());
		while (rs.next()) {
			ACommeProfil donnees = new ACommeProfil();
			donnees.setUid(rs.getString("uid"));
			donnees.setUai(rs.getString("uai"));
			donnees.setNomProfil(rs.getString("nomprofil"));
			donnees.setDateDebutProfil(rs.getDate("datedebutprofil"));
			donnees.setDateFinProfil(rs.getDate("datefinprofil"));

			this.result.add(donnees);
		}
		rs.close();

		return this.result;
	}
}