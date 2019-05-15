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
package gov.ca.water.eppt.nbui;

import java.awt.BorderLayout;
import java.net.MalformedURLException;
import java.net.URL;
import javax.help.BadIDException;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.JHelp;
import javax.swing.*;

import gov.ca.water.calgui.EpptInitializationException;
import gov.ca.water.calgui.constant.Constant;
import org.apache.log4j.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
		preferredID = "EpptHelpTopComponent",
		iconBase = "gov/ca/water/eppt/nbui/questionmark18.gif"
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "gov.ca.water.eppt.nbui.EpptHelpTopComponent")
@ActionReference(path = "Menu/Help", position = 0)
@TopComponent.OpenActionRegistration(
		displayName = "EPPT Help",
		preferredID = "EpptHelpTopComponent"
)
public final class EpptHelpTopComponent extends TopComponent
{
	private final JHelp _helpViewer;

	public EpptHelpTopComponent() throws EpptInitializationException
	{
		setName("EPPT Help");
		try
		{
			String path = Constant.DOCS_DIR + "\\Help\\CalLite3-GUI-Help_JavaHelp_V2_082614.hs";
			URL url = new URL("file:///" + path);
			_helpViewer = new JHelp(new HelpSet(null, url));
			setLayout(new BorderLayout());
			add(_helpViewer, BorderLayout.CENTER);
			initListener();
		}
		catch(MalformedURLException | HelpSetException ex)
		{
			throw new EpptInitializationException("Unable to establish EPPT Help", ex);
		}
	}

	private void initListener()
	{
		TopComponent.getRegistry().addPropertyChangeListener(e -> selectActivatedHelp());
	}

	private void selectActivatedHelp()
	{
		TopComponent activated = TopComponent.getRegistry().getActivated();
		if(activated instanceof EpptTopComponent)
		{
			String javaHelpId = ((EpptTopComponent) activated).getJavaHelpId();
			selectCurrentHelperId(javaHelpId);
		}
	}

	void selectCurrentHelperId(String id)
	{
		try
		{
			_helpViewer.setCurrentID(id);
		}
		catch(BadIDException ex)
		{
			Logger.getLogger(EpptHelpTopComponent.class.getName()).debug(ex);
		}
	}

	@Override
	protected void componentOpened()
	{
		super.componentOpened();
		selectActivatedHelp();
		SwingUtilities.invokeLater(() -> WindowManager.getDefault().setTopComponentFloating(this, true));
	}
}
