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

package gov.ca.water.reportengine.detailedissues;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import hec.heclib.util.HecTime;

public class DetailedIssueViolation
{
	private static final Logger LOGGER = Logger.getLogger(DetailedIssueViolation.class.getName());

	private final String _title;
	private final int _totalNumberOfViolations;
	private final List<Issue> _issues = new ArrayList<>();

	DetailedIssueViolation(List<LocalDateTime> times, String title, Map<LocalDateTime, Double> actualValues,
						   Map<LocalDateTime, Double> thresholdValues, Map<LocalDateTime, String> waterYearType, String valueUnits,
						   String standardUnits, int totalNumberOfViolations)
	{

		_title = title;
		_totalNumberOfViolations = totalNumberOfViolations;

		for(LocalDateTime time : times)
		{
			String waterYear = waterYearType.get(time);
			Double value = actualValues.get(time);
			Double standard = thresholdValues.get(time);

			Issue issue = new Issue(time, waterYear, value, valueUnits, standard, standardUnits);
			_issues.add(issue);
		}
	}

	int getTotalNumberOfViolations()
	{
		return _totalNumberOfViolations;
	}

	public String getTitle()
	{
		return _title;
	}

	public List<Issue> getIssues()
	{
		return _issues;
	}

	static class Issue
	{

		private final YearMonth _time;
		private final String _waterYearType;
		private final Double _value;
		private final String _valueUnits;
		private final Double _standard;
		private final String _standardUnits;

		Issue(LocalDateTime time, String waterYearType, Double value, String valueUnits, Double standard, String standardUnits)
		{

			_time = YearMonth.of(time.minusMonths(1).getYear(), time.minusMonths(1).getMonth());
			_waterYearType = waterYearType;
			_value = value;
			_valueUnits = valueUnits;
			_standard = standard;
			_standardUnits = standardUnits;
		}

		@Override
		public String toString()
		{

			String actualValue = "";
			if(_value != null)
			{
				if(Double.isNaN(_value))
				{
					actualValue = "NR";
				}
				else
				{
					actualValue = String.format("%.2f", _value) + " " + _valueUnits;
				}
			}
			String standardValue = "";
			if(_standard != null)
			{
				if(!actualValue.isEmpty())
				{
					standardValue += " | ";
				}
				if(Double.isNaN(_standard))
				{
					standardValue += "NR";
				}
				else
				{
					standardValue += String.format("%.2f", _standard) + " " + _standardUnits;
				}
			}
			String values = "";
			if(!actualValue.isEmpty())
			{
                values = " @ " + actualValue + " " + standardValue;
			}
			String formattedTime = _time.getMonth().getValue() + "/" + _time.getYear();
			return formattedTime + " " + _waterYearType + values;
		}
	}

}
