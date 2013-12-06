package org.esco.indicators.backend.service;



import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import javax.sql.DataSource;

import org.esco.indicators.backend.exception.LogLineToIgnore;
import org.esco.indicators.backend.jdbc.JDBC;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(value=SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:memoryDb/db-context.xml"})
public class LectureTest {

	private static final Date MAY_01_2012;
	private static final Date JUNE_11_2012;
	static {
		Calendar cal = Calendar.getInstance();
		cal.clear();

		cal.set(2012, 4, 1);
		MAY_01_2012 = cal.getTime();

		cal.set(2012, 5, 11);
		JUNE_11_2012 = cal.getTime();
	}

	@Autowired
	private DataSource dataSource;

	private Lecture lecture;

	@Before
	public void initLecture() throws ClassNotFoundException, SQLException {
		final JDBC jdbc = new JDBC(this.dataSource);
		this.lecture = new Lecture(jdbc);

		this.clearMaps();
	}

	public void clearMaps() {
		this.lecture.getDonneesConnexionEtabPersonne().clear();
		this.lecture.getDebutConnexion().clear();
		this.lecture.getDonneesConnexionPersonne().clear();
		this.lecture.getDonneesConnexionEtab().clear();
		this.lecture.getServiceJour().clear();
	}

	@Before
	public void transactions() throws SQLException {
		this.dataSource.getConnection().setAutoCommit(false);
		this.dataSource.getConnection().rollback();
	}

	/**
	 * Test de remplissage d'une ligne de service.
	 *  Cas normal, la ligne est correcte, donc toutes les valeurs doivent être remplies.
	 */
	@Test
	public void testFillCtargCcall_ExtNormal() throws Exception { // Cas normal : la ligne est correcte
		String ligne = "2012-06-14 07:24:19,785	netocentre3	CCALL_EXT	ENTEleve	18474	F11007ti	Pronote_JMonnet_Joue_Les_Tours	0450062Y	7";

		LogLine obtenue=this.lecture.fillCtargCcall_Ext(ligne,3);

		LogLine attendue = new LogLine();
		attendue.setEventType(LogLineTypeEnum.CCALL_EXT);
		// Date for 2012-05-01 00:13:26,120
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(2012, 5, 14, 7, 24, 19);
		cal.setTimeInMillis(cal.getTimeInMillis() + 785);
		attendue.setDate(cal.getTime());
		attendue.setNbacces(1);
		attendue.setObjectClass("ENTEleve");
		attendue.setPortail("netocentre3");
		attendue.setService("VieScolaire");
		attendue.setUai("0450062Y");
		attendue.setUid("F11007ti");
		attendue.setUiduPortal("18474");
		attendue.setIdSession("7");
		attendue.setTruncatedFname("Pronote");

		Assert.assertEquals("Wrong interpretation of CCALL_EXT line !", attendue, obtenue);
	}

	/**
	 *  Test de remplissage d'une ligne de service.
	 *  Cas d'erreur : objectclass non valide.
	 */
	@Test(expected=LogLineToIgnore.class)
	public void testFillCtargCcall_ExtCasErreur() throws Exception {
		String ligne = "2012-06-14 07:38:30,876	netocentre3	CTARG	ENTEleve42	4179	F0900ixy	ServiceTest	1	2	0180008L	3";

		this.lecture.fillCtargCcall_Ext(ligne, 3);
	}

	/**
	 *  Test de remplissage d'une ligne de connexion.
	 *  Cas normal, la ligne est correcte.
	 */
	@Test
	public void testFillStartStopNormal() throws Exception {
		// Cas normal : la ligne est correcte
		String ligne = "2012-05-01 00:13:26,120	netocentre3	SSTART	ENTEleve	4179	F0900ixy	0280036M	10";

		LogLine obtenue = this.lecture.fillStartStop(ligne, 2);
		LogLine attendue = new LogLine();
		attendue.setEventType(LogLineTypeEnum.SSTART);

		// Date for 2012-05-01 00:13:26,120
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(2012, 4, 1, 0, 13, 26);
		cal.setTimeInMillis(cal.getTimeInMillis() + 120);
		attendue.setDate(cal.getTime());
		attendue.setObjectClass("ENTEleve");
		attendue.setPortail("netocentre3");
		attendue.setIdSession("10");
		attendue.setUai("0280036M");
		attendue.setUid("F0900ixy");

		Assert.assertEquals("Wrong interpretation of SSTART line !", attendue, obtenue);
	}

	/**
	 *  Test de remplissage d'une ligne de connexion.
	 *  Cas d'erreur : objectclass non valide.
	 */
	@Test(expected=LogLineToIgnore.class)
	public void testFillStartStopCasErreur() throws Exception {
		String ligne = "2012-05-01 00:13:26,120	netocentre3	SSTOP	ENTTest	4179	F0900ixy	0280036M	10";

		this.lecture.fillStartStop(ligne, 2);
	}

	/**
	 *  Test de la méthode Jousuivant, qui renvoie le jour suivant de la date passée en parametre.
	 *  Tests de differents cas : changement d'année, de mois...
	 */
	@Test
	public void testJourSuivant() throws Exception {
		String obtenue1 = this.lecture.jourSuivant("2011-12-31");
		String attendue1 = "2012-01-01";

		String obtenue2 = this.lecture.jourSuivant("2012-06-30");
		String attendue2 = "2012-07-01";

		String obtenue3 = this.lecture.jourSuivant("2012-06-15");
		String attendue3 = "2012-06-16";

		Assert.assertEquals("Bad result : not the next day !", attendue1, obtenue1);
		Assert.assertEquals("Bad result : not the next day !", attendue2, obtenue2);
		Assert.assertEquals("Bad result : not the next day !", attendue3, obtenue3);
	}

	/**
	 *  Test du traitement des services.
	 *  Cas d'erreur : pas de debut associé à la connexion : pas de prise en compte du service.
	 */
	@Test
	public void testTraitementServiceCasErreur() throws Exception {
		String ligne = "2012-05-01 00:13:22,523	netocentre3	CTARG	ENTEleve	4179	F0900ixy	Pronote_JMonnet_Joue_Les_Tours	1	2	0180008L	3";
		LogLine l = this.lecture.fillCtargCcall_Ext(ligne, 3);
		this.lecture.traitementService(l);

		//Cas du  CTARG sans début de connexion associé donc erreur donc pas ajouté à la liste donc la taille de la liste doit etre 0
		Assert.assertEquals("The list must be empty if no Session start previously analyzed !", 0, this.lecture.getServiceJour().size());
	}

	/**
	 *  Test du traitement des services.
	 *  Cas d'insertion nouvel accès au service.
	 */
	@Test
	public void testTraitementServiceCasNormal1() throws Exception {
		String deb = "2012-05-01 00:10:39,353	netocentre3	SSTART	ENTEleve	4179	F0900ixy	0280036M	1001";
		String ligne = "2012-05-01 00:13:22,523	netocentre3	CTARG	ENTEleve	4179	F0900ixy	Pronote_JMonnet_Joue_Les_Tours	1	2	0280036M	1001";
		LogLine start = this.lecture.fillStartStop(deb, 0);
		LogLine event = this.lecture.fillCtargCcall_Ext(ligne, 3);

		Sstart debutCo = new Sstart(start);
		SstartValue val = new SstartValue(start);
		val.terminate();
		// Add start in map
		this.lecture.getDebutConnexion().put(debutCo, val);

		// Process event
		this.lecture.traitementService(event);

		//Cas insertion la connexion au service a bien ete ajoutée
		Assert.assertEquals("The size of the list must be 1 !", 1, this.lecture.getServiceJour().size());
	}

	/**
	 * Test du traitement des services.
	 * Cas du service deja existant pour cette personne, profil, uai, jour donc mise a jour du nombre d'accès.
	 */
	@Test
	public void testTraitementServiceCasNormal2() throws Exception {
		//Cas mise à jour d'une connexion aux services pour une personne

		String deb = "2012-05-01 00:10:39,353	netocentre3	SSTART	ENTEleve	4179	F0900ixy	0280036M	1001";
		String ligne1 = "2012-05-01 00:13:22,523	netocentre3	CTARG	ENTEleve	4179	F0900ixy	Pronote_JMonnet_Joue_Les_Tours	1	2	0280036M	1001";
		LogLine start = this.lecture.fillStartStop(deb, 0);
		LogLine event1 = this.lecture.fillCtargCcall_Ext(ligne1, 3);

		Sstart debutCo = new Sstart(start);
		SstartValue val = new SstartValue(start);
		val.terminate();
		//FIXME acces à la map en direct
		this.lecture.getDebutConnexion().put(debutCo, val);
		this.lecture.traitementService(event1);

		String ligne2 = "2012-05-01 00:13:22,523	netocentre3	CCALL_EXT	ENTEleve	4179	F0900ixy	Webclasseur	0280036M	1001";
		LogLine event2 = this.lecture.fillCtargCcall_Ext(ligne2, 3);

		Service s=new Service();
		s.setDate(LectureTest.MAY_01_2012);
		s.setObjectClass("ENTEleve");
		s.setService("Webclasseur");
		s.setUai("0280036M");
		s.setUid("F0900ixy");

		this.lecture.getServiceJour().put(s, 5);
		this.lecture.traitementService(event2);

		//Cas service deja existant : mise a jour.
		Assert.assertEquals("Bad hit count for service !", (Integer)6, this.lecture.getServiceJour().get(s));
	}

	/**
	 * Test du traitement des connexions de personnes.
	 * Cas d'erreur : pas de début associé à cette fin.
	 */
	@Test
	public void testTraitementConnexionPersonneCasErreur() throws Exception {
		String ligne = "2012-05-01 00:13:39,120	netocentre3	SSTOP	ENTEleve	4179	F0900ixy	0280036M	1001";

		LogLine l = this.lecture.fillStartStop(ligne, 2);
		this.lecture.traitementFermetureSession(l);

		//Pas de début associé donc on prend pas en compte donc la taille reste de 0
		Assert.assertEquals("Bad hit count for personne !", 0, this.lecture.getDonneesConnexionPersonne().size());
	}

	/**
	 * Test du traitement des connexions de personnes.
	 * Cas normal : Insertion de la connexion.
	 */
	@Test
	public void testTraitementConnexionPersonneCasNormal1() throws Exception {
		String ligne="2012-05-01 00:13:39,120	netocentre3	SSTOP	ENTEleve	4179	F0900ixy	0280036M	1001";

		LogLine l = this.lecture.fillStartStop(ligne, 2);

		String deb = "2012-05-01 00:10:39,353	netocentre3	SSTART	ENTEleve	4179	F0900ixy	0280036M	1001";
		LogLine debut = this.lecture.fillStartStop(deb, 0);
		Sstart debutCo = new Sstart(debut);
		SstartValue val = new SstartValue(debut);
		this.lecture.getDebutConnexion().put(debutCo, val);

		this.lecture.traitementFermetureSession(l);

		//Debut associé , on ajoute la connexion
		Assert.assertEquals("Bad hit count for personne !", 1, this.lecture.getDonneesConnexionPersonne().size());
	}

	/**
	 * Test du traitement des connexions de personnes.
	 * Cas normal : Mise a jour de la connexion.
	 */
	@Test
	public void testTraitementConnexionPersonneCasNormal2() throws Exception {
		String deb = "2012-05-01 00:10:39,353	netocentre3	SSTART	ENTEleve	4179	F0900ixy	0280036M	1001";
		String ligne = "2012-05-01 00:13:09,353	netocentre3	SSTOP	ENTEleve	4179	F0900ixy	0280036M	1001";

		LogLine lStart1 = this.lecture.fillStartStop(deb, 0);
		LogLine lStop1 = this.lecture.fillStartStop(ligne, 2);

		Sstart debutCo = new Sstart(lStart1);
		SstartValue val1 = new SstartValue(lStart1);

		this.lecture.getDebutConnexion().put(debutCo, val1);
		this.lecture.traitementFermetureSession(lStop1);

		deb = "2012-05-01 00:10:39,353	netocentre3	SSTART	ENTEleve	4179	F0900ixy	0280036M	1001";
		ligne = "2012-05-01 00:13:09,353	netocentre3	SSTOP	ENTEleve	4179	F0900ixy	0280036M	1001";

		LogLine lStart2 = this.lecture.fillStartStop(deb, 0);
		LogLine lStop2 = this.lecture.fillStartStop(ligne, 2);

		debutCo = new Sstart(lStart2);
		SstartValue val2 = new SstartValue(lStart2);

		this.lecture.getDebutConnexion().put(debutCo, val2);
		this.lecture.traitementFermetureSession(lStop2);

		ConnexionPersonne dc= new ConnexionPersonne();
		dc.setDate(LectureTest.MAY_01_2012);
		dc.setObjectClass("ENTEleve");
		dc.setUai("0280036M");
		dc.setUid("F0900ixy");

		DonneesConnexionPersonne dcpobtenue = this.lecture.getDonneesConnexionPersonne().get(dc);

		DonneesConnexionPersonne dcpattendue = new DonneesConnexionPersonne();
		dcpattendue.setNbCo(2);
		dcpattendue.setMoyenne(2.5D);

		// Cas deja une connexion pour cette personne
		Assert.assertEquals("Bad personne connexion data !", dcpattendue, dcpobtenue);
	}

	/**
	 * Test du traitement des connexions à un établissement.
	 * Cas normal : La liste est vide au départ donc à la fin il doit y avoir l'établissement + 4 sommes. On verifie donc la présence des sommes
	 * (Pour que le traitement fonctionne correctement, l'établissement Etab1 doit exister en base de données (Etab1	45	CFA).
	 */
	@Test
	public void testTraitementVisiteursEtabCasNormal1() throws Exception {
		ConnexionPersonne cp = new ConnexionPersonne();
		cp.setDate(LectureTest.JUNE_11_2012);
		cp.setObjectClass("ENTEleve");
		cp.setUai("Etab1");
		cp.setUid("AAAAAAA");
		DonneesConnexionPersonne dcp = new DonneesConnexionPersonne();
		dcp.setMoyenne(20);
		dcp.setNbCo(5);
		this.lecture.traitementVisiteursEtab(cp, dcp);

		//On va tester la présence des différentes sommes
		DonneesConnexion dc = new DonneesConnexion();
		dc.setDate(LectureTest.JUNE_11_2012);
		dc.setTypeEtab("CFA");
		dc.setUai("Etab1");
		Assert.assertTrue(this.lecture.getDonneesConnexionEtab().containsKey(dc));

		dc.setDate(LectureTest.JUNE_11_2012);
		dc.setTypeEtab(Lecture.TOTAL);
		dc.setUai(Lecture.TOTAL);
		Assert.assertTrue(this.lecture.getDonneesConnexionEtab().containsKey(dc));

		dc.setDate(LectureTest.JUNE_11_2012);
		dc.setTypeEtab(Lecture.TOTAL);
		dc.setUai("28");
		Assert.assertTrue(this.lecture.getDonneesConnexionEtab().containsKey(dc));

		dc.setDate(LectureTest.JUNE_11_2012);
		dc.setTypeEtab("CFA");
		dc.setUai(Lecture.TOTAL);
		Assert.assertTrue(this.lecture.getDonneesConnexionEtab().containsKey(dc));

		dc.setDate(LectureTest.JUNE_11_2012);
		dc.setTypeEtab("CFA");
		dc.setUai("28");
		Assert.assertTrue(this.lecture.getDonneesConnexionEtab().containsKey(dc));
	}

	/**
	 * Test du traitement des connexions à un établissement.
	 * Cas où on a une mise à jour de certaines sommes. (connexion dans le meme departement sur un etablissement d'un type différent).
	 * On verifie que ces sommes ont bien été mises à jour et que on a la bonne taille pour la liste
	 */
	@Test
	public void testTraitementVisiteursEtabCasNormal2() throws Exception {
		ConnexionPersonne cp = new ConnexionPersonne();
		cp.setDate(LectureTest.JUNE_11_2012);
		cp.setObjectClass("ENTEleve");
		cp.setUai("Etab1");
		cp.setUid("AAAAAAA");
		DonneesConnexionPersonne dcp = new DonneesConnexionPersonne();
		dcp.setMoyenne(20);
		dcp.setNbCo(5);
		this.lecture.traitementVisiteursEtab(cp, dcp);

		cp.setDate(LectureTest.JUNE_11_2012);
		cp.setObjectClass("ENTEleve");
		cp.setUai("Etab2");
		cp.setUid("BBBBBBBB");
		dcp.setMoyenne(20);
		dcp.setNbCo(5);
		this.lecture.traitementVisiteursEtab(cp, dcp);

		Assert.assertEquals(8, this.lecture.getDonneesConnexionEtab().size());

		DonneesConnexion dc=new DonneesConnexion();
		dc.setDate(LectureTest.JUNE_11_2012);
		dc.setTypeEtab(Lecture.TOTAL);
		dc.setUai("28");

		DonneesEtab deobtenues=this.lecture.getDonneesConnexionEtab().get(dc);
		DonneesEtab deattendues= new DonneesEtab();
		deattendues.setNbvisites(10);
		deattendues.setNbvisiteurs(2);
		deattendues.setTypestat(TypeStatEnum.TOTAL_UN_DEPARTEMENT);
		Assert.assertEquals(deattendues, deobtenues);

	}

	@Test(expected=FileNotFoundException.class)
	public void fileNotFoundTest() throws Exception {
		String statsFilePath = "/indicateurs_usages/src/test/resources/logsalire/2012/00/2012-00.log";
		ProcessingModeEnum processMode = ProcessingModeEnum.DAILY;

		this.lecture.informations(statsFilePath, processMode, true);
	}

	@Test
	public void fileNotFoundTest2() throws Exception {
		String statsFilePath = "/indicateurs_usages/src/test/resources/logsalire/2012/00/2012-00.log";
		ProcessingModeEnum processMode = ProcessingModeEnum.DAILY;

		boolean ok = this.lecture.traitementLog(statsFilePath, processMode, true);

		Assert.assertFalse("Traitement not KO !", ok);
	}

	@Test
	public void dailyProcessingDryRunTest() throws Exception {
		String statsFilePath = "src/test/resources/logsalire/2013/02/2013-02-10.log";
		ProcessingModeEnum processMode = ProcessingModeEnum.DAILY;

		boolean ok = this.lecture.traitementLog(statsFilePath, processMode, true);

		Assert.assertTrue("Traitement KO !", ok);
	}

	@Test
	@Ignore
	public void monthlyProcessingDryRunTest() throws Exception {
		String statsFilePath = "src/test/resources/logsalire/2012/09/2012-09.log";
		ProcessingModeEnum processMode = ProcessingModeEnum.MONTHLY;

		this.lecture.traitementLog(statsFilePath, processMode, true);
	}

}
