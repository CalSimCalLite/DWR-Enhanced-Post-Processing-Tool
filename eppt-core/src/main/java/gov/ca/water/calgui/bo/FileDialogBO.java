/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */

package gov.ca.water.calgui.bo;

//! Custom file chooser for selection of different CalLite file types

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;

import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.constant.EpptPreferences;
import gov.ca.water.calgui.techservice.IErrorHandlingSvc;
import gov.ca.water.calgui.techservice.impl.ErrorHandlingSvcImpl;
import org.apache.log4j.Logger;

/**
 * Supports selection of different types of CalLite files from customized
 * JFileChoosers.
 *
 * @author tslawecki
 */
public class FileDialogBO implements ActionListener
{

	private static final Logger LOG = Logger.getLogger(FileDialogBO.class.getName());
	private final DefaultListModel _lmScenNames;
	private JFileChooser _fc = new JFileChooser();
	private JTextField _theTextField;
	private boolean _theMultipleFlag = false;
	private String _theFileExt = null;
	private Component _parentComponent;
	private IErrorHandlingSvc _errorHandlingSvc = new ErrorHandlingSvcImpl();


	/**
	 * Constructor for use with DSS files, result is appended to list and placed
	 * in textfield.
	 *
	 * @param aTextField
	 */
	public FileDialogBO(JTextField aTextField, Component mainFrame)
	{
		_parentComponent = mainFrame;
		_lmScenNames = null;
		_theFileExt = "DSS";
		_theTextField = aTextField;
		setup();
	}

	/**
	 * Constructor for use with arbitrary files, result is appended to list and
	 * placed in textfield.
	 *
	 * @param aTextField
	 * @param aFileExt
	 */
	public FileDialogBO(JTextField aTextField, String aFileExt, Component mainFrame)
	{
		_parentComponent = mainFrame;
		_lmScenNames = null;
		_theTextField = aTextField;
		_theFileExt = aFileExt;
		setup();
	}

	/**
	 * Constructor used for DSS files, result is appended to list, radiobuttons
	 * are enabled when list length greater than 1, button is enabled when list
	 * is not empty;
	 */
	public FileDialogBO(DefaultListModel<RBListItemBO> lmScenNames, boolean isMultiple, Component mainFrame)
	{
		_parentComponent = mainFrame;
		_theFileExt = "DSS";
		_theTextField = null;
		_lmScenNames = lmScenNames;
		setup();
		_theMultipleFlag = isMultiple;
	}

	public DefaultListModel getLmScenNames()
	{
		return _lmScenNames;
	}

	private void setup()
	{

		try
		{
			// Set up file chooser

			if("DSS".equals(_theFileExt))
			{
				_fc.setFileFilter(new SimpleFileFilter("DSS"));
				_fc.setCurrentDirectory(EpptPreferences.getScenariosPaths().toFile());
			}
			else if("DSS2".equals(_theFileExt))
			{
				_theFileExt = "DSS";
				_fc.setFileFilter(new SimpleFileFilter("DSS"));
				_fc.setCurrentDirectory(EpptPreferences.getModelDssPath().toFile());
			}
			else
			{
				_fc.setFileFilter(new SimpleFileFilter(_theFileExt));
				if("PDF".equals(_theFileExt) || "CLS".equals(_theFileExt))
				{
					_fc.setCurrentDirectory(EpptPreferences.getReportsPath().toFile());
				}
				else
				{
					_fc.setCurrentDirectory(new File(Constant.CONFIG_DIR));
				}
			}
		}
		catch(RuntimeException e)
		{
			LOG.error(e.getMessage());
			String messageText = "Unable to set up file dialog.";
			_errorHandlingSvc.businessErrorHandler(messageText, e);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			setup();
			Object source = e.getSource();
			if((source != null) && (((Component) source).getName() != null)
					&& "btnDelScenario".equals(((Component) source).getName()))
			{
				deleteScenario();
			}
			else if("btnClearScenario".equals(e.getActionCommand()))
			{
				clearAllScenarios();
			}
			else
			{
				GUILinksAllModelsBO.Model model = chooseModel();
				if(model != null)
				{
					addScenario(model);
				}
			}
		}
		catch(HeadlessException e1)
		{
			LOG.error(e1.getMessage());
			String messageText = "Unable to show file chooser.";
			_errorHandlingSvc.businessErrorHandler(messageText, e1);
		}
	}

