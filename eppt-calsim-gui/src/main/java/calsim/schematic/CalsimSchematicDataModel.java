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
package calsim.schematic;

import java.awt.Dimension;

/**
 * This interface is to be implemented by any class that wishes to
 * provide data for the schematic. This interface requires the implementation
 * to iterate over each symbol that needs to be drawn. The data required for
 * drawing a particular symbol is encapsulated in the CalsimSymbolData class
 *
 * @author Nicky Sandhu
 * @version $Id: CalsimSchematicDataModel.java,v 1.1.2.4 1999/07/18 20:57:55 nsandhu Exp $
 * @see CalsimSymbolData
 */
public interface CalsimSchematicDataModel
{
	/**
	 * resets the data model
	 */
	void reset();

	/**
	 * gets the data required to draw the next symbol.
	 *
	 * @see CalsimSymbolData
	 */
	CalsimSymbolData nextSymbolData();

	/**
	 * @return true while has more symbols to be added
	 */
	boolean hasMoreSymbols();
	/**
	 * A rectangle from xmin,ymin to xmax-xmin, ymax-ymin
	 */
	//  public DoubleRect getRealBounds();
	/**
	 * set the bounds from xmin,ymin to xmax,ymax
	 */
	//  public void setRealBounds(double xmin, double ymin, double xmax, double ymax);

	/**
	 * gets the maximum value for the x axis
	 */
	double getXMax();

	/**
	 * gets the maximum value for the x axis
	 */
	double getXMin();

	/**
	 * gets the maximum value for the x axis
	 */
	double getYMax();

	/**
	 * gets the maximum value for the x axis
	 */
	double getYMin();

	/**
	 * gets the title text for this schematic
	 */
	String getTitleText();

	/**
	 * sets the title text for this schematic
	 */
	void setTitleText(String str);

	/**
	 * gets the width and height of the schematic in screen pixels
	 */
	Dimension getScreenSize();
	/**
	 * sets the width and height of the schematic in screen pixels
	 */
	//  public void setScreenSize(int width, int height);
}
