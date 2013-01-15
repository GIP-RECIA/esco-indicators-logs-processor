/**
 * 
 */
package org.esco.indicators.backend.jdbc.model;

import java.util.Date;

import org.esco.indicators.backend.model.ConnexionPersonne;
import org.esco.indicators.backend.model.DonneesConnexionPersonne;


/**
 * Bean mapping SeConnecteSemaine et SeConnecteMois tables.
 * 2 SeConnectePeriode are comprimable if they share the same compressionKey.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 *
 */
public class SeConnectePeriode implements Comparable<SeConnectePeriode>{

	private CompressionKey compressionKey;
	private String uid;
	private double moyenne;

	public SeConnectePeriode() {
		super();
	}

	public SeConnectePeriode(final CompressionKey compressionKey) {
		super();
		this.compressionKey = compressionKey;
	}

	public SeConnectePeriode(final ConnexionPersonne cp, final DonneesConnexionPersonne dcp) {
		super();
		this.compressionKey = new CompressionKey(cp.getUai(), cp.getObjectClass(), cp.getDate(), dcp.getNbCo());
		this.uid = cp.getUid();
		this.moyenne = dcp.getMoyenne();
	}

	@Override
	public int compareTo(final SeConnectePeriode o) {
		return this.compressionKey.compareTo(o.getCompressionKey());
	}

	@Override
	public String toString() {
		return "SeConnectePeriode [compressionKey=" + this.compressionKey + ", uid=" + this.uid + ", moyenne=" + this.moyenne + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.compressionKey.getNomProfil() == null) ? 0 : this.compressionKey.getNomProfil().hashCode());
		result = (prime * result) + ((this.compressionKey.getPremierJourPeriode() == null) ? 0 : this.compressionKey.getPremierJourPeriode().hashCode());
		result = (prime * result) + ((this.compressionKey.getUai() == null) ? 0 : this.compressionKey.getUai().hashCode());
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
		SeConnectePeriode other = (SeConnectePeriode) obj;
		if (this.compressionKey.getNomProfil() == null) {
			if (other.compressionKey.getNomProfil() != null) {
				return false;
			}
		} else if (!this.compressionKey.getNomProfil().equals(other.compressionKey.getNomProfil())) {
			return false;
		}
		if (this.compressionKey.getPremierJourPeriode() == null) {
			if (other.compressionKey.getPremierJourPeriode() != null) {
				return false;
			}
		} else if (!this.compressionKey.getPremierJourPeriode().equals(other.compressionKey.getPremierJourPeriode())) {
			return false;
		}
		if (this.compressionKey.getUai() == null) {
			if (other.compressionKey.getUai() != null) {
				return false;
			}
		} else if (!this.compressionKey.getUai().equals(other.compressionKey.getUai())) {
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

	public CompressionKey getCompressionKey() {
		return this.compressionKey;
	}

	public String getUai() {
		return this.compressionKey.getUai();
	}

	public String getNomProfil() {
		return this.compressionKey.getNomProfil();
	}

	public String getUid() {
		return this.uid;
	}
	public void setUid(final String uid) {
		this.uid = uid;
	}
	public Date getPremierJourPeriode() {
		return this.compressionKey.getPremierJourPeriode();
	}

	public int getNbConnexion() {
		return this.compressionKey.getNbConnexion();
	}

	public double getMoyenne() {
		return this.moyenne;
	}
	public void setMoyenne(final double moyenne) {
		this.moyenne = moyenne;
	}

}