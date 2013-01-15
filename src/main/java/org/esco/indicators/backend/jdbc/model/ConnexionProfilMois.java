/**
 * 
 */
package org.esco.indicators.backend.jdbc.model;

import java.util.Date;


/**
 * Bean mapping ConnexionProfilMois table.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 * @deprecated use ConnexionProfilPeriode instead
 */
@Deprecated
public class ConnexionProfilMois {

	private String nomProfil;
	private String uai;
	private Date mois;
	private int nbConnexion;
	private int nbPersonne;
	private double moyenneConnexion;

	@Override
	public String toString() {
		return "ConnexionProfilMois [nomProfil=" + this.nomProfil + ", uai=" + this.uai + ", mois=" + this.mois + ", nbConnexion="
				+ this.nbConnexion + ", nbPersonne=" + this.nbPersonne + ", moyenneConnexion=" + this.moyenneConnexion + "]";
	}

	public String getNomProfil() {
		return this.nomProfil;
	}
	public void setNomProfil(final String nomprofil) {
		this.nomProfil = nomprofil;
	}
	public String getUai() {
		return this.uai;
	}
	public void setUai(final String uai) {
		this.uai = uai;
	}
	public Date getMois() {
		return this.mois;
	}
	public void setMois(final Date mois) {
		this.mois = mois;
	}
	public int getNbConnexion() {
		return this.nbConnexion;
	}
	public void setNbConnexion(final int nbConnexion) {
		this.nbConnexion = nbConnexion;
	}
	public int getNbPersonne() {
		return this.nbPersonne;
	}
	public void setNbPersonne(final int nbPersonne) {
		this.nbPersonne = nbPersonne;
	}
	public double getMoyenneConnexion() {
		return this.moyenneConnexion;
	}
	public void setMoyenneConnexion(final double moyenneConnexion) {
		this.moyenneConnexion = moyenneConnexion;
	}

}