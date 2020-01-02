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

package gov.ca.water.trendreporting;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import gov.ca.water.calgui.bo.WaterYearDefinition;
import gov.ca.water.calgui.bo.WaterYearIndex;
import gov.ca.water.calgui.constant.Constant;

import static java.util.stream.Collectors.toMap;

/**
 * Company: Resource Management Associates
 *
 * @author <a href="mailto:adam@rmanet.com">Adam Korynta</a>
 * @since 09-23-2019
 */
class TrendStatistics
{
	private static final Logger LOGGER = Logger.getLogger(TrendStatistics.class.getName());
	private final ScriptEngine _scriptEngine = new ScriptEngineManager(getClass().getClassLoader()).getEngineByName("python");
	private final Path _jythonFilePath;
	private final String _name;

	TrendStatistics(Path jythonFilePath)
	{
		_jythonFilePath = jythonFilePath;
		_name = loadStatisticName();
		setupScriptEngine();
	}

	private void setupScriptEngine()
	{
		Path jython = Paths.get(Constant.CONFIG_DIR).resolve("jython");
		try(Stream<Path> stream = Files.walk(jython, 5))
		{
			List<Path> py = stream.filter(p -> p.toFile().isFile()).filter(p -> p.toString().endsWith("py")).collect(Collectors.toList());
			for(Path p : py)
			{
				try(BufferedReader reader = Files.newBufferedReader(p))
				{
					_scriptEngine.eval(reader);
				}
			}
		}
		catch(IOException | ScriptException e)
		{
			LOGGER.log(Level.SEVERE, "Unable to load helper scripts from: " + jython, e);
		}
	}

	String getName()
	{
		return _name;
	}

	@SuppressWarnings(value = "unchecked")
	SortedMap<Month, Double> calculateMonthly(SortedMap<Month, NavigableMap<Integer,Double>> data, WaterYearDefinition waterYearDefinition,
											  WaterYearIndex waterYearIndex, List<WaterYearIndex> waterYearIndices,
											  EpptReportingMonths.MonthPeriod monthPeriod)
	{
		Map<Month, Double> retval = new EnumMap<>(Month.class);
		try
		{
			for(Map.Entry<Month, NavigableMap<Integer,Double>> entry : data.entrySet())
			{
				NavigableMap<Integer, Double> value = entry.getValue();
				Month month = entry.getKey();
				Object obj = runScript(value, waterYearDefinition, waterYearIndex, waterYearIndices);
				if(obj instanceof OptionalDouble)
				{
					OptionalDouble opt = ((OptionalDouble) obj);
					if(opt.isPresent())
					{
						retval.put(month, opt.getAsDouble());
					}
					else
					{
						retval.put(month, null);
					}
				}
				else
				{
					retval.put(month, (Double) obj);
				}
			}
		}
		catch(ClassCastException e)
		{
			LOGGER.log(Level.SEVERE,
					"Error computing statistic " + getName() + " from Jython script: " + _jythonFilePath +
							" ensure method calculate(Map<LocalDateTime, Double> data) returns a Map<? extends Month, ? extends Double>", e);
		}
		catch(IOException | ScriptException e)
		{
			LOGGER.log(Level.SEVERE,
					"Error computing statistic " + getName() + " from Jython script: " + _jythonFilePath +
							" ensure method calculate(Map<LocalDateTime, Double> data) is defined",
					e);
		}
		return sort(retval, monthPeriod);
	}

	private Object runScript(Map<Integer, Double> data, WaterYearDefinition waterYearDefinition, WaterYearIndex waterYearIndex,
							 List<WaterYearIndex> waterYearIndices) throws ScriptException, IOException
	{
		try(BufferedReader bufferedReader = Files.newBufferedReader(_jythonFilePath))
		{
			_scriptEngine.eval(bufferedReader);
			_scriptEngine.put("waterYearIndices", waterYearIndices);
			_scriptEngine.put("data", data);
			_scriptEngine.put("waterYearIndex", waterYearIndex);
			_scriptEngine.put("waterYearDefinition", waterYearDefinition);
			return _scriptEngine.eval("calculate(data)");
		}
	}

	private SortedMap<Month, Double> sort(Map<Month, Double> calculate, EpptReportingMonths.MonthPeriod monthPeriod)
	{
		List<Month> months = EpptReportingMonths.getMonths(monthPeriod);
		SortedMap<Month, Double> retval = new TreeMap<>(Comparator.comparingInt(months::indexOf));
		retval.putAll(calculate);
		return retval;
	}

	private String loadStatisticName()
	{
		String retval = _jythonFilePath.getFileName().toString();

		try(BufferedReader bufferedReader = Files.newBufferedReader(_jythonFilePath))
		{
			_scriptEngine.eval(bufferedReader);
			retval = Objects.toString(_scriptEngine.eval("getName()"));
		}
		catch(IOException | ScriptException e)
		{
			LOGGER.log(Level.SEVERE, "Error getting name from Jython script: " + _jythonFilePath + " ensure method getName() is defined", e);
		}
		return retval;
	}

	public Double calculateYearly(SortedMap<Integer, Double> data,
													  WaterYearDefinition waterYearDefinition,
													  WaterYearIndex waterYearIndex, List<WaterYearIndex> waterYearIndices)
	{
		Double retval = null;
		try
		{
			//				value.values().stream().mapToDouble(e->e).min()
			Object obj = runScript(data, waterYearDefinition, waterYearIndex, waterYearIndices);
			if(obj instanceof OptionalDouble)
			{
				OptionalDouble opt = ((OptionalDouble) obj);
				if(opt.isPresent())
				{
					retval = opt.getAsDouble();
				}
			}
			else if(obj instanceof Double)
			{
				retval = (Double) obj;
			}
		}
		catch(ClassCastException e)
		{
			LOGGER.log(Level.SEVERE,
					"Error computing statistic " + getName() + " from Jython script: " + _jythonFilePath +
							" ensure method calculate(Map<LocalDateTime, Double> data) returns a Map<? extends Month, ? extends Double>", e);
		}
		catch(IOException | ScriptException e)
		{
			LOGGER.log(Level.SEVERE,
					"Error computing statistic " + getName() + " from Jython script: " + _jythonFilePath +
							" ensure method calculate(Map<LocalDateTime, Double> data) is defined",
					e);
		}
		return retval;
	}
}
