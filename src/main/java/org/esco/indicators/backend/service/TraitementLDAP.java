/*
 * Projet ENT-CRLR - Conseil Regional Languedoc Roussillon.
 * Copyright (c) 2009 Bull SAS
 *
 * $Id: jalopy.xml,v 1.1 2009/03/17 16:30:44 ent_breyton Exp $
 */

package org.esco.indicators.backend.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.esco.indicators.backend.exception.TransactionException;
import org.esco.indicators.backend.jdbc.JDBC;
import org.esco.indicators.backend.jdbc.model.ACommeProfil;
import org.esco.indicators.backend.jdbc.model.EstActivee;
import org.esco.indicators.backend.jdbc.model.Etablissement;
import org.esco.indicators.backend.utils.Chrono;
import org.esco.indicators.backend.utils.DatasConfiguration;
import org.esco.indicators.backend.utils.LdapPagination;
import org.esco.indicators.backend.utils.LdapUtils;
import org.springframework.util.Assert;



public class TraitementLDAP {

	/** */
	private static final String INACTIVE_ACCOUNT_PASSWORD = "{SCRIPT}LOCK";

	/** */
	private static final String PEOPLE_PASSWORD_LDAP_FIELD = "userpassword";

	/** */
	private static final String AUTORITE_PARENTALE_LDAP_FIELD = "ENTEleveAutoriteParentale";

	/** */
	private static final String ESCO_UAI_LDAP_FIELD = "ESCOUAI";

	/** */
	private static final String PEOPLE_UID_LDAP_FIELD = "uid";

	/** */
	private static final String OBJECT_CLASS_LDAP_FIELD = "objectClass";

	/**  */
	private static final String PARENT_LDAP_OBJECT_CLASS = "ENTAuxPersRelEleve";

	/** */
	private static final String STRUCT_TYPE_LDAP_FIELD = "ENTStructureTypeStruct";

	/** */
	private static final String STRUCT_UAI_LDAP_FIELD = "ENTStructureUAI";

	/** */
	private static final String STRUCT_SIREN_LDAP_FIELD = "ENTStructureSIREN";

	/** Logger. */
	private static final Log LOGGER = LogFactory.getLog(TraitementLDAP.class);

	/** Date format for second in year. */
	private static final SimpleDateFormat secondInYearFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	/** Date format for second in year. */
	private static final SimpleDateFormat ldapTimestampFormat = new SimpleDateFormat("yyyyMMddHHmmss");

	/** If set to true the process cannot be launch multiple times the same day. */
	private final boolean runOncePerDay = true;

	/** Configuration. */
	private final Config config = Config.getInstance();

	private final JDBC jdbc;

	/** All profils in BD loaded by UID. */
	private Map<String, Set<ACommeProfil>> profilsByUid;

	private String lastLdapProcessingTimestamp;

	private int globalLoadedPeople = 0;

	private int globalProcessedEtab = 0;

