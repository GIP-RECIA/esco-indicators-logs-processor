/**
 * 
 */
package org.esco.indicators.backend.jdbc.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.ResultSetHandler;
import org.esco.indicators.backend.jdbc.model.ACommeProfil;

/**
 * ResultSetHandler which build a ACommeProfil bean.
 * 
 * @author  GIP RECIA 2012 - Maxime BOSSARD.
 */
public class ACommeProfilInEtabRSHandler implements ResultSetHandler<Map<String, Set<ACommeProfil>>> {

	private Map<String, Set<ACommeProfil>> result;

	@Override
	public Map<String, Set<ACommeProfil>> handle(final ResultSet rs) throws SQLException {
		this.result = new HashMap<String, Set<ACommeProfil>>(rs.getFetchSize());
		while (rs.next()) {
			final String uid = rs.getString("uid");
			Set<ACommeProfil> profils = this.result.get(uid);

			if (profils == null) {
				profils = new HashSet<ACommeProfil>(1);
				this.result.put(uid, profils);
			}

			final ACommeProfil donnees = new ACommeProfil();
			donnees.setUid(rs.getString("uid"));
			donnees.setUai(rs.getString("uai"));
			donnees.setNomProfil(rs.getString("nomprofil"));
			donnees.setDateDebutProfil(rs.getDate("datedebutprofil"));
			donnees.setDateFinProfil(rs.getDate("datefinprofil"));

			profils.add(donnees);
		}
		rs.close();

		return this.result;
	}
}