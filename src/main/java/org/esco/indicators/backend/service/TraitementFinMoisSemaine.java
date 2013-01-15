package org.esco.indicators.backend.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.esco.indicators.backend.exception.TransactionException;
import org.esco.indicators.backend.jdbc.JDBC;
import org.esco.indicators.backend.jdbc.model.CompressionKey;
import org.esco.indicators.backend.jdbc.model.ConnexionProfilPeriode;
import org.esco.indicators.backend.jdbc.model.SeConnectePeriode;
import org.esco.indicators.backend.model.CompressionModeEnum;
import org.esco.indicators.backend.utils.Chrono;
import org.esco.indicators.backend.utils.DateHelper;
import org.springframework.util.Assert;


public class TraitementFinMoisSemaine {

	private static final Logger LOGGER = Logger.getLogger(TraitementFinMoisSemaine.class.getName());

	/** Date format for month of year. */
	private static final SimpleDateFormat dayOfYearFormat = new SimpleDateFormat("yyyy-MM-dd");

	/** Date format for month of year. */
	private static final SimpleDateFormat monthOfYearFormat = new SimpleDateFormat("yyyy-MM");

	/** Date format for day of month. */
	private static final SimpleDateFormat dayOfMonthFormat = new SimpleDateFormat("d MMM");

	/** Special label in DB for empty compression rows. */
	private static final String EMPTY_COMPRESSION = "EMPTY";

	private JDBC jdbc;

	/** Months already processed. */
	private Set<Date> allProcessedMonth;

	/** Weeks already processed. */
	private Set<Date> allProcessedWeek;

	public TraitementFinMoisSemaine() throws ClassNotFoundException, SQLException {
		this(new JDBC());
	}

	public TraitementFinMoisSemaine(final JDBC jdbc) {
		super();
		this.jdbc = jdbc;
	}

	/**
	 * Effectue une compression pour un jour donnéee.
	 * En mode automatique, tente d'effectuer toutes les compressions jusqu'a cette journée.
	 * En mode manuel mensuel, tente d'effectuer une compression sur le mois correspondant à cette journée.
	 * En mode manuel hebdomadaire, tente d'effectuer une compression sur la semaine correspondant à cette journée.
	 * 
	 * @param day
	 * @throws SQLException
	 */
	public void traitementCompression(final CompressionModeEnum compressionMode, final Date day) throws SQLException {
		TraitementFinMoisSemaine.LOGGER.info("----- Debut traitement compression ----------");
		Chrono chrono = new Chrono();
		chrono.start();

		// Truncate the date to the DAY.
		final Date truncatedDay = DateUtils.truncate(day, Calendar.DAY_OF_MONTH);

		TraitementFinMoisSemaine.LOGGER.info(String.format("Compression jusqu'au [%1$s]", truncatedDay));

		switch (compressionMode) {

		case AUTO:
			TraitementFinMoisSemaine.LOGGER.info(
					"Mode de compression : AUTO => Compression automatique sur toutes les periodes possibles ...");
			this.traitementAuto(truncatedDay);
			break;
		case MONTH:
			TraitementFinMoisSemaine.LOGGER.info(
					"Mode de compression : MONTH => Compression sur le mois passé en parametre ...");
			this.traitementMensuelOneShot(truncatedDay, true);
			break;
		case WEEK:
			TraitementFinMoisSemaine.LOGGER.info(
					"Mode de compression : MONTH => Compression sur la semaine passée en parametre ...");
			this.traitementHebdomadaireOneShot(truncatedDay, true);
			break;
		case OFF:
			TraitementFinMoisSemaine.LOGGER.info(
					"Mode de compression : OFF => Compression désactivée !");
			break;
		}

		chrono.stop();

		TraitementFinMoisSemaine.LOGGER.info(String.format("Durée complète du traitement des compressions : %s .", chrono.getDureeHumain()));
		TraitementFinMoisSemaine.LOGGER.info("----- Fin traitement compression ----------");
	}

