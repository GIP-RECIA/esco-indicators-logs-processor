package org.esco.indicators.backend.model;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

public class ConnexionPersonne {

	private Date date;
	private String objectClass;
	private String uid;
	private String uai;

	public ConnexionPersonne() {
		super();
	}

	public ConnexionPersonne(final LogLine logLine) {
		super();

		this.setDate(logLine.getDate());
		this.setObjectClass(logLine.getObjectClass());
		this.setUai(logLine.getUai());
		this.setUid(logLine.getUid());
	}

	public Date getDate() {
		return this.date;
	}

	public void setDate(final Date date) {
		this.date = DateUtils.truncate(date, Calendar.DAY_OF_MONTH);
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

	public String getUai() {
		return this.uai;
	}

	public void setUai(final String uai) {
		this.uai = uai;
	}

	@Override
	public String toString() {
		return this.date + "\t" + this.objectClass + "\t" + this.uid + "\t" + this.uai;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.date == null) ? 0 : this.date.hashCode());
		result = (prime * result) + ((this.objectClass == null) ? 0 : this.objectClass.hashCode());
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
		ConnexionPersonne other = (ConnexionPersonne) obj;
		if (this.date == null) {
			if (other.date != null) {
				return false;
			}
		} else if (!this.date.equals(other.date)) {
			return false;
		}
		if (this.objectClass == null) {
			if (other.objectClass != null) {
				return false;
			}
		} else if (!this.objectClass.equals(other.objectClass)) {
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
