/**
 * 
 */
package org.esco.indicators.backend.jdbc.model;

/**
 * Bean mapping etablissement table.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 *
 */
public class Etablissement {

	private String uai;
	private String siren;
	private String departement;
	private String typeEtablissement;


	/** Default constructor. */
	public Etablissement() {
	}

	/**
	 * Etablissement builder.
	 * 
	 * @param uai2
	 * @param departement2
	 * @param typeEtab
	 */
	public Etablissement(final String uai2, final String siren2, final String departement2, final String typeEtab) {
		this.uai = uai2;
		this.siren = siren2;
		this.departement = departement2;
		this.typeEtablissement = typeEtab;
	}

	@Override
	public String toString() {
		return "UAI# " + this.uai;
	}

	public String getUai() {
		return this.uai;
	}

	public void setUai(final String uai) {
		this.uai = uai;
	}

	public String getSiren() {
		return this.siren;
	}

	public void setSiren(final String siren) {
		this.siren = siren;
	}

	public String getDepartement() {
		return this.departement;
	}

	public void setDepartement(final String departement) {
		this.departement = departement;
	}

	public String getTypeEtablissement() {
		return this.typeEtablissement;
	}

	public void setTypeEtablissement(final String typeEtablissement) {
		this.typeEtablissement = typeEtablissement;
	}


}
