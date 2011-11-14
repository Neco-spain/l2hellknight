/*
 * $Header: AdminTest.java, 25/07/2005 17:15:21 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 25/07/2005 17:15:21 $
 * $Revision: 1 $
 * $Log: AdminTest.java,v $
 * Revision 1  25/07/2005 17:15:21  luisantonioa
 * Added copyright notice
 *
 *
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
package com.l2js.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2js.gameserver.cache.HtmCache;
import com.l2js.gameserver.handler.IAdminCommandHandler;
import com.l2js.gameserver.instancemanager.MapRegionManager;
import com.l2js.gameserver.instancemanager.ZoneManager;
import com.l2js.gameserver.model.L2World;
import com.l2js.gameserver.model.L2WorldRegion;
import com.l2js.gameserver.model.Location;
import com.l2js.gameserver.model.actor.L2Character;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.model.zone.L2ZoneType;
import com.l2js.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2js.util.StringUtil;

public class AdminZone implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
			"admin_zone_check", "admin_zone_reload", "admin_zone_visual", "admin_zone_visual_clear"
	};
	
	/**
	 * 
	 * @see com.l2js.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String,
	 *      com.l2js.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;
		
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		
		// String val = "";
		// if (st.countTokens() >= 1) {val = st.nextToken();}
		
		if (actualCommand.equalsIgnoreCase("admin_zone_check"))
		{
			showHtml(activeChar);
			activeChar.sendMessage("MapRegion: x:" + MapRegionManager.getInstance().getMapRegionX(activeChar.getX()) + " y:" + MapRegionManager.getInstance().getMapRegionY(activeChar.getY()) + " ("
					+ MapRegionManager.getInstance().getMapRegion(activeChar).getLocId() + ")");
			getGeoRegionXY(activeChar);
			activeChar.sendMessage("Closest Town: " + MapRegionManager.getInstance().getClosestTownName(activeChar));
			
			Location loc;
			
			loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, MapRegionManager.TeleportWhereType.Castle);
			activeChar.sendMessage("TeleToLocation (Castle): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());
			
			loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, MapRegionManager.TeleportWhereType.ClanHall);
			activeChar.sendMessage("TeleToLocation (ClanHall): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());
			
			loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, MapRegionManager.TeleportWhereType.SiegeFlag);
			activeChar.sendMessage("TeleToLocation (SiegeFlag): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());
			
			loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, MapRegionManager.TeleportWhereType.Town);
			activeChar.sendMessage("TeleToLocation (Town): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());
		}
		else if (actualCommand.equalsIgnoreCase("admin_zone_reload"))
		{
			ZoneManager.getInstance().reload();
			activeChar.sendMessage("All Zones have been reloaded");
		}
		else if (actualCommand.equalsIgnoreCase("admin_zone_visual"))
		{
			String next = st.nextToken();
			if (next.equalsIgnoreCase("all"))
			{
				for (L2ZoneType zone : ZoneManager.getInstance().getZones(activeChar))
				{
					zone.visualizeZone(activeChar.getZ());
				}
				showHtml(activeChar);
			}
			else
			{
				int zoneId = Integer.parseInt(next);
				ZoneManager.getInstance().getZoneById(zoneId).visualizeZone(activeChar.getZ());
			}
		}
		else if (actualCommand.equalsIgnoreCase("admin_zone_visual_clear"))
		{
			ZoneManager.getInstance().clearDebugItems();
			showHtml(activeChar);
		}
		return true;
	}
	
	private static void showHtml(L2PcInstance activeChar)
	{
		final String htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/admin/zone.htm");
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(htmContent);
		String yes = "<font color=\"LEVEL\">YES</font>";
		adminReply.replace("%PEACE%", (activeChar.isInsideZone(L2Character.ZONE_PEACE) ? yes : "NO"));
		adminReply.replace("%PVP%", (activeChar.isInsideZone(L2Character.ZONE_PVP) ? yes : "NO"));
		adminReply.replace("%SIEGE%", (activeChar.isInsideZone(L2Character.ZONE_SIEGE) ? yes : "NO"));
		adminReply.replace("%TOWN%", (activeChar.isInsideZone(L2Character.ZONE_TOWN) ? yes : "NO"));
		adminReply.replace("%CASTLE%", (activeChar.isInsideZone(L2Character.ZONE_CASTLE) ? yes : "NO"));
		adminReply.replace("%FORT%", (activeChar.isInsideZone(L2Character.ZONE_FORT) ? yes : "NO"));
		adminReply.replace("%HQ%", (activeChar.isInsideZone(L2Character.ZONE_HQ) ? yes : "NO"));
		adminReply.replace("%CLANHALL%", (activeChar.isInsideZone(L2Character.ZONE_CLANHALL) ? yes : "NO"));
		adminReply.replace("%LAND%", (activeChar.isInsideZone(L2Character.ZONE_LANDING) ? yes : "NO"));
		adminReply.replace("%NOLAND%", (activeChar.isInsideZone(L2Character.ZONE_NOLANDING) ? yes : "NO"));
		adminReply.replace("%NOSUMMON%", (activeChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND) ? yes : "NO"));
		adminReply.replace("%WATER%", (activeChar.isInsideZone(L2Character.ZONE_WATER) ? yes : "NO"));
		adminReply.replace("%SWAMP%", (activeChar.isInsideZone(L2Character.ZONE_SWAMP) ? yes : "NO"));
		adminReply.replace("%DANGER%", (activeChar.isInsideZone(L2Character.ZONE_DANGERAREA) ? yes : "NO"));
		adminReply.replace("%NOSTORE%", (activeChar.isInsideZone(L2Character.ZONE_NOSTORE) ? yes : "NO"));
		adminReply.replace("%SCRIPT%", (activeChar.isInsideZone(L2Character.ZONE_SCRIPT) ? yes : "NO"));
		StringBuilder zones = new StringBuilder(100);
		L2WorldRegion region = L2World.getInstance().getRegion(activeChar.getX(), activeChar.getY());
		for (L2ZoneType zone : region.getZones())
		{
			if (zone.isCharacterInZone(activeChar))
			{
				if (zone.getName() != null)
				{
					StringUtil.append(zones, zone.getName());
					StringUtil.append(zones, "<br1>");
					if (zone.getId() < 300000) // not display id for dynamic zones
						StringUtil.append(zones, "(", String.valueOf(zone.getId()), ")");
				}
				else
					StringUtil.append(zones, String.valueOf(zone.getId()));
				StringUtil.append(zones, " ");
			}
		}
		adminReply.replace("%ZLIST%", zones.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private static void getGeoRegionXY(L2PcInstance activeChar)
	{
		int worldX = activeChar.getX();
		int worldY = activeChar.getY();
		int geoX = ((((worldX - (-327680)) >> 4) >> 11) + 10);
		int geoY = ((((worldY - (-262144)) >> 4) >> 11) + 10);
		activeChar.sendMessage("GeoRegion: " + geoX + "_" + geoY + "");
	}
	
	/**
	 * 
	 * @see com.l2js.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
	 */
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
}
