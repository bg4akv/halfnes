package com.grapeshot.halfnes.util;

public class ThreadLoop {
	private final Runnable runnable;
	private final int interval;
	private Thread thread = null;
	private volatile boolean terminated = true;

	public ThreadLoop(Runnable runnable, int interval)
	{
		this.runnable = runnable;
		this.interval = interval;
	}

	public synchronized void start()
	{
		if (runnable == null || thread != null) {
			return;
		}

		thread = new Thread(() -> {
			final long currentThreadId = Thread.currentThread().getId();
			while (thread != null
					&& currentThreadId == thread.getId()) {
				try {
					runnable.run();
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					Thread.sleep(interval);
				} catch (Exception e) {
					;
				}
			}
			terminated = true;
		});
		terminated = false;
		thread.start();
	}

	public synchronized void stop()
	{
		wakeup();
		thread = null;
		long stopTime = System.currentTimeMillis() + 1000;
		while (true) {
			if (terminated
					|| (System.currentTimeMillis() >= stopTime)) {
				break;
			}
		}
	}

	public synchronized boolean isRunning()
	{
		return thread != null;
	}

	public void wakeup()
	{
		if (thread != null) {
			thread.interrupt();
		}
	}
}