	/**
	 * Mensuel : Try to run compression on a specific month designated by day date.
	 * This operation can be performed even if datas are missing but is irreversible !
	 * 
	 * @param day
	 * @param b
	 * @param connection
	 * @throws SQLException
	 */
	protected void traitementMensuelOneShot(final Date day,
			final boolean forceEmptyCompression) throws SQLException {
		final Date firstDayOfMonth = DateHelper.getFirstDayOfMonth(day);
		TraitementFinMoisSemaine.LOGGER.info(String.format("Tentative de forçage de la compression du Mois [%1$s]...",
				TraitementFinMoisSemaine.monthOfYearFormat.format(firstDayOfMonth)));

		if (!this.isMonthAlreadyProcessed(firstDayOfMonth)) {
			this.traitementCompressionMois(firstDayOfMonth, forceEmptyCompression);
		} else {
			TraitementFinMoisSemaine.LOGGER.error(String.format("Mois [%1$s] déjà compréssé !",
					TraitementFinMoisSemaine.monthOfYearFormat.format(firstDayOfMonth)));
		}
	}

	/**
	 * Hebdomadaire : Try to run compression on a specific week designated by day date.
	 * This operation can be performed even if datas are missing but is irreversible !
	 * 
	 * @param day
	 * @param forceEmptyCompression
	 * @param connection
	 * @throws SQLException
	 */
	protected void traitementHebdomadaireOneShot(final Date day,
			final boolean forceEmptyCompression) throws SQLException {
		final Date firstDayOfWeek= DateHelper.getFirstDayOfWeek(day);
		final Calendar cal = Calendar.getInstance();
		cal.setTime(firstDayOfWeek);
		TraitementFinMoisSemaine.LOGGER.info(String.format("Tentative de forçage de la compression de la semaine [%1$s] (%2$s)...",
				cal.get(Calendar.WEEK_OF_YEAR), TraitementFinMoisSemaine.monthOfYearFormat.format(firstDayOfWeek)));

		if (!this.isWeekAlreadyProcessed(firstDayOfWeek)) {
			this.traitementCompressionSemaine(firstDayOfWeek, forceEmptyCompression);
		} else {
			TraitementFinMoisSemaine.LOGGER.error(String.format("Semaine [%1$s] (%2$s) déjà compréssée !",
					cal.get(Calendar.WEEK_OF_YEAR), TraitementFinMoisSemaine.monthOfYearFormat.format(firstDayOfWeek)));
		}
	}

