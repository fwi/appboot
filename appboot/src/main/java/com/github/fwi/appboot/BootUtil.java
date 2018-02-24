package com.github.fwi.appboot;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Various utility functions used by classes in this package.
 * @author fwiers
 *
 */
public class BootUtil {

	/** New line character(s) from system-property line.separator. */
	public static final String CR = System.lineSeparator();
	
	/** Default value ("unknown") for a version number that can not be found with {@link #getPomVersion(Class)}. */
	public static String UNKNOWN_VERSION = "unknown";
	
	/** 
	 * Maximum depth (default 10) for finding the root-cause of an exception.
	 * Some libraries manage to create a loop in the exception cause-chain,
	 * this maximum will break the loop. 
	 */
	public static int MAX_CAUSE_COUNT = 10;
	
	public static String EMPTY_STRING = "";
	
	private BootUtil() {}

	/**
	 * Rethrows the root-cause of te given exception as a runtime-exception
	 * using the stack-trace and message of the root-cause.
	 * <br>An instance of {@link Error} is always re-thrown directly.
	 * <br>Does nothing if given throwable is null.
	 */
	public static void throwRootCause(Throwable e) {
		
		if (e == null) {
			return;
		}
		if (e instanceof Error) {
			throw (Error) e;
		}
		Throwable cause = e;
		int causeCount = 0;
		while (cause.getCause() != null && cause.getCause() != cause && causeCount < MAX_CAUSE_COUNT) {
			cause = cause.getCause();
			causeCount++;
		}
		if (cause instanceof RuntimeException) {
			throw (RuntimeException) cause;
		}
		String msg = cause.getClass().getSimpleName() + 
				(cause.getMessage() == null ? "" : " - " + cause.getMessage());
		RuntimeException re = new RuntimeException(msg);
		re.setStackTrace(cause.getStackTrace());
		throw re;
	}

	/**
	 * Returns true if the prop exists as parameter in args 
	 * (e.g. my.switch) or as system (environment) property (e.g. -Dmy.switch or MY_SWITCH).
	 * <br>Returns false if the prop does not exist or is set to value "false" (case insensitive),
	 */
	public static boolean getPropBool(final String prop, final String... args) {
		return (Boolean.TRUE.toString().equals(getProp(prop, true, args)));
	}

	/**
	 * Returns the (empty) value of the prop if it exists as parameter in args 
	 * (e.g. my.prop="a value") or as system-property (e.g. -Dmy.prop="a value").
	 * The value if stripped of double-quotes. 
	 * <br>Returns null if the prop does not exist as parameter in args or as system-property.
	 */
	public static String getProp(final String prop, final String... args) {
		return getProp(prop, false, args);
	}
	
	private static String getProp(final String prop, final boolean boolType, final String... args) {
		
		if (!isEmpty(args)) {
			final String argProp = prop + "=";
			for (String s : args) {
				if (isEmpty(s)) {
					continue;
				}
				s = s.trim();
				if (s.startsWith("-") && s.length() > 1) {
					s = s.substring(1);
				}
				if (s.equals(prop)) {
					return getPropValue(boolType, EMPTY_STRING);
				}
				if (s.length() > argProp.length() && s.startsWith(argProp)) {
					s = s.substring(argProp.length());
					return getPropValue(boolType, s);
				}
			}
		}
		if (System.getProperties().containsKey(prop)) {
			return getPropValue(boolType, System.getProperty(prop));
		}
		if (!System.getenv().isEmpty()) {
			String sysProp = prop.toUpperCase().replace('.', '_');
			if (System.getenv().containsKey(sysProp)) {
				return getPropValue(boolType, System.getenv(sysProp));
			}
		}
		return (boolType ? Boolean.FALSE.toString() : null);
	}
	
	private static String getPropValue(final boolean boolType, String value) {
		
		String s = (value == null ? EMPTY_STRING : value);
		s = s.trim();
		s = stripDoubleQuotes(s);
		return (boolType ? toBoolString(s) : s);
	}
	
	private static String toBoolString(String value) {
			return (Boolean.FALSE.toString().equalsIgnoreCase(value) ? Boolean.FALSE.toString() : Boolean.TRUE.toString());
	}

	/**
	 * Removes surrounding double quotes from a string value.
	 */
	public static String stripDoubleQuotes(String value) {
		return (value.length() > 1 && value.startsWith("\"") && value.endsWith("\"") ? value.substring(1, value.length()-1) : value);
	}
	
