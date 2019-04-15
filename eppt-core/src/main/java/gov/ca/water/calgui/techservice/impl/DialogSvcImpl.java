/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */

package gov.ca.water.calgui.techservice.impl;

import javax.swing.*;

import gov.ca.water.calgui.techservice.IDialogSvc;

/**
 * Provides JOptionPane access with CalLite icon and consistent (center of main
 * frame) positioning
 *
 * @author tslawecki
 */
public class DialogSvcImpl implements IDialogSvc
{

	private static IDialogSvc dialogSvc = null;
	private static JFrame _mainFrame = null;

	private DialogSvcImpl()
	{

	}

	public static void installMainFrame(JFrame mainFrame)
	{
		_mainFrame = mainFrame;
	}

	/**
	 * Provides singleton management
	 */
	public static IDialogSvc getDialogSvcInstance()
	{
		if(dialogSvc == null)
		{
			dialogSvc = new DialogSvcImpl();
		}
		return dialogSvc;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * gov.ca.water.calgui.techservice.impl.IDialogSvc#getOK(java.lang.String,
	 * int)
	 */
	@Override
	public String getOK(String message, int messageType)
	{
		Object[] options = {"OK"};
		return common(message, messageType, JOptionPane.OK_OPTION, options);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gov.ca.water.calgui.techservice.impl.IDialogSvc#getYesNo(java.lang.
	 * String, int)
	 */
	@Override
	public String getYesNo(String message, int messageType)
	{
		Object[] options = {"Yes", "No"};
		return common(message, messageType, JOptionPane.OK_CANCEL_OPTION, options);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * gov.ca.water.calgui.techservice.impl.IDialogSvc#getOKCancel(java.lang.
	 * String, int)
	 */
	@Override
	public String getOKCancel(String message, int messageType)
	{
		Object[] options = {"OK", "Cancel"};
		return common(message, messageType, JOptionPane.OK_CANCEL_OPTION, options);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * gov.ca.water.calgui.techservice.impl.IDialogSvc#getSaveDontSaveCancel(
	 * java.lang.String, int)
	 */
	@Override
	public String getSaveDontSaveCancel(String message, int messageType)
	{
		Object[] options = {"Save", "Don't Save", "Cancel"};
		return common(message, messageType, JOptionPane.YES_NO_CANCEL_OPTION, options);

	}

	public String getYesNoCancel(String message, int messageType)
	{
		Object[] options = {"Yes", "No", "Cancel"};
		return common(message, messageType, JOptionPane.YES_NO_CANCEL_OPTION, options);

	}

	/**
	 * Method for shared display options (icon, location)
	 *
	 * @param message     String to show in JOptionPane
	 * @param messageType JOptionPane message type
	 * @param optionType  JOptionPane option type
	 * @param options     Array conting options to be presented as buttons
	 * @return
	 */
	private String common(String message, int messageType, int optionType, Object[] options)
	{
		JOptionPane optionPane = new JOptionPane(message, messageType, optionType, null, options, options[0]);
		JDialog dialog = optionPane.createDialog(_mainFrame, "CalLite GUI");
		dialog.setIconImage(_mainFrame.getIconImage());
		dialog.setResizable(false);
		dialog.setVisible(true);
		return optionPane.getValue().toString();
	}

}