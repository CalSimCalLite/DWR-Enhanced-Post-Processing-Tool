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
package vista.set;

import java.util.regex.Pattern;

/**
 * A path parts filter which filters on a data reference and returns true to
 * keep it and false to reject it.
 *
 * @author Nicky Sandhu
 * @version $Id: PathPartsFilter.java,v 1.1 2003/10/02 20:49:28 redwood Exp $
 */
public class PathPartsFilter implements Predicate<DataReference>
{
	Pattern[] _patterns = new Pattern[6];

	/**
	 * sets up a path parts filter with the given filters
	 */
	public PathPartsFilter(String[] pathParts)
	{
		setPathParts(pathParts);
	}

	/**
	 *
	 */
	public void setPathParts(String[] pathParts)
	{
		for(int i = 0; i < Math.min(_patterns.length, pathParts.length); i++)
		{
			String part = pathParts[i] == null ? "" : pathParts[i].trim()
																  .toUpperCase();
			try
			{
				if(part.equals(""))
				{
					_patterns[i] = null;
				}
				else
				{
					_patterns[i] = Pattern.compile(part);
				}
			}
			catch(Exception mpe)
			{
				throw new RuntimeException("Incorrect Regular Expression "
						+ part);
			}
		}
	}

	@Override
	public boolean apply(DataReference ref)
	{
		if(ref == null)
		{
			return false;
		}
		Pathname path = ref.getPathname();
		boolean keepThis = true;
		for(int i = 0; i < _patterns.length; i++)
		{
			if(_patterns[i] == null)
			{
				continue;
			}
			keepThis = keepThis && _patterns[i].matcher(path.getPart(i)).find();
		}
		return keepThis;
	}
}
