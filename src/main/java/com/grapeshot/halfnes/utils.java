/*
 * HalfNES by Andrew Hoffman
 * Licensed under the GNU GPL Version 3. See LICENSE file
 */
package com.grapeshot.halfnes;

import java.util.Locale;


public class utils {
	public static final int BIT0 = 1;
	public static final int BIT1 = 1 << 1;
	public static final int BIT2 = 1 << 2;
	public static final int BIT3 = 1 << 3;
	public static final int BIT4 = 1 << 4;
	public static final int BIT5 = 1 << 5;
	public static final int BIT6 = 1 << 6;
	public static final int BIT7 = 1 << 7;
	public static final int BIT8 = 1 << 8;
	public static final int BIT9 = 1 << 9;
	public static final int BIT10 = 1 << 10;
	public static final int BIT11 = 1 << 11;
	public static final int BIT12 = 1 << 12;
	public static final int BIT13 = 1 << 13;
	public static final int BIT14 = 1 << 14;
	public static final int BIT15 = 1 << 15;


	private utils()
	{

	}

	public static boolean isBitSet(int data, int bit)
	{
		return (data & (1 << bit)) != 0;
	}

	public static int setbit(final int num, final int bitnum, final boolean state)
	{
		return (state) ? (num | (1 << bitnum)) : (num & ~(1 << bitnum));
	}

	public static String hex(final int num)
	{
		String s = Integer.toHexString(num).toUpperCase(Locale.US);
		if ((s.length() & 1) == 1) {
			s = "0" + s;
		}
		return s;
	}

	public static String hex(final long num)
	{
		String s = Long.toHexString(num).toUpperCase(Locale.US);
		if ((s.length() & 1) == 1) {
			s = "0" + s;
		}
		return s;
	}

	public static int reverseByte(int nibble)
	{
		//reverses 8 bits packed into int.
		return (Integer.reverse(nibble) >> 24) & 0xff;
	}

	public static void printarray(final int[] a)
	{
		StringBuilder s = new StringBuilder();
		for (int i : a) {
			s.append(i);
			s.append(", ");
		}
		if (s.length() >= 1) {
			s.deleteCharAt(s.length() - 1);
		}
		s.append("\n");
		System.err.print(s.toString());
	}

	public static void printarray(final boolean[] a)
	{
		StringBuilder s = new StringBuilder();
		for (boolean i : a) {
			s.append(i);
			s.append(", ");
		}
		if (s.length() >= 1) {
			s.deleteCharAt(s.length() - 1);
		}
		s.append("\n");
		System.err.print(s.toString());
	}

	public static void printarray(final double[] a)
	{
		StringBuilder s = new StringBuilder();
		for (double i : a) {
			s.append(i);
			s.append(", ");
		}
		if (s.length() >= 1) {
			s.deleteCharAt(s.length() - 1);
		}
		s.append("\n");
		System.err.print(s.toString());
	}

	public static void printarray(final float[] a)
	{
		StringBuilder s = new StringBuilder();
		for (float i : a) {
			s.append(i);
			s.append(", ");
		}
		if (s.length() >= 1) {
			s.deleteCharAt(s.length() - 1);
		}
		s.append("\n");
		System.err.print(s.toString());
	}

	public static void printarray(final Object[] a)
	{
		StringBuilder s = new StringBuilder();
		for (Object i : a) {
			s.append(i.toString());
			s.append(", ");
		}
		if (s.length() >= 1) {
			s.deleteCharAt(s.length() - 1);
		}
		s.append("\n");
		System.err.print(s.toString());
	}

	public static int max(final int[] array)
	{
		int m = array[0];
		for (Integer i : array) {
			if (i > m) {
				m = i;
			}
		}
		return m;
	}
}
