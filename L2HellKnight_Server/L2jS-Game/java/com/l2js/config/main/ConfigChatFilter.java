/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2js.config.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;

import com.l2js.Config;

/**
 * @author L0ngh0rn
 */
public class ConfigChatFilter extends Config
{
	private final static String path = CHAT_FILTER_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			FILTER_LIST = new ArrayList<String>();
			LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(new File(path))));
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().isEmpty() || line.startsWith("#"))
					continue;
				FILTER_LIST.add(line.trim());
			}
			_log.info("Loaded " + FILTER_LIST.size() + " Filter Words.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