	/**
	 * Automatique : Try to run compression on all possible period until day Date
	 * 
	 * @param day
	 * @param connection
	 * @throws SQLException
	 */
	protected void traitementAuto(final Date day) throws SQLException {
		final Date oldestDate = this.jdbc.findOldestStatDate();
		final Date firstDayOfFirstMonth = DateHelper.getFirstDayOfMonth(oldestDate);

		final Calendar monthCal = Calendar.getInstance();
		monthCal.clear();
		monthCal.setTime(firstDayOfFirstMonth);

		// Next day of processing day
		final Calendar nextDay = Calendar.getInstance();
		nextDay.clear();
		nextDay.setTime(day);
		nextDay.add(Calendar.DAY_OF_YEAR, 1);

		// first day of month for the next day of processing day
		final Date nextDayFirstDayOfFirstMonth = DateHelper.getFirstDayOfMonth(nextDay.getTime());
		// first day of week for the next day of processing day
		final Date nextDayFirstDayOfWeek = DateHelper.getFirstDayOfWeek(nextDay.getTime());

		while (monthCal.getTime().before(day)) {
			// Loop on all month until day date
			final Date firstDayOfMonth = monthCal.getTime();

			if (firstDayOfMonth.before(nextDayFirstDayOfFirstMonth)) {
				// Si la date de compression demandé est dans un mois suivant

				// Try month compression
				final boolean isMonthAlreadyProcessed = this.isMonthAlreadyProcessed(firstDayOfMonth);
				if (!isMonthAlreadyProcessed) {
					final boolean holeInMonth = this.trousMois(firstDayOfMonth);
					if (!holeInMonth) {
						this.traitementCompressionMois(firstDayOfMonth, false);
					}
				}
			}

			// Loop on all month until day date
			Date firstDayOfWeek = DateHelper.getFirstDayOfWeek(firstDayOfMonth);

			final Calendar weekCal = Calendar.getInstance();
			weekCal.setTime(firstDayOfWeek);

			// Add 1 month on month cal
			monthCal.add(Calendar.MONTH, 1);
			final Date firstDayOfFirstWeekNextMonth = DateHelper.getFirstDayOfWeek(monthCal.getTime());

			while (firstDayOfWeek.before(firstDayOfFirstWeekNextMonth)
					&& firstDayOfWeek.before(nextDayFirstDayOfWeek)) {
				// Loop on all week in the month but before the week of the processing day

				// Try week compression
				final boolean isWeekAlreadyProcessed = this.isWeekAlreadyProcessed(firstDayOfWeek);
				if (!isWeekAlreadyProcessed) {
					final boolean holeInWeek = this.trousSemaine(firstDayOfWeek);
					if (!holeInWeek) {
						this.traitementCompressionSemaine(firstDayOfWeek, false);
					}
				}

				// Add 1 week on week cal
				weekCal.add(Calendar.WEEK_OF_YEAR, 1);
				firstDayOfWeek = weekCal.getTime();
			}
		}
	}

	/**
	 * Perform the compression for a month period.
	 * 
	 * @param firstDayOfMonth the first day of month to compress.
	 * @param forceEmptyCompression
	 * @param connection
	 * @throws SQLException
	 * @throws ParseException
	 * @throws TransactionException
	 */
	protected boolean traitementCompressionMois(final Date firstDayOfMonth,
			final boolean forceEmptyCompression) throws SQLException {
		Assert.notNull(firstDayOfMonth, "Aucune date fournie en paramètre");

		TraitementFinMoisSemaine.LOGGER.info((String.format(
				"---------- Tentative de compression des logs du mois [%1$s] (%2$s) ----------",
				TraitementFinMoisSemaine.monthOfYearFormat.format(firstDayOfMonth),
				TraitementFinMoisSemaine.dayOfYearFormat.format(firstDayOfMonth))));

		Chrono chrono = new Chrono();
		chrono.start();

		// Month processing
		boolean monthOk = false;
		final Connection connection = this.jdbc.getConnection();
		try {
			final List<SeConnectePeriode> listeSimpleCo = this.jdbc.getPersonneSimpleCoMois(firstDayOfMonth);
			if (listeSimpleCo != null) {
				final Set<ConnexionProfilPeriode> toInsertAfterCompressionSet = new HashSet<ConnexionProfilPeriode>(listeSimpleCo.size());
				final Set<SeConnectePeriode> toDeleteAfterCompressionSet = new HashSet<SeConnectePeriode>(listeSimpleCo.size());

				// Compression for the month
				this.compressionOnPeriod(listeSimpleCo, toInsertAfterCompressionSet, toDeleteAfterCompressionSet);

				if (forceEmptyCompression) {
					// Check if empty
					this.compressedEmptyDatas(toInsertAfterCompressionSet, firstDayOfMonth);
				}

				this.jdbc.insertMonthCompression(toInsertAfterCompressionSet, connection);
				this.jdbc.deleteAfterMonthCompression(toDeleteAfterCompressionSet, connection);


				monthOk = this.jdbc.commitTransaction(connection);
			}
		} catch (Exception e) {
			TraitementFinMoisSemaine.LOGGER.error("An error occured ! Month Compression processing will be rolled back !", e);
		}

		chrono.stop();

		TraitementFinMoisSemaine.LOGGER.info(String.format(
				"Durée complète du traitement de la compression : %s .",
				chrono.getDureeHumain()));
		TraitementFinMoisSemaine.LOGGER.info((String.format(
				"---------- Fin de compression des logs du mois [%1$s] ----------",
				TraitementFinMoisSemaine.monthOfYearFormat.format(firstDayOfMonth))));

		return monthOk;
	}

