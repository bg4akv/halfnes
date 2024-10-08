/*
 * HalfNES by Andrew Hoffman
 * Licensed under the GNU GPL Version 3. See LICENSE file
 */
package com.grapeshot.halfnes;

import java.awt.EventQueue;
import java.io.*;


/**
 *
 * @author Andrew
 */
public class FileUtils {

	private FileUtils() {
	}

	public static String getExtension(final File f)
	{
		return getExtension(f.getName());
	}

	public static String getExtension(final String s)
	{
		if (s == null || s.equals("")) {
			return "";
		}
		int split = s.lastIndexOf('.');
		if (split < 0) {
			return "";
		}
		return s.substring(split);

	}

	public static String stripExtension(final File f)
	{
		String s = f.getName();
		if (s == null || s.equals("")) {
			return "";
		}
		int split = s.lastIndexOf('.');
		if (split < 0) {
			return "";
		}
		return s.substring(0, split);
	}

	public static String stripExtension(final String s)
	{
		if (s == null || s.equals("")) {
			return "";
		}
		int split = s.lastIndexOf('.');
		if (split < 0) {
			return "";
		}
		return s.substring(0, split);
	}

	public static void writetofile(final int[] array, final String path)
	{
		//note: does NOT write the ints directly to the file - only the low bytes.
		AsyncWriter writer = new AsyncWriter(array, path);
		writer.run();
	}

	public static void asyncwritetofile(final int[] array, final String path)
	{
		//now does the file writing in the dispatch thread
		//hopefully that will eliminate annoying hitches when file system's slow
		//and not do pathological stuff like threads are prone to
		AsyncWriter writer = new AsyncWriter(array, path);
		EventQueue.invokeLater(writer);
	}

	private static class AsyncWriter implements Runnable {

		private final int[] a;
		private final String path;

		public AsyncWriter(final int[] a, final String path) {
			this.a = a;
			this.path = path;
		}

		@Override
		public void run()
		{
			if (a != null && path != null) {
				try {
					FileOutputStream b = new FileOutputStream(path);
					byte[] buf = new byte[a.length];
					for (int i = 0; i < a.length; ++i) {
						buf[i] = (byte) (a[i] & 0xff);
					}
					b.write(buf);
					b.flush();
					b.close();
				} catch (IOException e) {
					System.err.print("Could not save. ");
					System.err.println(e);
				}
			}
		}
	}

	public static String getFilenamefromPath(String path)
	{
		return new File(path).getName();
	}

	public static int[] readFromFile(final String path)
	{
		File file = new File(path);
		if (!file.exists()
				|| !file.canRead()
				|| file.isDirectory()) {
			return null;
		}

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		byte[] bytes = new byte[(int) file.length()];
		try {
			fis.read(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			fis.close();
		} catch (Exception e) {
			;
		}

		int[] ints = new int[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			ints[i] = (short) (bytes[i] & 0xFF);
		}

		return ints;
	}

	public static boolean exists(final String path)
	{
		File f = new File(path);
		return f.canRead() && !f.isDirectory();
	}
}
