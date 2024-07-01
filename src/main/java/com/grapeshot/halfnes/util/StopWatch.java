package com.grapeshot.halfnes.util;

import java.util.concurrent.TimeUnit;


public class StopWatch {
	private boolean running = false;
	private long startTime;
	private long stopTime;

	public void start()
	{
		if (!running) {
			startTime = System.nanoTime();
			running = true;
		}
	}

	public void stop()
	{
		if (running) {
			stopTime = System.nanoTime();
			running = false;
		}
	}

	public void restart()
	{
		stop();
		start();
	}

	public boolean isRunning()
	{
		return running;
	}

	public long getTime(TimeUnit timeUnit)
	{
		return timeUnit.convert(getTime(), TimeUnit.NANOSECONDS);
	}

	public long getTime()
	{
		return (running ? System.nanoTime() : stopTime) - startTime;
	}
}
