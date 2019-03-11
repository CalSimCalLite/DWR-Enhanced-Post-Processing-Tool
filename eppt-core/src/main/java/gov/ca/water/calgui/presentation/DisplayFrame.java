/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */

package gov.ca.water.calgui.presentation;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;

import calsim.app.DerivedTimeSeries;
import calsim.app.MultipleTimeSeries;
import gov.ca.water.calgui.bo.RBListItemBO;
import gov.ca.water.calgui.bus_service.IDSSGrabber1Svc;
import gov.ca.water.calgui.bus_service.impl.DSSGrabber1SvcImpl;
import gov.ca.water.calgui.bus_service.impl.DSSGrabber2SvcImpl;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.presentation.display.BoxPlotChartPanel;
import gov.ca.water.calgui.presentation.display.BoxPlotChartPanel2;
import gov.ca.water.calgui.presentation.display.ChartPanel1;
import gov.ca.water.calgui.presentation.display.ChartPanel2;
import gov.ca.water.calgui.presentation.display.DefaultPlotHandler;
import gov.ca.water.calgui.presentation.display.MonthlyTablePanel;
import gov.ca.water.calgui.presentation.display.MonthlyTablePanel2;
import gov.ca.water.calgui.presentation.display.SummaryTablePanel;
import gov.ca.water.calgui.presentation.display.SummaryTablePanel2;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;
import org.apache.log4j.Logger;
import org.jfree.data.time.Month;

import hec.io.TimeSeriesContainer;

/**
 * DisplayFrame class provides a frame for showing charts.
 *
 * @author tslawecki
 */
public class DisplayFrame
{

	private static final Logger LOG = Logger.getLogger(DisplayFrame.class.getName());
	private static final IErrorHandlingSvc ERROR_HANDLING_SVC = new ErrorHandlingSvcImpl();
	private static PlotHandler _topComponentPlotHandler = new DefaultPlotHandler();

