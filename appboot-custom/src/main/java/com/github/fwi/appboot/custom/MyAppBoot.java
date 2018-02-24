package com.github.fwi.appboot.custom;

import java.lang.reflect.Method;

import com.github.fwi.appboot.AppBoot;

/**
 * Example main class with additional methods that can be called from outside the JVM (e.g. by Apache Commons 
 * <a href="https://commons.apache.org/proper/commons-daemon/">Daemon</a>
 * or <a href="http://devwizard.free.fr/html/en/JavaExe.html">JavaExe</a>).
 * This class is not allowed to have dependencies on other jar-files.
 * The class receiving events (set in {@link #SYS_EVENT_CLASS}) is allowed to have dependencies on other jar-files.
 * <p>
 * I.e. this class lives in isolation (just like {@link AppBoot}) 
 * while the class receiving events from this class
 * has full access to all dependencies in the application.
 *  
 * @author frederik
 *
 */
public class MyAppBoot {
	
	/**
	 * The class represented by this string would normally reside inside your application module/project.
	 * It will be called by the <tt>fireEvent</tt> method in this <tt>MyAppBoot</tt> class 
	 * when one of the static methods in this class are called from outside the JVM ("system events").
	 * <br>The class is expected to have a static method <tt>fireEvent(String name, Object... params)</tt>
	 * (just like this class).
	 */
	public static final String SYS_EVENT_CLASS = "com.github.fwi.appboot.custom.myapp.MyAppSysEvent";

	public static void main(String[] args) {
		AppBoot.main(args);
	}
	
	private static final Object[] NO_PARAMS = new Object[0];
	
	/**
	 * Example method called from outside the JVM (system/service event).
	 */
	public static void runAsService() {
		fireEvent("startService", NO_PARAMS);
	}
	
	/**
	 * Another example method called from outside the JVM (system/service event).
	 */
	public static void stopService() {
		fireEvent("stopService", NO_PARAMS);
	}
	
	/**
	 * To prevent loading the system-event class twice, 
	 * use the Appboot boot class-loader to find the system-event class 
	 * and use reflection to invoke the fireEvent method.
	 * This will also prevent "class not found" errors for classes used by the system event class. 
	 */
	public static void fireEvent(String name, Object... params) {
		
		try {
			Class<?> sysEvent = AppBoot.bootClassLoader.loadClass(SYS_EVENT_CLASS);
			Method fireEvent = sysEvent.getMethod("fireEvent", String.class, Object[].class);
			fireEvent.invoke(null, name, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
