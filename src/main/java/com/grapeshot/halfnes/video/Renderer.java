/*
 * HalfNES by Andrew Hoffman
 * Licensed under the GNU GPL Version 3. See LICENSE file
 */
package com.grapeshot.halfnes.video;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;

import com.grapeshot.halfnes.ppu.PPU;

/**
 *
 * @author Andrew
 */
public abstract class Renderer {
	private final int frameWidth;
	/*
	there's stuff involving this variable that's much uglier
	than it needs to be because of me not really remembering
	how abstract classes work
	 */
	private final BufferedImage[] images = {null, null, null, null};
	private int imgIdx = 0;
	private int clip;
	private int height;


	public Renderer(int frameWidth)
	{
		this.frameWidth = frameWidth;

		setClip(8);
		for (int i = 0; i < images.length; ++i) {
			images[i] = new BufferedImage(frameWidth, height, BufferedImage.TYPE_INT_ARGB_PRE);
		}
	}

	public void setClip(int clip)
	{
		//how many lines to clip from top + bottom
		this.clip = clip;
		height = PPU.HEIGHT - 2 * clip;
	}

	protected BufferedImage getBufferedImage(int[] frame)
	{
		final BufferedImage image = images[++imgIdx % images.length];
		final WritableRaster raster = image.getRaster();
		final int[] pixels = ((DataBufferInt) raster.getDataBuffer()).getData();


		System.arraycopy(frame, frameWidth * clip, pixels, 0, frameWidth * height);

		return image;
	}

	public abstract BufferedImage render(int[] nespixels, int[] bgcolors, boolean dotcrawl);
}
