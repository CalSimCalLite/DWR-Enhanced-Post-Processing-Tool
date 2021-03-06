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

package gov.ca.water.calgui.busservice.impl;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.LongStream;

import gov.ca.water.calgui.EpptInitializationException;
import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.project.EpptConfigurationController;
import gov.ca.water.calgui.techservice.IFileSystemSvc;
import gov.ca.water.calgui.techservice.impl.FilePredicates;
import gov.ca.water.calgui.techservice.impl.FileSystemSvcImpl;
import org.apache.log4j.Logger;

import static gov.ca.water.calgui.constant.Constant.CONFIG_DIR;
import static gov.ca.water.calgui.constant.Constant.CSV_EXT;
import static java.util.stream.Collectors.toList;

/**
 * Company: Resource Management Associates
 *
 * @author <a href="mailto:adam@rmanet.com">Adam Korynta</a>
 * @since 07-25-2019
 */
public final class EpptReportingMonths
{
	private static final Map<Integer, MonthPeriod> MONTH_PERIODS = new TreeMap<>();
	private static final Logger LOG = Logger.getLogger(EpptReportingMonths.class.getName());
	private static final String MONTH_PERIODS_FILENAME = CONFIG_DIR + "MonthlyPeriodFilters" + CSV_EXT;
	private static EpptReportingMonths seedDataSvc;

	private EpptReportingMonths() throws EpptInitializationException
	{
		LOG.debug("Building SeedDataSvcImpl Object.");
		initMonthPeriods();
	}

	public static EpptReportingMonths getInstance()
	{
		if(seedDataSvc == null)
		{
			throw new IllegalArgumentException();
		}
		else
		{
			return seedDataSvc;
		}
	}

	public static void createTrendReportingMonthsInstance() throws EpptInitializationException
	{
		if(seedDataSvc == null)
		{
			seedDataSvc = new EpptReportingMonths();
		}
	}

	private void initMonthPeriods() throws EpptInitializationException
	{
		IFileSystemSvc fileSystemSvc = new FileSystemSvcImpl();
		try
		{
			List<String> monthPeriodStrings = fileSystemSvc.getFileData(Paths.get(MONTH_PERIODS_FILENAME), true,
					FilePredicates.commentFilter());
			for(String guiLinkString : monthPeriodStrings)
			{
				String[] list = guiLinkString.split(Constant.DELIMITER);
				Month start = null;
				Month end = null;
				int index = 0;
				if(list.length > 0)
				{
					index = Integer.parseInt(list[0]);
				}
				if(list.length > 1)
				{
					start = Month.valueOf(list[1].toUpperCase());
				}
				if(list.length > 2)
				{
					end = Month.valueOf(list[2].toUpperCase());
				}
				MONTH_PERIODS.put(index, new MonthPeriod("", start, end));
			}
		}
		catch(CalLiteGUIException | RuntimeException ex)
		{
			throw new EpptInitializationException("Failed to get file data for file: " + MONTH_PERIODS_FILENAME, ex);
		}
	}

	public List<MonthPeriod> getAllMonthPeriods()
	{
		return new ArrayList<>(MONTH_PERIODS.values());
	}

	static List<Month> getMonths(MonthPeriod monthPeriod)
	{
		if(monthPeriod.getStart() == null || monthPeriod.getEnd() == null)
		{
			return new ArrayList<>();
		}
		long between = ChronoUnit.MONTHS.between(LocalDate.now().withDayOfMonth(1).withMonth(monthPeriod.getStart().getValue()),
				LocalDate.now().withDayOfMonth(1).withMonth(monthPeriod.getEnd().getValue()));
		if(between < 0)
		{
			between += 12;
		}
		return LongStream.iterate(0, i -> ++i)
						 .limit(between + 1)
						 .mapToObj(monthPeriod.getStart()::plus)
						 .collect(toList());
	}
}
