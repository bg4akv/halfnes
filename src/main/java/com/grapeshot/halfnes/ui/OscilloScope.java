/*
 * HalfNES by Andrew Hoffman
 * Licensed under the GNU GPL Version 3. See LICENSE file
 */
package com.grapeshot.halfnes.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.grapeshot.halfnes.audio.AudioOutInterface;

/**
 *
 * @author Andrew
 */
public class OscilloScope implements AudioOutInterface {
	private static final int width = 400, length = 640;
	private static final int scf = 65536 / width / 2;

	private DebugUI debugUI;
	private BufferedImage image;
	private Graphics2D graphics;
	private AudioOutInterface iface;
	private int[] buffer = new int[length];
	private int idx = 0;
	private int prevSample = 0;


	public OscilloScope(AudioOutInterface i)
	{
		this.iface = i;
		debugUI = new DebugUI(length, width);
		image = new BufferedImage(length, width, BufferedImage.TYPE_INT_ARGB_PRE);

		graphics = image.createGraphics();
		graphics.setBackground(Color.black);
		graphics.setColor(Color.green);

		debugUI.pack();
		debugUI.run();
	}

	@Override
	public void outputSample(int sample)
	{
		if (idx > 0 || (prevSample <= 0 && sample >= 0)) {
			//start cap @ zero crossing
			if (idx < buffer.length) {
				buffer[idx++] = sample;
			}
		}

		prevSample = sample;
		if (iface != null) {
			iface.outputSample(sample);
		}
	}

	@Override
	public void flushFrame(boolean waitIfBufferFull)
	{
		if (iface != null) {
			iface.flushFrame(waitIfBufferFull);
		}

		//Graphics2D graphics = image.createGraphics();
		graphics.clearRect(0, 0, length, width);
		for (int i = 1; i < idx; ++i) {
			graphics.drawLine(i - 1, (buffer[i - 1] / scf) + width / 2, i, (buffer[i] / scf) + width / 2);
		}
		graphics.drawLine(0, width / 2, length, width / 2);
		debugUI.setFrame(image);
		idx = 0;

	}

	@Override
	public void pause()
	{
		if (iface != null) {
			iface.pause();
		}
	}

	@Override
	public void resume()
	{
		if (iface != null) {
			iface.resume();
		}
	}

	@Override
	public void destroy()
	{

		debugUI.setVisible(false);
		debugUI.dispose();

		if (iface != null) {
			iface.destroy();
		}
	}

	@Override
	public boolean bufferHasLessThan(int samples)
	{
		if (iface != null) {
			return iface.bufferHasLessThan(samples);
		} else {
			return false;
		}
	}
}
