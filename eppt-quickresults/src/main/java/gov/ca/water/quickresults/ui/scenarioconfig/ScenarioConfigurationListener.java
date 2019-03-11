/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */

package gov.ca.water.quickresults.ui.scenarioconfig;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import gov.ca.water.calgui.bo.FileDialogBO;
import org.apache.log4j.Logger;


/**
 * Company: Resource Management Associates
 *
 * @author <a href="mailto:adam@rmanet.com">Adam Korynta</a>
 * @since 02-23-2019
 */
public class ScenarioConfigurationListener implements ActionListener
{
	private static final Logger LOGGER = Logger.getLogger(ScenarioConfigurationListener.class.getName());
	private final ScenarioConfigurationPanel _scenarioConfigurationPanel;
	private final FileDialogBO _addScnearioFileDialogBO;

	public ScenarioConfigurationListener(ScenarioConfigurationPanel scenarioConfigurationPanel)
	{
		_scenarioConfigurationPanel = scenarioConfigurationPanel;

		JList<?> lstScenarios = _scenarioConfigurationPanel.getScenarioList();
		JRadioButton rdb1 = _scenarioConfigurationPanel.getRadioButton1();
		JRadioButton rdb2 = _scenarioConfigurationPanel.getRadioButton2();
		JLabel lblBase = _scenarioConfigurationPanel.getLabelBase();
		_addScnearioFileDialogBO = new FileDialogBO(lstScenarios, lblBase, rdb1, rdb2,
				null, true, (JFrame) SwingUtilities.windowForComponent(_scenarioConfigurationPanel));
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		switch(e.getActionCommand())
		{
			case "Rep_ClearMonths":
				setQRMonthCheckBoxesSelected(false);
				break;
			case "Rep_All":
				setQRMonthCheckBoxesSelected(true);
				break;
			case "btnAddScenario":
				launchFileDialogToAddScenarios(e);
				break;
			case "btnDelScenario":
				launchFileDialogToAddScenarios(e);
				break;
			case "btnClearScenario":
				launchFileDialogToAddScenarios(e);
				break;
			default:
		}
		ScenarioConfigurationPanel.getScenarioConfigurationPanel().setModified(true);
	}

	private void launchFileDialogToAddScenarios(ActionEvent e)
	{
		_addScnearioFileDialogBO.actionPerformed(e);
	}

	/**
	 * Selects/deselects all monthly checkboxes on Quick Result control panel
	 *
	 * @param b
	 */
	private void setQRMonthCheckBoxesSelected(boolean b)
	{
		JPanel controls2 = _scenarioConfigurationPanel.getControls2();
		Component[] components = controls2.getComponents();
		for(final Component component : components)
		{
			if(component instanceof JCheckBox)
			{
				JCheckBox c = (JCheckBox) component;
				String cName = c.getName();
				if(cName != null && cName.startsWith("RepchkMon"))
				{
					c.setSelected(b);
				}
			}
		}
	}
}
