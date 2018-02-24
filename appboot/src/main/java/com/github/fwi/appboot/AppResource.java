package com.github.fwi.appboot;

import static com.github.fwi.appboot.AppBoot.*;
import static com.github.fwi.appboot.BootUtil.*;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Functions related to looking up resources for the boot class loader.
 *
 */
public class AppResource {

	/**
	 * The main function looking up the files and directories for the boot class loader.
	 */
	public static List<File> getResources(AppBootDirs bootDirs) {
		
		@SuppressWarnings("serial")
		List<File> classPaths = new ArrayList<File>() {
			public boolean add(File f) {
				if (debug) {
					showln("Added to class-path: " + f);
				}
				return super.add(f);
			}
		};
		if (mavenTest || bootDirs.appLibDir == null || bootDirs.appConfDir == null) {
			classPaths.add(bootDirs.appHomeDir);
			addJars(classPaths, bootDirs.appHomeDir);
		}
		if (mavenTest) {
			// add target/classes
			classPaths.add(new File(endWithSep(bootDirs.appHomeDir.getParent()) + "classes"));
		}
		if (bootDirs.appConfDir != null) {
			classPaths.add(bootDirs.appConfDir);
		}
		if (bootDirs.appLibDir != null) {
			classPaths.add(bootDirs.appLibDir);
			addJars(classPaths, bootDirs.appLibDir);
		}
		return classPaths;
	}
	
	/**
	 * Adds any file with a name ending with ".jar" (case-insensitive) in dir to classPaths.
	 */
	public static void addJars(List<File> classPaths, File dir) {
		
		String[] fileNames = dir.list();
		String dirName = endWithSep(dir.getPath());
		for (String fname : fileNames) {
			if (fname.toLowerCase().endsWith(".jar")) {
				File f = new File(dirName + fname);
				if (f.isFile()) {
					classPaths.add(new File(dirName + fname));
				}
			}
		}
	}

	/**
	 * Searches for a jar-file that matches appName (case-insensitive).
	 * A jar-file named "appName.jar" takes precedence over a jar-file named "appName-*.jar"
	 * @return A main class (never null)
	 * @throws RuntimeException when no main class can be found
	 */
	public static String findMainClass(List<File> appFiles, String appName) {
		
		List<File> mainJarFiles = new ArrayList<File>();
		String appNameLC = appName.toLowerCase();
		for (File f : appFiles) {
			if (f.isDirectory()) continue;
			String fname = f.getName().toLowerCase();
			if (fname.startsWith(appNameLC)) {
				if (mainJarFiles.isEmpty()) {
					mainJarFiles.add(f);
				} else {
					if (fname.equals(appNameLC + ".jar")) {
						mainJarFiles.add(0, f);
					} else if (fname.startsWith(appNameLC + "-")) {
						if (mainJarFiles.get(0).getName().equalsIgnoreCase(appName + ".jar")) {
							mainJarFiles.add(1, f);
						} else {
							mainJarFiles.add(0, f);
						}
					} else {
						mainJarFiles.add(f);
					}
				}
			}
		}
		if (mainJarFiles.isEmpty()) {
			throw new RuntimeException("Could not find a jar-file for application name " + appName);
		}
		if (debug) {
			if (mainJarFiles.size() == 1) {
				showln("Found jar-file " + mainJarFiles.get(0).getName() + " for application name " + appName);
			} else {
				StringBuilder sb = new StringBuilder("Found ").append(mainJarFiles.size()).append(" jar-files for application name ").append(appName);
				sb.append(": ");
				boolean first = true;
				for (File f : mainJarFiles) {
					if (first) {
						first = false;
					} else {
						sb.append(", ");
					}
					sb.append(f.getName());
				}
				sb.append(".");
				showln(sb.toString());
			}
		}
		String mainClass = null;
		File appbootJar = getJarFile(AppBoot.class);
		if (appbootJar == null && debug) {
			showln("Unable to determine the AppBoot jar-file itself.");
		}
		for (File f : mainJarFiles) {
			mainClass = getMainClass(f);
			if (mainClass == null) {
				if (debug) {
					showln("No main-class found in " + f.getName());
				}
				continue;
			}
			// check that we did not find ourselves
			if (f.equals(appbootJar)) {
				if (debug) {
					showln("Ignoring main-class found in AppBoot jar-file " + appbootJar);
				}
				mainClass = null;
				continue;
			}
			if (debug) {
				showln("Main class [" + mainClass + "] found in jar-file " + f.getName());
			}
			break;
		}
		if (mainClass == null) {
			throw new RuntimeException("Could not find a candidate main-class in manifest(s) of jar-file(s): " + mainJarFiles);
		} 
		return mainClass;
	}
	
	/**
	 * Reads the "Main-class" from "META-INF/MANIFEST.MF".
	 */
	public static String getMainClass(File jarFile) {
		
		String mainClass = null;
		InputStream in = null;
		try {
			in = ZipUtil.getInputStream(jarFile, "META-INF/MANIFEST.MF", null);
			Manifest manifest = new Manifest(in);
			Attributes attr = manifest.getMainAttributes();
			mainClass = attr.getValue("Main-Class");
		} catch (Exception ignored) {
			;
		}
		return mainClass;
	}

}
