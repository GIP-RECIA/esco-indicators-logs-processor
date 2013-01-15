package org.esco.indicators.backend.model;

import java.util.Date;

public class ConnexionEtablissementPersonne {

	private String uai;
	private String typeEtab;
	private Date date;
	private String uid;

	public Date getDate() {
		return this.date;
	}

	public void setDate(final Date date) {
		this.date = date;
	}

	public String getUai() {
		return this.uai;
	}

	public void setUai(final String uai) {
		this.uai = uai;
	}

	public String getUid() {
		return this.uid;
	}

	public void setUid(final String uid) {
		this.uid = uid;
	}

	public String getTypeEtab() {
		return this.typeEtab;
	}

	public void setTypeEtab(final String typeEtab) {
		this.typeEtab = typeEtab;
	}

	@Override
	public String toString() {
		return "ConnexionEtablissementPersonne [uai=" + this.uai + ", typeEtab=" + this.typeEtab + ", date="
				+ this.date + ", uid=" + this.uid + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.date == null) ? 0 : this.date.hashCode());
		result = (prime * result) + ((this.typeEtab == null) ? 0 : this.typeEtab.hashCode());
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
		ConnexionEtablissementPersonne other = (ConnexionEtablissementPersonne) obj;
		if (this.date == null) {
			if (other.date != null) {
				return false;
			}
		} else if (!this.date.equals(other.date)) {
			return false;
		}
		if (this.typeEtab == null) {
			if (other.typeEtab != null) {
				return false;
			}
		} else if (!this.typeEtab.equals(other.typeEtab)) {
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

}
