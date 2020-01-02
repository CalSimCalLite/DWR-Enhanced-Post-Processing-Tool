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

package gov.ca.water.reportengine.executivereport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.google.common.flogger.FluentLogger;
import gov.ca.water.calgui.bo.DetailedIssue;
import gov.ca.water.calgui.busservice.impl.DSSGrabber1SvcImpl;
import gov.ca.water.calgui.project.EpptScenarioRun;
import gov.ca.water.reportengine.EpptReportException;
import gov.ca.water.reportengine.standardsummary.StandardSummaryErrors;

import hec.heclib.util.HecTime;
import hec.heclib.util.HecTimeArray;
import hec.io.TimeSeriesContainer;
import hec.lang.Const;

import static gov.ca.water.reportengine.EPPTReport.checkInterrupt;

public class DTSProcessor
{
	private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();
	private final List<Module> _modules = new ArrayList<>();
	private final StandardSummaryErrors _standardSummaryErrors;


	public DTSProcessor(List<Module> modules, StandardSummaryErrors standardSummaryErrors)
	{
		_standardSummaryErrors = standardSummaryErrors;
		_modules.addAll(modules);
	}

	public Map<EpptScenarioRun, Map<SubModule, List<FlagViolation>>> processDSSFiles(List<EpptScenarioRun> runs)
			throws EpptReportException
	{

		Map<EpptScenarioRun, Map<SubModule, List<FlagViolation>>> runToViolations = new HashMap<>();
		for(EpptScenarioRun run : runs)
		{
			checkInterrupt();
			Map<SubModule, List<FlagViolation>> subModuleToViolations = processDSSFile(run);
			runToViolations.put(run, subModuleToViolations);
		}
		return runToViolations;
	}

	/**
	 * Gets the violations from the dssFile and puts them into the correct subModule
	 *
	 * @param epptScenarioRun Path to dss file with dts records
	 * @throws Exception
	 */
	private Map<SubModule, List<FlagViolation>> processDSSFile(EpptScenarioRun epptScenarioRun) throws EpptReportException
	{
		LOGGER.at(Level.INFO).log("Processing DTS Records in file scenario: %s", epptScenarioRun);
		Map<SubModule, List<FlagViolation>> subModToViolations = new HashMap<>();
		for(Module mod : _modules)
		{
			checkInterrupt();

			if("COA".equalsIgnoreCase(mod.getName()))
			{
				Map<SubModule, List<FlagViolation>> subModuleListMap = setMaxValueForCOAModuleFromDssFile(mod, epptScenarioRun);
				for(Map.Entry<SubModule, List<FlagViolation>> entry : subModuleListMap.entrySet())
				{
					subModToViolations.put(entry.getKey(), entry.getValue());
				}
				continue;
			}

			List<SubModule> subModules = mod.getSubModules();
			for(SubModule sm : subModules)
			{
				checkInterrupt();
				List<FlagViolation> violations = getViolations(epptScenarioRun, sm);
				subModToViolations.put(sm, violations);
			}
		}
		return subModToViolations;
	}

	private List<FlagViolation> getViolations(EpptScenarioRun epptScenarioRun, SubModule sm) throws EpptReportException
	{
		List<FlagViolation> violations = new ArrayList<>();
		List<DetailedIssue> linkedRecords = sm.getLinkedRecords();
		SubModule.FlagType flagValue = sm.getFlagValue();
		for(DetailedIssue detailedIssue : linkedRecords)
		{
			LOGGER.at(Level.INFO).log("Processing DTS ID: %s and DTS Record Path: %s ",
					detailedIssue.getDetailedIssueId(), detailedIssue.getLinkedVar());
			checkInterrupt();
			DSSGrabber1SvcImpl dssGrabber1Svc = buildDssGrabber(epptScenarioRun, detailedIssue);
			TimeSeriesContainer[] primarySeries = dssGrabber1Svc.getPrimarySeries();
			if(primarySeries != null && primarySeries[0] != null && primarySeries[0].values.length > 0)
			{
				TimeSeriesContainer result = primarySeries[0];
				FlagViolation flagViolationFromRecord = createFlagViolationFromRecord(result, flagValue, detailedIssue);
				if(flagViolationFromRecord != null)
				{
					violations.add(flagViolationFromRecord);
				}
			}
			else
			{
				_standardSummaryErrors.addError(LOGGER,
						"Unable to process flagged violations for DTS ID: "
								+ detailedIssue.getDetailedIssueId() + " DTS record: " + detailedIssue.getLinkedVar());
			}
		}
		return violations;
	}


