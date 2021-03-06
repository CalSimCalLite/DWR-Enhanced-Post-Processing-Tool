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

package gov.ca.water.quickresults.ui.report;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.constant.EpptPreferences;
import gov.ca.water.calgui.project.EpptScenarioRun;
import gov.ca.water.calgui.wresl.ProcessOutputConsumer;
import gov.ca.water.reportengine.EPPTReport;
import gov.ca.water.reportengine.QAQCReportException;
import gov.ca.water.reportengine.ReportParameters;
import gov.ca.water.reportengine.standardsummary.StandardSummaryErrors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.stream.Collectors.toList;

/**
 * Company: Resource Management Associates
 *
 * @author <a href="mailto:adam@rmanet.com">Adam Korynta</a>
 * @since 05-14-2019
 */
class QAQCReportGenerator
{
	private final ProcessOutputConsumer _consumer;

	QAQCReportGenerator(ProcessOutputConsumer consumer)
	{
		_consumer = consumer;
	}

	void generateQAQCReport(EpptScenarioRun baseRun, EpptScenarioRun altRun, ReportParameters reportParameters, Path outputPdf)
			throws QAQCReportException
	{
		StandardSummaryErrors standardSummaryErrors = new StandardSummaryErrors();
		Path path = writeReportData(baseRun, altRun, reportParameters, standardSummaryErrors);
		QAQCProcessRunner processRunner = new QAQCProcessRunner(outputPdf, path, _consumer);
		processRunner.run();
		standardSummaryErrors.log();
	}

	private Path writeReportData(EpptScenarioRun baseRun, EpptScenarioRun altRun, ReportParameters reportParameters,
								 StandardSummaryErrors standardSummaryErrors)
			throws QAQCReportException
	{
		try
		{
			Path lastProjectConfiguration = EpptPreferences.getLastProjectConfiguration();
			Path reports = lastProjectConfiguration.getParent().resolve("Reports");
			if(!reports.toFile().exists() ||
					!reports.resolve("QAQC_Report.jrxml").toFile().exists())
			{
				QaQcFileUtils.copyJasperPaths();
			}
			List<EpptScenarioRun> altRuns = Collections.emptyList();
			if(altRun != null)
			{
				altRuns = Collections.singletonList(altRun);
			}
			Path dataFile = reports.resolve("DWR_QA_QC_Reports").resolve("Datasource").resolve("EPPT_Data.xml");
			EPPTReport epptReport = new EPPTReport(dataFile,
					baseRun, altRuns, reportParameters, standardSummaryErrors);
			epptReport.writeReport();
			return reports.resolve("QAQC_Report.jrxml");
		}
		catch(IOException | RuntimeException ex)
		{
			throw new QAQCReportException("Unable to copy Jasper Report files", ex);
		}
	}


}
