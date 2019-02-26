/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */
package vista.set;


/**
 * A class for creation of data sets
 * 
 * @author Nicky Sandhu
 * @version $Id: DataSetFactory.java,v 1.3 1998/11/02 20:22:15 nsandhu Exp $
 */
public class DataSetFactory {
	/**
	 * creates a default data set of x and y and flag arrays with given
	 * attributes
	 */
	public static DataSet createDefaultDataSet(String name, double[] x,
			double[] y, int[] flags, DataSetAttr attr) {
		return new DefaultDataSet(name, x, y, flags, attr);
	}

	/**
	 * creates a default data set of x and y and flag arrays with given
	 * attributes
	 */
	public static DataSet createIndexedDataSet(String name, double xi,
			double xf, double step, double[] y, int[] flags, DataSetAttr attr) {
		return new IndexedDataSet(name, xi, xf, step, y, flags, attr);
	}

	/**
	 * creates a data set
	 */
	public static DataSet createTimeSeries(String name, String startTime,
			String timeInterval, double[] y, int[] flags, DataSetAttr attr) {
		return new RegularTimeSeries(name, startTime, timeInterval, y, flags,
				attr);
	}
}