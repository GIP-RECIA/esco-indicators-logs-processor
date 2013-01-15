/**
 * 
 */
package org.esco.indicators.backend.jdbc.model;

import java.util.Date;

/**
 * Key used for compression system.
 * Equals and hashcode are specific. They are design to regroup SeConnectePeriode for compression.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 *
 */
public class CompressionKey implements Comparable<CompressionKey> {

	private String uai;
	private String nomProfil;
	private Date premierJourPeriode;
	private int nbConnexion;

	public CompressionKey(final String uai, final String nomProfil, final Date premierJourPeriode, final int nbConnexion) {
		super();
		this.uai = uai;
		this.nomProfil = nomProfil;
		this.premierJourPeriode = premierJourPeriode;
		this.nbConnexion = nbConnexion;
	}

	/**
	 * Sorted by premierJourPeriode.
	 * Not consistent with equals !
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(final CompressionKey o) {
		if (this.premierJourPeriode.after(o.premierJourPeriode)) {
			return 1;
		} else if (this.premierJourPeriode.before(o.premierJourPeriode)) {
			return -1;
		}

		return 0;
	}

	@Override
	public String toString() {
		return "SeConnectePeriode [uai=" + this.uai + ", nomProfil=" + this.nomProfil + ", premierJourPeriode="
				+ this.premierJourPeriode + ", nbConnexion=" + this.nbConnexion + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + this.nbConnexion;
		result = (prime * result) + ((this.nomProfil == null) ? 0 : this.nomProfil.hashCode());
		result = (prime * result) + ((this.premierJourPeriode == null) ? 0 : this.premierJourPeriode.hashCode());
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
		CompressionKey other = (CompressionKey) obj;
		if (this.nbConnexion != other.nbConnexion) {
			return false;
		}
		if (this.nomProfil == null) {
			if (other.nomProfil != null) {
				return false;
			}
		} else if (!this.nomProfil.equals(other.nomProfil)) {
			return false;
		}
		if (this.premierJourPeriode == null) {
			if (other.premierJourPeriode != null) {
				return false;
			}
		} else if (!this.premierJourPeriode.equals(other.premierJourPeriode)) {
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

	public Date getPremierJourPeriode() {
		return this.premierJourPeriode;
	}

	public void setPremierJourPeriode(final Date premierJourPeriode) {
		this.premierJourPeriode = premierJourPeriode;
	}

	public int getNbConnexion() {
		return this.nbConnexion;
	}

	public void setNbConnexion(final int nbConnexion) {
		this.nbConnexion = nbConnexion;
	}


}
