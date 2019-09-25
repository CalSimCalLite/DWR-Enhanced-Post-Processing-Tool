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


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.xml.parsers.ParserConfigurationException;

import gov.ca.water.calgui.EpptInitializationException;
import gov.ca.water.calgui.bo.GUILinksAllModelsBO;
import gov.ca.water.calgui.bo.WaterYearDefinition;
import gov.ca.water.calgui.bo.WaterYearIndex;
import gov.ca.water.calgui.busservice.IGuiLinksSeedDataSvc;
import gov.ca.water.calgui.busservice.impl.GuiLinksSeedDataSvcImpl;
import gov.ca.water.calgui.busservice.impl.WaterYearDefinitionSvc;
import gov.ca.water.calgui.busservice.impl.WaterYearTableReader;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.project.EpptScenarioRun;
import gov.ca.water.quickresults.ui.projectconfig.ProjectConfigurationPanel;
import gov.ca.water.trendreporting.monthpicker.FXCalendar;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import static java.util.stream.Collectors.toList;

/**
 * Company: Resource Management Associates
 *
 * @author <a href="mailto:adam@rmanet.com">Adam Korynta</a>
 * @since 05-21-2019
 */
public class TrendReportPanel extends JFXPanel
{
	private static final Logger LOGGER = Logger.getLogger(TrendReportPanel.class.getName());

	private final List<EpptScenarioRun> _scenarioRuns = new ArrayList<>();
	private final ToggleGroup _toggleGroup = new ToggleGroup();
	private TrendReportFlowPane _javascriptPane;
	private ListView<GUILinksAllModelsBO> _parameterListView;
	private ListView<TrendStatistics> _statisticsListView;
	private ListView<EpptReportingMonths.MonthPeriod> _seasonalPeriodListView;
	private CheckBox _tafCheckbox;
	private ComboBox<WaterYearIndex> _waterYearIndexComboBox;
	private ComboBox<WaterYearDefinition> _waterYearDefinitionComboBox;
	private FXCalendar _startCalendar;
	private FXCalendar _endCalendar;
	private ProgressBar _progressBar;

	public TrendReportPanel()
	{
		setBackground(java.awt.Color.WHITE);
		Platform.setImplicitExit(false);
		Platform.runLater(this::init);
	}

	private void init()
	{
		_javascriptPane = new TrendReportFlowPane();
		_parameterListView = new ListView<>();
		_statisticsListView = new ListView<>();
		_seasonalPeriodListView = new ListView<>();

		_parameterListView.setStyle("-fx-selection-bar:-fx-focus-color ;-fx-selection-bar-non-focused: -fx-focus-color ;");
		_statisticsListView.setStyle("-fx-selection-bar:-fx-focus-color ;-fx-selection-bar-non-focused: -fx-focus-color ;");
		_seasonalPeriodListView.setStyle("-fx-selection-bar:-fx-focus-color ;-fx-selection-bar-non-focused: -fx-focus-color ;");

		_tafCheckbox = new CheckBox("CFS -> TAF/mon");
		_waterYearIndexComboBox = new ComboBox<>();
		_waterYearDefinitionComboBox = new ComboBox<>();
		fillWaterYearDefinitionCombo();

		Insets insets = new Insets(10);
		BorderPane mainPane = new BorderPane();
		mainPane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
		BorderPane center = buildJavascriptPane();
		BorderPane.setMargin(center, insets);
		mainPane.setCenter(center);
		Pane top = buildParameterControls();
		BorderPane.setMargin(top, insets);
		mainPane.setTop(top);
		Node bottom = buildProgressControls();
		BorderPane.setMargin(bottom, insets);
		mainPane.setBottom(bottom);
		Scene scene = new Scene(mainPane);
		setScene(scene);
		try
		{
			ProjectConfigurationPanel.getProjectConfigurationPanel().addScenarioChangedListener(this::setScenarioRuns);
		}
		catch(RuntimeException e)
		{
			LOGGER.log(Level.SEVERE, "Error listening to scenario configuration changes", e);
		}
	}

