package com.github.fwi.appboot;

import static com.github.fwi.appboot.AppBoot.debug;
import static com.github.fwi.appboot.AppBoot.mavenTest;
import static com.github.fwi.appboot.BootKeys.APP_CONF_DIR;
import static com.github.fwi.appboot.BootKeys.APP_HOME_DIR;
import static com.github.fwi.appboot.BootKeys.APP_LIB_DIRNAME;
import static com.github.fwi.appboot.BootUtil.endWithSep;
import static com.github.fwi.appboot.BootUtil.getFullPath;
import static com.github.fwi.appboot.BootUtil.getJarFile;
import static com.github.fwi.appboot.BootUtil.getProp;
import static com.github.fwi.appboot.BootUtil.showln;

import java.io.File;

public class AppBootDirs {

	public File appHomeDir;
	public File appLibDir;
	public File appConfDir;
	
	/**
	 * Determines values for home-, lib- and conf-dir.
	 * If {@link #appHomeDir} is not set, the application should exit - further startup will not be possible.
	 */
	public void getAppDirs(String... args) {
		
		String libDirName = getProp(APP_LIB_DIRNAME, args);
		if (libDirName == null) {
			libDirName = "lib";
		}
		String homeProp = getProp(APP_HOME_DIR, args);
		if (homeProp != null) {
			appHomeDir = new File(homeProp);
			if (!appHomeDir.isDirectory()) {
				throw new RuntimeException("Application home directory does not exist: " + homeProp);
			}
		} else {
			File bootFile = getJarFile(AppResource.class);
			if (bootFile == null) {
				return;
			}
			appHomeDir = bootFile.getParentFile();
			if (mavenTest) {
				// target/dependency
				appLibDir = appHomeDir;
				// target/test-classes
				appHomeDir = new File(endWithSep(appLibDir.getParent()) + "test-classes");
			} else if (appHomeDir.getName().equalsIgnoreCase(libDirName)) {
				appLibDir = appHomeDir;
				appHomeDir = appHomeDir.getParentFile();
			}
		}
		System.setProperty(APP_HOME_DIR, endWithSep(getFullPath(appHomeDir)));
		if (appLibDir == null) {
			appLibDir = new File(endWithSep(appHomeDir.getPath()) + libDirName);
			if (!appLibDir.isDirectory()) {
				appLibDir = null;
			}
		}
		String confProp = getProp(APP_CONF_DIR, args);
		if (confProp != null) {
			appConfDir = new File(System.getProperty(APP_HOME_DIR) + confProp);
			if (!appConfDir.isDirectory()) {
				appConfDir = new File(confProp);
			}
			if (!appConfDir.isDirectory()) {
				throw new RuntimeException("Application configuration directory does not exist: " + confProp);
			}
		} else {
			 appConfDir = new File(endWithSep(appHomeDir.getPath()) + "conf");
			 if (!appConfDir.isDirectory()) {
				 appConfDir = null;
			 }
		}
		if (appConfDir != null) {
			System.setProperty(APP_CONF_DIR, endWithSep(getFullPath(appConfDir)));
		}
		if (debug) {
			showln("Application home: " + appHomeDir);
			showln("Application lib : " + appLibDir);
			showln("Application conf: " + appConfDir);
		}
	}
	
}
