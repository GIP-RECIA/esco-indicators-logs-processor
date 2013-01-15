package org.esco.indicators.backend.service;


import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.esco.indicators.backend.model.CompressionModeEnum;
import org.esco.indicators.backend.model.ProcessingModeEnum;
import org.esco.indicators.backend.utils.Chrono;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;


/**
 * Lance le traitement global de calcul des stats.
 * 
 * 1- Execute le traitement LDAP
 * 2- Execute le traitement des logs
 * 3- Execute le traitement semaines/mois
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 *
 */
public class TraitementGlobal {

	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(TraitementGlobal.class);

	/** Configuraion. */
	private static final Config CONFIG = Config.getInstance();

	/** Date format for day of year. */
	private static final SimpleDateFormat dayOfYearFormat = new SimpleDateFormat("yyyy-MM-dd");

	/** Date format for exact time. */
	private static final SimpleDateFormat exactTimeFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss,SSS");

	public static void main(final String[] args) {
		try {
			final Chrono c = new Chrono();
			c.start();

			TraitementGlobal.LOGGER.info(" ");
			TraitementGlobal.LOGGER.info(" ");
			TraitementGlobal.LOGGER.info("-------------------- Lancement du batch des indicateurs d'usages --------------------");
			TraitementGlobal.LOGGER.info(String.format("Date du lancement : [%1$s].", TraitementGlobal.exactTimeFormat.format(new Date())));
			TraitementGlobal.LOGGER.info(String.format("Lancement avec les arguments : [%1$s].", ArrayUtils.toString(args)));
			TraitementGlobal.LOGGER.info("-------------------------------------------------------------------------------------");
			TraitementGlobal.LOGGER.info(" ");

			TraitementGlobal.checkArgsCount(args, 0);

			// ---------- Compression mode from config file
			final CompressionModeEnum compressionMode = TraitementGlobal.parseCompressionMode(args);
			Assert.notNull(compressionMode, "Bad configuration for compression mode !");

			// ---------- Processing mode from config file
			String processingModeConf = TraitementGlobal.CONFIG.getConfigValue(Config.CONF_PROCESSING_MODE);
			final ProcessingModeEnum processingMode = TraitementGlobal.parseProcessingMode(processingModeConf);
			Assert.notNull(processingMode, "Bad configuration for processing mode in config.properties !");

			// ---------- Wich traitement launch ?
			if (TraitementGlobal.isCompressionModeManuel(compressionMode)) {
				TraitementGlobal.traitementCompressionManuel(compressionMode, args);

			} else {
				TraitementGlobal.traitementAutomatique(compressionMode, processingMode, args);
			}

			// ---------- End
			c.stop();

			TraitementGlobal.LOGGER.info(String.format("Fin de l'application. Temps total : %s",
					c.getDureeHumain()));

		} catch (Exception e) {
			TraitementGlobal.LOGGER.error("Error during stats processing !", e);
			System.exit(1);
		}

		System.exit(0);
	}

	/**
	 * Traitement automatique par defaut.
	 * 
	 * @param compressionMode
	 * @param processingMode
	 * @param args
	 * @throws Exception
	 */
	protected static void traitementAutomatique(final CompressionModeEnum compressionMode,
			final ProcessingModeEnum processingMode, final String[] args) throws Exception {
		TraitementGlobal.checkArgsCount(args, 1);

		TraitementGlobal.LOGGER.info("------------------------- Traitement automatique séléctioné -------------------------");

		// last arg
		final String filePath = args[args.length - 1];

		if (!StringUtils.hasText(filePath)) {
			System.out.println("Incorrect file path argument !");
			TraitementGlobal.printUsageAndExit();
		}

		final File f = new File(filePath);
		if (!f.exists()) {
			System.out.println(filePath + " file doesn't exists !");
			TraitementGlobal.printUsageAndExit();
		}

		// ---------- LDAP processing ----------
		final TraitementLDAP traitementLdap = new TraitementLDAP();
		traitementLdap.traitementLdap();

		// ---------- Log processing ----------
		final Lecture lecture = new Lecture();
		lecture.traitementLog(filePath, processingMode, false);

		// ---------- Compression processing ----------
		final TraitementFinMoisSemaine traitementFin = new TraitementFinMoisSemaine();
		// Compression jusqu'à la veille minuit car traitement auto de nuit
		final Calendar hier = Calendar.getInstance();
		hier.add(Calendar.DAY_OF_YEAR, -1);
		traitementFin.traitementCompression(compressionMode, hier.getTime());

	}