	/**
	 * Constructor from Jdbc object.
	 * 
	 * @param jdbc
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public TraitementLDAP() throws ClassNotFoundException, SQLException {
		this(new JDBC());
	}

	protected TraitementLDAP(final JDBC jdbc) throws ClassNotFoundException, SQLException {
		super();

		Assert.notNull(jdbc, "JDBC Object not supplied !");
		this.jdbc = jdbc;
	}

	public void traitementLdap() throws SQLException {
		TraitementLDAP.LOGGER.info("----- Debut traitement LDAP ----------");
		Chrono chrono = new Chrono();
		chrono.start();

		final Calendar cal = Calendar.getInstance();

		try {
			// Initialization by reading last ldap processing time in file
			Calendar lastLdapProcessing = this.readLastLdapProcessingTimeFromDb();
			if (this.runOncePerDay && DateUtils.isSameDay(lastLdapProcessing, Calendar.getInstance())) {
				// If last LDAP processing time was today => no need to rerun it
				TraitementLDAP.LOGGER.info("Traitement LDAP already run once today !");
			} else {
				// LDAP processing wasn't run yet today (or it can be run multiple times a day)

				final Connection connection = this.jdbc.getConnection();

				// Load establishment from LDAP
				this.processEtablishments(connection);

				// Load all profils from group of etabs
				this.profilsByUid = this.jdbc.getAllProfils(connection);

				this.jdbc.commitTransaction(connection);

				// Load account activation from LDAP
				this.processAccountsActivation();

				// Write in file the new last LDAP processing time
				this.writeLastLdapProcessingTimeInDb(cal);
			}
		} catch (Exception e) {
			TraitementLDAP.LOGGER.error("An error occured ! LDAP Etablishments process will be rolled back !", e);
		}

		chrono.stop();

		TraitementLDAP.LOGGER.info(String.format("Durée complète du traitement LDAP : %s .", chrono.getDureeHumain()));
		TraitementLDAP.LOGGER.info("----- Fin traitement LDAP ----------");
	}

	/**
	 * On va recuperer tous les etablissement.
	 * Puis on les stocke dans la base.
	 * On update l'etablissement si il y a modification.
	 * ( peu probable, cas du changement de type d'etablissement )
	 * @param connectionEtab
	 * 
	 * @param jdbc
	 * @throws SQLException
	 * @throws IOException
	 * @throws NamingException
	 * @throws TransactionException
	 */
	protected void processEtablishments(final Connection connectionEtab) throws SQLException, IOException, NamingException, TransactionException {
		Chrono chrono = new Chrono();
		chrono.start();

		TraitementLDAP.LOGGER.info("---------- Début du chargement des établissements depuis LDAP. ----------");

		final Properties etabTypeProps = new Properties();
		etabTypeProps.load(new FileInputStream(Config.ETABLISHMENT_TYPE_CONFIG_FILE_NAME));

		final StringBuilder sb = new StringBuilder(128);
		sb.append("(&");
		sb.append("(");
		sb.append(TraitementLDAP.STRUCT_UAI_LDAP_FIELD);
		sb.append("=*)");
		sb.append("(|");
		sb.append("(objectClass=ENTEtablissement)");
		sb.append("(objectClass=ENTServAc)");
		sb.append("(objectClass=ENTCollLoc)");
		sb.append(")");
		sb.append(")");
		final String filter = sb.toString();

		// Liste des établissements modifiés depuis le dernier traitement LDAP
		final List<Attributes> listeModifiedEtab = LdapUtils.searchWithPagedStructure(filter);

		if ((listeModifiedEtab == null) || (listeModifiedEtab.size() == 0)){
			TraitementLDAP.LOGGER.info("Pas d'établissement à inserer ou à modifier");
		} else {
			for(Attributes attrs : listeModifiedEtab){
				// Pour chaque établissement modifié ou crée
				final String uai = String.valueOf(attrs.get(TraitementLDAP.STRUCT_UAI_LDAP_FIELD).get());
				final String siren = String.valueOf(attrs.get(TraitementLDAP.STRUCT_SIREN_LDAP_FIELD).get());
				final String departement = uai.substring(1,3);
				final String typeStructure = String.valueOf(attrs.get(TraitementLDAP.STRUCT_TYPE_LDAP_FIELD).get());

				String typeEtab = etabTypeProps.getProperty(typeStructure);
				if(typeEtab == null){
					if (!StringUtils.isBlank(typeStructure)) {
						// Build new type dynamically !
						final String[] words = typeStructure.split("\\s+");
						if (words.length > 0) {
							typeEtab = "NEW_";
							for (String word : words) {
								typeEtab = typeEtab + StringUtils.capitalize(word.substring(0, 1));
							}
							typeEtab = StringUtils.abbreviate(typeEtab, 16);
						}
					}

					if (typeEtab == null) {
						// if null again
						typeEtab = "NEW_TYPE_LDAP";
					}

					TraitementLDAP.LOGGER.warn("Aucun type d'etablissement correspondant au type de structure: ["
							+ typeStructure + "] n'apparait pas dans le fichier de config "
							+ Config.ETABLISHMENT_TYPE_CONFIG_FILE_NAME);
					TraitementLDAP.LOGGER.warn("Utilisation d'un nouveau type arbitrairement : " + typeEtab);
				}

				final Etablissement etablissement = new Etablissement(uai, siren, departement, typeEtab);

				// On insert ou on met a jour l'établissement
				this.jdbc.insertOrUpdateEtablissement(etablissement, connectionEtab);
			}
			TraitementLDAP.LOGGER.info(listeModifiedEtab.size()+ " établissement(s) crée(s) ou modifié(s) ");
		}

		chrono.stop();
		TraitementLDAP.LOGGER.info(String.format("Durée du chargement des établissements : %s .", chrono.getDureeHumain()));
		TraitementLDAP.LOGGER.info("---------- Fin du chargement des établissements depuis LDAP ----------");
	}

