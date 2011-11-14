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

import gnu.trove.TIntArrayList;

import java.io.File;

import com.l2js.Config;
import com.l2js.util.L2Properties;

/**
 * @author L0ngh0rn
 */
public class ConfigGameServer extends Config
{
	private final static String path = GAME_SERVER_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			GAMESERVER_HOSTNAME = getString(properties, "GameserverHostname", "*");
			PORT_GAME = getInt(properties, "GameserverPort", 7777);
			GAME_SERVER_LOGIN_PORT = getInt(properties, "LoginPort", 9014);
			GAME_SERVER_LOGIN_HOST = getString(properties, "LoginHost", "127.0.0.1");
			REQUEST_ID = getInt(properties, "RequestServerID", 0);
			ACCEPT_ALTERNATE_ID = getBoolean(properties, "AcceptAlternateID", true);
			DATABASE_DRIVER = getString(properties, "Driver", "com.mysql.jdbc.Driver");
			DATABASE_URL = getString(properties, "URL", "jdbc:mysql://localhost/l2js_gs");
			DATABASE_LOGIN = getString(properties, "Login", "root");
			DATABASE_PASSWORD = getString(properties, "Password", "root");
			DATABASE_MAX_CONNECTIONS = getInt(properties, "MaximumDbConnections", 10);
			DATAPACK_ROOT = new File(getString(properties, "DatapackRoot", ".")).getCanonicalFile();
			CNAME_TEMPLATE = getString(properties, "CnameTemplate", ".*");
			PET_NAME_TEMPLATE = getString(properties, "PetNameTemplate", ".*");
			MAX_CHARACTERS_NUMBER_PER_ACCOUNT = getInt(properties, "CharMaxNumber", 0);
			MAXIMUM_ONLINE_USERS = getInt(properties, "MaximumOnlineUsers", 100);
			String[] protocols = getString(properties, "AllowedProtocolRevisions", "267;268;271;273").split(";");
			PROTOCOL_LIST = new TIntArrayList(protocols.length);
			for (String protocol : protocols)
			{
				try
				{
					PROTOCOL_LIST.add(Integer.parseInt(protocol.trim()));
				}
				catch (NumberFormatException e)
				{
					_log.info("Wrong config protocol version: " + protocol + ". Skipped.");
				}
			}
			DATABASE_CLEAN_UP = getBoolean(properties, "DatabaseCleanUp", true);
			CONNECTION_CLOSE_TIME = getLong(properties, "ConnectionCloseTime", 60000L);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
