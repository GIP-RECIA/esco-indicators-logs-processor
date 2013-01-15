/**
 * 
 */
package org.esco.indicators.backend.jdbc.model;

import java.util.Date;


/**
 * Bean mapping ConnexionProfilSemaine table.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 *
 */
public class ConnexionProfilPeriode {

	private String nomProfil;
	private String uai;
	private Date debutPeriode;
	private int nbConnexion;
	private int nbPersonne;
	private double moyenneConnexion;

	@Override
	public String toString() {
		return "ConnexionProfilSemaine [nomProfil=" + this.nomProfil + ", uai=" + this.uai + ", debutPeriode=" + this.debutPeriode
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

	public Date getDebutPeriode() {
		return this.debutPeriode;
	}

	public void setDebutPeriode(final Date debutPeriode) {
		this.debutPeriode = debutPeriode;
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