	private GUILinksAllModelsBO.Model chooseModel()
	{
		return (GUILinksAllModelsBO.Model) JOptionPane.showInputDialog(_parentComponent, "EPPT Model",
				"Choose Model:", JOptionPane.QUESTION_MESSAGE, null,
				GUILinksAllModelsBO.Model.values(),
				GUILinksAllModelsBO.Model.CAL_LITE);

	}

	private void addScenario(GUILinksAllModelsBO.Model model)
	{
		// Otherwise, show dialog
		int rc;
		if(_theMultipleFlag)
		{
			UIManager.put("FileChooser.openDialogTitleText", "Select Scenarios");
			_fc.setMultiSelectionEnabled(true);
			int dialogRC = _fc.showDialog(_parentComponent, "Select");
			if(_lmScenNames != null && dialogRC != 1)
			{
				for(File file : _fc.getSelectedFiles())
				{
					addFileToList(file, model);
				}
			}
		}
		else
		{
			if(_theFileExt == null || "inp".equalsIgnoreCase(_theFileExt))
			{
				_fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				rc = _fc.showOpenDialog(_parentComponent);
			}
			else
			{
				_fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				rc = _fc.showDialog(_parentComponent,
						"DSS".equals(_theFileExt) ? "Open" : "Save");
			}
			File file;
			if(rc == 0)
			{
				file = _fc.getSelectedFile();
				if("PDF".equals(_theFileExt) && !file.getName().toLowerCase().endsWith(".pdf"))
				{
					file = new File(file.getPath() + ".PDF");
				}
				if("CLS".equals(_theFileExt) && !file.getName().toLowerCase().endsWith(".cls"))
				{
					file = new File(file.getPath() + ".CLS");
				}
				if(_lmScenNames != null)
				{
					addFileToList(file, model);
				}
				else if(_theTextField != null)
				{
					_theTextField.setText(file.getName());
					_theTextField.setToolTipText(file.getPath());
				}
			}
		}
	}

	private void clearAllScenarios()
	{
		_lmScenNames.clear();
	}

	private void deleteScenario()
	{
		// If invoked by QR DelScenario button, delete a scenario from
		// Quick
		// Results scenario list
		if((_lmScenNames != null) && _lmScenNames.getSize() > 0)
		{
			int todel = -1;
			for(int i = 0; i < _lmScenNames.getSize(); i++)
			{
				if(((RBListItemBO) _lmScenNames.getElementAt(i)).isSelected())
				{
					todel = i;
				}
			}
			if(todel > 0)
			{
				((RBListItemBO) _lmScenNames.getElementAt(todel - 1)).setSelected(true);
			}
			else if(todel < _lmScenNames.getSize() - 1)
			{
				((RBListItemBO) _lmScenNames.getElementAt(todel + 1)).setSelected(true);
			}
			_lmScenNames.remove(todel);
		}
	}

	/**
	 * Adds file to list of scenarios if not already in list. Currently used to
	 * manage list of scenarios on Quick Result dashboard
	 *
	 * @param file
	 */
	private void addFileToList(File file, GUILinksAllModelsBO.Model model)
	{
		try
		{
			boolean match = false;
			for(int i = 0; i < _lmScenNames.getSize(); i++)
			{
				RBListItemBO rbli = (RBListItemBO) _lmScenNames.getElementAt(i);
				match = rbli.toString().equals(file.getPath());
				if(match)
				{
					break;
				}
			}

			if(!match)
			{
				_lmScenNames.addElement(new RBListItemBO(file.getPath(), file.getName(), model));
				if(_lmScenNames.getSize() == 1)
				{
					((RBListItemBO) _lmScenNames.getElementAt(0)).setSelected(true);
				}
			}
		}
		catch(RuntimeException e)
		{
			LOG.error(e.getMessage());
			String messageText = "Unable to update list.";
			_errorHandlingSvc.businessErrorHandler(messageText, e);
		}
	}


}
