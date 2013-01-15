package org.esco.indicators.backend.model;


public class Sstart {

	private String portail;
	private String objectClass;
	private String uid;
	private String idSession;
	private String uai;

	public Sstart() {
	}

	public Sstart(final LogLine l) {
		this.setIdSession(l.getIdSession());
		this.setObjectClass(l.getObjectClass());
		this.setPortail(l.getPortail());
		this.setUai(l.getUai());
		this.setUid(l.getUid());
	}

	public String getPortail() {
		return this.portail;
	}

	public void setPortail(final String portail) {
		this.portail = portail;
	}

	public String getObjectClass() {
		return this.objectClass;
	}

	public void setObjectClass(final String objectClass) {
		this.objectClass = objectClass;
	}

	public String getUid() {
		return this.uid;
	}

	public void setUid(final String uid) {
		this.uid = uid;
	}

	public String getIdSession() {
		return this.idSession;
	}

	public void setIdSession(final String idSession) {
		this.idSession = idSession;
	}

	public String getUai() {
		return this.uai;
	}

	public void setUai(final String uai) {
		this.uai = uai;
	}

	@Override
	public String toString() {
		return this.portail + "\t" + this.objectClass + "\t" + this.uid + "\t" + this.idSession + "\t" + this.uai;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.idSession == null) ? 0 : this.idSession.hashCode());
		result = (prime * result) + ((this.objectClass == null) ? 0 : this.objectClass.hashCode());
		result = (prime * result) + ((this.portail == null) ? 0 : this.portail.hashCode());
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
		Sstart other = (Sstart) obj;
		if (this.idSession == null) {
			if (other.idSession != null) {
				return false;
			}
		} else if (!this.idSession.equals(other.idSession)) {
			return false;
		}
		if (this.objectClass == null) {
			if (other.objectClass != null) {
				return false;
			}
		} else if (!this.objectClass.equals(other.objectClass)) {
			return false;
		}
		if (this.portail == null) {
			if (other.portail != null) {
				return false;
			}
		} else if (!this.portail.equals(other.portail)) {
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