	/**
	 * Perform the compression for a week period.
	 * 
	 * @param firstDayOfWeek the first day of week to compress.
	 * @param forceEmptyCompression
	 * @param connection
	 * @throws SQLException
	 * @throws ParseException
	 * @throws TransactionException
	 */
	protected boolean traitementCompressionSemaine(final Date firstDayOfWeek, final boolean forceEmptyCompression) throws SQLException {
		Assert.notNull(firstDayOfWeek, "Aucune date fournie en paramètre");

		Calendar cal = Calendar.getInstance();
		cal.setTime(firstDayOfWeek);
		TraitementFinMoisSemaine.LOGGER.info((String.format(
				"---------- Tentative de compression des logs de la semaine [%1$d] (%2$s) ----------",
				cal.get(Calendar.WEEK_OF_YEAR), TraitementFinMoisSemaine.dayOfYearFormat.format(firstDayOfWeek))));

		Chrono chrono = new Chrono();
		chrono.start();

		// Week processing
		boolean weekOk = false;
		final Connection connection = this.jdbc.getConnection();
		try {
			final List<SeConnectePeriode> listeSimpleCo = this.jdbc.getPersonneSimpleCoSemaine(firstDayOfWeek);
			if (listeSimpleCo != null) {
				final Set<ConnexionProfilPeriode> toInsertAfterCompressionSet = new HashSet<ConnexionProfilPeriode>(listeSimpleCo.size());
				final Set<SeConnectePeriode> toDeleteAfterCompressionSet = new HashSet<SeConnectePeriode>(listeSimpleCo.size());

				// Compression for the week
				this.compressionOnPeriod(listeSimpleCo, toInsertAfterCompressionSet, toDeleteAfterCompressionSet);

				if (forceEmptyCompression) {
					// Check if empty
					this.compressedEmptyDatas(toInsertAfterCompressionSet, firstDayOfWeek);
				}

				this.jdbc.insertWeekCompression(toInsertAfterCompressionSet, connection);
				this.jdbc.deleteAfterWeekCompression(toDeleteAfterCompressionSet, connection);

				weekOk = this.jdbc.commitTransaction(connection);
			}
		} catch (Exception e) {
			TraitementFinMoisSemaine.LOGGER.error("An error occured ! Week Compression processing will be rolled back !", e);
		}

		chrono.stop();

		TraitementFinMoisSemaine.LOGGER.info(String.format(
				"Durée complète du traitement de la compression : %s .",
				chrono.getDureeHumain()));
		TraitementFinMoisSemaine.LOGGER.info((String.format(
				"---------- Fin de compression des logs de la semaine [%1$d] (%2$s) ----------",
				cal.get(Calendar.WEEK_OF_YEAR), TraitementFinMoisSemaine.dayOfYearFormat.format(firstDayOfWeek))));

		return weekOk;
	}

