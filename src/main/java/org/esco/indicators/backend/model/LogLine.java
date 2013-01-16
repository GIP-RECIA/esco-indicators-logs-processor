package org.esco.indicators.backend.model;

import java.util.Date;

import nl.bitwalker.useragentutils.UserAgent;

/**
 * Representaion of a statistique log file line. A line is a record of an user
 * event.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 * 
 */
public class LogLine {

	/** Date of the event. */
	private Date date;

	/** Type of the event. */
	private LogLineTypeEnum eventType;

	/** Portal which record the event. */
	private String portail;

	/** User profil. */
	private String objectClass;

	/** User portal Id. */
	private String uiduPortal;

	/** User UID. */
	private String uid;

	/** User session Id. */
	private String idSession;

	/** Service representation Id. */
	private String service;

	/** Service truncated fname. */
	private String truncatedFname;

	/** Service hit count. */
	private int nbacces;

	/** User etablishment Id. */
	private String uai;

	/** User-Agent. */
	private UserAgent userAgent;

	/** True if logline represent an unknown service from services.conf. */
	private boolean unknownService = false;

	public LogLine() {
		this.nbacces = 0;
		this.idSession = "";
		this.date = null;
		this.objectClass = "";
		this.uid = "";
		this.uai = "";
		this.portail = "";
		this.service = "";
		this.truncatedFname = "";
		this.uai = "";
		this.userAgent = null;
	}

	public String getPortail() {
		return this.portail;
	}

	public void setPortail(final String portail) {
		this.portail = portail;
	}

	public Date getDate() {
		return this.date;
	}

	public void setDate(final Date date) {
		this.date = date;
	}

	public LogLineTypeEnum getEventType() {
		return this.eventType;
	}

	public void setEventType(final LogLineTypeEnum type) {
		this.eventType = type;
	}

	public String getObjectClass() {
		return this.objectClass;
	}

	public void setObjectClass(final String objectClass) {
		this.objectClass = objectClass;
	}

	public String getIdSession() {
		return this.idSession;
	}

	public void setIdSession(final String idSession) {
		this.idSession = idSession;
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

	public int getNbacces() {
		return this.nbacces;
	}

	public void setNbacces(final int nbacces) {
		this.nbacces = nbacces;
	}

	public String getUiduPortal() {
		return this.uiduPortal;
	}

	public void setUiduPortal(final String uiduPortal) {
		this.uiduPortal = uiduPortal;
	}

	public UserAgent getUserAgent() {
		return this.userAgent;
	}

	public void setUserAgent(final UserAgent userAgent) {
		this.userAgent = userAgent;
	}

	public boolean isUnknownService() {
		return this.unknownService;
	}

	public void setUnknownService(final boolean unknownService) {
		this.unknownService = unknownService;
	}

	@Override
	public String toString() {
		return "LogLine [date=" + this.date + ", eventType=" + this.eventType + ", portail=" + this.portail + ", objectClass="
				+ this.objectClass + ", uiduPortal=" + this.uiduPortal + ", uid=" + this.uid + ", idSession=" + this.idSession
				+ ", service=" + this.service + ", truncatedFname=" + this.truncatedFname + ", nbacces=" + this.nbacces + ", uai="
				+ this.uai + ", userAgent=" + this.userAgent + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.date == null) ? 0 : this.date.hashCode());
		result = (prime * result) + ((this.eventType == null) ? 0 : this.eventType.hashCode());
		result = (prime * result) + ((this.idSession == null) ? 0 : this.idSession.hashCode());
		result = (prime * result) + this.nbacces;
		result = (prime * result) + ((this.objectClass == null) ? 0 : this.objectClass.hashCode());
		result = (prime * result) + ((this.portail == null) ? 0 : this.portail.hashCode());
		result = (prime * result) + ((this.service == null) ? 0 : this.service.hashCode());
		result = (prime * result) + ((this.truncatedFname == null) ? 0 : this.truncatedFname.hashCode());
		result = (prime * result) + ((this.uai == null) ? 0 : this.uai.hashCode());
		result = (prime * result) + ((this.uid == null) ? 0 : this.uid.hashCode());
		result = (prime * result) + ((this.uiduPortal == null) ? 0 : this.uiduPortal.hashCode());
		result = (prime * result) + ((this.userAgent == null) ? 0 : this.userAgent.hashCode());
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
		LogLine other = (LogLine) obj;
		if (this.date == null) {
			if (other.date != null) {
				return false;
			}
		} else if (!this.date.equals(other.date)) {
			return false;
		}
		if (this.eventType != other.eventType) {
			return false;
		}
		if (this.idSession == null) {
			if (other.idSession != null) {
				return false;
			}
		} else if (!this.idSession.equals(other.idSession)) {
			return false;
		}
		if (this.nbacces != other.nbacces) {
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
		if (this.service == null) {
			if (other.service != null) {
				return false;
			}
		} else if (!this.service.equals(other.service)) {
			return false;
		}
		if (this.truncatedFname == null) {
			if (other.truncatedFname != null) {
				return false;
			}
		} else if (!this.truncatedFname.equals(other.truncatedFname)) {
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
		if (this.uiduPortal == null) {
			if (other.uiduPortal != null) {
				return false;
			}
		} else if (!this.uiduPortal.equals(other.uiduPortal)) {
			return false;
		}
		if (this.userAgent == null) {
			if (other.userAgent != null) {
				return false;
			}
		} else if (!this.userAgent.equals(other.userAgent)) {
			return false;
		}
		return true;
	}

}