	private Node buildProgressControls()
	{
		FlowPane flowPane = new FlowPane(Orientation.HORIZONTAL);
		_progressBar = new ProgressBar();
		_progressBar.setVisible(false);
		flowPane.getChildren().add(_progressBar);
		flowPane.setAlignment(Pos.BOTTOM_RIGHT);
		return flowPane;
	}

	private Pane buildParameterControls()
	{
		FlowPane flowPane = new FlowPane(Orientation.HORIZONTAL);
		flowPane.alignmentProperty().set(Pos.CENTER);
		TilePane tilePane = new TilePane(Orientation.HORIZONTAL, 10.0, 5.0);
		tilePane.alignmentProperty().set(Pos.CENTER);
		tilePane.setMinWidth(800);
		tilePane.getChildren().addAll(buildParameterListView(), buildStatisticsListView(), buildSeasonalPeriodListView(), buildTimeWindowControls());
		flowPane.getChildren().addAll(tilePane);
		return flowPane;
	}

	private GridPane buildTimeWindowControls()
	{
		_startCalendar = new FXCalendar();
		_startCalendar.setSelectedMonth(Month.OCTOBER.getValue());
		_startCalendar.setSelectedYear(1921);
		_endCalendar = new FXCalendar();
		_endCalendar.setSelectedMonth(Month.SEPTEMBER.getValue());
		_endCalendar.setSelectedYear(2007);
		GridPane gridPane = new GridPane();
		gridPane.add(new Label("Start: "), 0, 0);
		gridPane.add(_startCalendar, 1, 0);
		gridPane.add(new Label("End: "), 0, 1);
		gridPane.add(_endCalendar, 1, 1);
		gridPane.add(new Label("Water Year Index: "), 0, 2);
		gridPane.add(_waterYearIndexComboBox, 1, 2);
		gridPane.add(new Label("Water Year Definition: "), 0, 3);
		gridPane.add(_waterYearDefinitionComboBox, 1, 3);
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setPrefHeight(125);
		_waterYearIndexComboBox.setMaxWidth(Double.MAX_VALUE);
		_waterYearDefinitionComboBox.setMaxWidth(Double.MAX_VALUE);
		_tafCheckbox.selectedProperty().addListener(this::inputsChanged);
		_startCalendar.selectedMonthProperty().addListener(this::inputsChanged);
		_startCalendar.selectedYearProperty().addListener(this::inputsChanged);
		_endCalendar.selectedMonthProperty().addListener(this::inputsChanged);
		_endCalendar.selectedYearProperty().addListener(this::inputsChanged);
		return gridPane;
	}

	private Pane buildParameterListView()
	{
		IGuiLinksSeedDataSvc seedDataSvcImplInstance = GuiLinksSeedDataSvcImpl.getSeedDataSvcImplInstance();
		_parameterListView.getItems().addAll(seedDataSvcImplInstance.getAllGuiLinks());
		_parameterListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		_parameterListView.getSelectionModel().select(0);
		_parameterListView.getSelectionModel().selectedItemProperty().addListener(this::inputsChanged);
		_parameterListView.setPrefHeight(125);
		_parameterListView.setPrefWidth(250);
		_parameterListView.setCellFactory(param -> new ListCell<GUILinksAllModelsBO>()
		{
			@Override
			protected void updateItem(GUILinksAllModelsBO item, boolean empty)
			{
				super.updateItem(item, empty);

				if(empty || item == null || item.getPlotTitle() == null)
				{
					setText(null);
				}
				else
				{
					setText(item.getPlotTitle());
				}
			}
		});
		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(_parameterListView);
		borderPane.setTop(new Label("Parameter"));
		return borderPane;
	}

