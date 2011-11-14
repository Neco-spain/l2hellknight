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

import java.io.File;

import com.l2js.Config;
import com.l2js.util.L2Properties;

/**
 * @author L0ngh0rn
 */
public class ConfigLoginServer extends Config
{
	private final static String	path	= LOGIN_SERVER_CONFIG;

	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			GAME_SERVER_LOGIN_HOST = getString(properties, "LoginHostname", "*");
			GAME_SERVER_LOGIN_PORT = getInt(properties, "LoginPort", 9013);
			LOGIN_BIND_ADDRESS = getString(properties, "LoginserverHostname", "*");
			PORT_LOGIN = getInt(properties, "LoginserverPort", 2106);
			DEBUG = getBoolean(properties, "Debug", false);
			ACCEPT_NEW_GAMESERVER = getBoolean(properties, "AcceptNewGameServer", true);
			LOGIN_TRY_BEFORE_BAN = getInt(properties, "LoginTryBeforeBan", 10);
			LOGIN_BLOCK_AFTER_BAN = getInt(properties, "LoginBlockAfterBan", 600);
			LOG_LOGIN_CONTROLLER = getBoolean(properties, "LogLoginController", true);
			DATABASE_DRIVER = getString(properties, "Driver", "com.mysql.jdbc.Driver");
			DATABASE_URL = getString(properties, "URL", "jdbc:mysql://localhost/l2js_ls");
			DATABASE_LOGIN = getString(properties, "Login", "root");
			DATABASE_PASSWORD = getString(properties, "Password", "root");
			DATABASE_MAX_CONNECTIONS = getInt(properties, "MaximumDbConnections", 10);
			SHOW_LICENCE = getBoolean(properties, "ShowLicence", false);
			AUTO_CREATE_ACCOUNTS = getBoolean(properties, "AutoCreateAccounts", true);
			FLOOD_PROTECTION = getBoolean(properties, "EnableFloodProtection", true);
			FAST_CONNECTION_LIMIT = getInt(properties, "FastConnectionLimit", 15);
			NORMAL_CONNECTION_TIME = getInt(properties, "NormalConnectionTime", 700);
			FAST_CONNECTION_TIME = getInt(properties, "FastConnectionTime", 350);
			MAX_CONNECTION_PER_IP = getInt(properties, "MaxConnectionPerIP", 50);
			 //FIXME: in login?
			DATAPACK_ROOT = new File(getString(properties, "DatapackRoot", ".")).getCanonicalFile();
			
			LOGIN_SERVER_SCHEDULE_RESTART = getBoolean(properties, "LoginRestartSchedule", false); 
			LOGIN_SERVER_SCHEDULE_RESTART_TIME = getLong(properties, "LoginRestartTime", 24L); 
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
