/*
 * Projet ENT-CRLR - Conseil Regional Languedoc Roussillon.
 * Copyright (c) 2009 Bull SAS
 *
 * $Id: jalopy.xml,v 1.1 2009/03/17 16:30:44 ent_breyton Exp $
 */

/**
 * 
 */
package org.esco.indicators.backend.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import org.apache.log4j.Logger;
import org.esco.indicators.backend.exception.TransactionException;
import org.esco.indicators.backend.service.Config;

/**
 * LdapUtils : Une méthode qui permet de créer une connexion puis d'effectuer
 * une recherche paginée, et enfin fermer la connexion puis retourner le
 * resultat.
 * 
 * @author breytond.
 */
public final class LdapUtils {

	/** Logger. */
	private static final Logger logger = Logger.getLogger(LdapUtils.class);

	/** Configuration. */
	private static final Config CONFIG = Config.getInstance();

	/** Strucuture DN for LDAP search on structure branch. */
	public static final String STRCUTURE_DN = "ou=" + LdapUtils.CONFIG.getConfigValue(Config.CONF_LDAP_BRANCHE_STRUCTURE)
			+ "," + LdapUtils.CONFIG.getConfigValue(Config.CONF_LDAP_BASE_DN);

	/** People DN for LDAP search on people branch. */
	public static final String PEOPLE_DN = "ou=" + LdapUtils.CONFIG.getConfigValue(Config.CONF_LDAP_BRANCHE_PEOPLE)
			+ "," + LdapUtils.CONFIG.getConfigValue(Config.CONF_LDAP_BASE_DN);


	/** PAGE_SIZE. */
	public static final int PAGE_SIZE = 2000;

	public static final Hashtable<String, Object> env = new Hashtable<String, Object>(11);
	static {
		LdapUtils.env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		LdapUtils.env.put(Context.PROVIDER_URL, LdapUtils.CONFIG.getConfigValue(Config.CONF_LDAP_URL));
		LdapUtils.env.put(Context.SECURITY_AUTHENTICATION, "simple");
		LdapUtils.env.put(Context.SECURITY_PRINCIPAL, LdapUtils.CONFIG.getConfigValue(Config.CONF_LDAP_USER));
		LdapUtils.env.put(Context.SECURITY_CREDENTIALS, LdapUtils.CONFIG.getConfigValue(Config.CONF_LDAP_PASSWORD));
		LdapUtils.logger.info("Url LDAP : " + LdapUtils.CONFIG.getConfigValue(Config.CONF_LDAP_URL));
		LdapUtils.logger.info("User LDAP : " + LdapUtils.CONFIG.getConfigValue(Config.CONF_LDAP_USER));
		// Maxime BOSSARD : ajout du Pooling
		LdapUtils.env.put("com.sun.jndi.ldap.connect.pool", "true");
		LdapUtils.env.put("com.sun.jndi.ldap.connect.pool.maxsize", LdapUtils.CONFIG.getConfigValue(Config.CONF_LDAP_THREAD_COUNT));
		LdapUtils.env.put("com.sun.jndi.ldap.connect.pool.prefsize", LdapUtils.CONFIG.getConfigValue(Config.CONF_LDAP_THREAD_COUNT));
	}


	private static long nbRequest = 0;

	/**
	 * Default constructor.
	 */
	private LdapUtils() {
	};

	private static List<Attributes> searchWithPaged(final String dn, final String filter) {
		if (LdapUtils.logger.isDebugEnabled()) {
			LdapUtils.logger.debug(String.format(
					"LDAP search with DN: [%1$s] and filter: [%2$s]...", dn, filter));
		}

		final List<Attributes> resultatRequete = new ArrayList<Attributes>();

		LdapContext ctx = null;

		try {
			ctx = new InitialLdapContext(LdapUtils.env, null);

			// Activate paged results
			byte[] cookie = null;
			ctx.setRequestControls(new Control[] { new PagedResultsControl(LdapUtils.PAGE_SIZE, Control.NONCRITICAL) });

			do {
				/* perform the search */
				final NamingEnumeration<SearchResult> results = ctx.search(dn, filter, new SearchControls());

				/* for each entry print out name + all attrs and values */
				while ((results != null) && results.hasMore()) {
					final SearchResult entry = results.next();
					resultatRequete.add(entry.getAttributes());
				}

				// Examine the paged results control response
				final Control[] controls = ctx.getResponseControls();
				if (controls != null) {
					for (Control control : controls) {
						if (control instanceof PagedResultsResponseControl) {
							final PagedResultsResponseControl prrc = (PagedResultsResponseControl) control;
							cookie = prrc.getCookie();
						}
					}
				} else {
					throw new RuntimeException("Le contrôleur de pagination n'est pas disponible pour ce serveur LDAP.");
				}
				// Re-activate paged results
				ctx.setRequestControls(new Control[] { new PagedResultsControl(LdapUtils.PAGE_SIZE, cookie, Control.CRITICAL) });

				LdapUtils.nbRequest++;
				// Log toutes les 100 requetes LDAP
				if ((LdapUtils.nbRequest % 100) == 0) {
					LdapUtils.logger.info("Nombre de requêtes LDAP: " + LdapUtils.nbRequest);
				}

			} while (cookie != null);
		} catch (NamingException e) {
			throw new RuntimeException("La pagination LDAP n'a pas aboutie : " + e.getMessage());
		} catch (IOException ie) {
			throw new RuntimeException("La pagination LDAP n'a pas aboutie : " + ie.getMessage());
		} finally {
			try {
				if (ctx != null) {
					ctx.close();
					// Recia Test correction "La pagination n'a pas abouti"
					// Machine trop rapide a ouvrir des connexions ?
					/*
					 * try { Thread.sleep(200); } catch(InterruptedException e)
					 * { throw new RuntimeException("Thread.sleep() error !" +
					 * e.getMessage()); }
					 */
					// Fin Correction RECIA
				}
			} catch (NamingException e) {
				throw new RuntimeException("Echec de la fermeture de la connexion : " + e.getMessage());
			}
		}
		return resultatRequete;
	}

	public static LdapPagination buildLdapPagination(final String dn, final String filter) throws TransactionException {
		final LdapPagination result = new LdapPagination(LdapUtils.PAGE_SIZE, dn, filter);

		return result;
	}

	public static List<Attributes> searchWithPagedStructure(final String filter) {
		return LdapUtils.searchWithPaged(LdapUtils.STRCUTURE_DN, filter);
	}

	public static List<Attributes> searchWithPagedPersonne(final String filter) {
		return LdapUtils.searchWithPaged(LdapUtils.PEOPLE_DN, filter);
	}
}
