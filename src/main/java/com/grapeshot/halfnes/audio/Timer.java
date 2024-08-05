/*
 * HalfNES by Andrew Hoffman
 * Licensed under the GNU GPL Version 3. See LICENSE file
 */
package com.grapeshot.halfnes.audio;

public abstract class Timer {
	protected int period;
	protected int position;


	protected Timer(int period, int position)
	{
		this.period = period;
		this.position = position;
	}

	public final int getPeriod()
	{
		return period;
	}

	public abstract void setPeriod(final int period);

	public abstract void setDuty(int duty);

	public abstract void setDuty(int[] duty);

	public abstract void reset();

	public abstract void clock();

	public abstract void clock(final int cycles);

	public abstract int getVal();
}
