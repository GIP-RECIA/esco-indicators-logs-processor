package org.esco.indicators.backend.model;

public class DonneesConnexionPersonne {

	private int NbCo;
	private double moyenne;

	@Override
	public String toString() {
		return this.NbCo + "\t" + this.moyenne;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + this.NbCo;
		long temp;
		temp = Double.doubleToLongBits(this.moyenne);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
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
		DonneesConnexionPersonne other = (DonneesConnexionPersonne) obj;
		if (this.NbCo != other.NbCo) {
			return false;
		}
		if (Double.doubleToLongBits(this.moyenne) != Double.doubleToLongBits(other.moyenne)) {
			return false;
		}
		return true;
	}

	public int getNbCo() {
		return this.NbCo;
	}

	public void setNbCo(final int nbCo) {
		this.NbCo = nbCo;
	}

	public double getMoyenne() {
		return this.moyenne;
	}

	public void setMoyenne(final double moyenne) {
		this.moyenne = moyenne;
	}

}
