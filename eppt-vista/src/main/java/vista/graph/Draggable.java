/*
 * Enhanced Post Processing Tool (EPPT) Copyright (c) 2019.
 *
 * EPPT is copyrighted by the State of California, Department of Water Resources. It is licensed
 * under the GNU General Public License, version 2. This means it can be
 * copied, distributed, and modified freely, but you may not restrict others
 * in their ability to copy, distribute, and modify it. See the license below
 * for more details.
 *
 * GNU General Public License
 */
package vista.graph;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * This class is useful when a graphic element is being dragged. In such a case
 * the GECanvas will not update in the double buffered mode. However if the
 * element being dragged is replaced by an instance of this class with that
 * element passed in to the construtor the animatenext method of this class will
 * simulate the dragging of that element.
 *
 * @author Nicky Sandhu
 * @version $Id: Draggable.java,v 1.2 1999/07/08 22:09:13 nsandhu Exp $
 * @see Drawable
 * @see Bounded
 * @see GEContainer
 * @see GELayoutManager
 * @see java.awt.Graphics
 */
public class Draggable extends GraphicElement
{
	private GraphicElement _dragee;

	public Draggable(GraphicElement dragee)
	{
		_dragee = dragee;
		setParent(dragee.getParent());
		setAttributes(dragee.getAttributes());
		setInsets(dragee.getInsets());
	}

	/**
	 * draws with the same graphics context and rectangle size.
	 */
	protected void Draw()
	{
		// do nothing
	}

	/**
	 * The update method is called when the animation frame needs to be updated.
	 * Each element is responsible for drawing itself within the bounds given.
	 */
	public void update(AnimationObservable o, Object arg)
	{
		// do nothing
	}

	/**
	 *
	 */
	public void setBounds(Rectangle r)
	{
		super.setBounds(r);
		_dragee.setBounds(r);
	}

	/**
	 *
	 */
	public void setGraphics(Graphics g)
	{
		super.setGraphics(g);
		_dragee.setGraphics(g);
	}

	/**
	 * Animates and displays next frame.
	 */
	public void animateNext()
	{
		if(_dragee != null)
		{
			_dragee.draw();
		}
	}

	/**
	 * returns the graphic element that is being dragged
	 */
	public GraphicElement getDragee()
	{
		return _dragee;
	}

	/**
	 * same preferred size as dragee
	 */
	public Dimension getPreferredSize()
	{
		return _dragee.getPreferredSize();
	}

	/**
	 * same preferred size as dragee
	 */
	public Dimension getMinimumSize()
	{
		return _dragee.getMinimumSize();
	}
}
