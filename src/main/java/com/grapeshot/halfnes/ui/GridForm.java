package com.grapeshot.halfnes.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

import com.grapeshot.halfnes.ui.layout.ContainerEF;
import com.grapeshot.halfnes.ui.layout.ExplicitConstraints;
import com.grapeshot.halfnes.ui.layout.ExplicitLayout;
import com.grapeshot.halfnes.ui.layout.MathEF;


public class GridForm extends JDialog {
	protected static final Toolkit toolkitDefault = Toolkit.getDefaultToolkit();
	protected static final Dimension screenPixel = toolkitDefault.getScreenSize();
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
	}

	protected List<CompInfo> compInfoList = new ArrayList<CompInfo>();

	protected int xPixel;
	protected int yPixel;
	protected int wPixel;
	protected int hPixel;

	protected int wGrid;
	protected int hGrid;


	public GridForm()
	{
		this(-1, -1, -1, -1, -1, -1);
	}

	public GridForm(int wGrid, int hGrid)
	{
		this(-1, -1, -1, -1, wGrid, hGrid);
	}

	public GridForm(int xPixel, int yPixel, int wPixel, int hPixel)
	{
		this(xPixel, yPixel, wPixel, hPixel, -1, -1);
	}

	public GridForm(int xPixel, int yPixel, int wPixel, int hPixel, int wGrid, int hGrid)
	{
		if (wPixel >= 1
			&& wPixel <= screenPixel.width) {
			this.wPixel = wPixel;
		} else {
			this.wPixel = screenPixel.width;
		}
		if (hPixel >= 1
			&& hPixel <= screenPixel.height) {
			this.hPixel = hPixel;
		} else {
			this.hPixel = screenPixel.height;
		}

		if (xPixel >= 0) {
			this.xPixel = xPixel;
		} else {
			this.xPixel = (screenPixel.width - this.wPixel) / 2;
		}
		if (yPixel >= 0) {
			this.yPixel = yPixel;
		} else {
			this.yPixel = (screenPixel.height - this.hPixel) / 2;
		}

		if (wGrid >= 1) {
			this.wGrid = wGrid;
		} else {
			this.wGrid = this.wPixel / (800 / 14);
		}
		if (hGrid >= 1) {
			this.hGrid = hGrid;
		} else {
			this.hGrid = this.hPixel / (600 / 10);
		}

		setModal(true);
		//setUndecorated(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setBounds(this.xPixel, this.yPixel, this.wPixel, this.hPixel);

		getContentPane().setLayout(new ExplicitLayout());
		getContentPane().setBackground(getBackground());
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

	//do not use this.removeAll();
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

	public void close()
	{
		dispose();
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

	public int getXPixel()
	{
		return xPixel;
	}

	public int getYPixel()
	{
		return yPixel;
	}

	public int getWPixel()
	{
		return wPixel;
	}

	public int getHPixel()
	{
		return hPixel;
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

	protected void placeWithGap(Container container,
					int columnCount, int rowCount,
					Component comp,
					double gridx, double gridy,
					double width, double height,
					double gapx, double gapy)
	{
		ExplicitConstraints ec = new ExplicitConstraints(comp);

		ec.setX(MathEF.add(ContainerEF.left(container), ContainerEF.widthFraction(container, gridx / columnCount)).add(gapx));
		ec.setY(MathEF.add(ContainerEF.top(container), ContainerEF.heightFraction(container, gridy / rowCount)).add(gapy));
		if (width > 0) {
			ec.setWidth(ContainerEF.widthFraction(container, width * 1.0 / columnCount).subtract(gapx * 2));
		}
		if (height > 0) {
			ec.setHeight(ContainerEF.heightFraction(container, height * 1.0 / rowCount).subtract(gapy * 2));
		}

		container.add(comp, ec);
	}
}
