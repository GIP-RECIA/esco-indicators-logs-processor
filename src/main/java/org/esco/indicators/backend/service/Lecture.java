package org.esco.indicators.backend.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nl.bitwalker.useragentutils.Browser;
import nl.bitwalker.useragentutils.OperatingSystem;
import nl.bitwalker.useragentutils.UserAgent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.esco.indicators.backend.exception.EstablishmentToIgnore;
import org.esco.indicators.backend.exception.LogLineToIgnore;
import org.esco.indicators.backend.jdbc.JDBC;
import org.esco.indicators.backend.jdbc.model.ConnexionServiceJour;
import org.esco.indicators.backend.jdbc.model.Etablissement;
import org.esco.indicators.backend.jdbc.model.NombreDeVisiteurs;
import org.esco.indicators.backend.jdbc.model.SeConnectePeriode;
import org.esco.indicators.backend.model.ConnexionEtablissementPersonne;
import org.esco.indicators.backend.model.ConnexionPersonne;
import org.esco.indicators.backend.model.DonneesConnexion;
import org.esco.indicators.backend.model.DonneesConnexionPersonne;
import org.esco.indicators.backend.model.DonneesEtab;
import org.esco.indicators.backend.model.LogLine;
import org.esco.indicators.backend.model.LogLineTypeEnum;
import org.esco.indicators.backend.model.ProcessingModeEnum;
import org.esco.indicators.backend.model.Service;
import org.esco.indicators.backend.model.Sstart;
import org.esco.indicators.backend.model.SstartValue;
import org.esco.indicators.backend.model.TypeStatEnum;
import org.esco.indicators.backend.utils.Chrono;
import org.esco.indicators.backend.utils.DatasConfiguration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;


public class Lecture {

	/** Logger. */
	private static final Log LOGGER = LogFactory.getLog(Lecture.class);

	/** Total chain */
	public static final String TOTAL = "Total";

	private final Map<Sstart, SstartValue> debutConnexion = new HashMap<Sstart, SstartValue>(30000);

	private final Map<Service, Integer> serviceJour = new HashMap<Service, Integer>(30000);

	private final Set<ConnexionEtablissementPersonne> donneesConnexionEtabPersonne = new HashSet<ConnexionEtablissementPersonne>(30000);

	private final Map<DonneesConnexion, DonneesEtab> donneesConnexionEtab = new HashMap<DonneesConnexion, DonneesEtab>(30000);

	private final Map<ConnexionPersonne, DonneesConnexionPersonne> donneesConnexionPersonne = new HashMap<ConnexionPersonne, DonneesConnexionPersonne>(30000);

	/** Ignored establishment (unabe to find them in DB). */
	private final Set<String> ignoredEstablishment = new HashSet<String>(128);

	private final JDBC jdbc;

	/** Configuration. */
	private final Config config = Config.getInstance();

	/** Date format for day of year. */
	private final SimpleDateFormat dayOfYearFormat = new SimpleDateFormat("yyyy-MM-dd");

	/** Date format for day of year. */
	private final SimpleDateFormat logTimeFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss,SSS");

	/** First log date in log file. */
	private Date firstLogDate;

	/** Day after the current processing day. (Based on firstLogDate). */
	private Date nextDay;

	/** First day of month after the current processing day. (Based on firstLogDate). */
	private Date nextMonth;

	/** Log row for type of the log line. */
	private final int logTypeRowId;

	/** Default session time length. */
	private final float defaultSessionTime;

	/** Count of line ignored because of unknow fname. */
	private Map<String, Long> unknowFnameLine = new HashMap<String, Long>(64);

	private Map<Browser, Long> userAgentsBrowser = new HashMap<Browser, Long>(256);

	private Map<OperatingSystem, Long> userAgentsOs = new HashMap<OperatingSystem, Long>(256);

	public Lecture() throws ClassNotFoundException, SQLException {
		this(new JDBC());
	}

	protected Lecture(final JDBC pJdbc) throws ClassNotFoundException, SQLException {
		Assert.notNull(pJdbc, "No JDBC object provided !");
		this.jdbc = new JDBC();

		Assert.notNull(this.config, "No configuration provided !");
		this.logTypeRowId = this.config.getLogRow(Config.LOG_ROW_TYPE);
		this.defaultSessionTime = Float.parseFloat(this.config.getConfigValue(Config.CONF_DEFAULT_SESSION_TIME));
	}

	public boolean traitementLog(final String fileName,
			final ProcessingModeEnum processMode, final boolean dryRun) throws SQLException {
		Lecture.LOGGER.info("----- Debut traitement des logs ----------");
		Chrono chrono = new Chrono();
		chrono.start();

		boolean traitementOk = false;

		final Connection connection = this.jdbc.getConnection();
		try {
			// Load file in memory
			this.informations(fileName, processMode, dryRun);

			if (!dryRun) {
				// Save stats in DB
				this.insertions(connection);
			} else {
				Lecture.LOGGER.info(String.format(
						"Dry Run: [%1$d] ConnexionServiceJour rows loaded for insertion in DB.",
						this.serviceJour.size()));
				Lecture.LOGGER.info(String.format(
						"Dry Run: [%1$d] SeConnectePeriode rows loaded for insertion or update in DB.",
						this.donneesConnexionPersonne.size()));
				Lecture.LOGGER.info(String.format(
						"Dry Run: [%1$d] NombreDeVisiteurs rows loaded for insertion in DB.",
						this.donneesConnexionEtab.size()));
			}

			traitementOk = this.jdbc.commitTransaction(connection);
		} catch (Exception e) {
			Lecture.LOGGER.error("An error occured ! LDAP traitement will be rolled back !", e);
		} finally {
			this.clear();
		}

		chrono.stop();

		if (Lecture.LOGGER.isInfoEnabled()) {
			// Browsers
			final Map<Long, Browser> browserIdentityMap = new IdentityHashMap<Long, Browser>(this.userAgentsBrowser.size());
			for (Entry<Browser, Long> entry : this.userAgentsBrowser.entrySet()) {
				browserIdentityMap.put(new Long(entry.getValue()), entry.getKey());
			}

			final List<Long> browserIdentityList = new ArrayList<Long>(browserIdentityMap.size());
			browserIdentityList.addAll(browserIdentityMap.keySet());

			Collections.sort(browserIdentityList, Collections.reverseOrder());

			StringBuilder sb = new StringBuilder(browserIdentityList.size() * 256);
			for (Long identityKey : browserIdentityList) {
				final Browser browser = browserIdentityMap.get(identityKey);
				sb.append("[ ");
				sb.append(identityKey);
				sb.append(" ] connexion(s) => browser [ ");
				sb.append(browser);
				sb.append(" ]\n");
			}

			Lecture.LOGGER.info(String.format(
					"Differents navigateurs utilisés : \n%s", sb.toString()));

			// Operating Systems
			final Map<Long, OperatingSystem> osIdentityMap = new IdentityHashMap<Long, OperatingSystem>(this.userAgentsOs.size());
			for (Entry<OperatingSystem, Long> entry : this.userAgentsOs.entrySet()) {
				osIdentityMap.put(new Long(entry.getValue()), entry.getKey());
			}

			final List<Long> osIdentityList = new ArrayList<Long>(osIdentityMap.size());
			osIdentityList.addAll(osIdentityMap.keySet());

			Collections.sort(osIdentityList, Collections.reverseOrder());

			sb = new StringBuilder(osIdentityList.size() * 256);
			for (Long identityKey : osIdentityList) {
				final OperatingSystem os = osIdentityMap.get(identityKey);
				sb.append("[ ");
				sb.append(identityKey);
				sb.append(" ] connexion(s) => operating system [ ");
				sb.append(os);
				sb.append(" ]\n");
			}

			Lecture.LOGGER.info(String.format(
					"Differents systèmes d'exploitation utilisés : \n%s", sb.toString()));
		}

		Lecture.LOGGER.info(String.format("Durée complète du traitement des logs : %s .", chrono.getDureeHumain()));
		Lecture.LOGGER.info("----- Fin traitement des logs ----------");

		return traitementOk;
	}

