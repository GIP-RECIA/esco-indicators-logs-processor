/**
 * 
 */
package org.esco.indicators.backend.jdbc.model;

import java.util.Date;


/**
 * Bean mapping ConnexionProfilSemaine and ConnexionProfilMois tables.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 * Deprecated use ConnexionProfilPeriode instead
 */
public class ConnexionProfilSemaine {

	private String nomProfil;
	private String uai;
	private Date semaine;
	private int nbConnexion;
	private int nbPersonne;
	private double moyenneConnexion;

	@Override
	public String toString() {
		return "ConnexionProfilSemaine [nomProfil=" + this.nomProfil + ", uai=" + this.uai + ", semaine=" + this.semaine
				+ ", nbConnexion=" + this.nbConnexion + ", nbPersonne=" + this.nbPersonne + ", moyenneConnexion="
				+ this.moyenneConnexion + "]";
	}

	public String getNomProfil() {
		return this.nomProfil;
	}
	public void setNomProfil(final String nomProfil) {
		this.nomProfil = nomProfil;
	}
	public String getUai() {
		return this.uai;
	}
	public void setUai(final String uai) {
		this.uai = uai;
	}
	public Date getSemaine() {
		return this.semaine;
	}
	public void setSemaine(final Date semaine) {
		this.semaine = semaine;
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