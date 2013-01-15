/**
 * 
 */
package org.esco.indicators.backend.jdbc.model;

import java.text.ParseException;
import java.util.Date;

import org.esco.indicators.backend.model.DonneesConnexion;
import org.esco.indicators.backend.model.DonneesEtab;



/**
 * Bean mapping NombreDeVisiteurs table.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 *
 */
public class NombreDeVisiteurs {

	private Date jour;
	private String uai;
	private int nbVisites;
	private int nbVisiteurs;
	private String typeEtab;
	private String typeStat;

	/**
	 * NombreDeVisiteurs builder.
	 * 
	 * @param connectionData Connexion Data
	 * @param etabData Etablishment data
	 * @throws ParseException if bad date format
	 */
	public NombreDeVisiteurs(final DonneesConnexion connectionData, final DonneesEtab etabData) {
		this.jour = connectionData.getDate();
		this.uai = connectionData.getUai();
		this.typeEtab = connectionData.getTypeEtab();
		this.nbVisiteurs = etabData.getNbvisiteurs();
		this.nbVisites =	etabData.getNbvisites();
		this.typeStat = etabData.getTypestat().name();
	}

	@Override
	public String toString() {
		return "NombreVisiteurs [jour=" + this.jour + ", uai=" + this.uai + ", nbVisites=" + this.nbVisites + ", nbVisiteurs="
				+ this.nbVisiteurs + ", typeEtab=" + this.typeEtab + ", typeStat=" + this.typeStat + "]";
	}

	public Date getJour() {
		return this.jour;
	}

	public void setJour(final Date jour) {
		this.jour = jour;
	}

	public String getUai() {
		return this.uai;
	}

	public void setUai(final String uai) {
		this.uai = uai;
	}

	public int getNbVisites() {
		return this.nbVisites;
	}

	public void setNbVisites(final int nbVisites) {
		this.nbVisites = nbVisites;
	}

	public int getNbVisiteurs() {
		return this.nbVisiteurs;
	}

	public void setNbVisiteurs(final int nbVisiteurs) {
		this.nbVisiteurs = nbVisiteurs;
	}

	public String getTypeEtab() {
		return this.typeEtab;
	}

	public void setTypeEtab(final String typeEtab) {
		this.typeEtab = typeEtab;
	}

	public String getTypeStat() {
		return this.typeStat;
	}

	public void setTypeStat(final String typeStat) {
		this.typeStat = typeStat;
	}

}