	/**
	 * Traitement de compression manuel uniquement !
	 * 
	 * @param compressionMode
	 * @param args
	 * @throws Exception
	 */
	protected static void traitementCompressionManuel(final CompressionModeEnum compressionMode,
			final String[] args) throws Exception {
		TraitementGlobal.checkArgsCount(args, 2);

		TraitementGlobal.LOGGER.info("------------------------- Compression manuelle séléctionée --------------------------");

		try {
			Date compressionDate = (TraitementGlobal.dayOfYearFormat.parse(args[1]));

			// ---------- Compression processing
			final TraitementFinMoisSemaine traitementFin = new TraitementFinMoisSemaine();
			traitementFin.traitementCompression(compressionMode, compressionDate);
		} catch (ParseException e) {
			System.out.println("Incorrect compression date argument !");
			TraitementGlobal.printUsageAndExit();
		}

	}

	protected static ProcessingModeEnum parseProcessingMode(final String processingModeConf) {
		ProcessingModeEnum processingMode = null;
		try {
			processingMode = ProcessingModeEnum.valueOf(processingModeConf);
		} catch (IllegalArgumentException e) {
			// Unable to parse processing mode
		}
		return processingMode;
	}

	protected static CompressionModeEnum parseCompressionMode(final String compressionModeConf) {
		CompressionModeEnum compressionMode = null;
		try {
			compressionMode = CompressionModeEnum.valueOf(compressionModeConf);
		} catch (IllegalArgumentException e) {
			// Unable to parse compression mode
		}
		return compressionMode;
	}

	protected static CompressionModeEnum parseCompressionMode(final String[] args) {
		CompressionModeEnum compressionMode = null;
		if ((args != null) && (args.length > 0)) {
			compressionMode = TraitementGlobal.parseCompressionMode(args[0]);
		}

		if(compressionMode == null) {
			compressionMode = CompressionModeEnum.AUTO;
			TraitementGlobal.LOGGER.warn("Compression mode AUTO selected by default !");
		}

		return compressionMode;
	}
	/**
	 * Check args count.
	 * 
	 * @param args
	 * @param argsCount
	 */
	protected static void checkArgsCount(final String[] args, final int argsCount) {
		if ((args == null) || (args.length < argsCount)) {
			TraitementGlobal.printUsageAndExit();
		}
	}

	/**
	 * Check if the user ask for a specific compression only.
	 * 
	 * @param compressionMode
	 * @return
	 */
	protected static boolean isCompressionModeManuel(final CompressionModeEnum compressionMode) {
		return (compressionMode == CompressionModeEnum.MONTH) || (compressionMode == CompressionModeEnum.WEEK);
	}

	/** Print usage to user and exit app. */
	protected static void printUsageAndExit() {
		System.out.println("Usage : indicateurs_usages [CompressionMode] StatsLogFilePath");
		System.out.println("Usage : indicateurs_usages CompressionMode CompressionDate");
		System.out.println("StatsLogFilePath : path to the log file containing stats. Needed only with CompressionMode to AUTO or OFF.");
		System.out.println("CompressionMode : AUTO | WEEK | MONTH | OFF");
		System.out.println("CompressionDate (ex 2012-08-01) : the date representing the period to compress. Needed only with CompressionMode to WEEK or MONTH.");

		TraitementGlobal.LOGGER.error("Bad arguments supplied !");
		System.exit(1);
	}
}
