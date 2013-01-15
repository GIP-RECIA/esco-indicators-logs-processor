/**
 * 
 */
package org.esco.indicators.backend.jdbc.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.esco.indicators.backend.jdbc.model.Etablissement;

/**
 * ResultSetHandler which build a Etablissement bean.
 * 
 * FIXME MBD: why loop on all activations instead of return the last object ?
 * 
 * @author  GIP RECIA 2012 - Maxime BOSSARD.
 */
public class EtablissementRSHandler implements ResultSetHandler<List<Etablissement>> {

	private List<Etablissement> etablissements;

	/**
	 * Unique constructor.
	 * 
	 * @param uai the uai
	 */
	public EtablissementRSHandler() {
		super();
	}

	public List<Etablissement> handle(final ResultSet rs) throws SQLException {
		this.etablissements = new ArrayList<Etablissement>(rs.getFetchSize());

		while (rs.next()) {
			Etablissement etab = new Etablissement();
			etab.setUai(rs.getString("uai"));
			etab.setSiren(rs.getString("siren"));
			etab.setDepartement(rs.getString("departement"));
			etab.setTypeEtablissement(rs.getString("typeetablissement"));

			this.etablissements.add(etab);
		}

		rs.close();

		return this.etablissements;
	}
}