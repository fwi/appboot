package com.github.fwi.appboot;

import static com.github.fwi.appboot.BootKeys.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.github.fwi.appboot.BootKeys;
import com.github.fwi.appboot.BootUtil;

public class TestUtil {

	@Test
	public void testVersion() {

		String v = BootUtil.getPomVersion(org.codehaus.plexus.util.Os.class);
		BootUtil.showln(v);
		assertFalse(v.equals(BootUtil.UNKNOWN_VERSION));
	}

	@Test
	public void testProps() {

		System.setProperty(APP_BOOT_DEBUG, "");
		boolean b = BootUtil.getPropBool(APP_BOOT_DEBUG, (String[])null);
		System.getProperties().remove(APP_BOOT_DEBUG);
		assertTrue(b);
		String[] args = new String[1];
		args[0] = APP_BOOT_DEBUG + "=false";
		b = BootUtil.getPropBool(APP_BOOT_DEBUG, args);
		assertFalse(b);

		System.setProperty(APP_NAME, "appboot");
		String s = BootUtil.getProp(APP_NAME, (String[])null);
		System.getProperties().remove(APP_NAME);
		assertEquals("appboot", s);
		args[0] = APP_NAME + "=appboot";
		s = BootUtil.getProp(APP_NAME, args);
		assertEquals("appboot", s);
		args[0] = APP_NAME + "=\"appboot\"";
		s = BootUtil.getProp(APP_NAME, args);
		assertEquals("appboot", s);

		assertEquals(0, BootKeys.filterAppBootArgs(args).length);
	}

	@Test
	public void testRethrow() {

		Exception e1 = new Exception("E1");
		Exception e2 = new Exception("E2", e1);
		Exception e3 = new Exception("E3", e2);
		e1.initCause(e3);
		try {
			BootUtil.throwRootCause(e3);
		} catch (Exception e) {
			// e.printStackTrace();
			assertNull(e.getCause());
			assertTrue(e instanceof RuntimeException);
			assertEquals("Exception - E2", e.getMessage());
			// assertEquals(e1, e.getCause());
			// System.out.println(e.toString() + " / " + e.getCause());
		}
		// System.out.println(e1.getClass().getName());
	}

}