	protected void informations(final String statsFilePath, final ProcessingModeEnum processMode,
			final boolean dryRun) throws IOException, SQLException {
		Chrono chrono = new Chrono();
		chrono.start();

		Lecture.LOGGER.info("---------- Début de lécture du fichier de log des statistiques. ----------");
		if (dryRun) {
			Lecture.LOGGER.warn("Processing a Dry Run ! No DB update will be perform !");
		}

		final File f = new File(statsFilePath);
		if (!f.exists()) {
			Lecture.LOGGER.fatal("Impossible de trouver le fichier de statistiques " + statsFilePath);
		}

		Lecture.LOGGER.info(String.format(
				"Nom du fichier : %s ; Type de log : %s ; duree par defaut des connexions : %s", statsFilePath, processMode,
				this.defaultSessionTime));

		final int portalNameRow = this.config.getLogRow(Config.LOG_ROW_PORTAL_NAME);

		try {
			final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			String lineOfLog;

			long numlignelog = 0; // nombre de ligne du fichier
			long nblignetraite = 0; // nombre de ligne comptabilisee
			long nbligneportail = 0; // nombre de ligne par portail
			String currentPortal = null; // portail courant
			this.unknowFnameLine.clear();

			while ((lineOfLog = br.readLine()) != null) {
				numlignelog ++;
				nbligneportail ++;

				// on boucle sur chaque ligne du fichier
				if (Lecture.LOGGER.isTraceEnabled()) {
					Lecture.LOGGER.trace(String.format("Ligne lue : %s", lineOfLog));
				}

				final String[] datas = lineOfLog.split(this.config.getLogValue(Config.LOG_ROW_SPACING));

				final String rowPortalName;
				try {
					rowPortalName = datas[portalNameRow];
				} catch (ArrayIndexOutOfBoundsException e) {
					// Malformed line !
					continue;
				}

				try {
					this.checkLineOfLogValidity(lineOfLog, numlignelog, datas);

					if (currentPortal == null) {
						// premiere ligne lue : on recupere le portail
						currentPortal = rowPortalName;
					} else if (!currentPortal.equals(rowPortalName)) {
						nbligneportail --;
						// Si on a changé de portail
						Lecture.LOGGER.info(String.format(
								"%s lignes de log lues sur le portail %s", nbligneportail, currentPortal));
						currentPortal = rowPortalName;
						nbligneportail = 1;
					}

					final String[] logTime = datas[this.config.getLogRow(Config.LOG_ROW_TIME)].split(" ");

					final Date logDay = this.dayOfYearFormat.parse(logTime[0]);
					if (this.firstLogDate == null) {
						// Init first log date
						this.firstLogDate = logDay;
					}

					final LogLine logLine = this.buildLogLine(lineOfLog, numlignelog);

					final Etablissement logLineEtab = this.jdbc.getEtablissement(logLine.getUai());
					if (logLineEtab == null) {
						// If etab not known in DB
						throw new LogLineToIgnore(
								String.format("Log line attached to an unknow establishment UAI: [%1$s] !",
										logLine.getUai()));
					}

					if (ProcessingModeEnum.MONTHLY == processMode) {
						// Monthly mode : treat all logs in the month
						final Date nextMonth = this.calculateStatsFileNextMonth();

						// Management of specific days in monthly logs
						if (DateUtils.isSameDay(logDay, Calendar.getInstance().getTime())) {
							// The log date is Today : current (incomplete) month under calculation
							this.todayMonthlyProcessing(logLine);
						} else if (this.isSameMonth(nextMonth, logDay)) {
							// The log date is in the next month : it maybe usefull to close open session
							this.nextMonthMonthlyProcessing(logLine);
						} else {
							// The log date is in a common day in the month
							//Le log peut être dans un mois précédent

							this.checkLogDateAlreadyProcessed(logDay);

							this.defaultMonthlyProcessing(logLine);
						}

					} else if (ProcessingModeEnum.DAILY == processMode) {
						// Daily mode : treat all logs in a day
						final Date nextDay = this.calculateStatsFileNextDay();

						if (DateUtils.isSameDay(logDay, nextDay)) {
							// The log date is in the next day : it maybe usefull to close open session
							this.nextDayDailyProcessing(logLine);
						} else {
							// The log date is in a common day

							//Le log peut être dans un jour précédent
							this.checkLogDateAlreadyProcessed(logDay);

							this.defaultDailyProcessing(logLine);
						}
					} else {
						throw new IllegalArgumentException(String.format(
								"Unsupported mode: [%s] !", processMode));
					}

					// Log line sucessfuly processed
					nblignetraite++;
				} catch (LogLineToIgnore e) {
					// The ligne must be ingored
					if (Lecture.LOGGER.isTraceEnabled()) {
						Lecture.LOGGER.trace(String.format("Log line [%s] ignored: [%s]", numlignelog, e.getMessage()));
					}
				} catch (ParseException e) {
					// Parsing error : Ignore the line
					Lecture.LOGGER.warn(String.format("Log line [%s] unable to parse : [%s]", numlignelog, lineOfLog), e);
				}
			}

			if (Lecture.LOGGER.isInfoEnabled()) {
				Lecture.LOGGER.info(String.format(
						"%s lignes de log lues sur le portail %s", nbligneportail, currentPortal));

				if (this.unknowFnameLine.size() > 0) {
					long total = 0;
					StringBuffer sb = new StringBuffer(1024);
					for (Entry<String, Long> entry : this.unknowFnameLine.entrySet()) {
						total = total + entry.getValue();
						sb.append("\nFname: [");
						sb.append(entry.getKey());
						sb.append("] => [");
						sb.append(entry.getValue());
						sb.append("] ligne(s) lue(s)");
					}
					Lecture.LOGGER.info(String.format(
							"[%s] lignes lue(s) et comptabilisés avec Fname inconnus dans services.conf: %s",
							total, sb.toString()));
				}
			}

			// traitement debut sans fin (temps co par defaut)
			Set<Entry<Sstart, SstartValue>> debut = this.debutConnexion.entrySet();
			Iterator<Entry<Sstart, SstartValue>> itDebut = debut.iterator();

			while (itDebut.hasNext()) {
				// on boucle sur chaque debut de connexion

				Map.Entry<Sstart, SstartValue> me = itDebut.next();
				SstartValue startValue = me.getValue();
				Sstart startKey = me.getKey();
				if (!startValue.isFin()) {
					// Nous avons un Début qui n'a jamais été terminé

					// Le termine
					startValue.terminate();

					if (Lecture.LOGGER.isTraceEnabled()) {
						Lecture.LOGGER.trace(String.format("debut sans fin : %s \t %s",
								startValue.toString(), startKey.toString()));
					}

					ConnexionPersonne c = new ConnexionPersonne();
					c.setDate(startValue.getDate());
					c.setObjectClass(startKey.getObjectClass());
					c.setUai(startKey.getUai());
					c.setUid(startKey.getUid());
					if (this.donneesConnexionPersonne.containsKey(c)) {
						// la personne existe donc maj
						DonneesConnexionPersonne dcp = this.donneesConnexionPersonne.get(c);
						dcp.setMoyenne(((dcp.getMoyenne() * dcp.getNbCo()) + this.defaultSessionTime) / (dcp.getNbCo() + 1));
						dcp.setNbCo(dcp.getNbCo() + 1);
					} else {
						// ajout
						DonneesConnexionPersonne dcp = new DonneesConnexionPersonne();
						dcp.setNbCo(1);
						dcp.setMoyenne(this.defaultSessionTime);
						this.donneesConnexionPersonne.put(c, dcp);
					}
				}
			}

			br.close();

			chrono.stop();

			// fin traitement debut sans fin (temps co par defaut)
			Lecture.LOGGER.info(String.format("Nombre de lignes lues : %s ; nombre de lignes comptabilisées : %s "
					+ "; durée du traitement : %s .", numlignelog, nblignetraite, chrono.getDureeHumain()));
			Lecture.LOGGER.info("---------- Fin de la lecture du fichier ----------");

		} catch (IOException e) {
			Lecture.LOGGER.error(String.format("Error while reading stats file %s", statsFilePath), e);
			throw e;
		}
	}

