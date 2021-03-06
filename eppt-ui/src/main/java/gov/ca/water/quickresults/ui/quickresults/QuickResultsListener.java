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

package gov.ca.water.quickresults.ui.quickresults;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import gov.ca.water.calgui.bo.GUILinksAllModelsBO;
import gov.ca.water.calgui.bo.ResultUtilsBO;
import gov.ca.water.calgui.busservice.impl.GuiLinksSeedDataSvcImpl;
import gov.ca.water.calgui.presentation.DisplayHelper;
import gov.ca.water.calgui.presentation.plotly.EpptPlotException;
import gov.ca.water.calgui.project.EpptConfigurationController;
import gov.ca.water.calgui.project.EpptScenarioRun;
import gov.ca.water.calgui.project.PlotConfigurationState;
import gov.ca.water.calgui.techservice.IDialogSvc;
import gov.ca.water.calgui.techservice.impl.DialogSvcImpl;
import gov.ca.water.quickresults.ui.EpptPanel;

import static java.util.stream.Collectors.toList;


/**
 * Company: Resource Management Associates
 *
 * @author <a href="mailto:adam@rmanet.com">Adam Korynta</a>
 * @since 02-23-2019
 */
public class QuickResultsListener implements ActionListener
{
	private static final Logger LOGGER = Logger.getLogger(QuickResultsListener.class.getName());
	private final QuickResultsPanel _quickResultsPanel;
	private final EpptConfigurationController _epptConfigurationController;

	public QuickResultsListener(QuickResultsPanel quickResultsPanel, EpptConfigurationController epptConfigurationController)
	{
		_quickResultsPanel = quickResultsPanel;
		_epptConfigurationController = epptConfigurationController;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		switch(e.getActionCommand())
		{
			case "Rep_AddList":
				addToReportList();
				break;
			case "Rep_ClearList":
				clearReportList();
				break;
			case "Rep_DispAll":
				displayReportList();
				break;
			case "Rep_SaveList":
				saveReportList();
				break;
			case "Rep_LoadList":
				loadReportList();
				break;
			case "AC_Clear":
				clearSelectedPanelCheckboxes();
				break;
		}

	}

	private void clearSelectedPanelCheckboxes()
	{
		JTabbedPane variables = _quickResultsPanel.getVariables();
		Component selectedComponent = variables.getSelectedComponent();
		if(selectedComponent instanceof Container)
		{
			EpptPanel.setCheckboxesSelectedRecusive(false, (Container) selectedComponent);
		}
	}

	private void loadReportList()
	{
		Window window = SwingUtilities.windowForComponent(_quickResultsPanel);
		Optional<List<String>> data = ResultUtilsBO.getResultUtilsInstance().readCGR((JFrame) window);
		if(data.isPresent())
		{
			Component component = _quickResultsPanel.getReportsJList();
			if(component instanceof JList)
			{
				JList<String> lstReports = (JList<String>) component;
				lstReports.setListData(data.get().toArray(new String[0]));
			}
		}
	}

	private void saveReportList()
	{
		Window window = SwingUtilities.windowForComponent(_quickResultsPanel);
		JList<?> lstReports = (JList<?>) _quickResultsPanel.getSwingEngine().find("lstReports");
		ResultUtilsBO.getResultUtilsInstance().writeCGR((JFrame) window, lstReports);
	}

