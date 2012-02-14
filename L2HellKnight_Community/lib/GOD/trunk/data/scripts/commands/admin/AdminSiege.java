package commands.admin;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.handler.AdminCommandHandler;
import l2rt.gameserver.handler.IAdminCommandHandler;
import l2rt.gameserver.instancemanager.CastleManager;
import l2rt.gameserver.instancemanager.ClanHallManager;
import l2rt.gameserver.instancemanager.FortressManager;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.model.entity.residence.ClanHall;
import l2rt.gameserver.model.entity.residence.Fortress;
import l2rt.gameserver.model.entity.residence.Residence;
import l2rt.gameserver.model.entity.siege.SiegeClan;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiegeDatabase;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.tables.ClanTable;

@SuppressWarnings("unused")
public class AdminSiege implements IAdminCommandHandler, ScriptFile
{
	protected static Logger _log = Logger.getLogger(AdminSiege.class.getName());

	private static enum Commands
	{
		admin_siege,
		admin_add_attacker,
		admin_add_defender,
		admin_add_guard,
		admin_list_siege_clans,
		admin_clear_siege_list,
		admin_move_defenders,
		admin_spawn_doors,
		admin_endsiege,
		admin_startsiege,
		admin_setcastle,
		admin_castledel,
		admin_territorysiege,
		admin_startterritorysiege,
		admin_stopterritorysiege,
		admin_addterritorymember,
		admin_addterritoryclan,
		admin_clearterritorylist,
		admin_listterritorymembers
	}

	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanEditNPC)
			return false;

		L2Object target = activeChar.getTarget();
		L2Player player = activeChar;
		if(target != null && target.isPlayer())
			player = (L2Player) target;

		StringTokenizer st = new StringTokenizer(fullString, " ");
		fullString = st.nextToken();

		if(fullString.equalsIgnoreCase("admin_territorysiege"))
		{
			showTerritorySiegePage(activeChar);
			return true;
		}
		else if(fullString.equalsIgnoreCase("admin_startterritorysiege"))
		{
			TerritorySiege.startSiege();
			showTerritorySiegePage(activeChar);
			return true;
		}
		else if(fullString.equalsIgnoreCase("admin_stopterritorysiege"))
		{
			TerritorySiege.endSiege();
			showTerritorySiegePage(activeChar);
			return true;
		}
		else if(fullString.equalsIgnoreCase("admin_addterritorymember"))
		{
			if(!st.hasMoreTokens())
			{
				activeChar.sendMessage("Incorrect territory number!");
				showTerritorySiegePage(activeChar);
				return false;
			}
			int territoryId = Integer.parseInt(st.nextToken());
			if(territoryId < 1 || territoryId > 9)
			{
				activeChar.sendMessage("Incorrect territory number!");
				showTerritorySiegePage(activeChar);
				return false;
			}
			TerritorySiege.getPlayers().put(player.getObjectId(), territoryId);
			TerritorySiegeDatabase.saveSiegeMember(player.getObjectId(), territoryId, 0);
			showTerritorySiegePage(activeChar);
			return true;
		}
		else if(fullString.equalsIgnoreCase("admin_addterritoryclan"))
		{
			if(!st.hasMoreTokens())
			{
				activeChar.sendMessage("Incorrect territory number!");
				showTerritorySiegePage(activeChar);
				return false;
			}
			int territoryId = Integer.parseInt(st.nextToken());
			if(territoryId < 1 || territoryId > 9)
			{
				activeChar.sendMessage("Incorrect territory number!");
				showTerritorySiegePage(activeChar);
				return false;
			}
			L2Clan clan = player.getClan();
			if(clan == null)
			{
				activeChar.sendMessage("Target must be a clan member!");
				showTerritorySiegePage(activeChar);
				return false;
			}
			TerritorySiege.getClans().put(new SiegeClan(clan.getClanId(), null), territoryId);
			TerritorySiegeDatabase.saveSiegeMember(clan.getClanId(), territoryId, 1);
			showTerritorySiegePage(activeChar);
			return true;
		}
		else if(fullString.equalsIgnoreCase("admin_clearterritorylist"))
		{
			TerritorySiege.getPlayers().clear();
			TerritorySiege.getClans().clear();
			TerritorySiegeDatabase.saveSiegeMembers();
			showTerritorySiegePage(activeChar);
			return true;
		}

		Residence siegeUnit = null;
		int siegeUnitId = 0;
		if(st.hasMoreTokens())
			siegeUnitId = Integer.parseInt(st.nextToken());

		if(siegeUnitId != 0)
		{
			siegeUnit = CastleManager.getInstance().getCastleByIndex(siegeUnitId);
			if(siegeUnit == null)
				siegeUnit = FortressManager.getInstance().getFortressByIndex(siegeUnitId);
			if(siegeUnit == null)
				siegeUnit = ClanHallManager.getInstance().getClanHall(siegeUnitId);
		}

		if(siegeUnit == null || siegeUnit.getId() < 0 || siegeUnit.getSiege() == null)
			showSiegeUnitSelectPage(activeChar);
		else
		{
			if(fullString.equalsIgnoreCase("admin_add_attacker"))
				siegeUnit.getSiege().registerAttacker(player, true);
			else if(fullString.equalsIgnoreCase("admin_add_defender"))
				siegeUnit.getSiege().registerDefender(player, true);
			else if(fullString.equalsIgnoreCase("admin_add_guard"))
			{
				// Get value
				String val = "";
				if(st.hasMoreTokens())
					val = st.nextToken();

				if(!val.equals(""))
					try
					{
						int npcId = Integer.parseInt(val);
						int loc_id = Integer.parseInt(val);
						siegeUnit.getSiege().getSiegeGuardManager().addSiegeGuard(activeChar, npcId, loc_id);
					}
					catch(Exception e)
					{
						activeChar.sendMessage("Value entered for Npc Id wasn't an integer");
					}
				else
					activeChar.sendMessage("Missing Npc Id");
			}
			else if(fullString.equalsIgnoreCase("admin_clear_siege_list"))
				siegeUnit.getSiege().getDatabase().clearSiegeClan();
			else if(fullString.equalsIgnoreCase("admin_endsiege"))
				siegeUnit.getSiege().endSiege();
			else if(fullString.equalsIgnoreCase("admin_list_siege_clans"))
			{
				siegeUnit.getSiege().listRegisterClan(activeChar);
				return true;
			}
			else if(fullString.equalsIgnoreCase("admin_move_defenders"))
				activeChar.sendPacket(Msg.NOT_WORKING_PLEASE_TRY_AGAIN_LATER);
			else if(fullString.equalsIgnoreCase("admin_setcastle"))
			{
				if(player.getClan() == null)
					activeChar.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
				else
				{
					siegeUnit.changeOwner(player.getClan());
					_log.fine(siegeUnit.getName() + " owned by clan " + player.getClan().getName());
				}
			}
			else if(fullString.equalsIgnoreCase("admin_castledel"))
				siegeUnit.changeOwner(null);
			else if(fullString.equalsIgnoreCase("admin_spawn_doors"))
				siegeUnit.spawnDoor();
			else if(fullString.equalsIgnoreCase("admin_startsiege"))
				siegeUnit.getSiege().startSiege();

			showSiegePage(activeChar, siegeUnit);
		}

		return true;
	}

	public void showSiegeUnitSelectPage(L2Player activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center><font color=\"LEVEL\">Siege Units</font></center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table><br>");

		replyMSG.append("<br><a action=\"bypass -h admin_territorysiege \">Territory siege menu</a><br>");

		replyMSG.append("<table width=260>");
		replyMSG.append("<tr><td>Unit Name</td><td>Owner</td></tr>");

		for(Castle castle : CastleManager.getInstance().getCastles().values())
			if(castle != null)
			{
				replyMSG.append("<tr><td>");
				replyMSG.append("<a action=\"bypass -h admin_siege " + castle.getId() + "\">" + castle.getName() + "</a>");
				replyMSG.append("</td><td>");

				L2Clan owner = castle.getOwnerId() == 0 ? null : ClanTable.getInstance().getClan(castle.getOwnerId());
				if(owner == null)
					replyMSG.append("NPC");
				else
					replyMSG.append(owner.getName());

				replyMSG.append("</td></tr>");
			}

		for(Fortress fortress : FortressManager.getInstance().getFortresses().values())
			if(fortress != null)
			{
				replyMSG.append("<tr><td>");
				replyMSG.append("<a action=\"bypass -h admin_siege " + fortress.getId() + "\">" + fortress.getName() + "</a>");
				replyMSG.append("</td><td>");

				L2Clan owner = fortress.getOwnerId() == 0 ? null : ClanTable.getInstance().getClan(fortress.getOwnerId());
				if(owner == null)
					replyMSG.append("NPC");
				else
					replyMSG.append(owner.getName());

				replyMSG.append("</td></tr>");
			}

		for(ClanHall clanhall : ClanHallManager.getInstance().getClanHalls().values())
			if(clanhall != null && clanhall.getSiege() != null)
			{
				replyMSG.append("<tr><td>");
				replyMSG.append("<a action=\"bypass -h admin_siege " + clanhall.getId() + "\">" + clanhall.getName() + "</a>");
				replyMSG.append("</td><td>");

				L2Clan owner = clanhall.getOwnerId() == 0 ? null : ClanTable.getInstance().getClan(clanhall.getOwnerId());
				if(owner == null)
					replyMSG.append("NPC");
				else
					replyMSG.append(owner.getName());

				replyMSG.append("</td></tr>");
			}

		replyMSG.append("</table>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	public void showSiegePage(L2Player activeChar, Residence siegeUnit)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Siege Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_siege\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<center>");
		replyMSG.append("<br><br><br>Siege Unit: " + siegeUnit.getName() + "<br><br>");
		replyMSG.append("Unit Owner: ");

		L2Clan owner = siegeUnit.getOwnerId() == 0 ? null : ClanTable.getInstance().getClan(siegeUnit.getOwnerId());
		if(owner == null)
			replyMSG.append("NPC");
		else
			replyMSG.append(owner.getName());

		replyMSG.append("<br><br><table>");
		replyMSG.append("<tr><td><button value=\"Add Attacker\" action=\"bypass -h admin_add_attacker " + siegeUnit.getId() + "\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Add Defender\" action=\"bypass -h admin_add_defender " + siegeUnit.getId() + "\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		replyMSG.append("<tr><td><button value=\"List Clans\" action=\"bypass -h admin_list_siege_clans " + siegeUnit.getId() + "\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Clear List\" action=\"bypass -h admin_clear_siege_list " + siegeUnit.getId() + "\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td><button value=\"Move Defenders\" action=\"bypass -h admin_move_defenders " + siegeUnit.getId() + "\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Spawn Doors\" action=\"bypass -h admin_spawn_doors " + siegeUnit.getId() + "\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td><button value=\"Start Siege\" action=\"bypass -h admin_startsiege " + siegeUnit.getId() + "\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"End Siege\" action=\"bypass -h admin_endsiege " + siegeUnit.getId() + "\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td><button value=\"Give Unit\" action=\"bypass -h admin_setcastle " + siegeUnit.getId() + "\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Take Unit\" action=\"bypass -h admin_castledel " + siegeUnit.getId() + "\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>NpcId: <edit var=\"value\" width=40>");
		replyMSG.append("<td><button value=\"Add Guard\" action=\"bypass -h admin_add_guard " + siegeUnit.getId() + " $value\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("</center>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	public void showTerritorySiegePage(L2Player activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Territory Siege Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_siege\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<center>");

		replyMSG.append("<br><br><table>");
		replyMSG.append("<tr><td>Territory Id: <edit var=\"value\" width=40></td><td></td></tr>");
		replyMSG.append("<tr><td><button value=\"Add Player\" action=\"bypass -h admin_addterritorymember $value\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Add Clan\" action=\"bypass -h admin_addterritoryclan $value\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		replyMSG.append("<tr><td><button value=\"List Members\" action=\"bypass -h admin_listterritorymembers\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"Clear List\" action=\"bypass -h admin_clearterritorylist\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		replyMSG.append("</table>");

		replyMSG.append("<br>");

		replyMSG.append("<table>");
		replyMSG.append("<tr><td><button value=\"Start Siege\" action=\"bypass -h admin_startterritorysiege\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td><button value=\"End Siege\" action=\"bypass -h admin_stopterritorysiege\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br>");

		replyMSG.append("</center>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	public void onLoad()
	{
		AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}