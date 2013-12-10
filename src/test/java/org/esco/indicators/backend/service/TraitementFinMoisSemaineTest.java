package org.esco.indicators.backend.service;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.lang.time.DateUtils;
import org.esco.indicators.backend.jdbc.JDBC;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(value=SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:memoryDb/db-context.xml"})
public class TraitementFinMoisSemaineTest {

	/** Fixed oldest stat Date to 02/12/2012. */
	private static final Date OLDEST_STAT_DATE = new Date(112, 11, 2);

	/** Fixed d day 1 for running tests. */
	private static final Date TEST_RUNNING_DAY1 = new Date(113, 0, 6);

	/** Fixed d day 2 for running tests. */
	private static final Date TEST_RUNNING_DAY2 = new Date(113, 0, 8);

	/** Fixed d day 3 for running tests. */
	private static final Date TEST_RUNNING_DAY3 = new Date(113, 0, 12);

	/** Fixed d day 4 for running tests. */
	private static final Date TEST_RUNNING_DAY4 = new Date(113, 0, 5);

	/** Fixed d day 5 for running tests. */
	private static final Date TEST_RUNNING_DAY5 = new Date(112, 11, 29);

	/** First first day of month for TEST_RUNNING_DAY. */
	private static final Date FIRST_FDOM = new Date(112, 11, 1);

	/** Last first day of month for TEST_RUNNING_DAY. */
	private static final Date LAST_FDOM = new Date(112, 11, 1);

	/** First first day of week for TEST_RUNNING_DAY. */
	private static final Date FIRST_FDOW = new Date(112, 10, 26);

	/** Last first day of week for TEST_RUNNING_DAY. */
	private static final Date LAST_FDOW = new Date(112, 11, 31);

	@Autowired
	private DataSource dataSource;

	@Mock(answer=Answers.RETURNS_MOCKS)
	private JDBC jdbc;

	/** Mocked with Mockito.spy(). */
	private TraitementFinMoisSemaine traitement;

	@Before
	public void initMocks() throws SQLException {
		MockitoAnnotations.initMocks(this);

		// Mock JDBC.findOldestStatDate() method to return a fixed Date
		Mockito.when(this.jdbc.findOldestStatDate()).thenReturn(TraitementFinMoisSemaineTest.OLDEST_STAT_DATE);

		// Mock TraitementAuto
		this.traitement = Mockito.spy(new TraitementFinMoisSemaine(this.jdbc));

		Mockito.doReturn(false).when(this.traitement).isMonthAlreadyProcessed(Matchers.any(Date.class));
		Mockito.doReturn(false).when(this.traitement).trousMois(Matchers.any(Date.class));
		// Compression mois ne doit pas être appelé dans le futur !
		Mockito.doAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(final InvocationOnMock invocation) throws Throwable {
				Date compressionMonth = (Date)invocation.getArguments()[0];
				Assert.assertNotNull("Compression month cannot be null !", compressionMonth);
				Assert.assertTrue("Compression month is in the future !", compressionMonth.before(TraitementFinMoisSemaineTest.LAST_FDOM)
						|| TraitementFinMoisSemaineTest.LAST_FDOM.equals(compressionMonth));
				Assert.assertTrue("Compression month is in the passed !", compressionMonth.after(TraitementFinMoisSemaineTest.FIRST_FDOM)
						|| TraitementFinMoisSemaineTest.FIRST_FDOM.equals(compressionMonth));

				return true;
			}
		}).when(this.traitement).traitementCompressionMois(Matchers.any(Date.class), Matchers.anyBoolean());
		Mockito.doReturn(false).when(this.traitement).isWeekAlreadyProcessed(Matchers.any(Date.class));
		Mockito.doReturn(false).when(this.traitement).trousSemaine(Matchers.any(Date.class));
		// Compression semaine ne doit pas être appelé dans le futur !
		Mockito.doAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(final InvocationOnMock invocation) throws Throwable {
				Date compressionWeek = (Date)invocation.getArguments()[0];
				Assert.assertNotNull("Compression week cannot be null !", compressionWeek);
				Assert.assertTrue("Compression week is in the future !", compressionWeek.before(TraitementFinMoisSemaineTest.LAST_FDOW)
						|| TraitementFinMoisSemaineTest.LAST_FDOW.equals(compressionWeek));
				Assert.assertTrue("Compression week is in the passed !", compressionWeek.after(TraitementFinMoisSemaineTest.FIRST_FDOW)
						|| TraitementFinMoisSemaineTest.FIRST_FDOW.equals(compressionWeek));

				return true;
			}
		}).when(this.traitement).traitementCompressionSemaine(Matchers.any(Date.class), Matchers.anyBoolean());
	}

	/**
	 * Test de la méthode TrousMois
	 * On verifie que on renvoie bien qu'il y a un trou pour le mois de juin de l'an 3000
	 */
	@Test
	public void testTrousMois() throws Exception {
		JDBC jdbc = new JDBC(this.dataSource);
		TraitementFinMoisSemaine traitement = new TraitementFinMoisSemaine(jdbc);
		Calendar cal = Calendar.getInstance();
		cal.set(3000, 6, 0, 0, 0);
		Assert.assertTrue(traitement.trousMois(cal.getTime()));
	}

	/**
	 * Test de la méthode TrousSemaine
	 * On vérifie qu'il y a un trou dans la semaine commençant le 12-09-2112
	 */
	@Test
	public void testTrousSemaine() throws Exception {
		JDBC jdbc=new JDBC(this.dataSource);
		TraitementFinMoisSemaine traitement = new TraitementFinMoisSemaine(jdbc);
		Calendar cal = Calendar.getInstance();
		cal.set(2112, 9, 12, 0, 0);
		Assert.assertTrue(traitement.trousMois(cal.getTime()));
	}

	/**
	 * Test un traitement auto via le mock de JDBC.
	 * Le traitement va s'executer depuis la plus vielle stat (date mocké OLDEST_STAT_DATE),
	 * jusqu'à la date passé en argument (le Dim. 06/01/2013)
	 * => 1 Mois et 5 Semaines à compresser
	 */
	@Test
	public void testTraitementAuto1() throws Exception {
		final Date truncatedDay = DateUtils.truncate(TraitementFinMoisSemaineTest.TEST_RUNNING_DAY1, Calendar.DAY_OF_MONTH);
		this.traitement.traitementAuto(truncatedDay);

		// 1 seule Compression Mois doit être executée
		Mockito.verify(this.traitement, Mockito.times(1))
		.traitementCompressionMois(Matchers.any(Date.class), Matchers.anyBoolean());

		// 6 Compression Semaine doivent être executées
		Mockito.verify(this.traitement, Mockito.times(6))
		.traitementCompressionSemaine(Matchers.any(Date.class), Matchers.anyBoolean());
	}

	/**
	 * Test un traitement auto via le mock de JDBC.
	 * Le traitement va s'executer depuis la plus vielle stat (date mocké OLDEST_STAT_DATE),
	 * jusqu'à la date passé en argument (le Mar. 08/01/2013)
	 * => 1 Mois et 5 Semaines à compresser
	 */
	@Test
	public void testTraitementAuto2() throws Exception {
		this.traitement.traitementAuto(TraitementFinMoisSemaineTest.TEST_RUNNING_DAY2);

		// 1 seule Compression Mois doit être executée
		Mockito.verify(this.traitement, Mockito.times(1))
		.traitementCompressionMois(Matchers.any(Date.class), Matchers.anyBoolean());

		// 6 Compression Semaine doivent être executées
		Mockito.verify(this.traitement, Mockito.times(6))
		.traitementCompressionSemaine(Matchers.any(Date.class), Matchers.anyBoolean());
	}

	/**
	 * Test un traitement auto via le mock de JDBC.
	 * Le traitement va s'executer depuis la plus vielle stat (date mocké OLDEST_STAT_DATE),
	 * jusqu'à la date passé en argument (le Dim. 12/01/2013)
	 * => 1 Mois et 5 Semaines à compresser
	 */
	@Test
	public void testTraitementAuto3() throws Exception {
		this.traitement.traitementAuto(TraitementFinMoisSemaineTest.TEST_RUNNING_DAY3);

		// 1 seule Compression Mois doit être executée
		Mockito.verify(this.traitement, Mockito.times(1))
		.traitementCompressionMois(Matchers.any(Date.class), Matchers.anyBoolean());

		// 6 Compression Semaine doivent être executées
		Mockito.verify(this.traitement, Mockito.times(6))
		.traitementCompressionSemaine(Matchers.any(Date.class), Matchers.anyBoolean());
	}


	/**
	 * Test un traitement auto via le mock de JDBC.
	 * Le traitement va s'executer depuis la plus vielle stat (date mocké OLDEST_STAT_DATE),
	 * jusqu'à la date passé en argument (le Sam. 05/01/2013)
	 * => 1 Mois et 4 Semaines à compresser
	 */
	@Test
	public void testTraitementAuto4() throws Exception {
		this.traitement.traitementAuto(TraitementFinMoisSemaineTest.TEST_RUNNING_DAY4);

		// 1 seule Compression Mois doit être executée
		Mockito.verify(this.traitement, Mockito.times(1))
		.traitementCompressionMois(Matchers.any(Date.class), Matchers.anyBoolean());

		// 6 Compression Semaine doivent être executées
		Mockito.verify(this.traitement, Mockito.times(5))
		.traitementCompressionSemaine(Matchers.any(Date.class), Matchers.anyBoolean());
	}

	/**
	 * Test un traitement auto via le mock de JDBC.
	 * Le traitement va s'executer depuis la plus vielle stat (date mocké OLDEST_STAT_DATE),
	 * jusqu'à la date passé en argument (le Sam. 29/12/2012)
	 * => 0 Mois et 4 Semaines à compresser
	 */
	@Test
	public void testTraitementAuto5() throws Exception {
		this.traitement.traitementAuto(TraitementFinMoisSemaineTest.TEST_RUNNING_DAY5);

		// 1 seule Compression Mois doit être executée
		Mockito.verify(this.traitement, Mockito.times(0))
		.traitementCompressionMois(Matchers.any(Date.class), Matchers.anyBoolean());

		// 6 Compression Semaine doivent être executées
		Mockito.verify(this.traitement, Mockito.times(4))
		.traitementCompressionSemaine(Matchers.any(Date.class), Matchers.anyBoolean());
	}
}