	private Pane buildStatisticsListView()
	{
		List<TrendStatistics> statistics = getTrendStatistics();
		_statisticsListView.getItems().addAll(statistics);
		_statisticsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		_statisticsListView.getSelectionModel().select(0);
		_statisticsListView.getSelectionModel().selectedItemProperty().addListener(this::inputsChanged);
		_statisticsListView.setCellFactory(param -> new ListCell<TrendStatistics>()
		{
			@Override
			protected void updateItem(TrendStatistics item, boolean empty)
			{
				super.updateItem(item, empty);

				if(empty || item == null || item.getName() == null)
				{
					setText(null);
				}
				else
				{
					setText(item.getName());
				}
			}
		});
		_statisticsListView.setPrefHeight(125);
		_statisticsListView.setPrefWidth(225);
		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(_statisticsListView);
		borderPane.setTop(new Label("Statistic"));
		return borderPane;
	}

	private List<TrendStatistics> getTrendStatistics()
	{
		List<TrendStatistics> retval = new ArrayList<>();
		Path jython = Paths.get(Constant.TREND_REPORTING_DIR).resolve("jython");
		try(Stream<Path> stream = Files.walk(jython, 5))
		{
			retval = stream.filter(p -> p.toFile().isFile()).filter(p -> p.toString().endsWith("py"))
						   .map(TrendStatistics::new)
						   .collect(toList());
		}
		catch(IOException e)
		{
			LOGGER.log(Level.SEVERE, "Unable to load Statistics for Trend Reporting dashboard", e);
		}
		return retval;
	}

	private Pane buildSeasonalPeriodListView()
	{
		List<EpptReportingMonths.MonthPeriod> allMonthPeriods = EpptReportingMonths.getAllMonthPeriods();
		_seasonalPeriodListView.getItems().addAll(allMonthPeriods);
		_seasonalPeriodListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		_seasonalPeriodListView.getSelectionModel().select(0);
		_seasonalPeriodListView.getSelectionModel().selectedItemProperty().addListener(this::inputsChanged);
		_seasonalPeriodListView.setPrefHeight(125);
		_seasonalPeriodListView.setPrefWidth(200);
		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(_seasonalPeriodListView);
		borderPane.setTop(new Label("Seasonal Period"));
		return borderPane;
	}

	private BorderPane buildJavascriptPane()
	{
		BorderPane borderPane = new BorderPane();
		borderPane.setTop(buildToggleControls());
		borderPane.setCenter(buildJavascriptControls());
		borderPane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
		BorderPane.setMargin(borderPane, new Insets(5.0, 0, 0, 0));
		return borderPane;
	}

