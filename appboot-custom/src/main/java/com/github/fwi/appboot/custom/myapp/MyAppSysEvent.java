package com.github.fwi.appboot.custom.myapp;

import java.util.Arrays;

/**
 * Class receiving events from the custom AppBoot main class ({@link com.github.fwi.appboot.custom.MyAppBoot}).
 * 
 * This class would normally be present in a separate jar-file/Maven module or project
 * and reside among the core-classes of your application.
 * 
 * @author frederik
 *
 */
public class MyAppSysEvent {

	/**
	 * This method is called via {@link com.github.fwi.appboot.custom.MyAppBoot#fireEvent(String, Object...)}
	 * and should understand all "name" values given.
	 */
	public static void fireEvent(String name, Object... params) {
		
		System.out.println("System event [" + name + "] with " + Arrays.toString(params));
		switch(name) {
		case "startService":
			System.out.println("Already started");
			break;
		case "stopService":
			System.out.println("Exiting");
			System.exit(0); // triggers any shutdown-hooks to run
			break;
		default:
			System.out.println("No event handler implemented for [" + name + "]");
			break;
		}
	}

}
