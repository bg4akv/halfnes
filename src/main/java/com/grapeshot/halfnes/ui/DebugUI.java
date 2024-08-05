/*
 * HalfNES by Andrew Hoffman
 * Licensed under the GNU GPL Version 3. See LICENSE file
 */
package com.grapeshot.halfnes.ui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.grapeshot.halfnes.NES;

public class DebugUI extends JFrame {
	private final FramePanel framePanel;
	private final int width, height;


	public DebugUI(int width, int height)
	{
		setTitle("HalfNES  Debug " + NES.VERSION);
		setResizable(false);
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		this.width = width;
		this.height = height;
		framePanel = new FramePanel(width, height);
		framePanel.setIgnoreRepaint(true);
		setContentPane(framePanel);
		pack();
	}

	public void run()
	{
		setVisible(true);
	}

	public void setFrame(BufferedImage frameImage)
	{
		framePanel.setImage(frameImage);

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				framePanel.repaint();
			}
		});
	}

	public class FramePanel extends JPanel {
		private final int width, height;
		public BufferedImage frameImage;

		public FramePanel(int width, int height)
		{
			this.width = width;
			this.height = height;
			setBounds(0, 0, width, height);
			setPreferredSize(new Dimension(width, height));
		}

		public void setImage(BufferedImage image)
		{
			frameImage = image;
		}

		@Override
		public void paint(final Graphics g)
		{
			g.drawImage(frameImage, 0, 0, width, height, null);
		}
	}
}
