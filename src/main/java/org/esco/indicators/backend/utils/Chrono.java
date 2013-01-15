/*
 * Projet ENT-CRLR - Conseil Regional Languedoc Roussillon.
 * Copyright (c) 2009 Bull SAS
 *
 * $Id: jalopy.xml,v 1.1 2009/03/17 16:30:44 ent_breyton Exp $
 */

package org.esco.indicators.backend.utils;

import java.io.Serializable;

/**
 * Chronomètre. Permet de mesurer le temps entre deux actions, en millisecondes.
 * Cette classe est <i>thread-safe</i>.
 * 
 * @author romana
 */
public class Chrono implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1869954178875988397L;

	/** The start time. */
	private long startTime;

	/** The elapsed. */
	private long elapsed;

	/** The running. */
	private boolean running;

	/**
	 * The Constructor.
	 */
	public Chrono() {
		this.reset();
	}

	/**
	 * Remet à zéro le chronomètre.
	 */
	public synchronized void reset() {
		this.startTime = 0;
		this.elapsed = 0;
		this.running = false;
	}

	/**
	 * Retourne si le chronomètre a été démarré.
	 * 
	 * @return true, if is running
	 */
	public synchronized boolean isRunning() {
		return this.running;
	}

	/**
	 * Démarre le chronomètre.
	 * 
	 * @throws Exception
	 */
	public synchronized void start() {
		if (this.running) {
			System.err.print("Chrono already running");
			return;
		}
		this.startTime = System.currentTimeMillis();
		this.running = true;
	}

	/**
	 * Arrête le chronomètre.
	 * 
	 * @throws Exception
	 */
	public synchronized void stop() {
		if (!this.running) {
			System.err.print("Chrono not running");
			return;
		}
		final long stopTime = System.currentTimeMillis();
		this.elapsed = stopTime - this.startTime;
		this.running = false;
	}

	/**
	 * Retourne le temps intermédiaire.
	 * 
	 * @return the last time
	 */
	public synchronized long getLastTime() {
		return System.currentTimeMillis() - this.startTime;
	}

	/**
	 * Retourne le temps mesuré par le chronomètre.
	 * 
	 * @return the elapsed
	 * 
	 * @throws Exception
	 */
	public synchronized long getElapsed() {
		if (this.running) {
			System.err.print("Chrono not stopped");
			return 0;
		}
		return this.elapsed;
	}

	/**
	 * Exécute une interface {@link Runnable}, en mesurant le temps passé.
	 * 
	 * @param runnable
	 *            the runnable
	 * 
	 * @throws Exception
	 */
	public synchronized void run(final Runnable runnable) {
		this.start();
		try {
			runnable.run();
		} catch (Exception e) {
			System.err.print("Exception");
			System.err.print(e.getStackTrace());
			return;
		} finally {
			this.stop();
		}
	}

	/**
	 * Retourne la duree en XXh XXm XXs.
	 * 
	 * @return duree au format humain.
	 */
	public String getDureeHumain() {
		long nbMinutes = this.elapsed / 60000;
		long nbHeures = 0;
		if (nbMinutes >= 60) {
			nbHeures = nbMinutes / 60;
			nbMinutes = nbMinutes % 60;
		}
		final long nbSecondes = (this.elapsed % 60000) / 1000;
		final long nbMillisecondes = this.elapsed % 1000;
		String retour = "";
		if (nbHeures > 0) {
			retour += (nbHeures + "h ");
		}
		if ((nbHeures > 0) || (nbMinutes > 0)) {
			retour += (this.padLeft("" + nbMinutes, 2, '0') + "m ");
		}
		if ((nbHeures > 0) || (nbMinutes > 0) || (nbSecondes > 0)) {
			retour += (this.padLeft("" + nbSecondes, 2, '0') + "s ");
		}
		return retour + this.padLeft("" + nbMillisecondes, 3, '0') + "ms";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this.elapsed + " ms";
	}

	/**
	 * Ajoute autant de 'caractere' au début de la chaine 'valeur' pour obtenir
	 * la taille 'tailleTotale'.
	 * 
	 * @param valeur
	 *            chaine de base
	 * @param tailleTotale
	 *            taille totale de la chaine de retour
	 * @param caractere
	 *            caractère à ajouter
	 * 
	 * @return la chaine avec les caractères ajoutés.
	 */
	public String padLeft(final String valeur, final int tailleTotale, final char caractere) {
		final StringBuilder retour = new StringBuilder();
		final String valeurPasNull = this.trimToBlank(valeur);
		for (int i = valeurPasNull.length(); i < tailleTotale; i++) {
			retour.append(caractere);
		}
		if (valeurPasNull.length() <= tailleTotale) {
			retour.append(valeurPasNull);
		} else {
			retour.append(valeurPasNull, 0, tailleTotale);
		}

		return retour.toString();
	}

	/**
	 * Supprime les caractères d'espacement (espace, tabulation, etc...) en
	 * début et en fin de chaîne. Si le résultat est une chaîne vide, ou si la
	 * chaîne en entrée vaut <code>null</code>, une chaîne vide est renvoyée.
	 * 
	 * @param str
	 *            the str
	 * 
	 * @return the string
	 */
	public String trimToBlank(final String str) {
		final String newStr = this.trimToNull(str);
		return (newStr != null) ? newStr : "";
	}

	/**
	 * 
	 * 
	 * @param str
	 *            DOCUMENTATION INCOMPLETE!
	 * 
	 * @return DOCUMENTATION INCOMPLETE!
	 */
	private String trimToNull(final String str) {
		if (str == null) {
			return null;
		}
		final String newStr = str.trim();
		return (newStr.length() == 0) ? null : newStr;
	}
}
