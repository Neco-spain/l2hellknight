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
package com.l2js.util;

import com.l2js.Config;

/**
 * @author L0ngh0rn
 *
 */
public class Tools
{
	/**
	 * @param s
	 */
	public static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 78)
			s = "-" + s;
		System.out.println(s);
	}
	
	public static String[] GSInfo()
	{
		return new String[] { 
				"GS Revision: " + Config.SERVER_VERSION,
				"GS Build Data: " + Config.SERVER_BUILD_DATE,
				"DP Revision: " + Config.DATAPACK_VERSION,
				"Site: http://www.l2js.com.br/"
		};
	}

	public static String[] LSInfo()
	{
		return new String[] { 
				"Site: http://www.l2js.com.br/" 
		};
	}
}
