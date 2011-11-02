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
package com.l2js.config.network;

import java.math.BigInteger;

import com.l2js.Config;
import com.l2js.util.L2Properties;

/**
 * @author L0ngh0rn
 */
public class ConfigCommunityServer extends Config
{
	private final static String path = COMMUNITY_SERVER_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			ENABLE_COMMUNITY_BOARD = getBoolean(properties, "EnableCommunityBoard", false);
			COMMUNITY_SERVER_ADDRESS = getString(properties, "CommunityServerHostname", "localhost");
			COMMUNITY_SERVER_PORT = getInt(properties, "CommunityServerPort", 9013);
			COMMUNITY_SERVER_HEX_ID = new BigInteger(properties.getProperty("CommunityServerHexId"), 16).toByteArray();
			COMMUNITY_SERVER_SQL_DP_ID = getInt(properties, "CommunityServerSqlDpId", 200);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
