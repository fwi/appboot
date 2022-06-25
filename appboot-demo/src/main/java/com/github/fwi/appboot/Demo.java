package com.github.fwi.appboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This project provides an assembly that can be used to test appboot functions.
 * <br>After running <code>maven clean package</code>:
 * <br>	- use <tt>target/test-classes/rundemo.bat</tt> to run Appboot in a Maven test environment and
 * <br>	- use <tt>target/appboot-demo-&lt;version&gt;/run.bat</tt> to run Appboot in an installed-app environment. 
 *
 * @author fwiers
 *
 */
public class Demo {

	private static final Logger log = LoggerFactory.getLogger(Demo.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		log.info("AppBoot Demo Jar " + BootUtil.getPomVersion(Demo.class) + " - main method in " + Demo.class);
		log.info("The log-statements should show a time-stamp as configured in logback(-test).xml");
		StringBuilder sb = new StringBuilder("Main Arguments: ");
		if (BootUtil.isEmpty(args)) {
			sb.append("none");
		} else {
			for (String s : args) {
				sb.append('\n').append(s);
			}
		}
		log.info(sb.toString());
		if (System.getProperties().containsKey(BootKeys.APP_MAVEN_TEST)) {
			log.info("Started in Maven test environment.");
		} else {
			log.info("Started in assembly environment.");
		}
	}

}