	/**
	 * Reads the "version" in "META-INF/.../pom.properties" in the jar-file of the given class.
	 * @return the found version or {@link #UNKNOWN_VERSION}.
	 */
	public static String getPomVersion(Class<?> clazz) {
		
		String version = UNKNOWN_VERSION;
		InputStream in = null;
		try {
			in = ZipUtil.getInputStream(getJarFile(clazz), "META-INF", "pom.properties");
			version = getProps(in).getProperty("version", UNKNOWN_VERSION);
		} catch (Exception ignored) {
			;
		}
		return version;
	}

	/**
	 * Returns null or the jar-file containing the given class.
	 */
	public static File getJarFile(Class<?> clazz) {
		
		URL jarUrl = (clazz.getProtectionDomain().getCodeSource() == null ? null :
			clazz.getProtectionDomain().getCodeSource().getLocation());
		File jarFile = null;
		if (jarUrl != null) {
			jarFile = getFile(jarUrl);
			if (!jarFile.isFile()) {
				jarFile = null;
			}
		}
		return jarFile;
	}
	
	/**
	 * Loads properties from input-stream using UTF-8. All errors are suppressed.
	 * @param in closed after usage.
	 * @return (empty) properties.
	 */
	public static Properties getProps(InputStream in) {
		
		Properties p = new Properties();
		if (in != null) {
			try { 
				p.load(new InputStreamReader(in, "UTF-8"));
			} catch (Exception ignored) {
				;
			} finally {
				close(in);
			}
		}
		return p;
	}

	/**
	 * Converts the url to a file.
	 */
	public static File getFile(final URL url) {
		
		if (url == null) {
			return null;
		}
		File f = null;
		try {
			f = new File(url.toURI());
		} catch(Exception e) {
			f = new File(url.getPath());
		}
		return f;
	}
	
	/**
	 * Converts the file to a url.
	 */
	public static URL getUrl(File f) {
		
		URL u = null;
		try {
			u = f.toURI().toURL();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return u;
	}
	
	/** 
	 * Returns the canonical or absolute path of the file. 
	 */
	public static String getFullPath(final File f) {
		
		String fullPath = null;
		try {
			fullPath = f.getCanonicalPath();
		} catch (Exception e) {
			fullPath = f.getAbsolutePath();
		}
		return fullPath;
	}

	/**
	 * @param s A path
	 * @return The path ending with a file separator (/ or \).
	 */
	public static String endWithSep(final String s) {
		return (s.endsWith("\\") || s.endsWith("/") ? s : s + File.separator);
	}

	/**
	 * Closes c, errors are ignored.
	 */
	public static void close(final Closeable c) {
		
		try { c.close(); } catch (Exception ignored) {
			;
		}
	}
	
	/**
	 * Returns a new set with all values prefixed with the prefix.
	 */
	public static Set<String> prefixValues(Collection<String> values, String prefix) {
		
		return concatValues(values, prefix, true);
	}
	
	/**
	 * Returns a new set with all values postfixed with the postfix.
	 */
	public static Set<String> postfixValues(Collection<String> values, String postfix) {
		
		return concatValues(values, postfix, false);
	}
	
	/**
	 * Returns a new set with all values postfixed or prefixes with the value to append.
	 */
	public static Set<String> concatValues(Collection<String> values, String toAppend, boolean prefix) {
		
		Set<String> sp = new HashSet<String>();
		for (String s : values) {
			if (prefix) {
				sp.add(toAppend + s);
			} else {
				sp.add(s + toAppend);
			}
		}
		return sp;
	}
	
	/**
	 * @return true if v is not empty and starts with a value in values.
	 */
	public static boolean valueStartsWith(Set<String> values, String v) {
		
		if (isEmpty(v)) return false;
		for (String s : values) {
			if (v.startsWith(s)) return true;
		}
		return false;
	}

	/**
	 * Returns true if s is null, length 0 or lenght is one and first element is empty. 
	 */
	public static boolean isEmpty(final String... s) {
		return (s == null || s.length == 0 || (s.length == 1 && isEmpty(s[0])));
	}

	/**
	 * Returns true if s is null or empty after trimming.
	 */
	public static boolean isEmpty(final String s) {
		return (s == null || s.trim().isEmpty());
	}

	/** Writes to system.out. */
	public static void show(final String s) { System.out.print(s); }
	/** Writes to system.out. */
	public static void showln() { System.out.println(); }
	/** Writes to system.out. */
	public static void showln(final String s) { System.out.println(s); }
	/** Writes to system.err with prefix "ERROR: ". */
	public static void showerrln(final String s) { System.err.println("ERROR: " + s); }
	/** Writes to system.err "ERROR: error-description" and also prints the stack-trace.. */
	public static void showerr(final Throwable t) { System.err.println("ERROR: " + t); t.printStackTrace(); }

}
