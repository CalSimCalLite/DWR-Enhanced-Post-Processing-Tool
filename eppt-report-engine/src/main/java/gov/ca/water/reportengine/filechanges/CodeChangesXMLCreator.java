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

package gov.ca.water.reportengine.filechanges;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.google.common.flogger.FluentLogger;
import gov.ca.water.reportengine.EpptReportException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CodeChangesXMLCreator
{
	private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();

	private static final String CODE_CHANGES = "code-changes";

	private static final String HEADER = "header";

	private static final String FILES_UPDATED = "files-updated";

	private static final String FILES_ADDED_TO = "files-added-to";

	private static final String FILES_DELETED_FROM_BASE = "files-deleted-from-base";

	private static final String TYPE = "type";
	private static final String CHANGE_TYPE = "change-type";
	private static final String SUBTYPE = "subtype";

	private static final String CHANGE = "change";
	private static final String CHANGE_SUBTYPE = "change-subtype";

	private static final String SECTION = "section";
	private static final String SECTION_NAME = "section-name";

	private static final String MOD_FILE = "modified";
	private static final String STAR_NAME = "new";
	private static final String STAR_DELETED = "deleted";
	private static final String UNCATEGORIZED = "Uncategorized";
	private static final String OTHERS = "Others";

	private final Set<Path> _modifiedFilesInMaster = new HashSet<>();
	private final Set<Path> _modifidFilesNotInMaster = new HashSet<>();

	private final Set<Path> _fileAddedToAltInMaster = new HashSet<>();
	private final Set<Path> _fileAddedToAltNotInMaster = new HashSet<>();

	private final Set<Path> _fileDeletedFromBaseInMaster = new HashSet<>();
	private final Set<Path> _fileDeletedFromBaseNotInMaster = new HashSet<>();

	public Element createCodeChangesElement(Path csvPath, Path baseOutputPath, Path altOutputPath, Document document)
			throws EpptReportException, IOException
	{
		LOGGER.at(Level.INFO).log("Writing Code Changes to XML");
		CodeChangesDataProcessor processor = new CodeChangesDataProcessor(csvPath);
		CodeChangesStatistics stats = processor.processCodeChanges(baseOutputPath, altOutputPath);
		List<CodeChangesType> codeChangesTypes = processor.getCodeChangesTypes();

		loadFileChangesFromStats(stats, codeChangesTypes);

		Element codeChangesElemRoot = document.createElement(CODE_CHANGES);

		//create header
		codeChangesElemRoot.appendChild(createHeader(stats, document));

		boolean atLeastOneSectionElemExists = false;
		//create modified section
		Element modifiedSectionElem = createSection(MOD_FILE, codeChangesTypes, _modifiedFilesInMaster, _modifidFilesNotInMaster, document);
		if(modifiedSectionElem != null)
		{
			atLeastOneSectionElemExists = true;
			codeChangesElemRoot.appendChild(modifiedSectionElem);
		}


		Element newSectionElem = createSection(STAR_NAME, codeChangesTypes, _fileAddedToAltInMaster, _fileAddedToAltNotInMaster, document);
		if(newSectionElem != null)
		{
			atLeastOneSectionElemExists = true;
			codeChangesElemRoot.appendChild(newSectionElem);
		}

		Element deleteSectionElem = createSection(STAR_DELETED, codeChangesTypes, _fileDeletedFromBaseInMaster, _fileDeletedFromBaseNotInMaster,
				document);
		if(deleteSectionElem != null)
		{
			atLeastOneSectionElemExists = true;
			codeChangesElemRoot.appendChild(deleteSectionElem);
		}

		if(!atLeastOneSectionElemExists)
		{
			//create empty elements
			Element emptySection = createEmptySection(document);
			codeChangesElemRoot.appendChild(emptySection);
		}

		return codeChangesElemRoot;
	}

	private Element createEmptySection(Document document)
	{
		Element sectionElem = document.createElement(SECTION);
		Element typeElem = document.createElement(TYPE);
		sectionElem.appendChild(typeElem);

		Element subTypeElem = document.createElement(SUBTYPE);
		typeElem.appendChild(subTypeElem);

		Element changeElem = document.createElement(CHANGE);
		subTypeElem.appendChild(changeElem);

		return sectionElem;
	}


	private Element createSection(String sectionName, List<CodeChangesType> codeChangesTypes, Set<Path> changedFilesInMaster,
								  Set<Path> changedFilesNotInMaster, Document document)
	{
		//create modified section
		Element sectionElem = createSectionElement(sectionName, document);
		boolean sectionHasAtLeastOneType = false;
		for(CodeChangesType cct : codeChangesTypes)
		{
			Element typeElement = createTypeElement(cct, changedFilesInMaster, document);
			if(typeElement != null)
			{
				sectionHasAtLeastOneType = true;
				sectionElem.appendChild(typeElement);
			}
		}
		Element uncategorizedElement = createUncategorizedElement(changedFilesNotInMaster, document);
		if(uncategorizedElement != null)
		{
			sectionHasAtLeastOneType = true;
			sectionElem.appendChild(uncategorizedElement);
		}

		if(!sectionHasAtLeastOneType)
		{
			return null;
		}
		else
		{
			return sectionElem;
		}
	}

	private void loadFileChangesFromStats(CodeChangesStatistics stats, List<CodeChangesType> codeChangesTypes)
	{
		//get all master files
		List<String> allMasterFiles = new ArrayList<>();
		for(CodeChangesType cct : codeChangesTypes)
		{
			for(CodeChangesSubType sub : cct.getSubTypes())
			{
				allMasterFiles.addAll(sub.getWreslFiles());
			}
		}

		for(Path modFile : stats.getCodeChangesModifiedFiles())
		{
			if(allMasterFiles.contains(modFile.toString()))
			{
				_modifiedFilesInMaster.add(modFile);
			}
			else
			{
				_modifidFilesNotInMaster.add(modFile);
			}
		}

		for(Path addedFile : stats.getFilesAddedToAlt())
		{
			if(allMasterFiles.contains(addedFile.toString()))
			{
				_fileAddedToAltInMaster.add(addedFile);
			}
			else
			{
				_fileAddedToAltNotInMaster.add(addedFile);
			}
		}

		for(Path deletedFile : stats.getFilesDeletedFromBase())
		{
			if(allMasterFiles.contains(deletedFile.toString()))
			{
				_fileDeletedFromBaseInMaster.add(deletedFile);
			}
			else
			{
				_fileDeletedFromBaseNotInMaster.add(deletedFile);
			}
		}
	}

	private Element createSectionElement(String sectionName, Document document)
	{
		Element sectionElem = document.createElement(SECTION);
		sectionElem.setAttribute(SECTION_NAME, sectionName);
		return sectionElem;
	}

	private Element createUncategorizedElement(Set<Path> files, Document document)
	{
		if(files.isEmpty())
		{
			return null;
		}
		Element uncategorizedElem = document.createElement(TYPE);
		uncategorizedElem.setAttribute(CHANGE_TYPE, UNCATEGORIZED);
		uncategorizedElem.appendChild(createOtherSubtype(files, document));

		return uncategorizedElem;
	}

	private Element createOtherSubtype(Set<Path> files, Document document)
	{
		Element subElem = document.createElement(SUBTYPE);
		subElem.setAttribute(CHANGE_SUBTYPE, OTHERS);

		for(Path file : files)
		{
			Element changeElem = document.createElement(CHANGE);
			changeElem.setTextContent(file.toString());
			subElem.appendChild(changeElem);
		}
		return subElem;
	}

	private Element createHeader( CodeChangesStatistics stats, Document document)
	{
		Element headerElem = document.createElement(HEADER);

		Element filesUpdatedElem = document.createElement(FILES_UPDATED);
		filesUpdatedElem.setTextContent(Integer.toString(stats.getCodeChangesModifiedFiles().size()));
		headerElem.appendChild(filesUpdatedElem);

		Element filesAddedElem = document.createElement(FILES_ADDED_TO);
		filesAddedElem.setTextContent(Integer.toString(stats.getFilesAddedToAlt().size()));
		headerElem.appendChild(filesAddedElem);

		Element filesDeletedElem = document.createElement(FILES_DELETED_FROM_BASE);
		filesDeletedElem.setTextContent(Integer.toString(stats.getFilesDeletedFromBase().size()));
		headerElem.appendChild(filesDeletedElem);

		return headerElem;
	}

	private Element createTypeElement(CodeChangesType type, Set<Path> files, Document document)
	{
		boolean atLeastOneSubTypeHasFileChanges = false;
		Element typeElem = document.createElement(TYPE);
		typeElem.setAttribute(CHANGE_TYPE, type.getName());

		List<CodeChangesSubType> subTypes = type.getSubTypes();

		for(CodeChangesSubType subType : subTypes)
		{
			Element subElem = createSubtype(subType, files, document);
			if(subElem != null)
			{
				atLeastOneSubTypeHasFileChanges = true;
				typeElem.appendChild(subElem);
			}
		}

		if(atLeastOneSubTypeHasFileChanges)
		{
			return typeElem;
		}
		else
		{
			return null;
		}
	}

	private Element createSubtype(CodeChangesSubType subType, Set<Path> files, Document document)
	{
		Element subTypeElem = document.createElement(SUBTYPE);
		if(!files.isEmpty())
		{
			subTypeElem.setAttribute(CHANGE_SUBTYPE, subType.getName());

			//create the list of changes
			for(Path change : files)
			{
				//the "files" are all files and not necessarily from this subtype
				if(subType.getWreslFiles().contains(change.toString()))
				{
					Element changeElem = document.createElement(CHANGE);
					changeElem.setTextContent(change.toString());
					subTypeElem.appendChild(changeElem);
				}
			}
		}
		if(subTypeElem.getChildNodes().getLength() == 0)
		{
			return null;
		}
		else
		{
			return subTypeElem;
		}
	}

}
