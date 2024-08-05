package com.grapeshot.halfnes.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.grapeshot.halfnes.ui.layout.ContainerEF;
import com.grapeshot.halfnes.ui.layout.ExplicitConstraints;
import com.grapeshot.halfnes.ui.layout.ExplicitLayout;
import com.grapeshot.halfnes.ui.layout.Expression;
import com.grapeshot.halfnes.ui.layout.MathEF;



public class GridPanel extends JPanel {
	private int imageType = BufferedImage.TYPE_4BYTE_ABGR_PRE;
	private static final int DELAY_FACTOR = 10;
	private int defaultXGap = 1;
	private int defaultYGap = 1;

	private class CompInfo {
		public Component comp;
		public int xGrid;
		public int yGrid;
		public int wGrid;
		public int hGrid;
		public int xGap;
		public int yGap;

		public CompInfo(Component comp, int xGrid, int yGrid, int wGrid, int hGrid, int xGap, int yGap)
		{
			this.comp  = comp;
			this.xGrid = xGrid;
			this.yGrid = yGrid;
			this.wGrid = wGrid;
			this.hGrid = hGrid;
			this.xGap = xGap;
			this.yGap = yGap;
		}
	};

	protected final List<CompInfo> compInfoList = new ArrayList<CompInfo>();


	protected int wGrid;
	protected int hGrid;

	private boolean isOpaque;
	private boolean reverseDisp = false;

	private boolean rcorner = false;


	public GridPanel(int wGrid, int hGrid, boolean rcorner)
	{
		if (wGrid >= 1) {
			this.wGrid = wGrid;
		} else {
			this.wGrid = 1;
		}
		if (hGrid >= 1) {
			this.hGrid = hGrid;
		} else {
			this.hGrid = 1;
		}

		setOpaque(true);
		setLayout(new ExplicitLayout());


	}

	protected CompInfo getCompInfo(Component comp)
	{
		if (compInfoList == null || compInfoList.isEmpty()) {
			return null;
		}
		if (comp == null) {
			return null;
		}

		for (CompInfo compInfo : compInfoList) {
			if (compInfo == null) {
				continue;
			}

			if (compInfo.comp == comp) {
				return compInfo;
			}
		}

		return null;
	}

	public void setDefaultGap(int gap)
	{
		setDefaultGap(gap, gap);
	}

	public void setDefaultGap(int xGap, int yGap)
	{
		setDefaultXGap(xGap);
		setDefaultYGap(yGap);
	}

	public void setDefaultXGap(int gap)
	{
		defaultXGap = (gap > 0)? gap : 0;
	}

	public void setDefaultYGap(int gap)
	{
		defaultYGap = (gap > 0)? gap : 0;
	}

	public void addComp(Component comp, int compXGrid, int compYGrid)
	{
		addComp(comp, compXGrid, compYGrid, 1, 1);
	}

	public void addComp(Component comp, int compXGrid, int compYGrid, int compWGrid, int compHGrid)
	{
		addComp(comp, compXGrid, compYGrid, compWGrid, compHGrid, defaultXGap, defaultYGap);
 	}

	public void addComp(Component comp, int compXGrid, int compYGrid,
				int compWGrid, int compHGrid, int compXGap, int compYGap)
	{
		if (compInfoList == null) {
			return;
		}

		rmvComp(comp);
		placeWithGap(this, wGrid, hGrid, comp,
				compXGrid, compYGrid, compWGrid, compHGrid, compXGap, compYGap);
		compInfoList.add(
			new CompInfo(comp, compXGrid, compYGrid, compWGrid, compHGrid, compXGap, compYGap));
 	}

	public void rmvComp(Component comp)
	{
		CompInfo compInfo = getCompInfo(comp);
		if (compInfo == null) {
			return;
		}

		remove(comp);
		compInfoList.remove(compInfo);
	}

	public void clearComp()
	{
		if (compInfoList == null || compInfoList.isEmpty()) {
			return;
		}

		for (CompInfo compInfo : compInfoList) {
			if (compInfo == null
				|| compInfo.comp == null) {
				continue;
			}
			remove(compInfo.comp);
		}

		compInfoList.clear();
	}

	public int getWGrid()
	{
		return wGrid;
	}

	public int getHGrid()
	{
		return hGrid;
	}

	public void setWGrid(int wGrid)
	{
		this.wGrid = wGrid;
		updateCompPos();
	}

	public void setHGrid(int hGrid)
	{
		this.hGrid = hGrid;
		updateCompPos();
	}

	private void updateCompPos()
	{
		if (compInfoList == null || compInfoList.isEmpty()) {
			return;
		}

		List<CompInfo> compInfoList = new ArrayList<CompInfo>(this.compInfoList);
		for (CompInfo compInfo : compInfoList) {
			if (compInfo != null && compInfo.comp != null) {
				addComp(compInfo.comp, compInfo.xGrid, compInfo.yGrid, compInfo.wGrid, compInfo.hGrid, compInfo.xGap , compInfo.yGap);
			}
		}
	}

	protected void placeWithGap(Container container, int containerWGrid, int containerHGrid,
			Component comp, double compXGrid, double compYGrid,
			double compWGrid, double compHGrid, double gapXPixel, double gapYPixel)
{
		ExplicitConstraints ec = new ExplicitConstraints(comp);

		Expression xExp = ContainerEF.widthFraction(container, compXGrid / containerWGrid);
		Expression yExp = ContainerEF.heightFraction(container, compYGrid / containerHGrid);
		Expression wExp = ContainerEF.widthFraction(container, compWGrid / containerWGrid);
		Expression hExp = ContainerEF.heightFraction(container, compHGrid / containerHGrid);

		ec.setX(MathEF.add(ContainerEF.left(container), xExp).add(gapXPixel));
		ec.setY(MathEF.add(ContainerEF.top(container), yExp).add(gapYPixel));

		if (compWGrid > 0) {
			ec.setWidth(wExp.subtract(gapXPixel * 2));
		}
		if (compHGrid > 0) {
			ec.setHeight(hExp.subtract(gapYPixel * 2));
		}

		container.add(comp, ec);
	}

	@Override
	public void paint(Graphics g)
	{
		if (reverseDisp) {
			((Graphics2D) g).setTransform(new AffineTransform(new double[]{-1, 0, 0, 1, getSize().width + getX(), getY()}));
		}
		super.paint(g);
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		Dimension size = getSize();
		Insets insets = getInsets();
		int w = size.width - insets.left - insets.right;
		int h = size.height - insets.top - insets.bottom;

		BufferedImage bufferImage = new BufferedImage(w, h, imageType);
		Graphics2D g2d = (Graphics2D) bufferImage.getGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

		g2d.setColor(getBackground());
		if (isOpaque) {
			if (rcorner) {
				g2d.fillRoundRect(0, 0, w, h, 10, 10);
			} else {
				g2d.fillRect(0, 0, w, h);
			}
		}

		g.drawImage(bufferImage, insets.left, insets.top, w, h, this);
		g2d.dispose();
	}

	@Override
	public void setOpaque(boolean isOpaque)
	{
		super.setOpaque(isOpaque);
		this.isOpaque = isOpaque;
	}

	public void setReverseDisplay(boolean reverseDisplay)
	{
		reverseDisp = reverseDisplay;
	}

	public void setBgImageType(int imageType)
	{
		this.imageType = imageType;
	}

	public void setRoundCorner(boolean rcorner)
	{
		this.rcorner = rcorner;
	}

}
