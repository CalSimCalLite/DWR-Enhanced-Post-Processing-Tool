/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */
package vista.app.commands;

import vista.app.DataGraphFrame;
import vista.app.DefaultGraphBuilder;
import vista.graph.Graph;
import vista.gui.Command;
import vista.gui.ExecutionException;
import vista.set.DataReference;
import vista.set.Group;

/**
 * Encapsulates commands implementing group related commands
 * 
 * @author Nicky Sandhu
 * @version $Id: GraphDataCommand.java,v 1.1 2003/10/02 20:48:30 redwood Exp $
 */
public class GraphDataCommand implements Command {
	private Group _group;
	private int[] _rNumbers;
	private String _filename;
	private DataReference _reference;

	/**
	 * opens group and sets current group to
	 */
	public GraphDataCommand(Group g, int[] referenceNumbers) {
		_group = g;
		_rNumbers = referenceNumbers;
	}

	/**
	 * executes command
	 */
	public void execute() throws ExecutionException {
		if (_rNumbers == null || _rNumbers.length == 0)
			return;
		DefaultGraphBuilder gb = new DefaultGraphBuilder();
		for (int i = 0; i < _rNumbers.length; i++) {
			gb.addData(_group.getDataReference(_rNumbers[i]));
		}
		Graph[] graphs = gb.createGraphs();
		if (graphs != null || graphs.length > 0) {
			for (int i = 0; i < graphs.length; i++) {
				new DataGraphFrame(graphs[i], "Graph").setVisible(true);
			}
		}
	}

	/**
	 * unexecutes command or throws exception if not unexecutable
	 */
	public void unexecute() throws ExecutionException {
		throw new ExecutionException("Cannot undo graphing of data");
	}

	/**
	 * checks if command is executable.
	 */
	public boolean isUnexecutable() {
		return false;
	}

	/**
	 * writes to script
	 */
	public void toScript(StringBuffer buf) {
	}
} // end of GraphDataCommand