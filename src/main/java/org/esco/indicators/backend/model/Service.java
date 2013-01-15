package org.esco.indicators.backend.model;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

/**
 * Representation of a Service used by someone.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 *
 */
public class Service {

	/** Date of use. */
	private Date date;

	/** User profil. */
	private String objectClass;

	/** User uid. */
	private String uid;

	/** Service fname. */
	private String truncatedFname;

	/** Service representation id. */
	private String service;

	/** User etablishment Id. */
	private String uai;

	public Service() {

	}

	public Service(final LogLine line) {
		this.initWithLine(line);
	}

	protected void initWithLine(final LogLine logLine) {
		this.setDate(logLine.getDate());
		this.setObjectClass(logLine.getObjectClass());
		this.setUid(logLine.getUid());
		this.setUai(logLine.getUai());
		this.setService(logLine.getService());
		this.setTruncatedFname(logLine.getTruncatedFname());
	}

	public Date getDate() {
		return this.date;
	}

	public void setDate(final Date date) {
		this.date = DateUtils.truncate(date, Calendar.DAY_OF_MONTH);
	}

	public String getUai() {
		return this.uai;
	}

	public void setUai(final String uai) {
		this.uai = uai;
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

	public String getService() {
		return this.service;
	}

	public void setService(final String service) {
		this.service = service;
	}

	public String getTruncatedFname() {
		return this.truncatedFname;
	}

	public void setTruncatedFname(final String truncatedFname) {
		this.truncatedFname = truncatedFname;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.date == null) ? 0 : this.date.hashCode());
		result = (prime * result) + ((this.objectClass == null) ? 0 : this.objectClass.hashCode());
		result = (prime * result) + ((this.service == null) ? 0 : this.service.hashCode());
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
		Service other = (Service) obj;
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
		if (this.service == null) {
			if (other.service != null) {
				return false;
			}
		} else if (!this.service.equals(other.service)) {
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

	@Override
	public String toString() {
		return this.date + "\t" + this.objectClass + "\t" + this.uid + "\t" + this.service + "\t" + this.uai;
	}

}