	/**
	 * On boucle sur les etablissement ( decoupage pour eviter les plantages ).
	 * Recupere sur les eleves la liste des responsables a traiter (AutoriteParentale) et on traite les enfants dans le meme temps
	 * on traite ensuite le reste ( pour les responsables on traite seulement les personnes dont l'UID est dans la liste)
	 * @param connectionAccountsActivation
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws NamingException
	 * @throws TransactionException
	 */
	protected void processAccountsActivation() throws SQLException, NamingException, TransactionException {
		Chrono chrono = new Chrono();
		chrono.start();

		// Number of etabs load in memory for each thread.
		final int etabGroupSizeCount = Integer.valueOf(this.config.getConfigValue(Config.CONF_LDAP_ETAB_BY_THREAD_COUNT));
		// Thread count half the number of LDAP thread because each thread will have 2 connections to LDAP.
		final int threadPoolSize = Integer.valueOf(this.config.getConfigValue(Config.CONF_LDAP_THREAD_COUNT)) / 2;

		TraitementLDAP.LOGGER.info("---------- Début du chargement des comptes depuis LDAP. ----------");

		final Calendar cal = Calendar.getInstance();
		final Date today = cal.getTime();

		// Liste de tous les établissement
		final List<Etablissement> allEtabs = this.jdbc.getAllEtablissements();

		if ((allEtabs == null) || (allEtabs.size() == 0)){
			TraitementLDAP.LOGGER.fatal("Erreur : aucun etablissement récupéré");
			throw new TransactionException("Could not find any establishment in LDAP !");
		}

		Collection<Future<?>> futures = new ArrayList<Future<?>>(allEtabs.size());
		final BlockingQueue<Runnable> workQueue1 = new ArrayBlockingQueue<Runnable>(allEtabs.size());
		final ThreadPoolExecutor groupEtabsTpe = new ThreadPoolExecutor(
				threadPoolSize, threadPoolSize, 0, TimeUnit.SECONDS, workQueue1);
		groupEtabsTpe.prestartAllCoreThreads();

		final BlockingQueue<Runnable> workQueue2 = new ArrayBlockingQueue<Runnable>(threadPoolSize * 2);
		final ThreadPoolExecutor ldapRequestTpe = new ThreadPoolExecutor(threadPoolSize * 2, threadPoolSize * 2,
				0, TimeUnit.SECONDS, workQueue2);
		ldapRequestTpe.prestartAllCoreThreads();

		Iterator<Etablissement> itAllEtabs = allEtabs.iterator();

		while (itAllEtabs.hasNext()) {
			// Loop on all etabs
			final ArrayList<Etablissement> groupEtabs = new ArrayList<Etablissement>(etabGroupSizeCount);

			for (int i = 0; i < etabGroupSizeCount; i ++) {
				if (itAllEtabs.hasNext()) {
					groupEtabs.add(itAllEtabs.next());
				}
			}

			Future<Object> future = groupEtabsTpe.submit(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					int peopleCount = TraitementLDAP.this.processAccountsActivationForGroupEtabs(groupEtabs, today, ldapRequestTpe);

					TraitementLDAP.this.logLoadedPeopleCount(peopleCount, groupEtabs.size());

					return null;
				}
			});