	protected void checkLogDateAlreadyProcessed(final Date logDate) throws LogLineToIgnore {
		boolean isLogDateAlreadyProcessed = this.jdbc.isDayAlreadyProcessed(logDate);
		if (isLogDateAlreadyProcessed) {
			throw new LogLineToIgnore("The log line date was already processed in a previous run !");
		}
	}

	/**
	 * Check the validity of a line of log.
	 * 
	 * @param lineOfLog
	 * @param numlignelog
	 * @param datas
	 * @param lastLectureDate
	 * @return
	 * @throws LogLineToIgnore
	 */
	protected void checkLineOfLogValidity(final String lineOfLog, final long numlignelog,
			final String[] datas) throws LogLineToIgnore {
		if (datas.length < 7) {
			throw new LogLineToIgnore("Bad field count in log line !");
		}

		if (lineOfLog.charAt(0) == '#') {
			// ligne de commentaire
			throw new LogLineToIgnore("The log line is commented !");
		}
	}

	/**
	 * Monthly processing for log line which was logged today.
	 * 
	 * @param logLine the logLine currently processed
	 * @throws ParseException
	 * 
	 */
	protected void todayMonthlyProcessing(final LogLine logLine)
			throws LogLineToIgnore, ParseException {
		switch (logLine.getEventType()) {

		case CTARG:
			this.traitementService(logLine);
			break;

		case CCALL_EXT:
			// CCALL_EXT donc a ignorer
			throw new LogLineToIgnore("CCALL_EXT log line is ignored if log date == today.");

		case SSTART:
			throw new LogLineToIgnore("SSTART log line is ignored if log date == today.");

		case SSTOP:
			this.traitementFermetureSession(logLine);
			break;
		}
	}

	/**
	 * Monthly processing for log line which was logged the month after the month currently processed.
	 * 
	 * @param logLine the logLine currently processed
	 * @throws ParseException
	 * 
	 */
	protected void nextMonthMonthlyProcessing(final LogLine logLine)
			throws LogLineToIgnore, ParseException {
		switch (logLine.getEventType()) {

		case CTARG:
			// Check log date which may be already processed
			this.checkLogDateAlreadyProcessed(logLine.getDate());
			this.traitementService(logLine);
			break;

		case CCALL_EXT:
			throw new LogLineToIgnore("CCALL_EXT log line is ignored if log date == next month.");

		case SSTART:
			throw new LogLineToIgnore("SSTART log line is ignored if log date == next month.");

		case SSTOP:
			this.traitementFermetureSession(logLine);
			break;
		}
	}

