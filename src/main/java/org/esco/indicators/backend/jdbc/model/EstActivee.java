/**
 * 
 */
package org.esco.indicators.backend.jdbc.model;

import java.util.Date;

/**
 * Bean mapping est_activee table.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 *
 */
public class EstActivee {

	private String uid;
	private Date dateDebutActivation;
	private Date dateFinActivation;

	public boolean isActive() {
		return (this.dateDebutActivation != null) && (this.dateFinActivation == null);
	}

	@Override
	public String toString() {
		return "EstActivee [uid=" + this.uid + ", dateDebutActivation=" + this.dateDebutActivation + ", dateFinActivation="
				+ this.dateFinActivation + "]";
	}

	public String getUid() {
		return this.uid;
	}

	public void setUid(final String uid) {
		this.uid = uid;
	}

	public Date getDateDebutActivation() {
		return this.dateDebutActivation;
	}

	public void setDateDebutActivation(final Date dateDebutActivation) {
		this.dateDebutActivation = dateDebutActivation;
	}

	public Date getDateFinActivation() {
		return this.dateFinActivation;
	}

	public void setDateFinActivation(final Date dateFinActivation) {
		this.dateFinActivation = dateFinActivation;
	}


}
