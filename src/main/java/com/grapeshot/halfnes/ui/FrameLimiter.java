/*
 * HalfNES by Andrew Hoffman
 * Licensed under the GNU GPL Version 3. See LICENSE file
 */
package com.grapeshot.halfnes.ui;

import com.grapeshot.halfnes.NES;
import com.grapeshot.halfnes.PrefsSingleton;

/**
 *
 * @author Andrew
 */
public class FrameLimiter {
	private final NES nes;
	private long sleepingTest = 0;
	private long frameInterval; //nanoseconds


	public FrameLimiter(NES nes, long frameInterval)
	{
		this.nes = nes;
		this.frameInterval = frameInterval;
	}

	public void setInterval(long ns)
	{
		frameInterval = ns;
	}

	public void sleep()
	{
		//Frame Limiter
		if (!PrefsSingleton.getInstance().getBoolean("Sleep", true)) {
			return; //skip frame limiter if pref set
		}

		final long timeLeft = System.nanoTime() - nes.frameStartTime;
		if (timeLeft >= frameInterval) {
			return;
		}

		final long sleepyTime = (frameInterval - timeLeft + sleepingTest);
		if (sleepyTime < 0) {
			return;
			//don't sleep at all.
		}

		sleepingTest = System.nanoTime();
		try {
			//Thread.sleep(sleepyTime / 1000000);
			Thread.sleep(0, (int) sleepyTime);
			// sleep for rest of the time until the next frame
		} catch (InterruptedException e) {
			;
		}

		sleepingTest = System.nanoTime() - sleepingTest;
		//now sleeping test has how many ns the sleep *actually* was
		sleepingTest = sleepyTime - sleepingTest;
		//now sleepingtest has how much the next frame needs to be delayed by to make things match
	}


	public void sleepFixed()
	{
		try {
			//sleep for 16 ms
			Thread.sleep(16);
		} catch (InterruptedException e) {
			;
		}
	}

	public static void forceHighResolutionTimer() {
		//UGLY HACK ALERT: Just realized why sleep() rounds to nearest
		//multiple of 10: it's because no other program is using high resolution timer.
		//This should, hopefully, fix that.
		final Thread daemon = new Thread("ForceHighResolutionTimer") {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(99999999999L);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		};
		daemon.setDaemon(true);
		daemon.start();
	}

}