	/**
	 * showDisplayFrames method creates a frame showing multiple charts
	 * according to parameters.
	 *
	 * @param displayGroup
	 * @param scenarios
	 */
	public static void showDisplayFrames(String displayGroup, List<RBListItemBO> scenarios, Month startMonth,
										 Month endMonth)
	{
		List<JTabbedPane> tabbedPanes = new ArrayList<>();
		try
		{
			IDSSGrabber1Svc dssGrabber = new DSSGrabber1SvcImpl(scenarios);
			boolean doComparison = false;
			boolean doDifference = false;
			boolean doTimeSeries = false;
			boolean doBase = false;
			boolean doExceedance = false;
			boolean doBoxPlot = false;
			boolean isCFS = false;
			boolean doMonthlyTable = false;
			boolean doSummaryTable = false;
			String exceedMonths = "";
			String summaryTags = "";
			String names = "";
			String locations = "";
			String dateRange = "";
			String filename = "";

			String[] groupParts = displayGroup.split(";");
			String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov",
					"Dec"};

			for(final String groupPart : groupParts)
			{
				if("Base".equals(groupPart))
				{
					doBase = true;
				}
				if("Comp".equals(groupPart))
				{
					doComparison = true;
				}
				else if("Diff".equals(groupPart))
				{
					doDifference = true;
				}
				else if("TS".equals(groupPart))
				{
					doTimeSeries = true;
				}
				else if("BP".equals(groupPart))
				{
					doBoxPlot = true;
				}
				else if(groupPart.startsWith("EX-"))
				{
					doExceedance = true;
					exceedMonths = groupPart.substring(3);
				}
				else if("CFS".equals(groupPart))
				{
					isCFS = true;
				}
				else if("TAF".equals(groupPart))
				{
					isCFS = false;
				}
				else if("Monthly".equals(groupPart))
				{
					doMonthlyTable = true;
				}
				else if(groupPart.startsWith("ST-"))
				{
					doSummaryTable = true;
					summaryTags = groupPart.substring(4);
				}
				else if(groupPart.startsWith("Locs-"))
				{
					names = groupPart.substring(5);
				}
				else if(groupPart.startsWith("Index-"))
				{
					locations = groupPart.substring(6);
				}
				else if(groupPart.startsWith("File-"))
				{
					filename = groupPart.substring(5);
				}
				else
				{
					// Check to see if the groupPart parses as mmmyyyy-mmmyyy
					Pattern p = Pattern.compile("\\w\\w\\w\\d\\d\\d\\d-\\w\\w\\w\\d\\d\\d\\d");
					Matcher m = p.matcher(groupPart);
					if(m.find())
					{
						dateRange = groupPart;
					}
					else
					{
						LOG.debug("Unparsed display list component - " + groupPart);
					}
				}
			}

			dssGrabber.setIsCFS(isCFS);

			if(!filename.isEmpty())
			{
				dssGrabber.setBase(filename);
			}
			else
			{
				for(RBListItemBO item : scenarios)
				{
					if(item.isSelected())
					{
						dssGrabber.setBase(item.toString());
					}
				}
			}

			String[] locationNames = locations.split(",");
			String[] namesText = names.split(",");

			for(int i = 0; i < locationNames.length; i++)
			{
				String locationName = locationNames[i];
				if(locationName == null || locationName.isEmpty())
				{
					String message = "No Location selected.";
					ERROR_HANDLING_SVC.businessErrorHandler(message, message);
				}
				else
				{
					dssGrabber.setLocation(locationName);

					if(dssGrabber.getPrimaryDSSName() == null)
					{
						String message = "No GUI_Links3.csv entry found for " + namesText[i] + "/" + locationName + ".";
						ERROR_HANDLING_SVC.businessErrorHandler(message, message);
					}
					else if(dssGrabber.getPrimaryDSSName().isEmpty())
					{
						String message = "No DSS time series specified for " + namesText[i] + "/" + locationName + ".";
						ERROR_HANDLING_SVC.businessErrorHandler(message, message);
					}
					else
					{

						dssGrabber.setDateRange(dateRange);

						TimeSeriesContainer[] primaryResults = dssGrabber.getPrimarySeries(locationName);
						TimeSeriesContainer[] secondaryResults = dssGrabber.getSecondarySeries();

						dssGrabber.calcTAFforCFS(primaryResults, secondaryResults);

						TimeSeriesContainer[] diffResults = dssGrabber.getDifferenceSeries(primaryResults);
						TimeSeriesContainer[][] excResults = dssGrabber.getExceedanceSeries(primaryResults);
						TimeSeriesContainer[][] sexcResults = dssGrabber.getExceedanceSeries(secondaryResults);
						TimeSeriesContainer[][] dexcResults = dssGrabber.getExceedanceSeriesD(primaryResults);

						JTabbedPane tabbedpane = new JTabbedPane();

						if(doSummaryTable)
						{
							SummaryTablePanel stp;
							if(doDifference)
							{
								stp = new SummaryTablePanel(
										dssGrabber.getPlotTitle() + " - Difference from " + primaryResults[0].fileName,
										diffResults, null, summaryTags, "", dssGrabber);
							}
							else
							{
								stp = new SummaryTablePanel(dssGrabber.getPlotTitle(), primaryResults, secondaryResults,
										summaryTags, dssGrabber.getSLabel(), dssGrabber, doBase);
							}
							tabbedpane.insertTab("Summary - " + dssGrabber.getBase(), null, stp, null, 0);
						}

						if(doMonthlyTable)
						{
							MonthlyTablePanel mtp;
							if(doDifference)
							{
								mtp = new MonthlyTablePanel(
										dssGrabber.getPlotTitle() + " - Difference from " + primaryResults[0].fileName,
										diffResults, null, dssGrabber, "");
							}
							else
							{
								mtp = new MonthlyTablePanel(dssGrabber.getPlotTitle(), primaryResults, secondaryResults,
										dssGrabber, dssGrabber.getSLabel(), doBase);
							}
							tabbedpane.insertTab("Monthly - " + dssGrabber.getBase(), null, mtp, null, 0);
						}

						Date lower = new Date();
						lower.setTime(startMonth.getFirstMillisecond());

						Date upper = new Date();
						upper.setTime(endMonth.getLastMillisecond());

						ChartPanel1 cp3;
						if(doBoxPlot)
						{
							tabbedpane
									.insertTab("Box Plot", null,
											new BoxPlotChartPanel(dssGrabber.getPlotTitle(), dssGrabber.getYLabel(),
													primaryResults, null, lower, upper, dssGrabber.getSLabel(), doBase),
											null, 0);
						}
						if(doExceedance)
						{
							// Check if any monthly
							boolean plottedOne = false;
							// plots
							// were
							// done
							for(int m1 = 0; m1 < 12; m1++)
							{
								if(exceedMonths.contains(monthNames[m1]))
								{
									if(doDifference)
									{
										cp3 = new ChartPanel1(
												dssGrabber.getPlotTitle() + " - Exceedance (" + monthNames[m1] + ")"
														+ " - Difference from " + primaryResults[0].fileName,
												dssGrabber.getYLabel(), dexcResults[m1], null, true, upper, lower,
												dssGrabber.getSLabel());
									}
									else
									{
										cp3 = new ChartPanel1(
												dssGrabber.getPlotTitle() + " - Exceedance (" + monthNames[m1] + ")",
												dssGrabber.getYLabel(), excResults[m1],
												sexcResults == null ? null : sexcResults[m1], true, upper, lower,
												dssGrabber.getSLabel(), doBase);
									}
									plottedOne = true;
									tabbedpane.insertTab("Exceedance (" + monthNames[m1] + ")", null, cp3, null, 0);
								}
							}

							if(exceedMonths.contains("Annual"))
							{
								plottedOne = true;
								if("CFS".equals(dssGrabber.getOriginalUnits()))
								{
									if(doDifference)
									{
										cp3 = new ChartPanel1(
												dssGrabber.getPlotTitle() + " - Exceedance (annual total)"
														+ " - Difference from " + primaryResults[0].fileName,
												"Annual Total Volume (TAF)", dexcResults[12], null, true, upper, lower,
												dssGrabber.getSLabel());
									}
									else

									{
										cp3 = new ChartPanel1(
												dssGrabber.getPlotTitle() + " - Exceedance (Annual Total)",
												"Annual Total Volume (TAF)", excResults[12],
												sexcResults == null ? null : sexcResults[12], true, upper, lower,
												dssGrabber.getSLabel(), doBase);
									}
									tabbedpane.insertTab("Exceedance (annual total)", null, cp3, null, 0);
								}
								else
								{
									JPanel panel = new JPanel();
									panel.add(new JLabel("No chart - annual totals are only calculated for flows."));
									tabbedpane.insertTab("Exceedance (Annual Total)", null, panel, null, 0);
								}
								if(exceedMonths.contains("ALL") || !plottedOne)
								{
									if(doDifference)
									{
										cp3 = new ChartPanel1(
												dssGrabber.getPlotTitle() + " - Exceedance (all months)" + " - Difference from "
														+ primaryResults[0].fileName,
												dssGrabber.getYLabel(), dexcResults[13], null, true, upper, lower,
												dssGrabber.getSLabel());
									}
									else
									{
										cp3 = new ChartPanel1(dssGrabber.getPlotTitle() + " - Exceedance (all months)",
												dssGrabber.getYLabel(), excResults[13],
												sexcResults == null ? null : sexcResults[13], true, upper, lower,
												dssGrabber.getSLabel(), doBase);
									}
									tabbedpane.insertTab("Exceedance (all)", null, cp3, null, 0);
								}
							}
						}
						ChartPanel1 cp1;
						ChartPanel1 cp2;

						if(doTimeSeries)
						{
							if(locationName.contains(Constant.SCHEMATIC_PREFIX)
									&& dssGrabber.getPrimaryDSSName().contains(","))
							{
								cp2 = new ChartPanel1(Constant.SCHEMATIC_PREFIX + dssGrabber.getPlotTitle(),
										dssGrabber.getYLabel(), primaryResults, secondaryResults, false, upper, lower,
										String.join(",", dssGrabber.getPrimaryDSSName()), false);
								// abuse slabel to pass individual dataset names
								tabbedpane.insertTab("Time Series (experimental)", null, cp2, null, 0);

							}
							else if(doBase)
							{
								cp2 = new ChartPanel1(dssGrabber.getPlotTitle(), dssGrabber.getYLabel(), primaryResults,
										secondaryResults, false, upper, lower, dssGrabber.getSLabel(), doBase);
								tabbedpane.insertTab("Time Series", null, cp2, null, 0);

							}
							else if(primaryResults.length < 2)
							{
								JPanel panel = new JPanel();
								panel.add(new JLabel("No chart - need two or more time series."));
								tabbedpane.insertTab(doDifference ? "Difference" : "Comparison", null, panel, null, 0);
							}
							else
							{
								if(doDifference)
								{
									cp2 = new ChartPanel1(
											dssGrabber.getPlotTitle() + " - Difference from " + primaryResults[0].fileName,
											dssGrabber.getYLabel(), diffResults, null, false, upper, lower,
											dssGrabber.getSLabel());
									tabbedpane.insertTab("Difference", null, cp2, null, 0);
								}
								else if(doComparison)
								{
									cp1 = new ChartPanel1(dssGrabber.getPlotTitle() + " - Comparison ",
											dssGrabber.getYLabel(),
											primaryResults, secondaryResults, false, upper, lower,
											dssGrabber.getSLabel());
									tabbedpane.insertTab("Comparison", null, cp1, null, 0);
								}
							}
						}

						List<String> missing = dssGrabber.getMissingList();
						boolean showFrame = false;
						if(missing.isEmpty())
						{
							showFrame = true;
						}
						else if(!dssGrabber.getStopOnMissing())
						{
							StringBuilder buffer = new StringBuilder();
							buffer.append(
									"<html><br>Not all DSS records were found, some results may be missing:<br><br>");
							missing.forEach(id -> buffer.append(id).append("<br>"));
							buffer.append("</html>");
							JPanel panel = new JPanel();
							panel.setLayout(new BorderLayout());
							panel.add(new JLabel(buffer.toString()), BorderLayout.PAGE_START);
							//						tabbedpane.insertTab("Alert - Missing DSS records", null, panel, null, 0);
							showFrame = true;
						}
						if(showFrame)
						{
							tabbedpane.setName(namesText[i]);
							tabbedPanes.add(tabbedpane);
						}

					}
				}
			}
		}
		catch(RuntimeException e)
		{
			LOG.error(e.getMessage());
			String messageText = "Unable to display frame.";
			ERROR_HANDLING_SVC.businessErrorHandler(messageText, e);
		}
		_topComponentPlotHandler.openPlots(tabbedPanes);
	}


	/**
	 * Creates a frame to display DTS/MTS variables from WRIMS GUI
	 *
	 * @param displayGroup
	 * @param lstScenarios
	 * @param dts
	 * @param mts
	 */
	public static void showDisplayFramesWRIMS(String displayGroup,
											  List<RBListItemBO> lstScenarios,
											  DerivedTimeSeries dts,
											  MultipleTimeSeries mts,
											  Month startMonth, Month endMonth)
	{
		List<JTabbedPane> tabbedPanes = new ArrayList<>();
		try
		{
			DSSGrabber2SvcImpl dssGrabber = new DSSGrabber2SvcImpl(lstScenarios, dts, mts);
			boolean doComparison = false;
			boolean doDifference = false;
			boolean doTimeSeries = false;
			boolean doBase = false;
			boolean doExceedance = false;
			boolean doBoxPlot = false;
			boolean isCFS = false;
			boolean doMonthlyTable = false;
			boolean doSummaryTable = false;
			String exceedMonths = "";
			String summaryTags = "";
			String dateRange = "";
			String filename = "";

			String[] groupParts = displayGroup.split(";");
			String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov",
					"Dec"};

			for(final String groupPart : groupParts)
			{
				if("Base".equals(groupPart))
				{
					doBase = true;
				}
				if("Comp".equals(groupPart))
				{
					doComparison = true;
				}
				else if("Diff".equals(groupPart))
				{
					doDifference = true;
				}
				else if("TS".equals(groupPart))
				{
					doTimeSeries = true;
				}
				else if("BP".equals(groupPart))
				{
					doBoxPlot = true;
				}
				else if(groupPart.startsWith("EX-"))
				{
					doExceedance = true;
					exceedMonths = groupPart.substring(3);
				}
				else if("CFS".equals(groupPart))
				{
					isCFS = true;
				}
				else if("TAF".equals(groupPart))
				{
					isCFS = false;
				}
				else if("Monthly".equals(groupPart))
				{
					doMonthlyTable = true;
				}
				else if(groupPart.startsWith("ST-"))
				{
					doSummaryTable = true;
					summaryTags = groupPart.substring(4);
				}
				else if(groupPart.startsWith("File-"))
				{
					filename = groupPart.substring(5);
				}
				else
				{
					// Check to see if the groupPart parses as mmmyyyy-mmmyyy
					Pattern p = Pattern.compile("\\w\\w\\w\\d\\d\\d\\d-\\w\\w\\w\\d\\d\\d\\d");
					Matcher m = p.matcher(groupPart);
					if(m.find())
					{
						dateRange = groupPart;
					}
					else
					{
						LOG.debug("Unparsed display list component - " + groupPart);
					}
				}
			}

			JTabbedPane tabbedpane = new JTabbedPane();

			dssGrabber.setIsCFS(isCFS);

			if(!filename.isEmpty())
			{
				dssGrabber.setBase(filename);
			}
			else
			{
				for(int i = 0; i < lstScenarios.size(); i++)
				{
					RBListItemBO item = lstScenarios.get(i);
					if(item.isSelected())
					{
						dssGrabber.setBase(item.toString());
					}
				}
			}

			dssGrabber.setDateRange(dateRange);

			Date lower = new Date();
			lower.setTime(startMonth.getFirstMillisecond());

			Date upper = new Date();
			upper.setTime(endMonth.getLastMillisecond());

			if(mts != null)
			{

				// Handle MTS

				dssGrabber.setLocation("@@" + mts.getName());

				int n = mts.getNumberOfDataReferences();
				int s = lstScenarios.size();

				TimeSeriesContainer[][] results = new TimeSeriesContainer[n][s];
				for(int i = 0; i < n; i++)
				{
					results[i] = dssGrabber.getMultipleTimeSeries(i);
				}

				dssGrabber.calcTAFforCFS(results);

				TimeSeriesContainer[][] diffResults = dssGrabber.getDifferenceSeriesWithMultipleTimeSeries(results);
				TimeSeriesContainer[][][] excResults = dssGrabber.getExceedanceSeriesWithMultipleTimeSeries(results);
				TimeSeriesContainer[][][] dexcResults = dssGrabber.getExceedanceSeriesDWithMultipleTimeSeries(results);

				if(doSummaryTable)
				{
					SummaryTablePanel2 stp;
					if(doDifference)
					{
						stp = new SummaryTablePanel2(
								dssGrabber.getPlotTitle() + " - Difference from " + results[0][0].fileName, diffResults,
								null, summaryTags, "", null, dssGrabber, doBase, mts);
					}
					else
					{
						stp = new SummaryTablePanel2(dssGrabber.getPlotTitle(), results, null, summaryTags,
								dssGrabber.getSLabel(), null, dssGrabber, doBase, mts);
					}
					tabbedpane.insertTab("Summary - " + dssGrabber.getBase(), null, stp, null, 0);
				}

				if(doMonthlyTable)
				{
					MonthlyTablePanel2 mtp;
					if(doDifference)
					{
						mtp = new MonthlyTablePanel2(
								dssGrabber.getPlotTitle() + " - Difference from " + results[0][0].fileName, diffResults,
								dssGrabber, "", doBase, mts);
					}
					else
					{
						mtp = new MonthlyTablePanel2(dssGrabber.getPlotTitle(), results, dssGrabber,
								dssGrabber.getSLabel(),
								doBase, mts);
					}
					tabbedpane.insertTab("Monthly - " + dssGrabber.getBase(), null, mtp, null, 0);
				}

				if(doBoxPlot)
				{
					tabbedpane.insertTab("Box Plot", null, new BoxPlotChartPanel2(dssGrabber.getPlotTitle(),
									dssGrabber.getYLabel(), results, null, lower, upper, dssGrabber.getSLabel(), doBase, mts),
							null, 0);
				}

				ChartPanel2 cp3;
				if(doExceedance)
				{
					// Check if any monthly plots
					boolean plottedOne = false;
					// were
					// done

					for(int m1 = 0; m1 < 12; m1++)
					{
						if(exceedMonths.contains(monthNames[m1]))
						{
							if(doDifference)
							{
								cp3 = new ChartPanel2(
										dssGrabber.getPlotTitle() + " - Exceedance (" + monthNames[m1] + ")"
												+ " - Difference from " + results[0][0].fileName,
										dssGrabber.getYLabel(), dexcResults[m1], true, upper, lower, doBase, mts);
							}
							else
							{
								cp3 = new ChartPanel2(
										dssGrabber.getPlotTitle() + " - Exceedance (" + monthNames[m1] + ")",
										dssGrabber.getYLabel(), excResults[m1], true, upper, lower, doBase, mts);
							}
							plottedOne = true;
							tabbedpane.insertTab("Exceedance (" + monthNames[m1] + ")", null, cp3, null, 0);
						}
					}
					if(exceedMonths.contains("ALL") || !plottedOne)
					{
						if(doDifference)
						{
							cp3 = new ChartPanel2(
									dssGrabber.getPlotTitle() + " - Exceedance (all months)" + " - Difference from "
											+ results[0][0].fileName,
									dssGrabber.getYLabel(), dexcResults[13], true, upper, lower, mts);
						}
						else
						{
							cp3 = new ChartPanel2(dssGrabber.getPlotTitle() + " - Exceedance (all months)",
									dssGrabber.getYLabel(), excResults[13], true, upper, lower, mts);
						}
						tabbedpane.insertTab("Exceedance (all)", null, cp3, null, 0);
					}
					if(exceedMonths.contains("Annual"))
					{
						if("CFS".equals(dssGrabber.getOriginalUnits()))
						{
							if(doDifference)
							{
								cp3 = new ChartPanel2(
										dssGrabber.getPlotTitle() + " - Exceedance (annual total)" + " - Difference from "
												+ results[0][0].fileName,
										"Annual Total Volume (TAF)", dexcResults[12], true, upper, lower, mts);
							}
							else

							{
								cp3 = new ChartPanel2(dssGrabber.getPlotTitle() + " - Exceedance (Annual Total)",
										"Annual Total Volume (TAF)", excResults[12], true, upper, lower, doBase, mts);
							}
							tabbedpane.insertTab("Exceedance (annual total)", null, cp3, null, 0);
						}
						else
						{
							JPanel panel = new JPanel();
							panel.add(new JLabel("No chart - annual totals are only calculated for flows."));
							tabbedpane.insertTab("Exceedance (Annual Total)", null, panel, null, 0);
						}
					}
				}

				ChartPanel2 cp1;
				ChartPanel2 cp2;

				if(doTimeSeries)
				{

					if(doBase)
					{
						cp2 = new ChartPanel2(dssGrabber.getPlotTitle(), dssGrabber.getYLabel(), results, false, upper,
								lower, doBase, mts);
						tabbedpane.insertTab("Time Series", null, cp2, null, 0);

					}
					else if(results[0].length < 2)
					{
						JPanel panel = new JPanel();
						panel.add(new JLabel("No chart - need two or more time series."));
						tabbedpane.insertTab(doDifference ? "Difference" : "Comparison", null, panel, null, 0);
					}
					else
					{
						if(doDifference)
						{
							cp2 = new ChartPanel2(
									dssGrabber.getPlotTitle() + " - Difference from " + results[0][0].fileName,
									dssGrabber.getYLabel(), diffResults, false, upper, lower, mts);
							tabbedpane.insertTab("Difference", null, cp2, null, 0);
						}
						else if(doComparison)
						{
							cp1 = new ChartPanel2(dssGrabber.getPlotTitle() + " - Comparison ", dssGrabber.getYLabel(),
									results, false, upper, lower, mts);
							tabbedpane.insertTab("Comparison", null, cp1, null, 0);
						}
					}
				}

			}
			else
			{

				// Handle DTS

				dssGrabber.setLocation("@@" + dts.getName());

				TimeSeriesContainer[] primaryResults = dssGrabber.getPrimarySeries("DUMMY");
				TimeSeriesContainer[] secondaryResults = dssGrabber.getSecondarySeries();

				dssGrabber.calcTAFforCFS(primaryResults, secondaryResults);

				TimeSeriesContainer[] diffResults = dssGrabber.getDifferenceSeries(primaryResults);
				TimeSeriesContainer[][] excResults = dssGrabber.getExceedanceSeries(primaryResults);
				TimeSeriesContainer[][] sexcResults = dssGrabber.getExceedanceSeries(secondaryResults);
				TimeSeriesContainer[][] dexcResults = dssGrabber.getExceedanceSeriesD(primaryResults);

				if(doSummaryTable)
				{
					SummaryTablePanel stp;
					if(doDifference)
					{
						stp = new SummaryTablePanel(
								dssGrabber.getPlotTitle() + " - Difference from " + primaryResults[0].fileName,
								diffResults, null, summaryTags, "", dssGrabber);
					}
					else
					{
						stp = new SummaryTablePanel(dssGrabber.getPlotTitle(), primaryResults, secondaryResults,
								summaryTags, dssGrabber.getSLabel(), dssGrabber, doBase);
					}
					tabbedpane.insertTab("Summary - " + dssGrabber.getBase(), null, stp, null, 0);
				}

				if(doMonthlyTable)
				{
					MonthlyTablePanel mtp;
					if(doDifference)
					{
						mtp = new MonthlyTablePanel(
								dssGrabber.getPlotTitle() + " - Difference from " + primaryResults[0].fileName,
								diffResults, null, dssGrabber, "");
					}
					else
					{
						mtp = new MonthlyTablePanel(dssGrabber.getPlotTitle(), primaryResults, secondaryResults,
								dssGrabber, dssGrabber.getSLabel(), doBase);
					}
					tabbedpane.insertTab("Monthly - " + dssGrabber.getBase(), null, mtp, null, 0);
				}

				if(doBoxPlot)
				{
					tabbedpane
							.insertTab("Box Plot", null,
									new BoxPlotChartPanel(dssGrabber.getPlotTitle(), dssGrabber.getYLabel(),
											primaryResults, null, lower, upper, dssGrabber.getSLabel(), doBase),
									null, 0);
				}
				ChartPanel1 cp3;
				if(doExceedance)
				{
					// Check if any monthly plots
					// were
					// done
					boolean plottedOne = false;
					for(int m1 = 0; m1 < 12; m1++)
					{
						if(exceedMonths.contains(monthNames[m1]))
						{
							if(doDifference)
							{
								cp3 = new ChartPanel1(
										dssGrabber.getPlotTitle() + " - Exceedance (" + monthNames[m1] + ")"
												+ " - Difference from " + primaryResults[0].fileName,
										dssGrabber.getYLabel(), dexcResults[m1], null, true, upper, lower,
										dssGrabber.getSLabel());
							}
							else
							{
								cp3 = new ChartPanel1(
										dssGrabber.getPlotTitle() + " - Exceedance (" + monthNames[m1] + ")",
										dssGrabber.getYLabel(), excResults[m1],
										sexcResults == null ? null : sexcResults[m1], true, upper, lower,
										dssGrabber.getSLabel(), doBase);
							}
							plottedOne = true;
							tabbedpane.insertTab("Exceedance (" + monthNames[m1] + ")", null, cp3, null, 0);
						}
					}
					if(exceedMonths.contains("ALL") || !plottedOne)
					{
						if(doDifference)
						{
							cp3 = new ChartPanel1(
									dssGrabber.getPlotTitle() + " - Exceedance (all months)" + " - Difference from "
											+ primaryResults[0].fileName,
									dssGrabber.getYLabel(), dexcResults[13], null, true, upper, lower,
									dssGrabber.getSLabel());
						}
						else
						{
							cp3 = new ChartPanel1(dssGrabber.getPlotTitle() + " - Exceedance (all months)",
									dssGrabber.getYLabel(), excResults[13],
									sexcResults == null ? null : sexcResults[13], true, upper, lower,
									dssGrabber.getSLabel(), doBase);
						}
						tabbedpane.insertTab("Exceedance (all)", null, cp3, null, 0);
					}
					if(exceedMonths.contains("Annual"))
					{
						if("CFS".equals(dssGrabber.getOriginalUnits()))
						{
							if(doDifference)
							{
								cp3 = new ChartPanel1(
										dssGrabber.getPlotTitle() + " - Exceedance (annual total)" + " - Difference from "
												+ primaryResults[0].fileName,
										"Annual Total Volume (TAF)", dexcResults[12], null, true, upper, lower,
										dssGrabber.getSLabel());
							}
							else

							{
								cp3 = new ChartPanel1(dssGrabber.getPlotTitle() + " - Exceedance (Annual Total)",
										"Annual Total Volume (TAF)", excResults[12],
										sexcResults == null ? null : sexcResults[12], true, upper, lower,
										dssGrabber.getSLabel(), doBase);
							}
							tabbedpane.insertTab("Exceedance (annual total)", null, cp3, null, 0);
						}
						else
						{
							JPanel panel = new JPanel();
							panel.add(new JLabel("No chart - annual totals are only calculated for flows."));
							tabbedpane.insertTab("Exceedance (Annual Total)", null, panel, null, 0);
						}
					}
				}

				ChartPanel1 cp1;
				ChartPanel1 cp2;

				if(doTimeSeries)
				{

					if(doBase)
					{
						cp2 = new ChartPanel1(dssGrabber.getPlotTitle(), dssGrabber.getYLabel(), primaryResults,
								secondaryResults, false, upper, lower, dssGrabber.getSLabel(), doBase);
						tabbedpane.insertTab("Time Series", null, cp2, null, 0);

					}
					else if(primaryResults.length < 2)
					{
						JPanel panel = new JPanel();
						panel.add(new JLabel("No chart - need two or more time series."));
						tabbedpane.insertTab(doDifference ? "Difference" : "Comparison", null, panel, null, 0);
					}
					else
					{
						if(doDifference)
						{
							cp2 = new ChartPanel1(
									dssGrabber.getPlotTitle() + " - Difference from " + primaryResults[0].fileName,
									dssGrabber.getYLabel(), diffResults, null, false, upper, lower,
									dssGrabber.getSLabel());
							tabbedpane.insertTab("Difference", null, cp2, null, 0);
						}
						else if(doComparison)
						{
							cp1 = new ChartPanel1(dssGrabber.getPlotTitle() + " - Comparison ", dssGrabber.getYLabel(),
									primaryResults, secondaryResults, false, upper, lower, dssGrabber.getSLabel());
							tabbedpane.insertTab("Comparison", null, cp1, null, 0);
						}
					}
				}
			}
			List<String> missing = dssGrabber.getMissingList();
			boolean showFrame = false;
			if(missing.isEmpty())
			{
				showFrame = true;
			}
			else if(dssGrabber.getStopOnMissing())
			{
				showFrame = false;
			}
			else
			{
				StringBuilder buffer = new StringBuilder();
				buffer.append("<html><br>Not all DSS records were found, some results may be missing:<br><br>");
				missing.forEach(id -> buffer.append(id).append("<br>"));
				buffer.append("</html>");
				JPanel panel = new JPanel();
				panel.setLayout(new BorderLayout());
				panel.add(new JLabel(buffer.toString()), BorderLayout.PAGE_START);
				tabbedpane.insertTab("Alert - Missing DSS records", null, panel, null, 0);
				showFrame = true;
			}
			if(showFrame)
			{
				tabbedpane.setName("WRIMS GUI");
				tabbedPanes.add(tabbedpane);
			}
		}
		catch(HeadlessException e)
		{
			LOG.error(e.getMessage());
			String messageText = "Unable to display frame.";
			ERROR_HANDLING_SVC.businessErrorHandler(messageText, e);
		}
		_topComponentPlotHandler.openPlots(tabbedPanes);
	}

	public static void installPlotHandler(PlotHandler topComponentPlotHandler)
	{
		_topComponentPlotHandler = topComponentPlotHandler;
	}

	public interface PlotHandler
	{
		void openPlots(List<JTabbedPane> tabbedPanes);
	}
}