	private ScrollPane buildJavascriptControls()
	{
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
		scrollPane.setContent(_javascriptPane.getDashboardPane());
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);
		return scrollPane;
	}

	private Pane buildToggleControls()
	{
		try(Stream<Path> paths = Files.walk(Paths.get(Constant.TREND_REPORTING_DIR)))
		{
			paths.filter(path -> path.toString().endsWith(".htm") || path.toString().endsWith(".html"))
				 .map(this::buildToggleButton)
				 .filter(Objects::nonNull)
				 .forEach(_toggleGroup.getToggles()::add);
		}
		catch(IOException ex)
		{
			LOGGER.log(Level.SEVERE, "Unable to build javascript panels", ex);
		}
		TilePane tilePane = new TilePane();
		tilePane.alignmentProperty().set(Pos.CENTER);
		tilePane.getChildren().addAll(_toggleGroup.getToggles()
												  .stream()
												  .filter(b -> b instanceof ToggleButton)
												  .map(b -> (ToggleButton) b)
												  .collect(toList()));
		Platform.runLater(() ->
				_toggleGroup.getToggles()
							.stream()
							.filter(b -> b instanceof ToggleButton)
							.map(b -> (ToggleButton) b)
							.findAny()
							.ifPresent(b -> b.setSelected(true))
		);
		return tilePane;
	}

	private ToggleButton buildToggleButton(Path path)
	{
		try
		{
			TrendReportTabConfig trendReportTabConfig = new TrendReportTabConfig(path);
			TrendReportToggleButton trendReportToggleButton = new TrendReportToggleButton(trendReportTabConfig);
			trendReportToggleButton.selectedProperty().addListener((o, old, n) -> loadPane());
			return trendReportToggleButton;
		}
		catch(IOException | RuntimeException | ParserConfigurationException | SAXException ex)
		{
			LOGGER.log(Level.SEVERE, "Unable to extract toggle button text from: " + path, ex);
			return null;
		}
	}

	public void setScenarioRuns(EpptScenarioRun baseRun, List<EpptScenarioRun> alternatives)
	{
		_scenarioRuns.clear();
		_scenarioRuns.add(baseRun);
		_scenarioRuns.addAll(alternatives);
		Platform.runLater(() ->
		{
			fillWaterYearIndexCombo(baseRun);
			loadPane();
		});
	}

	private void fillWaterYearIndexCombo(EpptScenarioRun baseRun)
	{
		try
		{
			WaterYearTableReader waterYearTableReader = new WaterYearTableReader(baseRun.getWaterYearTable());
			WaterYearIndex selectedItem = _waterYearIndexComboBox.selectionModelProperty().get().getSelectedItem();
			List<WaterYearIndex> indices = waterYearTableReader.read();
			_waterYearIndexComboBox.itemsProperty().get().clear();
			_waterYearIndexComboBox.itemsProperty().get().addAll(indices);
			if(selectedItem == null)
			{
				_waterYearIndexComboBox.selectionModelProperty().get().select(0);
			}
			else
			{
				WaterYearIndex waterYearIndex = indices.stream().filter(f -> f.toString().equals(selectedItem.toString()))
													   .findAny().orElse(indices.get(0));
				_waterYearIndexComboBox.selectionModelProperty().get().select(waterYearIndex);
			}
		}
		catch(EpptInitializationException e)
		{
			LOGGER.log(Level.SEVERE, "Error reading water year table from: " + baseRun.getName(), e);
		}
	}

	private void fillWaterYearDefinitionCombo()
	{
		WaterYearDefinition selectedItem = _waterYearDefinitionComboBox.selectionModelProperty().get().getSelectedItem();
		List<WaterYearDefinition> definitions = WaterYearDefinitionSvc.getWaterYearDefinitionSvc().getDefinitions();
		_waterYearDefinitionComboBox.itemsProperty().get().clear();
		_waterYearDefinitionComboBox.itemsProperty().get().addAll(definitions);
		if(selectedItem == null)
		{
			_waterYearDefinitionComboBox.selectionModelProperty().get().select(0);
		}
		else
		{
			WaterYearDefinition waterYearIndex = definitions.stream().filter(f -> f.toString().equals(selectedItem.toString()))
															.findAny().orElse(definitions.get(0));
			_waterYearDefinitionComboBox.selectionModelProperty().get().select(waterYearIndex);
		}
	}

	private void loadPane()
	{
		TrendReportToggleButton button = (TrendReportToggleButton) _toggleGroup.getSelectedToggle();
		if(button != null)
		{
			TrendReportTabConfig trendReportTabConfig = button.getTrendReportTabConfig();
			boolean statUsesWaterYearDefinition = _statisticsListView.getSelectionModel().getSelectedItems()
																	 .stream()
																	 .map(TrendStatistics::usesWaterYearDefinition)
																	 .findAny()
																	 .orElse(true);
			_statisticsListView.setDisable(trendReportTabConfig.isStatsDisabled());
			_seasonalPeriodListView.setDisable(trendReportTabConfig.isSeasonalPeriodDisabled());
			_waterYearIndexComboBox.setDisable(trendReportTabConfig.isWaterYearIndexDisabled());
			_waterYearDefinitionComboBox.setDisable(trendReportTabConfig.isStatsDisabled() || !statUsesWaterYearDefinition);
			loadJavascript(trendReportTabConfig.getPath());
		}
	}

	private void inputsChanged(ObservableValue<?> e, Object o, Object n)
	{
		loadPane();
	}

	private void loadJavascript(Path path)
	{
		_javascriptPane.removeDashboardPanes();
		List<GUILinksAllModelsBO> guiLink = _parameterListView.getSelectionModel().getSelectedItems();
		List<TrendStatistics> statistic = _statisticsListView.getSelectionModel().getSelectedItems();
		List<EpptReportingMonths.MonthPeriod> monthPeriod = _seasonalPeriodListView.getSelectionModel().getSelectedItems();
		if(!_scenarioRuns.isEmpty() && !guiLink.isEmpty() && !statistic.isEmpty() && !monthPeriod.isEmpty() && !_scenarioRuns.contains(null))
		{

			_progressBar.setVisible(true);
			getScene().setCursor(Cursor.WAIT);
			CompletableFuture.supplyAsync(() -> computeScenarios(guiLink, statistic, monthPeriod))
							 .whenCompleteAsync((jsonObjects, t) ->
							 {
								 if(t != null)
								 {
									 LOGGER.log(Level.SEVERE, "Error plotting Trend Report", t);
								 }
								 if(jsonObjects != null)
								 {
									 for(JSONObject jsonObject : jsonObjects)
									 {
										 _javascriptPane.addDashboardPane(path, "plot(" + jsonObject + ");");
									 }
								 }
								 _progressBar.setVisible(false);
								 getScene().setCursor(Cursor.DEFAULT);
							 }, Platform::runLater);
		}
	}

	private List<JSONObject> computeScenarios(List<GUILinksAllModelsBO> guiLinks, List<TrendStatistics> statistics,
											  List<EpptReportingMonths.MonthPeriod> monthPeriods)
	{
		List<JSONObject> retval = new ArrayList<>();
		for(GUILinksAllModelsBO guiLink : guiLinks)
		{
			for(TrendStatistics statistic : statistics)
			{
				for(EpptReportingMonths.MonthPeriod monthPeriod : monthPeriods)
				{
					EpptReportingComputedSet epptReportingComputedSet = computeForMetrics(guiLink, statistic, monthPeriod);
					JSONObject jsonObject = epptReportingComputedSet.toJson();
					LOGGER.log(Level.INFO, "{0}", jsonObject);
					retval.add(jsonObject);
				}
			}
		}
		return retval;
	}

	private EpptReportingComputedSet computeForMetrics(GUILinksAllModelsBO guiLink, TrendStatistics statistic,
													   EpptReportingMonths.MonthPeriod monthPeriod)
	{
		WaterYearDefinition waterYearDefinition = _waterYearDefinitionComboBox.getSelectionModel().getSelectedItem();
		WaterYearIndex waterYearIndex = _waterYearIndexComboBox.getSelectionModel().getSelectedItem();
		EpptReportingComputer trendReportingComputer = new EpptReportingComputer(guiLink, statistic, monthPeriod, waterYearDefinition,
				waterYearIndex);
		List<EpptReportingComputed> trendReportingComputed = new ArrayList<>();

		for(EpptScenarioRun scenarioRun : _scenarioRuns)
		{
			if(_tafCheckbox.isSelected())
			{
				trendReportingComputed.add(trendReportingComputer.computeTaf(scenarioRun, _startCalendar.getLocalDate(),
						_endCalendar.getLocalDate()));
			}
			else
			{
				trendReportingComputed.add(trendReportingComputer.computeCfs(scenarioRun, _startCalendar.getLocalDate(),
						_endCalendar.getLocalDate()));
			}
		}
		return new EpptReportingComputedSet(guiLink, statistic, monthPeriod,
				_tafCheckbox.isSelected(), trendReportingComputed);
	}

}
