/**
 * 
 */
package org.esco.indicators.backend.jdbc.model;

import java.util.Date;

/**
 * Bean mapping ACommeProfil table.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 *
 */
public class ACommeProfil {

	private String uid;
	private String uai;
	private String nomProfil;
	private Date dateDebutProfil;
	private Date dateFinProfil;

	/** Default constructor. */
	public ACommeProfil() {
	}

	/**
	 * Constructor.
	 * 
	 * @param string
	 * @param string2
	 * @param string3
	 * @param today
	 * @param object
	 */
	public ACommeProfil(final String pUid, final String pUai, final String pNomProfil, final Date pDebut, final Date pFin) {
		this.uid = pUid;
		this.uai = pUai;
		this.nomProfil = pNomProfil;
		this.dateDebutProfil = pDebut;
		this.dateFinProfil = pFin;
	}

	@Override
	public String toString() {
		return "ACommeProfil [uid=" + this.uid + ", uai=" + this.uai + ", nomProfil=" + this.nomProfil + ", dateDebutProfil="
				+ this.dateDebutProfil + ", dateFinProfil=" + this.dateFinProfil + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.nomProfil == null) ? 0 : this.nomProfil.hashCode());
		result = (prime * result) + ((this.uai == null) ? 0 : this.uai.hashCode());
		result = (prime * result) + ((this.uid == null) ? 0 : this.uid.hashCode());
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
		ACommeProfil other = (ACommeProfil) obj;
		if (this.nomProfil == null) {
			if (other.nomProfil != null) {
				return false;
			}
		} else if (!this.nomProfil.equals(other.nomProfil)) {
			return false;
		}
		if (this.uai == null) {
			if (other.uai != null) {
				return false;
			}
		} else if (!this.uai.equals(other.uai)) {
			return false;
		}
		if (this.uid == null) {
			if (other.uid != null) {
				return false;
			}
		} else if (!this.uid.equals(other.uid)) {
			return false;
		}
		return true;
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

	public Date getDateDebutProfil() {
		return this.dateDebutProfil;
	}

	public void setDateDebutProfil(final Date dateDebutProfil) {
		this.dateDebutProfil = dateDebutProfil;
	}

	public Date getDateFinProfil() {
		return this.dateFinProfil;
	}

	public void setDateFinProfil(final Date dateFinProfil) {
		this.dateFinProfil = dateFinProfil;
	}


}
