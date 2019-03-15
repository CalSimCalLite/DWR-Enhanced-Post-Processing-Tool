/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */

package gov.ca.water.bo;

//! Representation of dynamic behavior control record

/**
 * This is used to hold the information of Trigger*.table in memory.
 *
 * @author Mohan
 */
public class TriggerBO
{
	/**
	 * The trigger gui id is the one which says the id for the Dynamic action.
	 */
	private String _triggerGuiId;
	/**
	 * The trigger action is the one which says whether it's selected or
	 * not-selected for the trigger gui id.
	 */
	private String _triggerAction;
	/**
	 * The affectde gui id is the one which says the id for which the Dynamic
	 * action is going to apply.
	 */
	private String _affectdeGuiId;
	/**
	 * The affected action is the one which says what should happen for the
	 * affected gui id.
	 */
	private String _affectdeAction;

	public TriggerBO(String triggerGuiId, String triggerAction, String affectdeGuiId, String affectdeAction)
	{
		_triggerGuiId = triggerGuiId;
		_triggerAction = triggerAction;
		_affectdeGuiId = affectdeGuiId;
		_affectdeAction = affectdeAction;
	}

	public String getTriggerGuiId()
	{
		return _triggerGuiId;
	}

	public void setTriggerGuiId(String triggerGuiId)
	{
		this._triggerGuiId = triggerGuiId;
	}

	public String getTriggerAction()
	{
		return _triggerAction;
	}

	public void setTriggerAction(String triggerAction)
	{
		this._triggerAction = triggerAction;
	}

	public String getAffectdeGuiId()
	{
		return _affectdeGuiId;
	}

	public void setAffectdeGuiId(String affectdeGuiId)
	{
		this._affectdeGuiId = affectdeGuiId;
	}

	public String getAffectdeAction()
	{
		return _affectdeAction;
	}

	public void setAffectdeAction(String affectdeAction)
	{
		this._affectdeAction = affectdeAction;
	}
}