	private void displayReportList()
	{
		Optional<EpptScenarioRun> baseScenario = _epptConfigurationController.getEpptScenarioBase();
		if(!baseScenario.isPresent())
		{
			IDialogSvc dialogSvc = DialogSvcImpl.getDialogSvcInstance();
			dialogSvc.getOK("Error - No Base Scenario defined", JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			Component component = _quickResultsPanel.getReportsJList();
			if(component instanceof JList)
			{
				JList<String> lstReports = (JList<String>) component;
				for(int i = 0; i < lstReports.getModel().getSize(); i++)
				{
					String elementAt = lstReports.getModel().getElementAt(i);
					PlotConfigurationState plotConfigurationState = PlotConfigurationStateBuilder.fromString(elementAt);
					List<String> locations = parseLocations(elementAt);
					DisplayHelper displayHelper = _quickResultsPanel.getDisplayHelper();
					List<GUILinksAllModelsBO> guiLinks = locations.stream()
																  .map(GuiLinksSeedDataSvcImpl.getSeedDataSvcImplInstance()::getGuiLink)
																  .collect(toList());
					displayHelper.showDisplayFramesGuiLink(plotConfigurationState, guiLinks);
				}
			}
		}
	}

	private List<String> parseLocations(String elementAt)
	{
		List<String> retval = new ArrayList<>();
		String[] split = elementAt.split("Index-");
		if(split.length > 1)
		{
			split = split[1].split(";");
			if(split.length > 0)
			{
				retval = Arrays.asList(split[0].split(","));
			}
		}
		return retval;
	}

	private List<String> getReports()
	{
		List<String> retval = new ArrayList<>();
		Component component = _quickResultsPanel.getReportsJList();
		if(component instanceof JList)
		{
			JList<String> lstReports = (JList<String>) component;
			ListModel<String> model = lstReports.getModel();
			for(int i = 0; i < model.getSize(); i++)
			{
				retval.add(model.getElementAt(i));
			}
		}
		return retval;
	}

	private void clearReportList()
	{
		Component component = _quickResultsPanel.getReportsJList();
		if(component instanceof JList)
		{
			JList<String> lstReports = (JList<String>) component;
			lstReports.setListData(new String[0]);
		}
	}

	/**
	 * Adds items to list of reports on Quick Result dashboard. One item is
	 * added for each checked item on the children panels for "variables"
	 */
	private void addToReportList()
	{
		try
		{
			// Store previous list items
			List<String> reports = getReports();
			int size = reports.size();
			int n = 0;
			String[] lstArray = new String[size];
			for(Object item : reports)
			{
				if(!" ".equals(item.toString()))
				{
					lstArray[n] = item.toString();
					n = n + 1;
				}
			}
			String[] lstArray1 = new String[n + 1];
			System.arraycopy(lstArray, 0, lstArray1, 0, n);
			// Add new items
			String reportDescriptor = extractReportDescriptor();
			Component component = _quickResultsPanel.getReportsJList();
			if(component instanceof JList)
			{
				lstArray1[n] = reportDescriptor;
				JList<String> lstReports = (JList<String>) component;
				lstReports.setListData(lstArray1);
			}
		}
		catch(RuntimeException | EpptPlotException e)
		{
			LOGGER.log(Level.SEVERE, "Unable to plot report list.", e);
		}
	}

	private String extractReportDescriptor() throws EpptPlotException
	{
		StringBuilder cSTOR = new StringBuilder(";Locs-");
		StringBuilder cSTORIdx = new StringBuilder(";Index-");
		Component[] components = _quickResultsPanel.getVariables().getComponents();
		extractCheckBoxes(cSTOR, cSTORIdx, components);
		return new PlotConfigurationStateBuilder(_quickResultsPanel.getSwingEngine())
				.createQuickStateString() + cSTOR + cSTORIdx;
	}

	private void extractCheckBoxes(StringBuilder cSTOR, StringBuilder cSTORIdx, Component[] components)
	{
		for(Component component : components)
		{
			if(component instanceof JCheckBox)
			{
				JCheckBox cb = (JCheckBox) component;
				String cName = cb.getName();
				if(cName.startsWith("ckbp") && cb.isSelected())
				{
					cSTOR.append(cb.getText().trim()).append(",");
					cSTORIdx.append(cName).append(",");
				}
			}
			else if(component instanceof Container)
			{
				extractCheckBoxes(cSTOR, cSTORIdx, ((Container) component).getComponents());
			}
		}
	}


}
