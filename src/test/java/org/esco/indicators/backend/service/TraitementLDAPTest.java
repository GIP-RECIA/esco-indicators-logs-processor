package org.esco.indicators.backend.service;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import org.esco.indicators.backend.jdbc.model.ACommeProfil;
import org.junit.Assert;
import org.junit.Test;


public class TraitementLDAPTest {

	private TraitementLDAP traitement;
	{
		try {
			this.traitement = new TraitementLDAP();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Test de ToTimestamp. On vérifie que le résultat renvoyé est le bon. (de
	 * YYYY-MM-DD on passe a YYYYMMDD0000000Z).
	 */
	@Test
	public void testToTimestamp2() throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(2012, 5, 13);
		String attendu = "20120613000000Z";
		Assert.assertEquals("", attendu, this.traitement.toTimestamp(cal));
	}

	/**
	 * Test de Parent. Cas d'un parent (doit renvoyer true).
	 * 
	 * @throws NamingException
	 */
	@Test
	public void testParentTrue() throws Exception {
		Attributes parent = new BasicAttributes();
		Attribute objc = new BasicAttribute("objectClass");
		parent.put(objc);
		objc.add("ENTTest1");
		objc.add("ENTTest2");
		objc.add("ENTAuxPersRelEleve");
		Assert.assertTrue(this.traitement.isParent(parent));
	}

	/**
	 * Test de Parent. Cas d'un non parent (doit renvoyer false).
	 * 
	 * @throws NamingException
	 */
	@Test
	public void testParentFalse() throws Exception {
		Attributes parent = new BasicAttributes();
		Attribute objc = new BasicAttribute("objectClass");
		parent.put(objc);
		objc.add("ENTTest1");
		objc.add("ENTTest2");
		objc.add("ENTPersonn");
		Assert.assertFalse(this.traitement.isParent(parent));
	}

	/**
	 * Test deListeProfilLdap. Doit renvoyer la listes des profils de la
	 * personne. On va comparer la liste de profils obtenue avec celle qu'on
	 * devrait avoir. Pour que le test soit correct, il faut que toutes les
	 * valeurs obtenues soient ce qu'on doit avoir et que le nombre de valeurs
	 * obtenues soit le bon.
	 * 
	 * @throws NamingException
	 */
	@Test
	public void testListeProfilLdap() throws Exception {
		Attribute profil = new BasicAttribute("objectClass");
		profil.add("ENTAuxEnseignant");
		profil.add("ENTAuxNonEnsEtab");
		profil.add("ENTPersonn");
		Attribute uai = new BasicAttribute("ESCOUAI");
		uai.add("Etab1");
		uai.add("Etab2");
		Attribute uid = new BasicAttribute("uid");
		uid.add("Personne1");

		Attributes att = new BasicAttributes();
		att.put(uai);
		att.put(uid);
		att.put(profil);
		Set<ACommeProfil> res = this.traitement.listeProfilLdap(att);

		ACommeProfil tmp1 = new ACommeProfil("Personne1", "Etab1", "ENTAuxEnseignant", null, null);
		ACommeProfil tmp2 = new ACommeProfil("Personne1", "Etab2", "ENTAuxEnseignant", null, null);
		ACommeProfil tmp3 = new ACommeProfil("Personne1", "Etab1", "ENTAuxNonEnsEtab", null, null);
		ACommeProfil tmp4 = new ACommeProfil("Personne1", "Etab2", "ENTAuxNonEnsEtab", null, null);

		for (ACommeProfil r : res) {
			if (tmp1.equals(r) || tmp2.equals(r) || tmp3.equals(r) || tmp4.equals(r)) {

			} else {
				Assert.fail("Le profil ne devrait pas exister");
			}
		}
		Assert.assertEquals(res.size(), 4);
	}

}
