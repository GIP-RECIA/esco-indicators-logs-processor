package org.esco.indicators.backend.model;

public class DonneesEtab {
	private TypeStatEnum typestat;
	private int nbvisites;
	private int nbvisiteurs;

	@Override
	public String toString() {
		return "DonneesEtab [typestat=" + this.typestat + ", nbvisites=" + this.nbvisites + ", nbvisiteurs="
				+ this.nbvisiteurs + "]";
	}

	public int getNbvisites() {
		return this.nbvisites;
	}

	public void setNbvisites(final int nbvisites) {
		this.nbvisites = nbvisites;
	}

	public int getNbvisiteurs() {
		return this.nbvisiteurs;
	}

	public void setNbvisiteurs(final int nbvisiteurs) {
		this.nbvisiteurs = nbvisiteurs;
	}

	public TypeStatEnum getTypestat() {
		return this.typestat;
	}

	public void setTypestat(final TypeStatEnum typestat) {
		this.typestat = typestat;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + this.nbvisites;
		result = (prime * result) + this.nbvisiteurs;
		result = (prime * result) + ((this.typestat == null) ? 0 : this.typestat.hashCode());
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
		DonneesEtab other = (DonneesEtab) obj;
		if (this.nbvisites != other.nbvisites) {
			return false;
		}
		if (this.nbvisiteurs != other.nbvisiteurs) {
			return false;
		}
		if (this.typestat == null) {
			if (other.typestat != null) {
				return false;
			}
		} else if (!this.typestat.equals(other.typestat)) {
			return false;
		}
		return true;
	}

}