	/**
	 * Default monthly processing for log line.
	 * 
	 * @param logLine the logLine currently processed
	 * @throws ParseException
	 * 
	 */
	protected void defaultMonthlyProcessing(final LogLine logLine)
			throws ParseException {
		switch (logLine.getEventType()) {

		case CTARG:
		case CCALL_EXT:
			this.traitementService(logLine);
			break;

		case SSTART:
			this.traitementOuvertureSession(logLine);
			break;

		case SSTOP:
			this.traitementFermetureSession(logLine);
			break;
		}
	}

	/**
	 * Daily processing for log line which was logged the day after the day currently processed.
	 * 
	 * @param logLine the logLine currently processed
	 * @throws ParseException
	 * 
	 */
	protected void nextDayDailyProcessing(final LogLine logLine)
			throws LogLineToIgnore, ParseException {
		switch (logLine.getEventType()) {

		case CTARG:
			// Check log date which may be already processed
			this.checkLogDateAlreadyProcessed(logLine.getDate());
			this.traitementService(logLine);
			break;

		case CCALL_EXT:
			throw new LogLineToIgnore("CCALL_EXT log line is ignored if log date == next day.");

		case SSTART:
			throw new LogLineToIgnore("SSTART log line is ignored if log date == next day.");

		case SSTOP:
			this.traitementFermetureSession(logLine);
			break;
		}
	}

	/**
	 * Default daily processing for log line.
	 * 
	 * @param logLine the logLine currently processed
	 * @throws ParseException
	 * 
	 */
	protected void defaultDailyProcessing(final LogLine logLine)
			throws ParseException {
		switch (logLine.getEventType()) {

		case CTARG:
		case CCALL_EXT:
			this.traitementService(logLine);
			break;

		case SSTART:
			this.traitementOuvertureSession(logLine);
			break;

		case SSTOP:
			this.traitementFermetureSession(logLine);
			break;
		}
	}

	/**
	 * Build a LogLine object based on a log line datas.
	 * 
	 * @param logLine the log line
	 * @param numlignelog the position of the log line
	 * @param nblignetraite
	 * @return the LogLine object
	 * @throws LogLineToIgnore if the log line must be ignored
	 * @throws ParseException
	 */
	protected LogLine buildLogLine(final String logLine, final long numlignelog) throws LogLineToIgnore, ParseException {
		final LogLine result;

		final String[] datas = logLine.split(this.config.getLogValue(Config.LOG_ROW_SPACING));
		final String rowType = datas[this.logTypeRowId];

		if (StringUtils.hasText(rowType)) {
			final String cleanRowType = rowType.replaceAll("\\s", "").trim();

			LogLineTypeEnum logLineType = LogLineTypeEnum.fromLogLineEventTypeRow(cleanRowType);

			switch (logLineType) {
			case SSTART:
			case SSTOP:
				result = this.fillStartStop(logLine, numlignelog);
				break;
			case CTARG:
			case CCALL_EXT:
				result = this.fillCtargCcall_Ext(logLine, numlignelog);
				break;
			default:
				Lecture.LOGGER.trace(String.format("Code non pris en comptes %s", rowType));
				throw new LogLineToIgnore("No treatment for this log line type.");
			}

		} else {
			throw new LogLineToIgnore("Log line type is empty !");
		}

		return result;
	}

