package org.esco.indicators.backend.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.esco.indicators.backend.jdbc.JDBC;
import org.esco.indicators.backend.model.ProcessingModeEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(value=SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:localDb/db-context.xml"})
public class LocalDbTest {

	@Autowired
	private DataSource dataSource;

	//@Before
	public void transactionsBefore() throws SQLException {
		final Connection connection = this.dataSource.getConnection();
		connection.setAutoCommit(false);

		final String query = "begin";
		Statement statement = connection.createStatement();
		statement.execute(query);
		statement.close();
	}

	//@After
	public void transactionsAfter() throws SQLException {
		this.dataSource.getConnection().rollback();
	}

	//@Test//(timeout=0)
	public void dailyProcessingTest() throws Exception {
		String statsFilePath = "src/test/resources/logsalire/2012/09/2012-09-24.log";
		ProcessingModeEnum processMode = ProcessingModeEnum.DAILY;

		JDBC jdbc = new JDBC(this.dataSource);

		final TraitementLDAP traitementLdap = new TraitementLDAP(jdbc);
		traitementLdap.processEtablishments(jdbc.getConnection());

		final Lecture lecture = new Lecture(jdbc);
		lecture.traitementLog(statsFilePath, processMode, false);
	}

	@Test//(timeout=0)
	public void dailyLdapProcessingTest() throws Exception {
		JDBC jdbc = new JDBC(this.dataSource);

		final TraitementLDAP traitementLdap = new TraitementLDAP(jdbc);
		traitementLdap.traitementLdap();
	}

	//@Test(timeout=0)
	public void monthlyProcessingTest() throws Exception {
		String statsFilePath = "src/test/resources/logsalire/2012/09/2012-09.log";
		ProcessingModeEnum processMode = ProcessingModeEnum.MONTHLY;

		JDBC jdbc = new JDBC(this.dataSource);

		final TraitementLDAP traitementLdap = new TraitementLDAP(jdbc);
		traitementLdap.processEtablishments(jdbc.getConnection());

		final Lecture lecture = new Lecture(jdbc);
		lecture.traitementLog(statsFilePath, processMode, false);
	}

	@Test
	public void ldapProcessingTest() throws Exception {
		final JDBC jdbc = new JDBC(this.dataSource);

		final TraitementLDAP traitementLdap = new TraitementLDAP(jdbc);
		traitementLdap.traitementLdap();
	}

}
