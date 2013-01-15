/**
 * 
 */
package org.esco.indicators.backend.jdbc.model;

import java.util.Date;


/**
 * Bean mapping SeConnecteSemaine table.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 * @deprecated use SeConnectePeriode instead
 */
@Deprecated
public class SeConnecteSemaine {

	private String uai;
	private String nomProfil;
	private String uid;
	private Date premierJourSemaine;
	private int nbConnexionSemaine;
	private double moyenneSemaine;

	@Override
	public String toString() {
		return "SeConnecteSemaine [uai=" + this.uai + ", nomProfil=" + this.nomProfil + ", uid=" + this.uid + ", premierJourSemaine="
				+ this.premierJourSemaine + ", nbConnexionSemaine=" + this.nbConnexionSemaine + ", moyenneSemaine="
				+ this.moyenneSemaine + "]";
	}

	public String getUai() {
		return this.uai;
	}

	public void setUai(final String uai) {
		this.uai = uai;
	}

	public String getNomProfil() {
		return this.nomProfil;
	}

	public void setNomProfil(final String nomProfil) {
		this.nomProfil = nomProfil;
	}

	public String getUid() {
		return this.uid;
	}

	public void setUid(final String uid) {
		this.uid = uid;
	}

	public Date getPremierJourSemaine() {
		return this.premierJourSemaine;
	}

	public void setPremierJourSemaine(final Date premierJourSemaine) {
		this.premierJourSemaine = premierJourSemaine;
	}

	public int getNbConnexionSemaine() {
		return this.nbConnexionSemaine;
	}

	public void setNbConnexionSemaine(final int nbConnexionSemaine) {
		this.nbConnexionSemaine = nbConnexionSemaine;
	}

	public double getMoyenneSemaine() {
		return this.moyenneSemaine;
	}

	public void setMoyenneSemaine(final double moyenneSemaine) {
		this.moyenneSemaine = moyenneSemaine;
	}

}