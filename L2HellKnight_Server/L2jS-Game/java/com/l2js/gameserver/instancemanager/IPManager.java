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
package com.l2js.gameserver.instancemanager;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2js.Config;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.network.L2GameClient;
import com.l2js.gameserver.util.Util;

/**
 * @author L0ngh0rn
 */
public class IPManager
{
	private static final Logger _log = Logger.getLogger(IPManager.class.getName());

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final IPManager _instance = new IPManager();
	}

	public static final IPManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public IPManager()
	{
		_log.log(Level.INFO, "IPManager - Loaded.");
	}

	private boolean testMultiBox(L2PcInstance activeChar, Integer numberBox, L2PcInstance[] world)
	{
		Map<String, List<L2PcInstance>> ipMap = new HashMap<String, List<L2PcInstance>>();
		for (L2PcInstance player : world)
		{
			if (player.getClient() == null || player.getClient().isDetached())
				continue;
			else
			{
				if (!compareAdress(activeChar.getAdress(), player.getAdress()))
				{
					String ip = joinIps(player.getAdress());
					if (ipMap.get(ip) == null)
						ipMap.put(ip, new ArrayList<L2PcInstance>());
					ipMap.get(ip).add(player);
					if (ipMap.get(ip).size() >= numberBox)
						return true;
				}
			}
		}
		return false;
	}

	public boolean validBox(L2PcInstance activeChar, Integer numberBox, L2PcInstance[] world,
			Boolean forcedLogOut)
	{
		if (testMultiBox(activeChar, numberBox, world))
		{
			if (forcedLogOut)
			{
				L2GameClient client = activeChar.getClient();
				_log.warning("Multibox Protection: " + client.getConnection().getInetAddress().getHostAddress()
						+ " was trying to use over " + numberBox + " clients!");
				Util.handleIllegalPlayerAction(activeChar, "Multibox Protection: "
						+ client.getConnection().getInetAddress().getHostAddress() + " was trying to use over "
						+ numberBox + " clients!", Config.MULTIBOX_PROTECTION_PUNISH);
			}
			return true;
		}
		return false;
	}

	private boolean compareAdress(String[] a1, String[] a2)
	{
		if (a1.length != a2.length)
			return false;

		int equal = 0;
		for (int i = 0; i < a1.length; i++)
			if (a1[i].equalsIgnoreCase(a2[i]))
				equal++;
		if (equal == a1.length)
			return false;
		return true;
	}

	private String joinIps(String[] ips)
	{
		StringBuilder ip = new StringBuilder();
		for (String t : ips)
		{
			ip.append(t);
			ip.append("|");
		}
		return ip.toString();
	}
}
