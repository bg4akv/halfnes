package com.grapeshot.halfnes.util;


public class Timer {
	private final ThreadLoop loop;
	private final Runnable runnable;
	private int delay;
	private long stopTime;

	public Timer(int delay)
	{
		this(delay, null);
	}

	public Timer(int delay, Runnable runnable)
	{
		this.delay = delay;
		this.runnable = runnable;
		loop = new ThreadLoop(new Runnable() {
			@Override
			public void run()
			{
				if (isRunning()) {
					return;
				}

				if (Timer.this.runnable != null) {
					new Thread(new Runnable() {
						@Override
						public void run()
						{
							try {
								Timer.this.runnable.run();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).start();
				}

				stop();
			}
		}, 0);
	}

	public void setDelay(int delay)
	{
		stop();
		this.delay = delay;
	}

	public void start()
	{
		if (delay <= 0 || isRunning()) {
			return;
		}

		stopTime = System.currentTimeMillis() + delay;
		if (runnable != null) {
			loop.start();
		}
	}

	public void stop()
	{
		stopTime = 0;
		if (loop.isRunning()) {
			loop.stop();
		}
	}

	public void restart()
	{
		stop();
		start();
	}

	public boolean isRunning()
	{
		return System.currentTimeMillis() < stopTime;
	}
}
