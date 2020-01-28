/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */

package gov.ca.water.quickresults.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.nio.file.Paths;
import javax.swing.*;

import gov.ca.water.calgui.EpptInitializationException;
import gov.ca.water.calgui.busservice.impl.EpptReportingMonths;
import gov.ca.water.calgui.busservice.impl.GuiLinksSeedDataSvcImpl;
import gov.ca.water.calgui.busservice.impl.ThresholdLinksSeedDataSvc;
import gov.ca.water.calgui.busservice.impl.WaterYearDefinitionSvc;
import gov.ca.water.calgui.techservice.impl.DialogSvcImpl;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Company: Resource Management Associates
 *
 * @author <a href="mailto:adam@rmanet.com">Adam Korynta</a>
 * @since 03-07-2019
 */
public abstract class EpptScaffold
{
	static
	{
		String userDir = Paths.get(System.getProperty("user.dir")).resolve(
				"target/test-classes").toAbsolutePath().toString();
		System.setProperty("user.dir", userDir);
	}

	abstract protected JComponent buildEpptPanel();

	public final void initScaffold() throws EpptInitializationException
	{

		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
		{
			fail(e);
		}
		GuiLinksSeedDataSvcImpl.createSeedDataSvcImplInstance();
		WaterYearDefinitionSvc.createSeedDataSvcImplInstance();
		ThresholdLinksSeedDataSvc.createSeedDataSvcImplInstance();
		EpptReportingMonths.createTrendReportingMonthsInstance();
		JComponent epptPanel = buildEpptPanel();
		JFrame jFrame = new JFrame();
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SwingUtilities.invokeLater(() ->
		{
			DialogSvcImpl.installMainFrame(jFrame);
			jFrame.setLayout(new BorderLayout());
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setViewportView(epptPanel);
			jFrame.add(scrollPane, BorderLayout.CENTER);
//			jFrame.add(buildUiManagerPanel(jFrame), BorderLayout.NORTH);
			jFrame.pack();
			jFrame.setVisible(true);
		});
	}

	private JPanel buildUiManagerPanel(JFrame jFrame)
	{
		JPanel panel = new JPanel();
		JComboBox<UIManager.LookAndFeelInfo> uiCombo = new JComboBox<>();
		UIManager.installLookAndFeel("SeaGlassLookAndFeel", "com.seaglasslookandfeel.SeaGlassLookAndFeel");
		UIManager.installLookAndFeel("SmartLookAndFeel", "com.jtattoo.plaf.smart.SmartLookAndFeel");
		UIManager.installLookAndFeel("AcrylLookAndFeel", "com.jtattoo.plaf.acryl.AcrylLookAndFeel");


		UIManager.LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
		for(UIManager.LookAndFeelInfo laf : installedLookAndFeels)
		{
			uiCombo.addItem(laf);
		}
		uiCombo.setSelectedItem(UIManager.getSystemLookAndFeelClassName());
		uiCombo.addActionListener(e ->
		{
			try
			{
				UIManager.LookAndFeelInfo selectedItem = (UIManager.LookAndFeelInfo) uiCombo.getSelectedItem();
				if(selectedItem != null)
				{
					UIManager.setLookAndFeel(selectedItem.getClassName());
					SwingUtilities.updateComponentTreeUI(jFrame);
					jFrame.pack();
				}
			}
			catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex)
			{
				fail(ex);
			}
		});
		panel.setLayout(new FlowLayout());
		panel.add(uiCombo);
		return panel;
	}

}
