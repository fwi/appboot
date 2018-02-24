package com.github.fwi.appboot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Various utility functions for zip-files used by classes in this package.
 * @author fwiers
 *
 */
public class ZipUtil {
	
	private ZipUtil() {}

	/**
	 * Searches for a zip-entry and returns it as an input-stream (backed by a byte-array). 
	 * @param entryNameStart null/empty or the first part of the name of the zip-entry that should be opened. 
	 * @param entryNameEnd null/empty or the last part of the name of the zip-entry that should be opened.
	 * @return An input-stream (that does not have to be closed, it is a {@link ByteArrayInputStream}.
	 */
	public static InputStream getInputStream(File zippedFile, String entryNameStart, String entryNameEnd) throws IOException {

		ZipFile zipFile = null;
		InputStream in = null;
		try {
			zipFile = new ZipFile(zippedFile);
			Enumeration<? extends ZipEntry> e = zipFile.entries();
			while (e.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				String fname = entry.getName();
				boolean foundEntry = BootUtil.isEmpty(entryNameStart) ? true : fname.startsWith(entryNameStart);
				if (foundEntry) {
					foundEntry = BootUtil.isEmpty(entryNameEnd) ? true : fname.endsWith(entryNameEnd);
				}
				if (foundEntry) {
					in = getInputStream(zipFile, entry);
					break;
				}
			}		
		} catch (Exception ignored) {
			//ignored.printStackTrace();
			;
		} finally {
			// JRE 6: zip file is NOT a closeable
			// BootUtil.close(zipFile);
			try {
				zipFile.close();
			} catch (Exception ignored) {
				;
			}
		}
		return in;
	}
	
	/**
	 * Returns the bytes form the zip-entry as input-stream.
	 */
	public static InputStream getInputStream(ZipFile zipFile, ZipEntry entry) throws IOException {

		return new ByteArrayInputStream(getBytes(zipFile, entry));
	}
	
	/**
	 * Returns the bytes form the zip-entry.
	 */
	public static byte[] getBytes(ZipFile zipFile, ZipEntry entry) throws IOException {
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		// InputStream is closed when zipFile is closed.
		InputStream in = zipFile.getInputStream(entry);
		byte[] buf = new byte[8192];
		int l = 0;
		while ((l = in.read(buf)) > 0) {
			bout.write(buf, 0, l);
		}
		return bout.toByteArray();
	}

}
