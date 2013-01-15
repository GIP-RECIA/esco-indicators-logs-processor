package org.esco.indicators.backend.model;

import java.util.Date;

public class ConnexionProfilMoisOuSemaine {

	private String uai;
	private String profil;
	private Date dateDebutPeriode;
	private int nbConnexion;

	public ConnexionProfilMoisOuSemaine() {

	}

	public String getUai() {
		return this.uai;
	}

	public void setUai(final String uai) {
		this.uai = uai;
	}

	public String getProfil() {
		return this.profil;
	}

	public void setProfil(final String profil) {
		this.profil = profil;
	}

	public int getNbConnexion() {
		return this.nbConnexion;
	}

	public void setNbConnexion(final int nbConnexion) {
		this.nbConnexion = nbConnexion;
	}

	public Date getDateDebutPeriode() {
		return this.dateDebutPeriode;
	}

	public void setDateDebutPeriode(final Date dateDebutPeriode) {
		this.dateDebutPeriode = dateDebutPeriode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.dateDebutPeriode == null) ? 0 : this.dateDebutPeriode.hashCode());
		result = (prime * result) + (this.nbConnexion);
		result = (prime * result) + ((this.profil == null) ? 0 : this.profil.hashCode());
		result = (prime * result) + ((this.uai == null) ? 0 : this.uai.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		ConnexionProfilMoisOuSemaine other = (ConnexionProfilMoisOuSemaine) obj;
		if (this.dateDebutPeriode == null) {
			if (other.dateDebutPeriode != null) {
				return false;
			}
		} else if (!this.dateDebutPeriode.equals(other.dateDebutPeriode)) {
			return false;
		}
		if (this.nbConnexion != other.nbConnexion) {
			return false;
		}
		if (this.profil == null) {
			if (other.profil != null) {
				return false;
			}
		} else if (!this.profil.equals(other.profil)) {
			return false;
		}
		if (this.uai == null) {
			if (other.uai != null) {
				return false;
			}
		} else if (!this.uai.equals(other.uai)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ConnexionProfilMoisOuSemaine [uai=" + this.uai + ", profil=" + this.profil + ", moisOuSemaine="
				+ this.dateDebutPeriode + ", nbConnexion=" + this.nbConnexion + "]";
	}

}
