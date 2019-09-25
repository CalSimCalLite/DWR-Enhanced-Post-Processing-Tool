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

import java.awt.BorderLayout;
import java.awt.Color;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.*;

import gov.ca.water.calgui.EpptInitializationException;
import gov.ca.water.calgui.bo.GUILinksAllModelsBO;
import gov.ca.water.calgui.busservice.impl.GuiLinksSeedDataSvcImpl;
import gov.ca.water.calgui.busservice.impl.ThresholdLinksSeedDataSvc;
import gov.ca.water.calgui.busservice.impl.WaterYearDefinitionSvc;
import gov.ca.water.calgui.project.EpptDssContainer;
import gov.ca.water.calgui.project.EpptScenarioRun;
import gov.ca.water.calgui.project.NamedDssPath;
import org.junit.jupiter.api.Assertions;

import hec.heclib.dss.HecDSSFileAccess;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Company: Resource Management Associates
 *
 * @author <a href="mailto:adam@rmanet.com">Adam Korynta</a>
 * @since 05-21-2019
 */
public class TrendReportScaffold
{
	public final void initScaffold() throws EpptInitializationException
	{
		HecDSSFileAccess.setMessageLevel(HecDSSFileAccess.MESS_LEVEL_CRITICAL);
		Path target = Paths.get(System.getProperty("user.dir")).resolve("target").resolve("test-classes");
		System.setProperty("user.dir", target.toString());
		WaterYearDefinitionSvc.createSeedDataSvcImplInstance();
		GuiLinksSeedDataSvcImpl.createSeedDataSvcImplInstance();
		ThresholdLinksSeedDataSvc.createSeedDataSvcImplInstance();
		EpptReportingMonths.createTrendReportingMonthsInstance();

		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
		{
			Assertions.fail(e);
		}
		GuiLinksSeedDataSvcImpl.createSeedDataSvcImplInstance();
		TrendReportPanel epptPanel = new TrendReportPanel();
		NamedDssPath namedDssPath = new NamedDssPath(
				Paths.get("J:\\DWR\\QA_QC\\SupportingDocs040219\\EPPTSupportingDoc040219\\SampleDSS_V1.01\\Inputs\\SampleDV_Base.dss"), "test",
				"CALSIM", "1MON", "2020D09E");
		EpptDssContainer dssContainer = new EpptDssContainer(namedDssPath,
				namedDssPath,
				namedDssPath,
				namedDssPath,
				Collections.emptyList());
		EpptScenarioRun baseRun = new EpptScenarioRun("Base", "desc", GUILinksAllModelsBO.Model.findModel("CalSim2"),
				Paths.get(""), Paths.get(""), Paths.get("C:\\Git\\DWR\\EPPT\\DWR-Enhanced-Post-Processing-Tool\\eppt-trend-reporting\\src\\test\\resources\\dwr_eppt\\wresl\\lookup\\wytypes.table"), dssContainer, javafx.scene.paint.Color.PINK);
		epptPanel.setScenarioRuns(baseRun, new ArrayList<>());
		JFrame jFrame = new JFrame();
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SwingUtilities.invokeLater(() ->
		{
			jFrame.setLayout(new BorderLayout());
			jFrame.add(epptPanel, BorderLayout.CENTER);
			jFrame.setSize(1600,700);
			jFrame.setBackground(Color.WHITE);
			jFrame.setVisible(true);
		});
	}

	public static void main(String[] args) throws EpptInitializationException
	{
		new TrendReportScaffold().initScaffold();
	}
}
