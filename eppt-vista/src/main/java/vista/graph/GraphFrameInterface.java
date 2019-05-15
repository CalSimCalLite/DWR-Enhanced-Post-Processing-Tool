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

import javax.swing.*;

/**
 * @author Nicky Sandhu
 * @version $Id: GraphFrameInterface.java,v 1.1 2003/10/02 20:49:00 redwood Exp
 * $
 */
public interface GraphFrameInterface
{
	/**
	 *
	 */
	void addToolBar(JToolBar tb);

	/**
	 *
	 */
	GECanvas getCanvas();

}
