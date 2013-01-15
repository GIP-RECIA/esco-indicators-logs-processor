package org.esco.indicators.backend.utils;

/*
 * Projet ENT-CRLR - Conseil Regional Languedoc Roussillon.
 * Copyright (c) 2009 Bull SAS
 *
 * $Id: jalopy.xml,v 1.1 2009/03/17 16:30:44 ent_breyton Exp $
 */

//import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public final class DatasConfiguration {

	/** Logger. */
	private static Logger logger = Logger.getLogger(DatasConfiguration.class);
	private static Map<String, String[]> profils;


	/**
	 * Map of services : bind fname truncated with service name.
	 * key = Column 1 if not empty, Col 0 otherwise. Value = Col 2.
	 */
	private static Map<String, String> services;

	/**
	 * Map of correspondance : bind grouping fname to real fname.
	 * key = Col 0. Value = Column 1 if not empty, Col 0 otherwise.
	 */
	private static Map<String, String> correspondanceService;

	private DatasConfiguration() {
	}

	public static Map<String, String[]> getProfils() {
		if (DatasConfiguration.profils == null) {
			DatasConfiguration.logger.debug("Géneration des profils");
			// 1 Recuperer le fichier
			final File f = new File("conf/profils.conf");
			if (!f.exists()) {
				DatasConfiguration.logger.fatal("Impossible de trouver le fichier de configuration des profils ");
				return null;
			}

			DatasConfiguration.profils = new LinkedHashMap<String, String[]>();

			// 2 Lire le fichier et stocker les données
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF8"));

				String ligne;
				while ((ligne = br.readLine()) != null) {
					final int firstIndexDiese = ligne.indexOf("#");
					if (firstIndexDiese >= 0) {
						ligne = ligne.substring(0, firstIndexDiese);
					}

					final String temp = ligne.replaceAll("\\s", "");
					if ("".equals(temp)) {
						continue;
					}

					final String[] profil = ligne.split("\\$");
					if (profil.length < 3) {
						DatasConfiguration.logger.error("Ligne de profil non traité : " + ligne);
						continue;
					}

					final String[] tab = new String[2];
					tab[0] = profil[1].trim();
					tab[1] = "#" + profil[2].replaceAll("\\s", "");
					DatasConfiguration.profils.put(profil[0].replaceAll("\\s", ""), tab);
				}
			} catch (IOException e) {
				DatasConfiguration.logger.fatal("Erreur lors de la lecture du fichier de configuration des profils : " + e.getMessage());
				DatasConfiguration.logger.fatal(e.getStackTrace());
				return null;
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						DatasConfiguration.logger.fatal("Erreur lors de la fermeture du fichier de configuration des profils : "
								+ e.getMessage());
						DatasConfiguration.logger.fatal(e.getStackTrace());
					}
				}
			}
		}
		return DatasConfiguration.profils;
	}

	protected static Map<String, String> getServices() {
		if (DatasConfiguration.services == null) {
			DatasConfiguration.initServices();
		}
		return DatasConfiguration.services;
	}

	protected static Map<String, String> getCorrespondanceService() {
		if (DatasConfiguration.correspondanceService == null) {
			DatasConfiguration.initServices();
		}
		return DatasConfiguration.correspondanceService;
	}

	private static void initServices() {
		DatasConfiguration.logger.debug("Géneration des services");

		// 1 Recuperer le fichier
		final File f = new File("conf/services.conf");
		if (!f.exists()) {
			DatasConfiguration.logger.fatal("Impossible de trouver le fichier de configuration des services ");
			return;
		}

		DatasConfiguration.services = new LinkedHashMap<String, String>();
		DatasConfiguration.correspondanceService = new LinkedHashMap<String, String>();

		// 2 Lire le fichier et stocker les données
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF8"));

			String ligne;
			while ((ligne = br.readLine()) != null) {
				final int firstIndexDiese = ligne.indexOf("#");
				if (firstIndexDiese >= 0) {
					ligne = ligne.substring(0, firstIndexDiese);
				}
				final String temp = ligne.replaceAll("\\s", "");
				if ("".equals(temp)) {
					continue;
				}

				final String[] profil = ligne.split("\\$");
				if (profil.length < 3) {
					DatasConfiguration.logger.error("Ligne de service non traité : " + ligne);
					continue;
				}

				final String codeService;

				// truncated fname
				final String column0 = profil[0].replaceAll("\\s", "");
				// grouping fname
				final String column1 = profil[1].replaceAll("\\s", "");
				// service name
				final String column2 = profil[2].trim();

				if (StringUtils.hasText(column1)) {
					// Si un code de regroupement
					codeService = column1;
					if (!DatasConfiguration.correspondanceService.containsKey(column0)) {
						DatasConfiguration.correspondanceService.put(column0, column1);
					}
				} else {
					codeService = column0;
				}

				if (!DatasConfiguration.services.containsKey(codeService)) {
					// Add service name in map by grouping fname or by truncated fname
					DatasConfiguration.services.put(codeService, column2);
				}


				DatasConfiguration.logger.debug(String.format("Regroupement de services Map: [%1$s].",
						DatasConfiguration.correspondanceService.toString()));
				DatasConfiguration.logger.debug(String.format("Services Map: [%1$s].",
						DatasConfiguration.services.toString()));
			}
		} catch (IOException e) {
			DatasConfiguration.logger.fatal("Erreur lors de la lecture du fichier de services des profils : " + e.getMessage());
			DatasConfiguration.logger.fatal(e.getStackTrace());
			return;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					DatasConfiguration.logger.fatal("Erreur lors de la fermeture du fichier de configuration des services : "
							+ e.getMessage());
					DatasConfiguration.logger.fatal(e.getStackTrace());
				}
			}
		}
	}

	/**
	 * Look in configuration for corresponding service name.
	 * 
	 * @param truncatedFname
	 * @return the service name or null if not found
	 */
	public static String findServiceName(final String truncatedFname) {
		final String serviceName;

		final String groupingFname = DatasConfiguration.getCorrespondanceService().get(truncatedFname);
		if (groupingFname != null) {
			serviceName = DatasConfiguration.getServices().get(groupingFname);
		} else {
			serviceName = DatasConfiguration.getServices().get(truncatedFname);
		}

		return serviceName;
	}

}
