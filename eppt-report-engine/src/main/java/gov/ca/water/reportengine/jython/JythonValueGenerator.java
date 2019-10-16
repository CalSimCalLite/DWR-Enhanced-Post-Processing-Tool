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

package gov.ca.water.reportengine.jython;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import javax.script.ScriptException;

import gov.ca.water.calgui.bo.CommonPeriodFilter;
import gov.ca.water.calgui.bo.PeriodFilter;
import gov.ca.water.calgui.bo.WaterYearDefinition;
import gov.ca.water.calgui.bo.WaterYearIndex;
import gov.ca.water.calgui.bo.WaterYearPeriod;
import gov.ca.water.calgui.bo.WaterYearPeriodFilter;
import gov.ca.water.calgui.bo.WaterYearPeriodRange;
import gov.ca.water.calgui.bo.WaterYearPeriodRangeFilter;
import gov.ca.water.calgui.bo.WaterYearType;
import gov.ca.water.calgui.project.EpptScenarioRun;
import gov.ca.water.calgui.scripts.JythonScriptRunner;
import gov.ca.water.reportengine.EpptReportException;
import sun.font.Script;

import static java.util.stream.Collectors.toList;

/**
 * Company: Resource Management Associates
 *
 * @author <a href="mailto:adam@rmanet.com">Adam Korynta</a>
 * @since 08-27-2019
 */
public class JythonValueGenerator
{
	private final PeriodFilter _periodFilter;
	private final EpptScenarioRun _scenarioRun;
	private final String _function;
	private final JythonScriptRunner _scriptRunner;

	public JythonValueGenerator(EpptScenarioRun scenarioRun, String function, CommonPeriodFilter commonPeriodFilter) throws ScriptException
	{
		this(input -> true, scenarioRun, function, commonPeriodFilter);
	}

	public JythonValueGenerator(PeriodFilter periodFilter, EpptScenarioRun base, String function, CommonPeriodFilter commonPeriodFilter)
			throws ScriptException
	{
		_scenarioRun = base;
		_function = JythonScriptBuilder.getInstance().buildFunctionFromTemplate(function);
		_periodFilter = periodFilter;
		_scriptRunner = new JythonScriptRunner(_scenarioRun, commonPeriodFilter);
		_scriptRunner.setPeriodFilter(_periodFilter);
		setWaterYearPeriodRange();
	}

	public JythonValueGenerator(EpptScenarioRun epptScenarioRun, String function, CommonPeriodFilter commonPeriodFilter,
								WaterYearIndex waterYearIndex) throws ScriptException
	{
		this(epptScenarioRun, function, commonPeriodFilter);
		_scriptRunner.setWaterYearIndex(waterYearIndex);
	}

	private void setWaterYearPeriodRange()
	{
		if(_periodFilter instanceof WaterYearPeriodRangeFilter)
		{
			WaterYearPeriodRangeFilter waterYearPeriodRangeFilter = (WaterYearPeriodRangeFilter) _periodFilter;
			_scriptRunner.setWaterYearPeriodRanges(Collections.singletonList(waterYearPeriodRangeFilter.getWaterYearPeriodRange()));
		}
		else if(_periodFilter instanceof WaterYearPeriodFilter)
		{
			WaterYearPeriodFilter waterYearPeriodFilter = (WaterYearPeriodFilter) _periodFilter;
			List<WaterYearPeriodRange> waterYearPeriodRanges = waterYearPeriodFilter.getWaterYearIndex()
																					.getWaterYearTypes()
																					.stream()
																					.filter(e -> e.getWaterYearPeriod().equals(waterYearPeriodFilter.getWaterYearPeriod()))
																					.map(e->new WaterYearPeriodRange(e.getWaterYearPeriod(), new WaterYearType(e.getYear(), e.getWaterYearPeriod()),new WaterYearType(e.getYear(), e.getWaterYearPeriod())))
																					.collect(toList());
			_scriptRunner.setWaterYearPeriodRanges(waterYearPeriodRanges);
		}
	}

	public JythonValueGenerator(EpptScenarioRun epptScenarioRun, String function, CommonPeriodFilter commonPeriodFilter, int comparisonValue)
			throws ScriptException
	{
		this(epptScenarioRun, function, commonPeriodFilter);
		_scriptRunner.setComparisonValue((double) comparisonValue);
	}

	public Double generateValue() throws EpptReportException
	{
		try
		{
			Object o = _scriptRunner.runScript(_function);
			Double retval;
			if(o instanceof BigInteger)
			{
				retval = ((BigInteger) o).doubleValue();
			}
			else
			{
				retval = (Double) o;
			}
			return retval;
		}
		catch(ScriptException e)
		{
			throw new EpptReportException("Error running script: " + _function, e);
		}
		catch(ClassCastException e)
		{
			throw new EpptReportException("Incorrect return type from function: " + _function +
					" Required: " + Double.class, e);
		}
	}

	public Object generateObjectValue() throws EpptReportException
	{
		try
		{
			return _scriptRunner.runScript(_function);
		}
		catch(ScriptException e)
		{
			throw new EpptReportException("Error running script: " + _function, e);
		}
	}

	@SuppressWarnings("unchecked")
	public long generateCount() throws EpptReportException
	{
		try
		{
			Object o = _scriptRunner.runScript(_function);
			return ((BigInteger) o).longValue();
		}
		catch(ScriptException e)
		{
			throw new EpptReportException("Error running script: " + _function, e);
		}
		catch(ClassCastException e)
		{
			throw new EpptReportException("Incorrect return type from function: " + _function +
					" Required: " + BigInteger.class, e);
		}
	}

	@SuppressWarnings("unchecked")
	public NavigableMap<Double, Double> generateExceedanceValues() throws EpptReportException
	{
		try
		{
			Object o = _scriptRunner.runScript(_function);
			if(o == null)
			{
				throw new ScriptException("Script returned null collection: " + _function);
			}
			return (NavigableMap<Double, Double>) o;
		}
		catch(ScriptException e)
		{
			throw new EpptReportException("Error running script: " + _function, e);
		}
		catch(ClassCastException e)
		{
			throw new EpptReportException("Incorrect return type from function: " + _function +
					" Required: " + List.class, e);
		}
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, Double> generateAnnualValues() throws EpptReportException
	{
		try
		{
			Object o = _scriptRunner.runScript(_function);
			if(o == null)
			{
				throw new ScriptException("Script returned null collection: " + _function);
			}
			return (Map<Integer, Double>) o;
		}
		catch(ScriptException e)
		{
			throw new EpptReportException("Error running script: " + _function, e);
		}
		catch(ClassCastException e)
		{
			throw new EpptReportException("Incorrect return type from function: " + _function +
					" Required: " + List.class, e);
		}
	}

	@SuppressWarnings("unchecked")
	public Map<Month, Double> generateMonthlyValues() throws EpptReportException
	{
		try
		{
			Object o = _scriptRunner.runScript(_function);
			if(o == null)
			{
				throw new ScriptException("Script returned null collection: " + _function);
			}
			else if(o instanceof Map && ((Map) o).isEmpty())
			{
				throw new ScriptException("No data found");
			}
			return new EnumMap((Map<Month, Double>) o);
		}
		catch(ScriptException e)
		{
			throw new EpptReportException("Error running script: " + _function, e);
		}
		catch(ClassCastException e)
		{
			throw new EpptReportException("Incorrect return type from function: " + _function +
					" Required: ", e);
		}
	}
}
