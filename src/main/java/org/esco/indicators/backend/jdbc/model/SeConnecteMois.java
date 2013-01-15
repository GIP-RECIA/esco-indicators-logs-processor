/**
 * 
 */
package org.esco.indicators.backend.jdbc.model;

import java.util.Date;


/**
 * Bean mapping SeConnecteMois table.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 * @deprecated use SeConnectePeriode instead
 */
@Deprecated
public class SeConnecteMois {

	private String uai;
	private String nomProfil;
	private String uid;
	private Date mois;
	private int nbConnexionMois;
	private double moyenneMois;

	@Override
	public String toString() {
		return "SeConnecteMois [uid=" + this.uid + ", uai=" + this.uai + ", nomProfil=" + this.nomProfil + ", mois=" + this.mois
				+ ", nbConnexionMois=" + this.nbConnexionMois + ", moyenneMois=" + this.moyenneMois + "]";
	}

	public String getUid() {
		return this.uid;
	}
	public void setUid(final String uid) {
		this.uid = uid;
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
	public Date getMois() {
		return this.mois;
	}
	public void setMois(final Date mois) {
		this.mois = mois;
	}
	public int getNbConnexionMois() {
		return this.nbConnexionMois;
	}
	public void setNbConnexionMois(final int nbConnexionMois) {
		this.nbConnexionMois = nbConnexionMois;
	}
	public double getMoyenneMois() {
		return this.moyenneMois;
	}
	public void setMoyenneMois(final double moyenneMois) {
		this.moyenneMois = moyenneMois;
	}

}