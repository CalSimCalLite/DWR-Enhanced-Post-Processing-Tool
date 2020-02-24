/*
 * Enhanced Post Processing Tool (EPPT) Copyright (c) 2020.
 *
 * EPPT is copyrighted by the State of California, Department of Water Resources. It is licensed
 * under the GNU General Public License, version 2. This means it can be
 * copied, distributed, and modified freely, but you may not restrict others
 * in their ability to copy, distribute, and modify it. See the license below
 * for more details.
 *
 * GNU General Public License
 */

package gov.ca.water.calgui.presentation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.*;

import calsim.app.DerivedTimeSeries;
import calsim.app.MultipleTimeSeries;
import gov.ca.water.calgui.EpptInitializationException;
import gov.ca.water.calgui.bo.GUILinksAllModelsBO;
import gov.ca.water.calgui.busservice.impl.DSSGrabber2SvcImpl;
import gov.ca.water.calgui.project.EpptScenarioRun;
import gov.ca.water.calgui.project.PlotConfigurationState;

import hec.io.TimeSeriesContainer;

/**
 * Company: Resource Management Associates
 *
 * @author <a href="mailto:adam@rmanet.com">Adam Korynta</a>
 * @since 01-20-2020
 */
class DisplayWRIMSFrames extends DisplayFrames
{
	private final DerivedTimeSeries _dts;
	private final MultipleTimeSeries _mts;

	DisplayWRIMSFrames(PlotConfigurationState plotConfigurationState,
					   DerivedTimeSeries dts,
					   MultipleTimeSeries mts,
					   EpptScenarioRun baseRun,
					   List<EpptScenarioRun> alternatives,
					   LocalDate start,
					   LocalDate end) throws EpptInitializationException
	{
		super(plotConfigurationState, baseRun, alternatives, start, end);
		_dts = dts;
		_mts = mts;
	}

	List<JTabbedPane> showDisplayFrames()
	{
		List<JTabbedPane> tabbedPanes = new ArrayList<>();

		DSSGrabber2SvcImpl dssGrabber = new DSSGrabber2SvcImpl(_dts, _mts);
		JTabbedPane tabbedPane = new JTabbedPane();
		dssGrabber.setIsCFS(!getPlotConfigurationState().isDisplayTaf());
		dssGrabber.setScenarioRuns(getBaseRun(), getAlternatives());
		dssGrabber.setDateRange(getStart(), getEnd());
		if(_mts != null)
		{
			tabbedPane.setName(_mts.getName());

			List<EpptScenarioRun> scenarioRuns = new ArrayList<>();
			scenarioRuns.add(getBaseRun());
			scenarioRuns.addAll(getAlternatives());
			plotMts(scenarioRuns, _mts, dssGrabber, tabbedPane);
		}
		if(_dts != null)
		{
			tabbedPane.setName(_dts.getName());
			plotDts(_dts, dssGrabber, tabbedPane);
		}
		Map<GUILinksAllModelsBO.Model, List<String>> missing = dssGrabber.getMissingList();
		if(!missing.isEmpty())
		{
			insertEmptyTab(tabbedPane, missing);
		}
		tabbedPanes.add(tabbedPane);
		return tabbedPanes;
	}

	private void plotMts(List<EpptScenarioRun> scenarios, MultipleTimeSeries mts, DSSGrabber2SvcImpl dssGrabber, JTabbedPane tabbedPane)
	{
		dssGrabber.setLocation("@@" + mts.getName());
		int n = mts.getNumberOfDataReferences();
		Map<EpptScenarioRun, List<TimeSeriesContainer>> scenarioRunData = new TreeMap<>(Comparator.comparing(scenarios::indexOf));
		Map<EpptScenarioRun, List<String>> primarySuffxies = new TreeMap<>(Comparator.comparing(scenarios::indexOf));
		for(int i = 0; i < n; i++)
		{
			TimeSeriesContainer[] result = dssGrabber.getMultipleTimeSeries(i);
			for(int j = 0; j < scenarios.size(); j++)
			{
				EpptScenarioRun epptScenarioRun = scenarios.get(j);
				TimeSeriesContainer tsc = result[j];
				String suffix = tsc.getFullName();
				primarySuffxies.computeIfAbsent(epptScenarioRun, v -> new ArrayList<>()).add(suffix);
				scenarioRunData.computeIfAbsent(epptScenarioRun, v -> new ArrayList<>()).add(tsc);
			}
		}

		if(getPlotConfigurationState().isDisplayTimeSeriesPlot())
		{
			plotTimeSeries(primarySuffxies, scenarioRunData, new HashMap<>(), new HashMap<>(), mts.getName(), tabbedPane);
		}
		if(getPlotConfigurationState().isDoExceedance())
		{
			plotExceedance(primarySuffxies, scenarioRunData, new HashMap<>(), new HashMap<>(), mts.getName(), tabbedPane);
		}
		if(getPlotConfigurationState().isDisplayBoxAndWhiskerPlot())
		{
			plotBoxPlot(primarySuffxies, scenarioRunData, new HashMap<>(), new HashMap<>(), mts.getName(), tabbedPane);
		}
		if(getPlotConfigurationState().isDisplayMonthlyTable())
		{
			plotMonthlyTable(primarySuffxies, scenarioRunData, new HashMap<>(), new HashMap<>(), mts.getName(), tabbedPane);
		}
		if(getPlotConfigurationState().isDisplaySummaryTable())
		{
			plotSummaryTable(primarySuffxies, scenarioRunData, new HashMap<>(), new HashMap<>(), mts.getName(), tabbedPane);
		}
	}

	private void plotDts(DerivedTimeSeries dts, DSSGrabber2SvcImpl dssGrabber, JTabbedPane tabbedPane)
	{
		// Handle DTS

		dssGrabber.setLocation("@@" + dts.getName());

		TimeSeriesContainer[] primaryResults = dssGrabber.getPrimarySeries();
		if(primaryResults != null)
		{
			List<EpptScenarioRun> scenarioRuns = new ArrayList<>();
			scenarioRuns.add(getBaseRun());
			scenarioRuns.addAll(getAlternatives());
			Map<EpptScenarioRun, List<TimeSeriesContainer>> scenarioRunData = new TreeMap<>(Comparator.comparing(scenarioRuns::indexOf));
			for(int i = 0; i < primaryResults.length; i++)
			{
				EpptScenarioRun epptScenarioRun = scenarioRuns.get(i);
				TimeSeriesContainer tsc = primaryResults[i];
				scenarioRunData.put(epptScenarioRun, Collections.singletonList(tsc));
			}
			if(getPlotConfigurationState().isDisplayTimeSeriesPlot())
			{
				plotTimeSeries(scenarioRunData, new HashMap<>(), dts.getName(), tabbedPane);
			}
			if(getPlotConfigurationState().isDoExceedance())
			{
				plotExceedance(scenarioRunData, new HashMap<>(), dts.getName(), tabbedPane);
			}
			if(getPlotConfigurationState().isDisplayBoxAndWhiskerPlot())
			{
				plotBoxPlot(scenarioRunData, new HashMap<>(), dts.getName(), tabbedPane);
			}
			if(getPlotConfigurationState().isDisplayMonthlyTable())
			{
				plotMonthlyTable(new HashMap<>(), scenarioRunData, new HashMap<>(), new HashMap<>(), dts.getName(), tabbedPane);
			}
			if(getPlotConfigurationState().isDisplaySummaryTable())
			{
				plotSummaryTable(new HashMap<>(), scenarioRunData, new HashMap<>(), new HashMap<>(), dts.getName(), tabbedPane);
			}
		}
	}
}