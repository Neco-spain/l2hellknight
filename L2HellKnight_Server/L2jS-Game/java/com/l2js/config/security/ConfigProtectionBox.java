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
package com.l2js.config.security;

import gnu.trove.TIntIntHashMap;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.l2js.Config;
import com.l2js.util.L2Properties;
import com.l2js.util.StringUtil;

/**
 * @author L0ngh0rn
 */
public class ConfigProtectionBox extends Config
{
	private final static String path = L2JS_PROTECTION_BOX;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			ANTIFEED_ENABLE = getBoolean(properties, "AntiFeedEnable", false);
			ANTIFEED_DUALBOX = getBoolean(properties, "AntiFeedDualbox", true);
			ANTIFEED_DISCONNECTED_AS_DUALBOX = getBoolean(properties, "AntiFeedDisconnectedAsDualbox", true);
			ANTIFEED_INTERVAL = getInt(properties, "AntiFeedInterval", 120) * 1000;

			DUALBOX_CHECK_MAX_PLAYERS_PER_IP = getInt(properties, "DualboxCheckMaxPlayersPerIP", 0);
			DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP = getInt(properties,
					"DualboxCheckMaxOlympiadParticipantsPerIP", 0);
			String[] propertySplit = getStringArray(properties, "DualboxCheckWhitelist", new String[]
			{
				"127.0.0.1,0"
			}, ";");
			DUALBOX_CHECK_MAX_L2EVENT_PARTICIPANTS_PER_IP = getInt(properties, "DualboxCheckMaxL2EventParticipantsPerIP", 0);
			DUALBOX_CHECK_WHITELIST = new TIntIntHashMap(propertySplit.length);
			for (String entry : propertySplit)
			{
				String[] entrySplit = entry.split(",");
				if (entrySplit.length != 2)
					_log.warning(StringUtil.concat(
							"DualboxCheck[load()]: invalid config property -> DualboxCheckWhitelist \"", entry, "\""));
				else
				{
					try
					{
						int num = Integer.parseInt(entrySplit[1]);
						num = num == 0 ? -1 : num;
						DUALBOX_CHECK_WHITELIST.put(InetAddress.getByName(entrySplit[0]).hashCode(), num);
					}
					catch (UnknownHostException e)
					{
						_log.warning(StringUtil.concat(
								"DualboxCheck[load()]: invalid address -> DualboxCheckWhitelist \"", entrySplit[0],
								"\""));
					}
					catch (NumberFormatException e)
					{
						_log.warning(StringUtil
								.concat("DualboxCheck[load()]: invalid number -> DualboxCheckWhitelist \"",
										entrySplit[1], "\""));
					}
				}
			}

			MULTIBOX_PROTECTION_ENABLED = getBoolean(properties, "MultiboxProtectionEnabled", false);
			MULTIBOX_PROTECTION_CLIENTS_PER_PC = getInt(properties, "ClientsPerPc", 2);
			MULTIBOX_PROTECTION_PUNISH = getInt(properties, "MultiboxPunish", 2);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