	/**
	 * Compression :
	 * Regroupe les stats par etab profil nbco periode (mois ou semaine).
	 * => Création de groupe de gens connectes le meme nombre de fois
	 * 
	 * @param listeSimpleCo la liste integrale des connexions à compresser sur la periode
	 * @return la liste des connexions compressés à supprimer de la BD
	 */
	protected void compressionOnPeriod(final List<SeConnectePeriode> listeSimpleCo,
			final Set<ConnexionProfilPeriode> toInsertAfterCompressionSet, final Set<SeConnectePeriode> toDeleteAfterCompressionSet) {
		final Map<CompressionKey, ConnexionProfilPeriode> compressionMap =
				new HashMap<CompressionKey, ConnexionProfilPeriode>(listeSimpleCo.size());

		for(SeConnectePeriode peopleStats : listeSimpleCo){
			ConnexionProfilPeriode compressedDatas = compressionMap.get(peopleStats.getCompressionKey());
			if (compressedDatas != null) {
				// Deja une entree dans la map pour cet etab/profil/nbco/mois

				final int oldNbPersonneInGroup = compressedDatas.getNbPersonne();
				final double oldMoyenneInGroup = compressedDatas.getMoyenneConnexion();

				// nouvelle moyenne : ((nombrepersonne*nombreconnexion)*moyenne) +((nombreconnexion*1)*moyenne) / nombrepersonne*nombreconnexion + nombreconnexion
				final double tempsTotalConnexionInGroup = oldMoyenneInGroup * oldNbPersonneInGroup * peopleStats.getNbConnexion();
				final double tempsTotalConnexionNewPersonne = peopleStats.getNbConnexion() * peopleStats.getMoyenne();

				final int newNbTotalConnexion = peopleStats.getNbConnexion() * (oldNbPersonneInGroup + 1);
				final double newMoyenneConnexion = (tempsTotalConnexionInGroup + tempsTotalConnexionNewPersonne) / newNbTotalConnexion;

				compressedDatas.setMoyenneConnexion(newMoyenneConnexion);
				// +1 personne pour ce groupe
				compressedDatas.setNbPersonne(oldNbPersonneInGroup + 1);
			} else {
				//First occurence : insert in map
				compressedDatas = new ConnexionProfilPeriode();

				compressedDatas.setUai(peopleStats.getUai());
				compressedDatas.setNomProfil(peopleStats.getNomProfil());
				compressedDatas.setDebutPeriode(peopleStats.getPremierJourPeriode());
				compressedDatas.setNbConnexion(peopleStats.getNbConnexion());
				compressedDatas.setNbPersonne(1);
				compressedDatas.setMoyenneConnexion(peopleStats.getMoyenne());

				compressionMap.put(peopleStats.getCompressionKey(), compressedDatas);

				toInsertAfterCompressionSet.add(compressedDatas);
			}

			// Compression donc on va devoir supprimer les donnees qui ont servi à la compression
			toDeleteAfterCompressionSet.add(peopleStats);
		}
	}

	/**
	 * Prepare a row of compressed data to insert if there was nothing to insert initialy.
	 * 
	 * @param toInsertAfterCompressionSet
	 * @param firstDayOfPeriod
	 */
	protected void compressedEmptyDatas(final Set<ConnexionProfilPeriode> toInsertAfterCompressionSet,
			final Date firstDayOfPeriod) {
		if (toInsertAfterCompressionSet.isEmpty()) {
			ConnexionProfilPeriode emptyCpp = new ConnexionProfilPeriode();
			emptyCpp.setDebutPeriode(firstDayOfPeriod);
			emptyCpp.setMoyenneConnexion(0D);
			emptyCpp.setNbConnexion(0);
			emptyCpp.setNbPersonne(0);
			emptyCpp.setNomProfil(TraitementFinMoisSemaine.EMPTY_COMPRESSION);
			emptyCpp.setUai(TraitementFinMoisSemaine.EMPTY_COMPRESSION);

			toInsertAfterCompressionSet.add(emptyCpp);
		}
	}

	/**
	 * Renvoie false si il n'y a pas de trous dans le mois et true si il y a des trous
	 * ( dans ce cas on log les jours manquant )
	 * 
	 * @param mois une date dans le mois à tester
	 * @return true si il existe un trou
	 * @throws SQLException
	 * @throws ParseException
	 */
	protected boolean trousMois(final Date mois) throws SQLException {
		boolean test = false;

		final Map<Integer,Boolean> jourDuMois = this.jdbc.jourDuMois(mois);
		final Calendar cal = Calendar.getInstance();
		cal.setTime(mois);
		cal.clear(Calendar.MILLISECOND);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.HOUR);
		cal.set(Calendar.DAY_OF_MONTH, 1);

