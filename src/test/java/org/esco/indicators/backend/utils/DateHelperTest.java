/**
 * 
 */
package org.esco.indicators.backend.utils;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author GIP RECIA 2012 - Maxime BOSSARD.
 *
 */
@RunWith(value=BlockJUnit4ClassRunner.class)
@ContextConfiguration
public class DateHelperTest {

	@Test
	public void testGetFirstDayOfWeek1() throws Exception {
		Date date = new Date(112, 11, 31);
		Date result = DateHelper.getFirstDayOfWeek(date);

		Assert.assertNotNull("First day of week cannot be null !", result);
		Assert.assertTrue("First day of week cannot be the same instance !", result != date);
		Assert.assertTrue("First day of week is invalid ! ", result.equals(date));
	}

	@Test
	public void testGetFirstDayOfWeek2() throws Exception {
		Date date = new Date(113, 0, 1);
		Date result = DateHelper.getFirstDayOfWeek(date);

		Date fdow = new Date(112, 11, 31);
		Assert.assertNotNull("First day of week cannot be null !", result);
		Assert.assertTrue("First day of week is invalid ! ", result.equals(fdow));
	}

	@Test
	public void testGetFirstDayOfWeek3() throws Exception {
		Date date = new Date(113, 0, 1);
		Date d1 = DateHelper.getFirstDayOfWeek(date);
		Date d2 = DateHelper.getFirstDayOfWeek(date);

		Assert.assertNotNull("First day of week d1 cannot be null !", d1);
		Assert.assertNotNull("First day of week d2 cannot be null !", d2);
		Assert.assertTrue("First day of week cannot be the same instance ! ", d1 != d2);
		Assert.assertTrue("First day of week is invalid ! ", d1.equals(d2));
		Assert.assertTrue("First day of week is invalid with before ! ", !d1.before(d2));
		Assert.assertTrue("First day of week is invalid with after ! ", !d1.after(d2));
	}

	@Test
	public void testGetFirstDayOfMonth1() throws Exception {
		Date date = new Date(112, 11, 31);
		Date result = DateHelper.getFirstDayOfMonth(date);

		Date fdom = new Date(112, 11, 1);
		Assert.assertNotNull("First day of month cannot be null !", result);
		Assert.assertTrue("First day of month is invalid ! ", result.equals(fdom));
	}

	@Test
	public void testGetFirstDayOfMonth2() throws Exception {
		Date date = new Date(113, 0, 1);
		Date result = DateHelper.getFirstDayOfMonth(date);

		Assert.assertNotNull("First day of month cannot be null !", result);
		Assert.assertTrue("First day of month cannot be the same instance !", result != date);
		Assert.assertTrue("First day of month is invalid ! ", result.equals(date));
	}

	@Test
	public void testGetFirstDayOfMonth3() throws Exception {
		Date date = new Date(113, 0, 1);
		Date d1 = DateHelper.getFirstDayOfMonth(date);
		Date d2 = DateHelper.getFirstDayOfMonth(date);

		Assert.assertNotNull("First day of month d1 cannot be null !", d1);
		Assert.assertNotNull("First day of month d2 cannot be null !", d2);
		Assert.assertTrue("First day of month cannot be the same instance ! ", d1 != d2);
		Assert.assertTrue("First day of month is invalid ! ", d1.equals(d2));
		Assert.assertTrue("First day of month is invalid with before ! ", !d1.before(d2));
		Assert.assertTrue("First day of month is invalid with after ! ", !d1.after(d2));
	}
}