	/**
	 * Test if 2 dates are in the same month.
	 * 
	 * @param date1
	 * @param date2
	 * @return true if the 2 dates are in the same month
	 */
	protected boolean isSameMonth(final Date date1, final Date date2) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);

		return cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
	}

	/**
	 * Calculate the next day based on the day indicate in the stats filepath.
	 * 
	 * @param statsFilePath the stats filepath
	 * @return a calendar representing the next day
	 */
	protected Date calculateStatsFileNextDay() {
		if (this.nextDay == null) {
			Assert.notNull(this.firstLogDate, "First log date cannot be null !");

			Calendar cal = Calendar.getInstance();
			cal.setTime(this.firstLogDate);
			cal.add(Calendar.DAY_OF_MONTH, 1);

			this.nextDay = cal.getTime();
		}

		return this.nextDay;
	}

	/**
	 * Caluclate the next month based on the month indicate in the stats filepath.
	 * 
	 * @param statsFilePath the stats filepath
	 * @return the next month (between 1 to 12)
	 */
	protected Date calculateStatsFileNextMonth() {
		if (this.nextMonth == null) {
			Assert.notNull(this.firstLogDate, "First log date cannot be null !");

			Calendar cal = Calendar.getInstance();
			cal.setTime(this.firstLogDate);
			cal.add(Calendar.MONTH, 1);
			cal.set(Calendar.DAY_OF_MONTH, 1);

			this.nextMonth = cal.getTime();
		}

		return this.nextMonth;
	}

	/**
	 * Remplit la structure LogLine a partir des données de la ligne de log.
	 * 
	 * @param ligne line of log
	 * @param numlignelog position of line of log in file
	 * @return the LogLine
	 * @throws LogLineToIgnore if the line of log must be ignored
	 * @throws ParseException
	 */
	protected LogLine fillCtargCcall_Ext(final String ligne, final long numlignelog) throws LogLineToIgnore, ParseException {
		final LogLine logLine = new LogLine();
		final String[] datas = ligne.split(this.config.getLogValue(Config.LOG_ROW_SPACING));

		final String fullFname;
		final String truncated_fname;

		try {
			final String logLineDate = datas[this.config.getLogRow(Config.LOG_ROW_TIME)];
			final Date logDate = DateUtils.parseDateStrictly(logLineDate, new String[]{this.logTimeFormat.toPattern()});
			final String logTypeCode = datas[this.logTypeRowId].replaceAll("\\s", "");
			final LogLineTypeEnum logType = LogLineTypeEnum.fromLogLineEventTypeRow(logTypeCode);
			logLine.setEventType(logType);
			logLine.setDate(logDate);
			logLine.setPortail(datas[this.config.getLogRow(Config.LOG_ROW_PORTAL_NAME)]);
			logLine.setUid(datas[this.config.getLogRow(Config.LOG_ROW_CTARG_PEOPLE_UID)].replaceAll("\\s", ""));

			if (LogLineTypeEnum.CCALL_EXT == logType) {
				logLine.setNbacces(1);
				logLine.setUai(datas[this.config.getLogRow(Config.LOG_ROW_CCALL_ETAB_ID)]);
				logLine.setIdSession(datas[this.config.getLogRow(Config.LOG_ROW_CCALL_SESSION_ID)]);

				// MBD: ajout du User-Agent
				final int userAgentRow = this.config.getLogRow(Config.LOG_ROW_CCALL_USER_AGENT);
				if (datas.length > userAgentRow) {
					// Test si la ligne est assez longue pour comporter le user-Agent
					final String userAgent = datas[userAgentRow];
					if (StringUtils.hasText(userAgent) && !"null".equals(userAgent)) {
						logLine.setUserAgent(UserAgent.parseUserAgentString(userAgent));
					}
				}
			} else if (LogLineTypeEnum.CTARG == logType) {
				logLine.setNbacces(Integer.valueOf(datas[this.config.getLogRow(Config.LOG_ROW_CTARG_ACCESS_COUNT)]));
				logLine.setUai(datas[this.config.getLogRow(Config.LOG_ROW_CTARG_ETAB_ID)]);
				logLine.setIdSession(datas[this.config.getLogRow(Config.LOG_ROW_CTARG_SESSION_ID)]);

				// MBD: ajout du User-Agent
				final int userAgentRow = this.config.getLogRow(Config.LOG_ROW_CTARG_USER_AGENT);
				if (datas.length > userAgentRow) {
					// Test si la ligne est assez longue pour comporter le user-Agent
					final String userAgent = datas[userAgentRow];
					if (StringUtils.hasText(userAgent) && !"null".equals(userAgent)) {
						logLine.setUserAgent(UserAgent.parseUserAgentString(userAgent));
					}
				}
			}

			logLine.setObjectClass(datas[this.config.getLogRow(Config.LOG_ROW_CTARG_PEOPLE_PROFIL)].replaceAll("\\s", ""));
			logLine.setUiduPortal(datas[this.config.getLogRow(Config.LOG_ROW_CTARG_PEOPLE_ID)]);

			fullFname = datas[this.config.getLogRow(Config.LOG_ROW_CTARG_FNAME)].replaceAll("\\s", "");
		} catch (ArrayIndexOutOfBoundsException e) {
			// Malformed log line
			throw new LogLineToIgnore("Malformed log line !");
		}

		final String etabUai = logLine.getUai();

		if ((etabUai == null) || etabUai.equals("null")) {
			if (Lecture.LOGGER.isDebugEnabled()) {
				final String peopleUid = logLine.getUid();
				String message = String.format("No Etablishment Id UAI: [%1$s] for user UID: [%2$s] !", etabUai, peopleUid);
				throw new LogLineToIgnore(message);
			}
			throw new LogLineToIgnore("No Etablishment Id (UAI) !");
		}

		if (!DatasConfiguration.getProfils().containsKey(logLine.getObjectClass())) {
			throw new LogLineToIgnore("User profil (objectClass) unknown from configuration !");
		}

		if (fullFname != null) {
			int index_underscore = fullFname.indexOf("_");
			if (index_underscore != -1) {
				truncated_fname = fullFname.substring(0, index_underscore);
			} else {
				truncated_fname = fullFname;
			}

			String service = DatasConfiguration.findServiceName(truncated_fname);

			if (service == null) {
				Long count = this.unknowFnameLine.get(fullFname);
				if (count == null) {
					count = 0L;
				}
				this.unknowFnameLine.put(fullFname, count + 1);
				service = truncated_fname;
			}

			logLine.setTruncatedFname(truncated_fname);
			logLine.setService(service);
		}

		return logLine;
	}

	/**
	 * Remplit la structure LogLine a partir des données de la ligne de log.
	 * 
	 * @param ligne line of log
	 * @param numlignelog position of line in file
	 * @return the LogLine object
	 * @throws LogLineToIgnore if the line of log must be ignored
	 * @throws ParseException
	 */
	protected LogLine fillStartStop(final String ligne, final long numlignelog) throws LogLineToIgnore, ParseException {
		final LogLine logLine = new LogLine();
		final String[] datas = ligne.split(this.config.getLogValue(Config.LOG_ROW_SPACING));

		try {
			final String logLineDate = datas[this.config.getLogRow(Config.LOG_ROW_TIME)];
			final Date logDate = DateUtils.parseDateStrictly(logLineDate, new String[]{this.logTimeFormat.toPattern()});
			final String logType = datas[this.logTypeRowId].replaceAll("\\s", "");
			logLine.setEventType(LogLineTypeEnum.fromLogLineEventTypeRow(logType));
			logLine.setPortail(datas[this.config.getLogRow(Config.LOG_ROW_PORTAL_NAME)]);
			logLine.setDate(logDate);
			logLine.setUid(datas[this.config.getLogRow(Config.LOG_ROW_STARTSTOP_PEOPLE_UID)].replaceAll("\\s", ""));
			logLine.setIdSession(datas[this.config.getLogRow(Config.LOG_ROW_STARTSTOP_SESSION_ID)].replaceAll("\\s", ""));
			logLine.setUai(datas[this.config.getLogRow(Config.LOG_ROW_STARTSTOP_ETAB_ID)]);
			logLine.setObjectClass(datas[this.config.getLogRow(Config.LOG_ROW_STARTSTOP_PEOPLE_PROFIL)].replaceAll("\\s", ""));

			// MBD: ajout du User-Agent
			final int userAgentRow = this.config.getLogRow(Config.LOG_ROW_STARTSTOP_USER_AGENT);
			if (datas.length > userAgentRow) {
				// Test si la ligne est assez longue pour comporter le user-Agent
				final String userAgent = datas[userAgentRow];
				if (StringUtils.hasText(userAgent) && !"null".equals(userAgent)) {
					logLine.setUserAgent(UserAgent.parseUserAgentString(userAgent));
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// Malformed log line
			String message = "Malformed log line !";

			throw new LogLineToIgnore(message, e);
		}

		final String etabUai = logLine.getUai();

		if ((etabUai == null) || "null".equals(etabUai)) {
			if (Lecture.LOGGER.isDebugEnabled()) {
				final String peopleUid = logLine.getUid();
				String message = String.format("No Etablishment Id UAI: [%1$s] for user UID: [%2$s] !", etabUai, peopleUid);
				throw new LogLineToIgnore(message);
			}
			throw new LogLineToIgnore("No Etablishment Id (UAI) !");
		}


		if (!DatasConfiguration.getProfils().containsKey(logLine.getObjectClass())) {
			throw new LogLineToIgnore("User profil (objectClass) unknown from configuration !");
		}

		final UserAgent userAgent = logLine.getUserAgent();
		if (userAgent != null) {
			Long browserCount = this.userAgentsBrowser.get(userAgent.getBrowser());

			if (browserCount == null) {
				browserCount = 1L;
			} else {
				browserCount ++;
			}

			this.userAgentsBrowser.put(userAgent.getBrowser(), browserCount);

			Long osCount = this.userAgentsOs.get(userAgent.getOperatingSystem());

			if (osCount == null) {
				osCount = 1L;
			} else {
				osCount ++;
			}

			this.userAgentsOs.put(userAgent.getOperatingSystem(), osCount);
		}

		return logLine;
	}

	protected void traitementVisiteursEtab(final ConnexionPersonne cp, final DonneesConnexionPersonne dcp)
			throws SQLException, EstablishmentToIgnore {

		final String uai = cp.getUai();
		final Etablissement etabDb = this.jdbc.getEtablissement(uai);
		if (etabDb == null) {
			// No etab found in DB
			throw new EstablishmentToIgnore(String.format(
					"Unable to find an establishment in DB for UAI: [%1$s]", uai));
		}

		final String typeEtab = etabDb.getTypeEtablissement();
		final String departement = etabDb.getDepartement();

		final ConnexionEtablissementPersonne cep = new ConnexionEtablissementPersonne();
		cep.setDate(cp.getDate());
		cep.setUid(cp.getUid());
		final DonneesConnexion dc = new DonneesConnexion();
		dc.setDate(cp.getDate());

		// Recalcul Nb de visiteurs pour un etablissement
		dc.setTypeEtab(typeEtab);
		dc.setUai(uai);
		if (this.donneesConnexionEtab.containsKey(dc)) {
			// l'etalissement a ete compté dans la liste
			final DonneesEtab de = this.donneesConnexionEtab.get(dc);
			de.setNbvisites(de.getNbvisites() + dcp.getNbCo());

			cep.setTypeEtab(null);
			cep.setUai(uai);
			if (!this.donneesConnexionEtabPersonne.contains(cep)) {
				// Si la personne n'a pas été comptée pour l'etab
				de.setNbvisiteurs(de.getNbvisiteurs() + 1);

				this.createDonneesConnectionEtabPersonne(dc, cp);
			}

		} else {
			// Si il n'existe pas d'etab pour ce jour
			this.createDonneesConnexionEtab(dc, cp, dcp, TypeStatEnum.TOTAL_UN_ETABLISSEMENT);
		}

		// Recalcul Nb de visiteurs global
		dc.setTypeEtab(Lecture.TOTAL);
		dc.setUai(Lecture.TOTAL);
		if (this.donneesConnexionEtab.containsKey(dc)) {
			// somme departement pour tout les types etablissement
			final DonneesEtab de = this.donneesConnexionEtab.get(dc);
			de.setNbvisites(de.getNbvisites() + dcp.getNbCo());

			cep.setTypeEtab(Lecture.TOTAL);
			cep.setUai(Lecture.TOTAL);
			if (!this.donneesConnexionEtabPersonne.contains(cep)) {
				// Si la personne n'a pas été comptée dans le total total
				de.setNbvisiteurs(de.getNbvisiteurs() + 1);

				this.createDonneesConnectionEtabPersonne(dc, cp);
			}

		} else {
			// si il n'existe pas le total total pour ce jour
			this.createDonneesConnexionEtab(dc, cp, dcp, TypeStatEnum.TOTAL_GLOBAL);
		}

		// Recalcul Nb de visiteurs total pour un type d'etablissement
		dc.setTypeEtab(typeEtab);
		dc.setUai(Lecture.TOTAL);
		if (this.donneesConnexionEtab.containsKey(dc)) {
			// total tous les depts pour le type d'etablissement
			final DonneesEtab de = this.donneesConnexionEtab.get(dc);
			de.setNbvisites(de.getNbvisites() + dcp.getNbCo());

			cep.setTypeEtab(typeEtab);
			cep.setUai(Lecture.TOTAL);
			if (!this.donneesConnexionEtabPersonne.contains(cep)) {
				// Si la personne n'a pas été comptée dans la somme globale du type d'établissement
				de.setNbvisiteurs(de.getNbvisiteurs() + 1);

				this.createDonneesConnectionEtabPersonne(dc, cp);
			}

		} else {
			// si il n'existe pas le total
			this.createDonneesConnexionEtab(dc, cp, dcp, TypeStatEnum.TOTAL_UN_TYPE_ETABLISSEMENT);
		}

		// Recalcul Nb de visiteurs total pour un departement
		dc.setTypeEtab(Lecture.TOTAL);
		dc.setUai(departement);
		if (this.donneesConnexionEtab.containsKey(dc)) {
			// total de tous les types d'etablissement pour ce departement
			final DonneesEtab de = this.donneesConnexionEtab.get(dc);
			de.setNbvisites(de.getNbvisites() + dcp.getNbCo());

			cep.setTypeEtab(Lecture.TOTAL);
			cep.setUai(departement);
			if (!this.donneesConnexionEtabPersonne.contains(cep)) {
				// Si la personne n'a pas ete comptee dans la somme de tout les types d'etablissement du departement
				de.setNbvisiteurs(de.getNbvisiteurs() + 1);

				this.createDonneesConnectionEtabPersonne(dc, cp);
			}

		} else {
			// si il n'existe pas le total
			this.createDonneesConnexionEtab(dc, cp, dcp, TypeStatEnum.TOTAL_UN_DEPARTEMENT);
		}

		// Recalcul Nb de visiteurs total pour un type d'etablissement dans un departement
		dc.setTypeEtab(typeEtab);
		dc.setUai(departement);
		if (this.donneesConnexionEtab.containsKey(dc)) {
			// total de ce type d'etablissement pour ce departement
			final DonneesEtab de = this.donneesConnexionEtab.get(dc);
			de.setNbvisites(de.getNbvisites() + dcp.getNbCo());

			cep.setTypeEtab(typeEtab);
			cep.setUai(departement);
			if (!this.donneesConnexionEtabPersonne.contains(cep)) {
				// Si la personne n'a pas ete comptee dans la somme du departement pour ce type d'etablissement
				de.setNbvisiteurs(de.getNbvisiteurs() + 1);

				this.createDonneesConnectionEtabPersonne(dc, cp);
			}

		} else {
			// Si il n'existe pas le total
			this.createDonneesConnexionEtab(dc, cp, dcp, TypeStatEnum.TOTAL_UN_TYPE_ETABLISSEMENT_DANS_UN_DEPARTEMENT);
		}

	}

	/**
	 * L'etablissement n'existe pas donc on l'ajoute et on ajoute la personne.
	 * 
	 * @param dc
	 * @param cp
	 * @param dcp
	 * @param typeStat
	 */
	protected void createDonneesConnexionEtab(final DonneesConnexion dc, final ConnexionPersonne cp,
			final DonneesConnexionPersonne dcp, final TypeStatEnum typeStat) {
		DonneesConnexion newDc = new DonneesConnexion();
		newDc.setDate(cp.getDate());
		newDc.setUai(dc.getUai());
		newDc.setTypeEtab(dc.getTypeEtab());

		DonneesEtab de = new DonneesEtab();
		de.setTypestat(typeStat);
		de.setNbvisiteurs(1);
		de.setNbvisites(dcp.getNbCo());

		this.donneesConnexionEtab.put(newDc, de);

		this.createDonneesConnectionEtabPersonne(newDc, cp);
	}

	/**
	 * Ajoute la personne comme ayant deja ete comptee dans l'etab pour ce jour.
	 * 
	 * @param typeEtab
	 * @param uai
	 * @param cp
	 */
	protected void createDonneesConnectionEtabPersonne(final DonneesConnexion dc, final ConnexionPersonne cp) {
		ConnexionEtablissementPersonne cep = new ConnexionEtablissementPersonne();
		cep.setDate(cp.getDate());
		cep.setTypeEtab(dc.getTypeEtab());
		cep.setUai(dc.getUai());
		cep.setUid(cp.getUid());

		this.donneesConnexionEtabPersonne.add(cep);
	}

	protected void traitementOuvertureSession(final LogLine logLine) {
		final SstartValue newValue = new SstartValue(logLine);

		final Sstart key = new Sstart(logLine);

		final SstartValue previousValue = this.debutConnexion.put(key, newValue);

		if (previousValue != null) {
			// si il y avait deja une entree
			if (!previousValue.isFin()) {
				// Nous avons un nouveau Début qui ecrase un ancien Début non terminé

				final ConnexionPersonne c = new ConnexionPersonne();
				c.setDate(previousValue.getDate());
				c.setObjectClass(key.getObjectClass());
				c.setUai(key.getUai());
				c.setUid(key.getUid());

				if (Lecture.LOGGER.isTraceEnabled()) {
					Lecture.LOGGER.trace(String.format("debut sans fin : %s \t %s",
							previousValue.toString(), key.toString()));
				}

				if (this.donneesConnexionPersonne.containsKey(c)) {
					// la cle existe -> MAJ
					final DonneesConnexionPersonne dcp = this.donneesConnexionPersonne.get(c);
					dcp.setNbCo(dcp.getNbCo() + 1);
					dcp.setMoyenne(((dcp.getMoyenne() * dcp.getNbCo()) + this.defaultSessionTime)
							/ (dcp.getNbCo() + 1));
					this.donneesConnexionPersonne.put(c, dcp);
				} else {
					DonneesConnexionPersonne dcp = new DonneesConnexionPersonne();
					dcp.setNbCo(1);
					dcp.setMoyenne(this.defaultSessionTime);
					this.donneesConnexionPersonne.put(c, dcp);
				}

			} else {
				// deja traitee donc rien a faire
			}
		}
	}

	protected void traitementFermetureSession(final LogLine l) throws ParseException {
		// SSTOP
		final Sstart start = new Sstart(l);
		final SstartValue startValue = this.debutConnexion.get(start);

		if (startValue == null) {
			// on a pas le debut correspondant (cas du fin sans debut)
			if (Lecture.LOGGER.isTraceEnabled()) {
				Lecture.LOGGER.trace("Session : fin mais pas debut : " + l.toString());
			}
		} else {
			// Nous avons une Fin attaché à un Début

			// Termine la connexion
			startValue.terminate();

			// On met la connexion de depart comme etant associée a une fin
			final ConnexionPersonne cp = new ConnexionPersonne(l);
			cp.setDate(startValue.getDate());

			final Date debut = startValue.getDate();
			final Date fin = l.getDate();
			final double diff = fin.getTime() - debut.getTime();
			final double dureeMinutes = diff / 60000;
			//final int roundedDureeMinutes = Math.round(dureeMinutes);

			DonneesConnexionPersonne dcp = this.donneesConnexionPersonne.get(cp);
			if (dcp != null) {
				// MAJ
				final double oldConnexionTime = dcp.getMoyenne() * dcp.getNbCo();
				final int newConnexionCount = dcp.getNbCo() + 1;
				final double newMoyenne = (oldConnexionTime + dureeMinutes) / newConnexionCount;

				dcp.setNbCo(newConnexionCount);
				dcp.setMoyenne(newMoyenne);
			} else {
				// Ajout
				dcp = new DonneesConnexionPersonne();
				dcp.setNbCo(1);
				dcp.setMoyenne(dureeMinutes);

				this.donneesConnexionPersonne.put(cp, dcp);
			}
		}
	}

	/**
	 * Traitement d'une ligne de service.
	 * Si CTARG date de debut de connexion correspondante
	 * Si CCALL_EXT date de la ligne : on regarde si l'entree existe dans la
	 * map si oui, on fait une MAJ, sinon on ajoute.
	 * 
	 * @param logLine
	 */
	protected void traitementService(final LogLine logLine) {
		if (LogLineTypeEnum.CTARG == logLine.getEventType()) {
			// si CTARG on ratache le service à la date de debut de connexion
			Sstart star = new Sstart(logLine);
			SstartValue val;
			if (!this.debutConnexion.containsKey(star)) {
				// on a pas le debut correspondant (cas de fin sans debut)
				if (Lecture.LOGGER.isTraceEnabled()) {
					Lecture.LOGGER.trace("Service : fin mais pas debut : " + logLine.toString());
				}
				return;
			} else {
				// la date devient celle de debut de connexion
				val = this.debutConnexion.get(star);
				logLine.setDate(val.getDate());
			}
		}

		Service s = new Service(logLine);
		int access = 0;
		if (this.serviceJour.containsKey(s)) {
			// si la personne s'est deja connectée a ce service ce jour là ->
			// maj nbconnexion;
			access = this.serviceJour.get(s) + logLine.getNbacces();
		} else {
			// ajout nb acces pour la personne
			access = logLine.getNbacces();
		}

		this.serviceJour.put(s, access);
	}

	protected void insertions(final Connection connection) throws SQLException {
		final Chrono chrono = new Chrono();
		chrono.start();

		Lecture.LOGGER.info("---------- Début des insertions dans la BD ----------");

		final String[] parSemaineConfig = this.config.getConfigValue(Config.CONF_WEEK_BASED_ETAB_TYPES).split(",");
		final Set<String> parSemaine = new HashSet<String>();
		CollectionUtils.addAll(parSemaine, parSemaineConfig);

		final Set<Entry<Service, Integer>> serviceEntries = this.serviceJour.entrySet();
		final Collection<ConnexionServiceJour> csjColl = new HashSet<ConnexionServiceJour>(serviceEntries.size());
		for (Entry<Service, Integer> entry : serviceEntries) {
			final Service service = entry.getKey();
			final int connexionCount = entry.getValue();
			final ConnexionServiceJour csj = new ConnexionServiceJour(service, connexionCount);

			csjColl.add(csj);
		}
		this.jdbc.insertServices(csjColl, connection);

		chrono.stop();
		Lecture.LOGGER.info(String.format("Temps écoulé : %s", chrono.getDureeHumain()));
		chrono.reset();
		chrono.start();

		serviceEntries.clear();

		final Set<Entry<ConnexionPersonne, DonneesConnexionPersonne>> donneesCo = this.donneesConnexionPersonne.entrySet();
		final List<SeConnectePeriode> scpList = new ArrayList<SeConnectePeriode>(donneesCo.size());
		for (Entry<ConnexionPersonne, DonneesConnexionPersonne> entry : donneesCo) {
			final ConnexionPersonne cp = entry.getKey();
			final DonneesConnexionPersonne dcp = entry.getValue();
			try {
				this.traitementVisiteursEtab(cp, dcp);
				SeConnectePeriode scp = new SeConnectePeriode(cp, dcp);
				scpList.add(scp);
			} catch (EstablishmentToIgnore e) {
				// Ignore the current establishment
				final String etabUai = cp.getUai();
				if (!this.ignoredEstablishment.contains(etabUai)) {
					Lecture.LOGGER.warn(e.getMessage());
					this.ignoredEstablishment.add(etabUai);
				}
			}
		}

		this.jdbc.updateConnexionsMoisVoirSemaine(scpList, parSemaine, connection);

		chrono.stop();
		Lecture.LOGGER.info(String.format("Temps écoulé : %s secondes", chrono.getDureeHumain()));
		chrono.reset();
		chrono.start();

		donneesCo.clear();

		final Set<Entry<DonneesConnexion, DonneesEtab>> donnesEtab = this.donneesConnexionEtab.entrySet();
		final Collection<NombreDeVisiteurs> ndvColl = new HashSet<NombreDeVisiteurs>(donnesEtab.size());
		for (Entry<DonneesConnexion, DonneesEtab> entry : donnesEtab) {
			final DonneesConnexion connectionData = entry.getKey();
			final DonneesEtab etabData = entry.getValue();

			final NombreDeVisiteurs ndv = new NombreDeVisiteurs(connectionData, etabData);
			ndvColl.add(ndv);
		}
		this.jdbc.insertConnexionsEtab(ndvColl, connection);

		this.clear();

		chrono.stop();

		Lecture.LOGGER.info(String.format("Temps écoulé : %s ", chrono.getDureeHumain()));
		Lecture.LOGGER.info("---------- Fin des insertions dans la BD ----------");


	}

	/**
	 * Retourne le jour suivant de la date passée en paramètre.
	 * 
	 * @param jour
	 *            doit etre de la forme YYYY-MM-DD
	 * @return le jour suivant sous la forme YYYY-MM-DD
	 */
	protected String jourSuivant(final String jour) {
		String result;

		String[] jourItems = jour.split("-");
		int year = Integer.parseInt(jourItems[0]);
		int month = Integer.parseInt(jourItems[1]) - 1;
		int day = Integer.parseInt(jourItems[2]);

		Calendar cal = new GregorianCalendar(year, month, day);
		// Add 1 day
		cal.add(Calendar.DAY_OF_YEAR, 1);

		result = this.dayOfYearFormat.format(cal.getTime());

		return result;
	}

	/**
	 * Retourne le jour suivant de la date passée en paramètre.
	 * 
	 * @param jour une java.util.Date
	 * @return le jour suivant sous la forme YYYY-MM-DD
	 */
	protected String jourSuivant(final Date jour) {
		String result;

		Calendar cal = Calendar.getInstance();
		cal.setTime(jour);
		// Add 1 day
		cal.add(Calendar.DAY_OF_YEAR, 1);

		result = this.dayOfYearFormat.format(cal.getTime());

		return result;
	}

	protected void clear() {
		this.donneesConnexionEtabPersonne.clear();
		this.debutConnexion.clear();
		this.donneesConnexionEtab.clear();
		this.serviceJour.clear();
		this.donneesConnexionPersonne.clear();
	}

	public int getIndicecode() {
		return this.logTypeRowId;
	}

	protected Map<Sstart, SstartValue> getDebutConnexion() {
		return this.debutConnexion;
	}

	protected Map<Service, Integer> getServiceJour() {
		return this.serviceJour;
	}

	protected Set<ConnexionEtablissementPersonne> getDonneesConnexionEtabPersonne() {
		return this.donneesConnexionEtabPersonne;
	}

	protected Map<DonneesConnexion, DonneesEtab> getDonneesConnexionEtab() {
		return this.donneesConnexionEtab;
	}

	protected Map<ConnexionPersonne, DonneesConnexionPersonne> getDonneesConnexionPersonne() {
		return this.donneesConnexionPersonne;
	}

}