		StringBuilder sb = new StringBuilder(256);
		sb.append("Impossible d'effectuer la compression pour le mois [");
		sb.append(TraitementFinMoisSemaine.monthOfYearFormat.format(mois));
		sb.append("] (");
		sb.append(TraitementFinMoisSemaine.dayOfYearFormat.format(mois));
		sb.append(") ! Les statistiques n'ont pas été caclculés pour les jours suivants : [");

		for(int i = 1; i <= cal.getActualMaximum(Calendar.DAY_OF_MONTH); i++){
			// du premier au dernier jour du mois
			if(!jourDuMois.containsKey(i)){
				// Si le jour n'a pas été traité
				sb.append(i);
				sb.append(", ");
				test = true;
			}
		}

		sb.append("].");

		if(test){
			final String message = sb.toString();

			TraitementFinMoisSemaine.LOGGER.warn(message);
		}

		return test;
	}

	/**
	 * Renvoie false si il n'y a pas de trous dans la semaine et true si il y a des trous.
	 * ( dans ce cas on log les jours manquant )
	 * 
	 * @param semaine
	 * @return
	 * @throws SQLException
	 * @throws ParseException
	 */
	protected boolean trousSemaine(final Date semaine) throws SQLException {
		boolean test = false;

		final Map<Integer,Boolean> jourDeLaSemaine = this.jdbc.jourDeLaSemaine(semaine);

		final Calendar cal = Calendar.getInstance();
		cal.setTime(semaine);
		cal.clear(Calendar.MILLISECOND);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.HOUR);

		final StringBuilder sb = new StringBuilder(256);
		sb.append("Impossible d'effectuer la compression pour la semaine [");
		sb.append(cal.get(Calendar.WEEK_OF_YEAR));
		sb.append("] (");
		sb.append(TraitementFinMoisSemaine.dayOfYearFormat.format(cal.getTime()));
		sb.append(") ! Les statistiques n'ont pas été caclculés pour les jours suivants : [");

		for(int i = 0; i < 7; i++){
			final int jour = cal.get(Calendar.DAY_OF_MONTH);
			// du premier au dernier jour du mois
			if(!jourDeLaSemaine.containsKey(jour)){
				// Si le jour n'a pas été traité
				sb.append(TraitementFinMoisSemaine.dayOfMonthFormat.format(cal.getTime()));
				sb.append(", ");
				test = true;
			}
			cal.add(Calendar.DATE, 1);
		}

		sb.append("].");

		if(test){
			final String message = sb.toString();

			TraitementFinMoisSemaine.LOGGER.warn(message);
		}

		return test;
	}

	/**
	 * Test if the week was already compressed.
	 * 
	 * @param firstDayOfWeek representing the week
	 * @return true if the week was already processed
	 * @throws SQLException
	 */
	protected boolean isWeekAlreadyProcessed(final Date firstDayOfWeek) throws SQLException {
		if (this.allProcessedWeek == null) {
			this.allProcessedWeek = this.jdbc.findAllCompressedWeek();
			TraitementFinMoisSemaine.LOGGER.debug(String.format("Weeks already compressed: [%1$s]",
					this.allProcessedWeek.toString()));
		}

		return this.allProcessedWeek.contains(firstDayOfWeek);
	}

	/**
	 * Test if the month was already compressed.
	 * 
	 * @param firstDayOfMonth representing the month
	 * @return true if the month was already processed
	 * @throws SQLException
	 */
	protected boolean isMonthAlreadyProcessed(final Date firstDayOfMonth) throws SQLException {
		if (this.allProcessedMonth == null) {
			this.allProcessedMonth = this.jdbc.findAllCompressedMonth();
			TraitementFinMoisSemaine.LOGGER.debug(String.format("Months already compressed: [%1$s]",
					this.allProcessedMonth.toString()));
		}

		return this.allProcessedMonth.contains(firstDayOfMonth);
	}

	public JDBC getJdbc() {
		return this.jdbc;
	}

	public void setJdbc(final JDBC jdbc) {
		this.jdbc = jdbc;
	}

}
