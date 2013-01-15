/**
 * 
 */
package org.esco.indicators.backend.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.esco.indicators.backend.exception.TransactionException;
import org.springframework.util.Assert;

/**
 * Allow iteration on page results of a LDAP search.
 * 
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 *
 */
public class LdapPagination {

	private static final Log LOGGER = LogFactory.getLog(LdapPagination.class);

	private List<Attributes> pageContent;

	private final int pageSize;

	private final String dn;

	private final String filter;

	private final SearchControls searchControls;

	private LdapContext pageContext;

	private boolean firstPageNotLoaded = true;

	private byte[] cookie;

	protected LdapPagination(final int size, final String dn, final String filter) throws TransactionException {
		super();

		Assert.isTrue(size > 0, "Size must be positive !");
		Assert.hasText(dn, "DN must be supplied !");
		Assert.hasText(filter, "Filter must be supplied !");

		this.pageSize = size;
		this.pageContent = new ArrayList<Attributes>(this.pageSize);
		this.dn = dn;
		this.filter = filter;

		final SearchControls sc = new SearchControls();
		sc.setCountLimit(0);
		sc.setTimeLimit(0);
		this.searchControls = sc;

		// Init search
		this.initSearch();
	}

	protected void initSearch() throws TransactionException {
		try {
			this.pageContext = new InitialLdapContext(LdapUtils.env, null);
			this.pageContext.setRequestControls(new Control[] { new PagedResultsControl(this.pageSize, Control.CRITICAL) });
		} catch (NamingException e) {
			LdapPagination.LOGGER.error("Unable to load LDAP page !", e);
			throw new TransactionException("Unable to load LDAP page !", e);
		} catch (IOException e) {
			LdapPagination.LOGGER.error("Unable to load LDAP page !", e);
			throw new TransactionException("Unable to load LDAP page !", e);
		}
	}

	protected void loadPage() throws TransactionException {
		this.pageContent.clear();

		try {
			/* perform the search */
			final NamingEnumeration<SearchResult> results = this.pageContext.search(this.dn, this.filter, this.searchControls);

			/* for each entry print out name + all attrs and values */
			while ((results != null) && results.hasMore()) {
				final SearchResult entry = results.next();
				this.pageContent.add(entry.getAttributes());
			}

			// Examine the paged results control response
			final Control[] controls = this.pageContext.getResponseControls();
			if (controls != null) {
				for (Control control : controls) {
					if (control instanceof PagedResultsResponseControl) {
						final PagedResultsResponseControl prrc = (PagedResultsResponseControl) control;
						this.cookie = prrc.getCookie();
					}
				}
			} else {
				throw new RuntimeException("Le contrôleur de pagination n'est pas disponible pour ce serveur LDAP.");
			}
			// Re-activate paged results
			this.pageContext.setRequestControls(new Control[] { new PagedResultsControl(this.pageSize, this.cookie, Control.CRITICAL) });

			this.firstPageNotLoaded = false;
		} catch (NamingException e) {
			LdapPagination.LOGGER.error("Unable to load LDAP page !", e);
			throw new TransactionException("Unable to load LDAP page !", e);
		} catch (IOException e) {
			LdapPagination.LOGGER.error("Unable to load LDAP page !", e);
			throw new TransactionException("Unable to load LDAP page !", e);
		}
	}

	/**
	 * Test if the current page is followed by another one.
	 * 
	 * @return true if the current page is not the last one
	 */
	public boolean hasNext() {
		boolean test = (this.cookie != null) || this.firstPageNotLoaded;

		if (!test) {
			// We reach the last page.
			try {
				this.pageContent = null;
				if (this.pageContext != null) {
					this.pageContext.close();
				}
			} catch (NamingException e) {
				LdapPagination.LOGGER.error("Unable to close LDAP context !", e);
			}
		}

		return test;
	}

	public List<Attributes> next() throws TransactionException {
		if (this.hasNext()) {
			this.loadPage();
		} else {
			throw new IllegalStateException("");
		}

		return this.pageContent;
	}

	public int getPageSize() {
		return this.pageSize;
	}

}
