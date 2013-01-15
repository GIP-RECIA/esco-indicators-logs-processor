package org.esco.indicators.backend.service;

import java.io.FileInputStream;
import java.util.Properties;

import org.springframework.util.Assert;

/**
 * Responsible for application configuration.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 *
 */
public class Config {

	/** File name to store de last LDAP processing time. */
	public static final String LDAP_LAST_PROCESSING_TIME_FILE_NAME = "conf/dernierTraitementLdap";

	/** File name to config etablishment types. */
	public static final String ETABLISHMENT_TYPE_CONFIG_FILE_NAME = "conf/typeetab.conf";

	/** File name to config log line rows index. */
	public static final String LOG_ROWS_CONFIG_FILE_NAME = "conf/log.properties";

	/** File name to global config . */
	public static final String GLOBAL_CONFIG_FILE_NAME = "conf/config.properties";

	/** Param key for spacing beetwin log rows. */
	public static final String LOG_ROW_SPACING = "log.row.spacing";

	/** Param key for index of the log time field. */
	public static final String LOG_ROW_TIME = "log.row.time";

	/** Param key for index of the portal name field. */
	public static final String LOG_ROW_PORTAL_NAME = "log.row.portalName";

	/** Param key for index of the log type field. */
	public static final String LOG_ROW_TYPE = "log.row.type";

	/** Param key for index of a START or STOP row people profil field. */
	public static final String LOG_ROW_STARTSTOP_PEOPLE_PROFIL = "log.row.startStop.people.profil";

	/** Param key for index of a START or STOP row portal people Id field. */
	public static final String LOG_ROW_STARTSTOP_PEOPLE_ID = "log.row.startStop.people.id";

	/** Param key for index of a START or STOP row people uid field. */
	public static final String LOG_ROW_STARTSTOP_PEOPLE_UID = "log.row.startStop.people.uid";

	/** Param key for index of a START or STOP row etablishment field. */
	public static final String LOG_ROW_STARTSTOP_ETAB_ID = "log.row.startStop.etab.id";

	/** Param key for index of a START or STOP row session Id field. */
	public static final String LOG_ROW_STARTSTOP_SESSION_ID = "log.row.startStop.session.id";

	/** Param key for index of a START or STOP row User-Agent field. */
	public static final String LOG_ROW_STARTSTOP_USER_AGENT = "log.row.startStop.user-agent";

	/** Param key for index of a CTARG row people profil field. */
	public static final String LOG_ROW_CTARG_PEOPLE_PROFIL = "log.row.ctarg.people.profil";

	/** Param key for index of a CTARG row people portal Id field. */
	public static final String LOG_ROW_CTARG_PEOPLE_ID = "log.row.ctarg.people.id";

	/** Param key for index of a CTARG row people uid field. */
	public static final String LOG_ROW_CTARG_PEOPLE_UID = "log.row.ctarg.people.uid";

	/** Param key for index of a CTARG row fname field. */
	public static final String LOG_ROW_CTARG_FNAME = "log.row.ctarg.fname";

	/** Param key for index of a CTARG row access count field. */
	public static final String LOG_ROW_CTARG_ACCESS_COUNT = "log.row.ctarg.accessCount";

	/** Param key for index of a CTARG row etablishment Id field. */
	public static final String LOG_ROW_CTARG_ETAB_ID = "log.row.ctarg.etab.id";

	/** Param key for index of a CTARG row session Id field. */
	public static final String LOG_ROW_CTARG_SESSION_ID = "log.row.ctarg.session.id";

	/** Param key for index of a CTARG row User-Agent field. */
	public static final String LOG_ROW_CTARG_USER_AGENT = "log.row.ctarg.user-agent";

	/** Param key for index of a CCALL row tablishment Id field. */
	public static final String LOG_ROW_CCALL_ETAB_ID = "log.row.ccall.etab.id";

	/** Param key for index of a CCALL row session Id field. */
	public static final String LOG_ROW_CCALL_SESSION_ID = "log.row.ccall.session.id";

	/** Param key for index of a CCALL row User-Agent field. */
	public static final String LOG_ROW_CCALL_USER_AGENT = "log.row.ccall.user-agent";

	/** Param key for config of DB URL. */
	public static final String CONF_DB_URL = "conf.db.url";

	/** Param key for config of DB hostname. */
	public static final String CONF_DB_USER = "conf.db.user";

	/** Param key for config of DB hostname. */
	public static final String CONF_DB_PASSWORD = "conf.db.password";

	/** Param key for config of LDAP URL. */
	public static final String CONF_LDAP_URL = "conf.ldap.url";

	/** Param key for config of LDAP user. */
	public static final String CONF_LDAP_USER = "conf.ldap.user";

	/** Param key for config of LDAP password. */
	public static final String CONF_LDAP_PASSWORD = "conf.ldap.password";

	/** Param key for config of LDAP base Dn. */
	public static final String CONF_LDAP_BASE_DN = "conf.ldap.baseDn";

	/** Param key for config of LDAP structure branche. */
	public static final String CONF_LDAP_BRANCHE_STRUCTURE = "conf.ldap.brancheStructure";

	/** Param key for config of LDAP people branche. */
	public static final String CONF_LDAP_BRANCHE_PEOPLE = "conf.ldap.branchePeople";

	/** Param key for config of the number of thread for connecting to LDAP. */
	public static final String CONF_LDAP_THREAD_COUNT = "conf.ldap.threadCount";

	/** Param key for config of the number of etabs to load in memory by thread. */
	public static final String CONF_LDAP_ETAB_BY_THREAD_COUNT = "conf.ldap.thread.etabCount";

	/** Param key for config of all week based etablishment types. */
	public static final String CONF_WEEK_BASED_ETAB_TYPES = "conf.weekBased.etab.types";

	/** Param key for config of default session time length. */
	public static final String CONF_DEFAULT_SESSION_TIME = "conf.default.session.time";

	/** Param key for config compression mode. */
	public static final String CONF_COMPRESSION_MODE = "conf.compression.mode";

	/** Param key for config of processing mode. */
	public static final String CONF_PROCESSING_MODE = "conf.processing.mode";

	private final Properties config;

	private final Properties log;

	private static final Config INSTANCE = new Config();

	private Config() {
		Properties configTemp = new Properties();
		Properties logTemp = new Properties();

		try {
			logTemp.load(new FileInputStream(Config.LOG_ROWS_CONFIG_FILE_NAME));
			configTemp.load(new FileInputStream(Config.GLOBAL_CONFIG_FILE_NAME));

			this.config = configTemp;
			this.log = logTemp;
		} catch (Exception e) {
			throw new UnsupportedOperationException("The properties files cannot be load !", e);
		}

	}

	public static final Config getInstance() {
		return Config.INSTANCE;
	}

	public String getLogValue(final String key) {
		final String value = Config.INSTANCE.log.getProperty(key);

		Assert.notNull(value, String.format(
				"The Log configuration value for key: [%1$s] cannot be null !", key));

		return value;
	}

	public int getLogRow(final String key) {
		final String value = Config.INSTANCE.log.getProperty(key);

		Assert.notNull(value, String.format(
				"The Log index row value for key: [%1$s] cannot be null !", key));

		return Integer.valueOf(value);
	}

	public String getConfigValue(final String key) {
		final String value = Config.INSTANCE.config.getProperty(key);

		Assert.notNull(value, String.format(
				"The configuration value for key: [%1$s] cannot be null !", key));

		return value;
	}

}
