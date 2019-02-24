/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */

package gov.ca.water.quickresults.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import javax.swing.*;

import org.swixml.SwingEngine;

import rma.swing.RmaJPanel;

/**
 * Company: Resource Management Associates
 *
 * @author <a href="mailto:adam@rmanet.com">Adam Korynta</a>
 * @since 02-21-2019
 */
public class QuickResultsPanel extends RmaJPanel
{
	private static final String LIST_REPORTS_ID = "lstReports";
	private static final String QUICK_RESULTS_XML_PATH = "ui/Quick_Results.xml";
	private final SwingEngine _engine;

	public QuickResultsPanel()
	{
		_engine = new SwingEngine();
		try
		{
			super.setLayout(new BorderLayout());
			Container swixmlQuickResultsPanel = _engine.render(QUICK_RESULTS_XML_PATH);
			super.add(swixmlQuickResultsPanel);
		}
		catch(Exception e)
		{
			throw new IllegalStateException(e);
		}
	}

	SwingEngine getSwingEngine()
	{
		return _engine;
	}

	Component getReportsJList()
	{
		return _engine.find(LIST_REPORTS_ID);
	}

	JTabbedPane getVariables()
	{
		return ((JTabbedPane) _engine.find("variables"));
	}
}
