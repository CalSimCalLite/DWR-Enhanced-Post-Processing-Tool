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

import java.nio.file.Path;

import gov.ca.water.calgui.presentation.JavaFxChartsPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * Company: Resource Management Associates
 *
 * @author <a href="mailto:adam@rmanet.com">Adam Korynta</a>
 * @since 07-17-2019
 */
class TrendReportFlowPane
{
	private final BorderPane _flowPane;
	private final TrendReportingAnimator _animator;

	TrendReportFlowPane()
	{
		_flowPane = new BorderPane();
		_flowPane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
		_animator = new TrendReportingAnimator();
		_animator.observe(_flowPane.getChildren());
	}

	Pane getDashboardPane()
	{
		return _flowPane;
	}

	void addDashboardPane(Path path, String callback)
	{
		JavaFxChartsPane javascriptPanel = new JavaFxChartsPane(path, callback);
		_flowPane.setCenter(javascriptPanel);
	}

	void removeDashboardPanes()
	{
		_flowPane.getChildren().clear();
	}
}
