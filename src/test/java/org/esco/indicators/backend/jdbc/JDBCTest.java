/**
 * 
 */
package org.esco.indicators.backend.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author GIP RECIA 2013 - Maxime BOSSARD.
 *
 */
@RunWith(value=SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:localDb/db-context.xml"})
public class JDBCTest {


	@Autowired
	private DataSource dataSource;

	private JDBC jdbc;

	@Before
	public void init() {
		try {
			this.jdbc = new JDBC(this.dataSource);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testSynchronizeDeletedLdapUser() throws Exception {
		Date today = new Date();
		Collection<String> ldapUids = new ArrayList<String>();
		ldapUids.add("f01");
		//ldapUids.add("f02"); => Already deactivated
		ldapUids.add("f03");
		//ldapUids.add("f04");
		//ldapUids.add("f05"); => Already deactivated
		ldapUids.add("f06");
		ldapUids.add("f07");
		ldapUids.add("f08");
		ldapUids.add("f09");
		//ldapUids.add("f10");
		//ldapUids.add("f11"); => Reactivated
		ldapUids.add("f12");
		ldapUids.add("f13");
		ldapUids.add("f14");
		ldapUids.add("f15");
		ldapUids.add("f16");
		ldapUids.add("f17");
		ldapUids.add("f18");

		int deactivateCount = this.jdbc.synchronizeDeletedLdapUser(ldapUids, today);

		Assert.assertEquals("Only 3 account should be deactivated !", 3, deactivateCount);
	}

}
