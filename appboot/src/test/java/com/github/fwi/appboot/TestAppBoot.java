package com.github.fwi.appboot;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestAppBoot {

	@Test
	public void testMain() {
		
		try {
			AppBoot.main("");
			// should just show some info
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
