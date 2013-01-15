/**
 * 
 */
package org.esco.indicators.backend.jdbc.model;

import java.text.ParseException;
import java.util.Date;

import org.esco.indicators.backend.model.Service;


/**
 * Bean mapping ConnexionServiceJour table.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 *
 */
public class ConnexionServiceJour {

	private String uid;
	private String nomProfil;
	private String truncatedFname;
	private String uai;
	private String nomService;
	private Date jour;
	private int nbConnexionService;

	/**
	 * Build a ConnexionServiceJour.
	 * 
	 * @param service the backend Service
	 * @param nbConnexion the count of connexion for the Service
	 * @throws ParseException if bad date format
	 */
	public ConnexionServiceJour(final Service service, final int nbConnexion) {
		this.jour = service.getDate();
		this.nomProfil = service.getObjectClass();
		this.uid = service.getUid();
		this.uai = service.getUai();
		this.nomService = service.getService();
		this.truncatedFname = service.getTruncatedFname();
		this.nbConnexionService = nbConnexion;
	}

	@Override
	public String toString() {
		return "ConnexionServiceJour [uid=" + this.uid + ", nomProfil=" + this.nomProfil + ", uai=" + this.uai + ", nomService="
				+ this.nomService + ", jour=" + this.jour + ", nbConnexionService=" + this.nbConnexionService + "]";
	}

	public String getUid() {
		return this.uid;
	}

	public void setUid(final String uid) {
		this.uid = uid;
	}

	public String getNomProfil() {
		return this.nomProfil;
	}

	public void setNomProfil(final String nomProfil) {
		this.nomProfil = nomProfil;
	}

	public String getUai() {
		return this.uai;
	}

	public void setUai(final String uai) {
		this.uai = uai;
	}

	public String getNomService() {
		return this.nomService;
	}

	public void setNomService(final String nomService) {
		this.nomService = nomService;
	}

	public String getTruncatedFname() {
		return this.truncatedFname;
	}

	public void setTruncatedFname(final String truncatedFname) {
		this.truncatedFname = truncatedFname;
	}

	public Date getJour() {
		return this.jour;
	}

	public void setJour(final Date jour) {
		this.jour = jour;
	}

	public int getNbConnexionService() {
		return this.nbConnexionService;
	}

	public void setNbConnexionService(final int nbConnexionService) {
		this.nbConnexionService = nbConnexionService;
	}

}