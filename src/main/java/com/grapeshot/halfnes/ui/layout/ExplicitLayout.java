
package com.grapeshot.halfnes.ui.layout;

import java.awt.*;
import java.util.*;
import java.io.*;


public class ExplicitLayout implements LayoutManager2, Serializable {
	private static final Dimension MINIMUM_SIZE = new Dimension(0,0);
	private static final Dimension MAXIMUM_SIZE = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	private static final Dimension PREFERRED_SIZE = new Dimension(100,100);
	private transient Container container;
	private Hashtable<Component, Object> component2constraints;
	private Hashtable<String, Object> name2constraints;
	private DimensionExpression maximumSize;
	private DimensionExpression preferredSize;
	private DimensionExpression minimumSize;


	public ExplicitLayout()
	{
		component2constraints = new Hashtable<Component, Object>();
		name2constraints = new Hashtable<String, Object>();
	}

	public Container getContainer()
	{
		return container;
	}

	public ExplicitConstraints getConstraints(Component component)
	{
		return (ExplicitConstraints)component2constraints.get(component);
	}

	public ExplicitConstraints getConstraints(String name)
	{
		return (ExplicitConstraints)name2constraints.get(name);
	}

	public Enumeration<Object> getNamedConstraints()
	{
		return name2constraints.elements();
	}

	static class DimensionExpression {
		private Expression width;
		private Expression height;
		private Dimension dimension;
		private boolean valid = false;

		public DimensionExpression(Expression width, Expression height)
		{
			if (width == null || height == null) {
				throw new IllegalArgumentException("Both width and height must be specified");
			}
			this.width = width;
			this.height = height;
			dimension = new Dimension();
		}


		public Dimension getDimension(ExplicitLayout layout)
		{
			if (!valid) {
				Insets insets = layout.getContainer().getInsets();
				dimension.width = (int)width.getValue(layout) + insets.left + insets.right;
				dimension.height = (int)height.getValue(layout) + insets.top + insets.bottom;
				valid = true;
			}
			return dimension;
		}

		public void invalidate()
		{
			width.invalidate();
			height.invalidate();
			valid = false;
		}
	}

	@Override
	public void addLayoutComponent(String name, Component comp)
	{
		throw new IllegalArgumentException("No constraints specified");
	}



	@Override
	public void addLayoutComponent(Component component, Object constraints)
	{
		String name;
		if (constraints == null) {
			constraints = new AbsoluteConstraints(component);
		} else {
			if (!(constraints instanceof ExplicitConstraints)) {
				throw new IllegalArgumentException("constraints must be an instance of ExplicitConstraints, not '" + constraints + "'");
			}
			if (((ExplicitConstraints)constraints).getComponent() != component) {
				throw new IllegalArgumentException("component does not match component specifed in the constraints");
			}
		}
		component2constraints.put(component, constraints);
		if ((name = ((ExplicitConstraints)constraints).getName()) != null) {
			name2constraints.put(name, constraints);
		}
	}

	@Override
	public void removeLayoutComponent(Component component)
	{
		ExplicitConstraints constraints = getConstraints(component);
		if (constraints != null) {
			component2constraints.remove(component);
		}
	}

	@Override
	public float getLayoutAlignmentX(Container target)
	{
		return 0.0f;
	}


	@Override
	public float getLayoutAlignmentY(Container target)
	{
		return 0.0f;
	}

	@Override
	public void invalidateLayout(Container parent)
	{
		synchronized (parent.getTreeLock()) {
			if (minimumSize != null) {
				minimumSize.invalidate();
			}
			if (maximumSize != null) {
				maximumSize.invalidate();
			}
			if (preferredSize != null) {
				preferredSize.invalidate();
			}
			Enumeration<Object> num = component2constraints.elements();
			while (num.hasMoreElements()) {
				((ExplicitConstraints) num.nextElement()).invalidate();
			}
		}
	}

	public void setMaximumLayoutSize(Expression width, Expression height)
	{
		maximumSize = new DimensionExpression(width, height);
	}

	@Override
	public Dimension maximumLayoutSize(Container parent)
	{
		synchronized (parent.getTreeLock()) {
			container = parent;
			if (maximumSize == null) {
				return MAXIMUM_SIZE;
			} else {
				return maximumSize.getDimension(this);
			}
		}
	}

	public void setPreferredLayoutSize(Expression width, Expression height)
	{
		preferredSize = new DimensionExpression(width, height);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent)
	{
		synchronized (parent.getTreeLock()) {
			container = parent;
			if (preferredSize == null) {
				return PREFERRED_SIZE;
			} else {
				return preferredSize.getDimension(this);
			}
		}
	}

	public void setMinimumLayoutSize(Expression width, Expression height)
	{
		minimumSize = new DimensionExpression(width, height);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent)
	{
		synchronized (parent.getTreeLock()) {
			container = parent;
			if (minimumSize == null) {
				return MINIMUM_SIZE;
			} else {
				return minimumSize.getDimension(this);
			}
		}
	}

	@Override
	public void layoutContainer(Container parent)
	{
		synchronized (parent.getTreeLock()) {
			container = parent;

			Component [] components = parent.getComponents();
			ExplicitConstraints constraints;
			int x,y,w,h;
			for (int i = 0; i < components.length; i++) {
				//get the constraints for the component
				constraints = (ExplicitConstraints)component2constraints.get(components[i]);
				if (constraints != null) {
					//set the component bounds based on the constraints
					w = constraints.getWidthValue(this);
					h = constraints.getHeightValue(this);
					x = constraints.getXValue(this);
					y = constraints.getYValue(this);
					components[i].setBounds(x,y,w,h);
				}
			}
		}
	}

	class AbsoluteConstraints extends ExplicitConstraints {
		public AbsoluteConstraints(Component component)
		{
			super(component);
		}

		@Override
		public int getXValue(ExplicitLayout layout)
		{
			return getComponent().getLocation().x;
		}

		@Override
		public int getYValue(ExplicitLayout layout)
		{
			return getComponent().getLocation().y;
		}

		@Override
		public int getWidthValue(ExplicitLayout layout)
		{
			return getComponent().getSize().width;
		}

		@Override
		public int getHeightValue(ExplicitLayout layout)
		{
			return getComponent().getSize().height;
		}
	}
}