			futures.add(future);
		}

		try {
			for (Future<?> future : futures) {
				// Timeout to process etabs group : 1 min by etablishment
				future.get(allEtabs.size(), TimeUnit.MINUTES);
			}
		} catch (Exception e) {
			throw new TransactionException(
					"Multithread problem while activating accounts !", e);
		} finally {
			// Shutdown all tasks when finished
			groupEtabsTpe.shutdownNow();
			ldapRequestTpe.shutdownNow();
		}

		chrono.stop();
		TraitementLDAP.LOGGER.info(String.format("Durée du chargement des comptes : %s .", chrono.getDureeHumain()));
		TraitementLDAP.LOGGER.info("---------- Fin du chargement des comptes depuis LDAP ----------");
	}

	/**
	 * Refresh global people loaded count and processed etab count.
	 * 
	 * @param peopleCount
	 * @param etabCount
	 */
	protected synchronized void logLoadedPeopleCount(final int peopleCount, final int etabCount) {
		this.globalLoadedPeople = this.globalLoadedPeople  + peopleCount;
		this.globalProcessedEtab = this.globalProcessedEtab  + etabCount;
		TraitementLDAP.LOGGER.info(String.format(
				"=====> [%1$d] Personnes dans [%2$d] Etablissements ont désormais été parsés dans le LDAP."
				, this.globalLoadedPeople, this.globalProcessedEtab));
	}

	/**
	 * Process the account activation for a group of etabs.
	 * 
	 * @param groupEtabs
	 * @param today
	 * @param ldapRequestTpe
	 * @return the count of people loaded in the group of etabs.
	 * @throws SQLException
	 * @throws NamingException
	 * @throws TransactionException
	 */
	protected int processAccountsActivationForGroupEtabs(final Collection<Etablissement> groupEtabs,
			final Date today, final ThreadPoolExecutor ldapRequestTpe) throws SQLException, NamingException, TransactionException {
		int k = 0;

		// LDAP thread connection count.
		final int threadPoolSize = Integer.valueOf(this.config.getConfigValue(Config.CONF_LDAP_THREAD_COUNT));

		final LdapPagination listeElevesPagination = this.loadAllEleves(groupEtabs);
		final LdapPagination listeNonElevesPagination = this.loadAllNonEleves(groupEtabs);

		final List<Attributes> listeElevesAttrs = new ArrayList<Attributes>(groupEtabs.size() * 3000);
		final List<Attributes> listeNonElevesAttrs = new ArrayList<Attributes>(groupEtabs.size() * 3000);
		//final Collection<Attributes> peopleToProcess = new ArrayList<Attributes>(groupEtabs.size() * 6000);

		if (TraitementLDAP.LOGGER.isDebugEnabled()) {
			TraitementLDAP.LOGGER.debug(String.format(
					"Traitement des personnes  dans les établissements [%1$s] ..."
					, groupEtabs.toString()));
		}

		while (listeElevesPagination.hasNext() || listeNonElevesPagination.hasNext()){
			k++;
			if (TraitementLDAP.LOGGER.isTraceEnabled()) {
				TraitementLDAP.LOGGER.trace(String.format(
						"Chargement de la page LDAP numéro [%d] (taille des pages: [%d]) ..."
						, k, listeElevesPagination.getPageSize()));
			}

			// Load people from LDAP for this page (async run in thread pool executor)
			Collection<Future<?>> futures = new ArrayList<Future<?>>(10);
			if (listeElevesPagination.hasNext()) {
				this.callNextPageAsynchronously(listeElevesPagination,
						listeElevesAttrs, ldapRequestTpe, futures, "Elèves");
			}
			if (listeNonElevesPagination.hasNext()) {
				this.callNextPageAsynchronously(listeNonElevesPagination,
						listeNonElevesAttrs, ldapRequestTpe, futures, "Non élèves");
			}

			try {
				for (Future<?> future : futures) {
					// Timeout to read LDAP pages : 3 min by concurrent thread
					future.get(threadPoolSize * 3, TimeUnit.MINUTES);
				}
			} catch (Exception e) {
				throw new TransactionException(
						"Multithread problem while requesting LDAP pages !", e);
			}
		}

		//on recupere la liste des responsables a prendre en compte + on fait les traitements pour les élèves de cet établissement
		final Map<String, Attributes> pap = this.loadAutoriteParentaleMap(listeElevesAttrs);

		final int parsedPeopleCount = listeElevesAttrs.size() + listeNonElevesAttrs.size();

		// And process all AUTORITE_PARENTALE people (not all non eleves)
		final Iterator<Attributes> itNonEleves = listeNonElevesAttrs.iterator();
		while (itNonEleves.hasNext()) {
			// pour chaque personne autre que les eleves dans l'etablissement
			Attributes personneAttrs = itNonEleves.next();
			if(this.isNotRepresentantLegal(pap, personneAttrs)) {
				// Si la personne est un parent mais pas un representant légal
				// On l'ignore !
				itNonEleves.remove();
			}
		}

		if (TraitementLDAP.LOGGER.isDebugEnabled()) {
			TraitementLDAP.LOGGER.debug(String.format(
					"[%1$d] on [%2$d] Eleves and Non élèves are eligibles to process account activation and update profils."
					, listeElevesAttrs.size() + listeNonElevesAttrs.size(), parsedPeopleCount));
		}

		final Connection connection = this.jdbc.getConnection();

		this.traitementActivation(today, listeElevesAttrs, connection);
		this.traitementActivation(today, listeNonElevesAttrs, connection);
		this.traitementProfilsPersonne(today, listeElevesAttrs, connection);
		this.traitementProfilsPersonne(today, listeNonElevesAttrs, connection);
		this.jdbc.commitTransaction(connection);

		// Read people count
		return parsedPeopleCount;
	}

	/**
	 * Load LDAP page asynchronously in a ThreadPoolExecutor.
	 * 
	 * @param pagination used to load pages
	 * @param previousPagesContent Content of the pages already loaded
	 * @param tpe the ThreadPoolExecutor which will load the page
	 * @param futures le list of futures representing completion of task
	 * @param pageType the type of the page
	 */
	protected void callNextPageAsynchronously(final LdapPagination pagination,
			final List<Attributes> previousPagesContent, final ThreadPoolExecutor tpe,
			final Collection<Future<?>> futures, final String pageType) {
		Future<List<Attributes>> future = tpe.submit(new Callable<List<Attributes>>() {
			@Override
			public List<Attributes> call() throws Exception {
				List<Attributes> noneleves = pagination.next();
				previousPagesContent.addAll(noneleves);

				if (TraitementLDAP.LOGGER.isTraceEnabled()) {
					TraitementLDAP.LOGGER.trace(String.format("[%1$d] %2$s loaded from LDAP."
							, previousPagesContent.size(), pageType));
				}

				return noneleves;
			}
		});

		if (future != null) {
			futures.add(future);
		}
	}

	/**
	 * Load all etabs from LDAP.
	 * @return
	 */
	protected List<Attributes> loadAllEtabs() {
		final String filterEtab ="(&(ENTStructureSIREN=*)(|(objectClass=ENTEtablissement)(objectClass=ENTServAc)(objectClass=ENTCollLoc)))";
		return LdapUtils.searchWithPagedStructure(filterEtab);
	}

	/**
	 * Load all eleves of a group of etabs from LDAP.
	 * 
	 * @param etabs group of etabs
	 * @return a LdapPagination object to navigate accros LDAP result pages
	 * @throws TransactionException
	 */
	protected LdapPagination loadAllEleves(final Collection<Etablissement> etabs)
			throws TransactionException {
		Assert.notEmpty(etabs, "Etablissement collection msut be supplied !");

		final StringBuilder sb = new StringBuilder(1024);
		sb.append("(&");
		sb.append("(uid=*)");
		sb.append("(objectClass=ENTEleve)");
		sb.append("(|");
		for (Etablissement etab : etabs) {
			// MBD: remplacement de la recherche via Siren rattachement par UAI rattachement en utilisant ESCOUAI en premier car indéxé

			//			sb.append("(ENTPersonStructRattach=ENTStructureSIREN=");
			//			sb.append(etab.getSiren());
			//			sb.append(",ou=structures,dc=esco-centre,dc=fr)");

			sb.append("(|");

			sb.append("(&");

			sb.append("(ESCOUAI=");
			sb.append(etab.getUai());
			sb.append(")(ESCOUAIRattachement=");
			sb.append(etab.getUai());
			sb.append(")");

			sb.append(")");

			sb.append("(&");

			sb.append("(ESCOUAI=");
			sb.append(etab.getUai());
			sb.append(")");
			sb.append("(!(ESCOUAIRattachement=*))");
			sb.append("(ESCOUAICourant=");
			sb.append(etab.getUai());
			sb.append(")");

			sb.append(")");

			sb.append(")");
		}
		sb.append(")");
		//sb.append("(modifyTimestamp>=");
		//sb.append(this.lastLdapProcessingTimestamp);
		//sb.append(")");
		sb.append(")");
		final String  filter = sb.toString();

		// On récupère la pagination LDAP
		final LdapPagination listeElevesPagination = LdapUtils.buildLdapPagination(LdapUtils.PEOPLE_DN, filter);

		return listeElevesPagination;
	}

	/**
	 * Load all non eleves of a group of etabs from LDAP.
	 * 
	 * @param etabs group of etabs
	 * @return a LdapPagination object to navigate accros LDAP result pages
	 * @throws TransactionException
	 */
	protected LdapPagination loadAllNonEleves(final Collection<Etablissement> etabs)
			throws TransactionException {
		Assert.notEmpty(etabs, "Etablissement collection msut be supplied !");

		StringBuilder sb = new StringBuilder(512);
		sb .append("(&");
		sb.append("(uid=*)");
		sb.append("(objectClass=*)(!(objectClass=ENTEleve))");
		sb.append("(|");
		for (Etablissement etab : etabs) {
			// MBD: remplacement de la recherche via Siren rattachement par UAI rattachement en utilisant ESCOUAI en premier car indéxé

			//			sb.append("(ENTPersonStructRattach=ENTStructureSIREN=");
			//			sb.append(etab.getSiren());
			//			sb.append(",ou=structures,dc=esco-centre,dc=fr)");

			sb.append("(|");

			sb.append("(&");

			sb.append("(ESCOUAI=");
			sb.append(etab.getUai());
			sb.append(")(ESCOUAIRattachement=");
			sb.append(etab.getUai());
			sb.append(")");

			sb.append(")");

			sb.append("(&");

			sb.append("(ESCOUAI=");
			sb.append(etab.getUai());
			sb.append(")");
			sb.append("(!(ESCOUAIRattachement=*))");
			sb.append("(ESCOUAICourant=");
			sb.append(etab.getUai());
			sb.append(")");

			sb.append(")");

			sb.append(")");
		}
		sb.append(")");
		//sb.append("(modifyTimestamp>=");
		//sb.append(this.lastLdapProcessingTimestamp);
		//sb.append(")");
		sb.append(")");
		final String filter = sb.toString();

		// On récupère la pagination LDAP
		final LdapPagination listeNonElevesPagination = LdapUtils.buildLdapPagination(LdapUtils.PEOPLE_DN, filter);

		return listeNonElevesPagination;
	}

	/**
	 * Load all eleves of an etab from LDAP.
	 * 
	 * @param siren SIREN of the etab
	 * @return the list of LDAP attributes for the eleves
	 * @throws TransactionException
	 */
	protected List<Attributes> loadElevesFromEtabRecentlyModified(final String siren) {
		final StringBuilder sb = new StringBuilder(256);
		sb.append("(&");
		sb.append("(uid=*)(objectClass=ENTEleve)");
		sb.append("(modifyTimestamp>=");
		sb.append(this.lastLdapProcessingTimestamp);
		sb.append(")");
		sb.append("(ENTPersonStructRattach=ENTStructureSIREN=");
		sb.append(siren);
		sb.append(",ou=structures,dc=esco-centre,dc=fr)");
		sb.append(")");
		final String  filter = sb.toString();

		// On récupère la liste de tous les elèves rattachés à l'établissement, qui ont été modifiés entre le dernier traitement LDAP et aujourd'hui
		final List<Attributes> listeElevesAttrs = LdapUtils.searchWithPagedPersonne(filter);

		if (CollectionUtils.isEmpty(listeElevesAttrs)) {
			TraitementLDAP.LOGGER.warn(
					"On a récupéré aucun élève récemment modifé dans l'etablissement " + siren + " !");
		} else {
			if (TraitementLDAP.LOGGER.isInfoEnabled()) {
				TraitementLDAP.LOGGER.info(listeElevesAttrs.size() + " éleves chargés.");
			}
		}

		return listeElevesAttrs;
	}

	/**
	 * Load all non eleves of an etab from LDAP.
	 * 
	 * @param siren SIREN of the etab
	 * @return the list of LDAP attributes for the non eleves
	 * @throws TransactionException
	 */
	protected List<Attributes> loadNonElevesFromEtabRecentlyModified(final String siren) {
		StringBuilder sb = new StringBuilder(256);
		sb .append("(&");
		sb.append("(uid=*)(objectClass=*)(!(objectClass=ENTEleve))");
		sb.append("(modifyTimestamp>=");
		sb.append(this.lastLdapProcessingTimestamp);
		sb.append(")");
		sb.append("(ENTPersonStructRattach=ENTStructureSIREN=");
		sb.append(siren);
		sb.append(",ou=structures,dc=esco-centre,dc=fr)");
		sb.append(")");
		final String filter = sb.toString();

		// On récupère la liste de toutes les personnes (sauf les elèves) rattachées à l'établissement,
		// qui ont été modifiés entre le dernier traitement LDAP et aujourd'hui
		final List<Attributes> listeNonElevesAttrs = LdapUtils.searchWithPagedPersonne(filter);

		if (CollectionUtils.isEmpty(listeNonElevesAttrs)) {
			TraitementLDAP.LOGGER.warn(
					"On a récupéré aucun non élève récemment modifé dans l'etablissement " + siren + " !");
		} else {
			if (TraitementLDAP.LOGGER.isInfoEnabled()) {
				TraitementLDAP.LOGGER.info(listeNonElevesAttrs.size() + " non éleves chargés.");
			}
		}

		return listeNonElevesAttrs;
	}

	/**
	 * Test if a LDAP Attributes represent a Parent.
	 * 
	 * @param objc the LDAP naming attributes
	 * @return true si la personne a comme objectclass ENTAuxPersRelEleve
	 * @throws NamingException
	 */
	protected boolean isParent(final Attributes attrs) throws NamingException {
		boolean result = false;

		final Attribute objectClassAttr = attrs.get(TraitementLDAP.OBJECT_CLASS_LDAP_FIELD);

		if(objectClassAttr != null) {
			for(int i = 0; i < objectClassAttr.size(); i++){
				//on parcourt tous les objectclass
				final String objectClass = String.valueOf(objectClassAttr.get(i));
				if(TraitementLDAP.PARENT_LDAP_OBJECT_CLASS.equals(objectClass)){
					//Si c'est un parent
					result = true;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Test if a LDAP Attributes represent a Parent.
	 * 
	 * @param objc the LDAP naming attributes
	 * @return true si la personne a comme objectclass ENTAuxPersRelEleve
	 * @throws NamingException
	 */
	protected boolean isNotRepresentantLegal(final Map<String, Attributes> autoritesParental,
			final Attributes attrs) throws NamingException {
		boolean result = false;

		if(this.isParent(attrs)) {
			final Attribute peopleUid = attrs.get(TraitementLDAP.PEOPLE_UID_LDAP_FIELD);
			if ((peopleUid == null) || !autoritesParental.containsKey(String.valueOf(peopleUid.get()))) {
				// Si c'est un parent
				// Et si la personne n'est pas dans la liste des responsables legaux
				//(La personne n'a pas l'autorite parentale) on l'ignore
				result = true;
			}
		}

		return result;
	}

	/**
	 * Process profils of a LDAP Attributes representing a people.
	 * 
	 * listeProfilLDAP: 0->uid, 1->uai,2->nomprofil
	 * 
	 * @param today
	 * @param connection
	 * @param peopleAttr LDAP Attributes representing a people
	 * @throws SQLException
	 * @throws NamingException
	 * @throws TransactionException
	 */
	@SuppressWarnings("unchecked")
	protected void traitementProfilsPersonne(final Date today, final Collection<Attributes> peopleToProcess,
			final Connection connection) throws SQLException, NamingException, TransactionException {

		final Collection<ACommeProfil> profilsToInsert = new HashSet<ACommeProfil>(peopleToProcess.size());
		final Collection<ACommeProfil> profilsToDelete = new HashSet<ACommeProfil>(peopleToProcess.size());

		for (Attributes peopleAttr : peopleToProcess) {
			final String peopleUid = String.valueOf(peopleAttr.get(TraitementLDAP.PEOPLE_UID_LDAP_FIELD).get()) ;

			if ("null".equals(peopleUid)) {
				TraitementLDAP.LOGGER.warn(String.format("No UID attached to the LDAP Attributes: [%1$s] !", peopleAttr.toString()));
				return;
			}

			// Liste des profils de la personne dans la BD (already loaded in the map)
			final Set<ACommeProfil> listeProfilBD = this.profilsByUid.get(peopleUid);
			// Liste des profils de la personne dans le LDAP
			final Set<ACommeProfil> listeProfilLdap = this.listeProfilLdap(peopleAttr);

			if (listeProfilBD == null) {
				profilsToInsert.addAll(listeProfilLdap);
			} else {
				profilsToInsert.addAll(CollectionUtils.subtract(listeProfilLdap, listeProfilBD));
				profilsToDelete.addAll(CollectionUtils.subtract(listeProfilBD, listeProfilLdap));
			}
		}

		this.jdbc.insertProfils(profilsToInsert, today, connection);

		this.jdbc.deleteProfils(profilsToDelete, today, connection);
	}

	/**
	 * Renvoie une liste de profils dans le ldap de la personne.
	 * (NB : les profils n'etant pas associés à un etablissement on associe chaque profil a chaque etablissement )
	 * 
	 * @param attributes
	 * @return
	 * @throws NamingException
	 */
	protected Set<ACommeProfil> listeProfilLdap(final Attributes attributes) throws NamingException {
		final Set<ACommeProfil> listeProfils = new  HashSet<ACommeProfil>(2);

		final Attribute attributeOBC = attributes.get(TraitementLDAP.OBJECT_CLASS_LDAP_FIELD);
		final Attribute attributeUAI = attributes.get(TraitementLDAP.ESCO_UAI_LDAP_FIELD);
		final String uid = String.valueOf(attributes.get(TraitementLDAP.PEOPLE_UID_LDAP_FIELD).get());

		if(attributeOBC != null){
			for(int i=0; i < attributeOBC.size(); i++){
				// pour chaque objectclass de la personne
				final String objectClass = String.valueOf(attributeOBC.get(i));
				if (!DatasConfiguration.getProfils().containsKey(objectClass)) {
					//objectclass pas dans la liste (normal pour certains)
					continue;
				}

				if(attributeUAI != null){
					for(int j=0; j < attributeUAI.size(); j++){
						// pour chaque uai
						final ACommeProfil acp = new ACommeProfil();
						acp.setUid(uid);
						acp.setUai(String.valueOf(attributeUAI.get(j)));
						acp.setNomProfil(String.valueOf(attributeOBC.get(i)));

						//on stocke dans la liste tous les cas possibles
						listeProfils.add(acp);
					}
				}
			}
		}

		return listeProfils;
	}

	/**
	 * On recupere les responsables à traiter.
	 * (ceux qui ont l'autorité parentale)
	 * Et on traite les eleves	en même temps.
	 * 
	 * @param uai
	 * @param today
	 * @return
	 * @throws NamingException
	 * @throws SQLException
	 * @throws TransactionException
	 */
	protected Map<String, Attributes> loadAutoriteParentaleMap(final List<Attributes> listeEleves)
			throws NamingException, SQLException, TransactionException{
		final Map<String, Attributes> autoriteParentale = new HashMap<String, Attributes>(listeEleves.size() * 2);

		if (CollectionUtils.isNotEmpty(listeEleves)) {
			for(Attributes attrs : listeEleves){
				//Pour chaque elève
				Attribute attr = attrs.get(TraitementLDAP.AUTORITE_PARENTALE_LDAP_FIELD);
				if(attr != null) {
					for(int i=0; i < attr.size(); i++){
						// Pour chaque personne ayant l'autorié parentale sur l'élève
						// On l'ajoute dans la map (si la personne est déjà présente dans la map, on réecrit la meme chose dessus)

						// On recupere l'uid du parent dans le dn.
						// chaine de la forme uid=XXXXXXXX,ou=people,dc=esco-centre,dc=fr
						final String dn = String.valueOf(attr.get(i));
						if (!"null".equals(dn)) {
							final String uid = dn.split(",")[0].split("=")[1];
							autoriteParentale.put(uid, attrs);
						}
					}
				}
			}
		}

		return autoriteParentale ;
	}

	protected void traitementActivation(final Date today, final Collection<Attributes> peopleToProces,
			final Connection connection) throws SQLException, NamingException, TransactionException {

		final Collection<String> uidWithActivationChange = new ArrayList<String>(peopleToProces.size());
		for (Attributes peopleAttrs : peopleToProces) {
			final String uid;
			final Attribute uidAttr = peopleAttrs.get(TraitementLDAP.PEOPLE_UID_LDAP_FIELD);
			if (uidAttr != null) {
				uid = String.valueOf(uidAttr.get());
			} else {
				uid = null;
			}

			// Is there a difference between LDAP and DB ?
			if (uid != null) {
				final EstActivee activationState = this.jdbc.getLastActivationState(uid, connection);

				String password = null;
				final Attribute passwdAttr = peopleAttrs.get(TraitementLDAP.PEOPLE_PASSWORD_LDAP_FIELD);
				if (passwdAttr != null) {
					final byte[] passb = (byte[]) passwdAttr.get();
					if (passb != null) {
						password = new String(passb);
					}
				}

				if(this.isValidAccount(password)) {
					// Si compte valide

					if (activationState != null) {
						final Date debut = activationState.getDateDebutActivation();
						final Date fin = activationState.getDateFinActivation();

						Assert.notNull(debut, "Date de début d'activation ne peut être null ici !");

						if (DateUtils.isSameDay(debut, today)
								|| ((fin != null) && DateUtils.isSameDay(fin, today))) {
							// Si l'état d'activation à déjà changé dans la journée : ne pas le prendre en compte !
							continue;
						}
					}

					boolean activatedInLdap = this.isActivatedAccount(password);

					if(((activationState == null) && (activatedInLdap))
							|| ((activationState != null) && (activationState.isActive() != activatedInLdap))){
						// Si activation inexistante en BD mais activation dans LDAP
						// Ou si activation dans des etats differents entre BD et LDAP
						uidWithActivationChange.add(uid);
					}
				}
			}
		}

		// Update accounts activation
		this.jdbc.updateAccountsActivation(uidWithActivationChange , today, connection);
	}

	/**
	 * Test if the account is valid based on the password.
	 * (si mot de passe pas en clair (cas des mots de passes en clair pour les comptes non valide))
	 * 
	 * @param password
	 * @return true if the account is valid
	 */
	protected boolean isValidAccount(final String password) {
		return (password != null) && ('{' == password.charAt(0));
	}

	/**
	 * Test if the account is activate.
	 * 
	 * @param password
	 * @return true if the account is activate
	 */
	protected boolean isActivatedAccount(final String password) {
		return !TraitementLDAP.INACTIVE_ACCOUNT_PASSWORD.equals(password);
	}

	/**
	 * Write in a file the last LDAP processing time.
	 * 
	 * @param cal
	 * @throws IOException
	 */
	protected void writeLastLdapProcessingTimeInFile(final Calendar cal) throws IOException {
		Date dat = cal.getTime();
		String date = TraitementLDAP.secondInYearFormat.format(dat);

		FileWriter fstream = new FileWriter(Config.LDAP_LAST_PROCESSING_TIME_FILE_NAME);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write("#  Date du dernier traitement Ldap effectué (pour regarder les modifications a partir de ce moment là)\n");
		out.append(date);
		out.close();
	}

	/**
	 * Read from a file the last LDAP processing time.
	 * 
	 * @throws IOException
	 * @throws TransactionException
	 */
	protected Calendar readLastLdapProcessingTimeFromFile() throws IOException, TransactionException {
		final Calendar time = Calendar.getInstance();
		time.setTimeInMillis(0);

		try {
			File f = new File(Config.LDAP_LAST_PROCESSING_TIME_FILE_NAME);
			if(f.exists()){
				final BufferedReader br = new BufferedReader(new InputStreamReader(	new FileInputStream(f)));
				// premiere ligne de commentaire a ignorer
				br.readLine();
				// récupération de la date du dernier traitement Ldap effectué (pour la premiere fois cette date sera en 2000 pour être sur de tout récuperer)
				String fileTime = br.readLine();
				if (fileTime != null) {
					final Date lastDate = DateUtils.parseDateStrictly(
							fileTime, new String[]{TraitementLDAP.secondInYearFormat.toPattern()});
					time.setTime(lastDate);
				} else {
					// Nothing to do
				}

				br.close();
			}else {
				// Le fichier n'existe pas (premier lancement)
			}
		} catch (FileNotFoundException e) {
			// Default time OK
			TraitementLDAP.LOGGER.warn("Last LDAP processing time storing file: ["
					+ Config.LDAP_LAST_PROCESSING_TIME_FILE_NAME + "] doesn't exists !");
		} catch (ParseException e) {
			// Default time OK
		}

		this.lastLdapProcessingTimestamp = this.toTimestamp(time);

		if (DateUtils.isSameDay(time, Calendar.getInstance())) {
			// If last LDAP processing time was today => no need to rerun it
			throw new TransactionException("Traitement LDAP already run once today !");
		}

		TraitementLDAP.LOGGER.info("Date du dernier traitement LDAP :" + this.lastLdapProcessingTimestamp);

		return time;
	}

	/**
	 * Write in DB the last LDAP processing time.
	 * 
	 * @param cal
	 * @throws IOException
	 * @throws SQLException
	 */
	protected void writeLastLdapProcessingTimeInDb(final Calendar cal) throws SQLException {
		Date date = cal.getTime();
		this.jdbc.updateLastLdapProcessingDate(date);
	}

	/**
	 * Read in DB the last LDAP processing time.
	 * 
	 * @throws TransactionException
	 * @throws SQLException
	 */
	protected Calendar readLastLdapProcessingTimeFromDb() throws SQLException, TransactionException {
		Date lastDay = this.jdbc.findLastLdapProcessing();
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.setTime(lastDay);
		this.lastLdapProcessingTimestamp = this.toTimestamp(cal);

		TraitementLDAP.LOGGER.info("Date du dernier traitement LDAP : " + this.lastLdapProcessingTimestamp);

		return cal;
	}

	/**
	 * Renvoie le timestamp ldap(YYYYMMDDHHMMSSZ)
	 * correspondant au calendrier passée en parametre.
	 * 
	 * @param date
	 * @return
	 */
	protected String toTimestamp(final Calendar cal){
		if(cal != null) {
			// return datetmp[0]+datetmp[1]+datetmp[2]+datetmp[3]+datetmp[4]+datetmp[5]+"Z";
			StringBuilder sb = new StringBuilder(20);
			sb.append(TraitementLDAP.ldapTimestampFormat.format(cal.getTime()));
			sb.append("Z");
			return sb.toString();
		} else {
			return "19700101000000Z";
		}
	}

}
