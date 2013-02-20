/**
 * 
 */
package org.esco.indicators.backend.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import nl.bitwalker.useragentutils.Browser;
import nl.bitwalker.useragentutils.UserAgent;
import nl.bitwalker.useragentutils.Version;

import org.springframework.util.Assert;

/**
 * Represent statistics for a Browser with specific major version.
 * Comparable by Usage Count;
 * 
 * @author GIP RECIA 2013 - Maxime BOSSARD.
 *
 */
public class MajorVersionBrowserStats implements Comparable<MajorVersionBrowserStats> {

	private Browser browser;

	private Version majorVersion;

	private Set<Version> fullVersions = new TreeSet<Version>(new MinorVersionComparator());

	private Long usageCount = 0L;

	/**
	 * Build a Browser stats object for a browser in a specific major version.
	 * 
	 * @param pBrowser
	 * @param pMajorVersion
	 */
	public MajorVersionBrowserStats(final UserAgent userAgent) {
		super();
		Assert.notNull(userAgent, "UserAgent not provided !");

		this.browser = userAgent.getBrowser();
		this.majorVersion = userAgent.getBrowserVersion();

		Assert.notNull(this.browser, "Browser cannot be null !");
	}

	@Override
	public int compareTo(final MajorVersionBrowserStats obj) {
		return this.usageCount.compareTo(obj.getUsageCount());
	}

	/**
	 * Add a specific full version usage (Major.Minor) to the stats.
	 * Call this method to increment browser usage stats.
	 * 
	 * @param version
	 */
	public void addVersionUsage(final Version version) {
		Assert.isTrue((this.majorVersion == version) ||
				this.majorVersion.getMajorVersion().equals(version.getMajorVersion()),
				"Full version is not compatible with this BrowserStats object !");

		if (version != null) {
			this.fullVersions.add(version);
		}

		// Increment usage
		this.usageCount ++;
	}

	/**
	 * Get all specific versions ordered by minor version.
	 * 
	 * @return all versions
	 */
	public Collection<Version> getVersions() {
		final Collection<Version> lockedCollection =
				Collections.unmodifiableCollection(this.fullVersions);
		return lockedCollection;
	}

	/**
	 * Retrieve the usage count for this major version browser.
	 * 
	 * @return the usage count
	 */
	public Long getUsageCount() {
		return this.usageCount;
	}

	/**
	 * Get this browser stats Id (BrowserName - MajorVersion).
	 * 
	 * @return browser stats Id
	 */
	public String getId() {
		return MajorVersionBrowserStats.getId(this.browser, this.majorVersion);
	}

	/**
	 * Get this browser stats Id (BrowserName - MajorVersion).
	 * 
	 * @return browser stats Id
	 */
	public static String getId(final Browser pBrowser, final Version pMajorVersion) {
		Assert.notNull(pBrowser, "Browser cannot be null !");

		StringBuilder sb = new StringBuilder(32);
		sb.append(pBrowser.getName());
		if (pMajorVersion != null) {
			sb.append(" - ");
			sb.append(pMajorVersion.getMajorVersion());
		}

		return sb.toString();
	}

	@Override
	public String toString() {
		return this.getId();
	}

	/**
	 * Comparator of Version by minor version.
	 * 
	 * @author GIP RECIA 2013 - Maxime BOSSARD.
	 *
	 */
	private class MinorVersionComparator implements Comparator<Version> {

		@Override
		public int compare(final Version v1, final Version v2) {
			return v1.getMinorVersion().compareTo(v2.getMinorVersion());
		}

	}

}