	private Map<SubModule, List<FlagViolation>> setMaxValueForCOAModuleFromDssFile(Module mod, EpptScenarioRun epptScenarioRun)
			throws EpptReportException
	{
		Map<SubModule, List<FlagViolation>> subModToViolations = new HashMap<>();
		List<SubModule> subModules = mod.getSubModules();
		for(SubModule sm : subModules)
		{
			List<DetailedIssue> linkedRecords = sm.getLinkedRecords();
			for(DetailedIssue detailedIssue : linkedRecords)
			{
				createViolationWithMaxValue(subModToViolations, sm, epptScenarioRun, detailedIssue);
			}
		}
		return subModToViolations;
	}

	private void createViolationWithMaxValue(Map<SubModule, List<FlagViolation>> subModToViolations, SubModule sm, EpptScenarioRun epptScenarioRun,
											 DetailedIssue detailedIssue) throws ExecutiveReportException
	{
		FlagViolation violation;
		try
		{
			DSSGrabber1SvcImpl dssGrabber1Svc = buildDssGrabber(epptScenarioRun, detailedIssue);
			TimeSeriesContainer[] primarySeries = dssGrabber1Svc.getPrimarySeries();
			if(primarySeries != null && primarySeries[0] != null)
			{
				TimeSeriesContainer result = primarySeries[0];

				double[] values = result.values;

				double maxValue = getMaxValue(values);

				violation = new FlagViolation(maxValue, detailedIssue.getLinkedVar());

				List<FlagViolation> violations = new ArrayList<>();
				violations.add(violation);
				subModToViolations.put(sm, violations);
			}
			else
			{
				_standardSummaryErrors.addError(LOGGER, "Error finding detailed issue:" + detailedIssue.getDetailedIssueId() +
						" during executive report generation: " + epptScenarioRun +
						" Could not find record with name: " + detailedIssue.getLinkedVar());
			}
		}
		catch(RuntimeException e)
		{
			throw new ExecutiveReportException("Error reading dssFile during executive report generation: " + epptScenarioRun, e);
		}
	}

	private DSSGrabber1SvcImpl buildDssGrabber(EpptScenarioRun epptScenarioRun, DetailedIssue dtsLink)
	{
		DSSGrabber1SvcImpl grabber1Svc = new DSSGrabber1SvcImpl();
		grabber1Svc.setScenarioRuns(epptScenarioRun, Collections.emptyList());
		grabber1Svc.setDtsLink(dtsLink);
		grabber1Svc.setIsCFS(false);
		return grabber1Svc;
	}

	private double getMaxValue(double[] numbers)
	{
		double maxValue = numbers[0];
		for(int i = 1; i < numbers.length; i++)
		{
			if(numbers[i] > maxValue)
			{
				maxValue = numbers[i];
			}
		}
		return maxValue;
	}

	private FlagViolation createFlagViolationFromRecord(TimeSeriesContainer tsc, SubModule.FlagType flagType, DetailedIssue detailedIssue)
	{
		FlagViolation retval = null;
		try
		{
			List<HecTime> violationTimes = new ArrayList<>();
			HecTimeArray times = tsc.getTimes();
			for(int i = 0; i < times.numberElements(); i++)
			{
				addTimeIfInViolation(tsc, flagType, violationTimes, times, i);
			}
			if(!violationTimes.isEmpty())
			{
				retval = new FlagViolation(violationTimes, detailedIssue.getLinkedVar());
			}
		}
		catch(RuntimeException e)
		{
			_standardSummaryErrors.addError(LOGGER, "Error processing flagged violation", e);
		}
		return retval;
	}

	private void addTimeIfInViolation(TimeSeriesContainer tsc, SubModule.FlagType flagType, List<HecTime> violationTimes, HecTimeArray times, int i)
	{
		HecTime hecTime = times.elementAt(i);
		int value = (int) Math.round(tsc.getValue(hecTime));
		if(Const.isValid(value))
		{
			switch(value)
			{
				case 100:
					if(flagType == SubModule.FlagType.VALUE_100)
					{
						violationTimes.add(hecTime);
					}
					break;
				case 200:
					if(flagType == SubModule.FlagType.VALUE_200)
					{
						violationTimes.add(hecTime);
					}
					break;
				case 300:
					if(flagType == SubModule.FlagType.VALUE_300)
					{
						violationTimes.add(hecTime);
					}
					break;
			}

		}
	}

}
