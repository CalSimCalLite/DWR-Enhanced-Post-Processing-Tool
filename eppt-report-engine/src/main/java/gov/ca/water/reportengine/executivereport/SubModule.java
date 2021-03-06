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

package gov.ca.water.reportengine.executivereport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.ca.water.calgui.bo.DetailedIssue;

public class SubModule
{
	private final FlagType _flagValue;
	private final String _title;
	private final List<DetailedIssue> _linkedRecords;
	private final String _name;
	private final int _id;

	private List<FlagViolation> _baseViolations = new ArrayList<>();
	private Map<Integer, List<FlagViolation>> _alternativeViolations = new HashMap<>();

	public SubModule(int id, String name, FlagType flagValue, String title)
	{
		_id = id;
		_name = name;
		_flagValue = flagValue;
		_title = title;
		_linkedRecords = new ArrayList<>();
	}

	public int getId()
	{
		return _id;
	}

	public void addLinkedRecords(List<DetailedIssue> recordNames)
	{
		_linkedRecords.addAll(recordNames);
	}

	public String getName()
	{
		return _name;
	}

	public String getTitle()
	{
		return _title;
	}

	FlagType getFlagValue()
	{
		return _flagValue;
	}

	List<DetailedIssue> getLinkedRecords()
	{
		return _linkedRecords;
	}

	void addBaseViolations(List<FlagViolation> violations)
	{
		_baseViolations.addAll(violations);
	}

	void addAlternativeViolations(int altName, List<FlagViolation> violations)
	{
		if(_alternativeViolations.containsKey(altName))
		{
			List<FlagViolation> flagViolations = _alternativeViolations.get(altName);
			flagViolations.addAll(violations);
		}
		else
		{
			_alternativeViolations.put(altName, violations);
		}
	}

	public enum FlagType
	{
		VALUE_100,
		VALUE_200,
		VALUE_300,
		NEGATIVE_INFINITY,
		OTHER
	}

	//     public List<FlagViolation> getBaseViolations()
	//    {
	//        return _baseViolations;
	//    }
	//
	//     public List<FlagViolation> getAlternativeViolations(int altNumber)
	//    {
	//        if(_alternativeViolations.containsKey(altNumber))
	//        {
	//            return _alternativeViolations.get(altNumber);
	//        }
	//        return new ArrayList<>();
	//    }


}
