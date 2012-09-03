package commands.admin;

import services.VoteManager;
import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.extensions.scripts.Scripts;
import l2rt.extensions.scripts.Scripts.ScriptClassAndMethod;
import l2rt.gameserver.TradeController;
import l2rt.gameserver.handler.AdminCommandHandler;
import l2rt.gameserver.handler.IAdminCommandHandler;
import l2rt.gameserver.instancemanager.InstancedZoneManager;
import l2rt.gameserver.instancemanager.ServerVariables;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.L2Multisell;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.Reflection;
import l2rt.gameserver.model.entity.olympiad.OlympiadDatabase;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.tables.DoorTable;
import l2rt.gameserver.tables.FishTable;
import l2rt.gameserver.tables.GmListTable;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.PetDataTable;
import l2rt.gameserver.tables.ReflectionTable;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.tables.SpawnTable;
import l2rt.gameserver.tables.StaticObjectsTable;
import l2rt.gameserver.tables.TerritoryTable;
import l2rt.util.Files;
import l2rt.util.HWID;
import l2rt.util.Strings;

public class AdminReload implements IAdminCommandHandler, ScriptFile
{
	private static enum Commands
	{
		admin_reload,
		admin_reload_multisell,
		admin_reload_gmaccess,
		admin_reload_htm,
		admin_reload_qs,
		admin_reload_qs_help,
		admin_reload_loc,
		admin_reload_skills,
		admin_reload_npc,
		admin_reload_spawn,
		admin_reload_fish,
		admin_reload_abuse,
		admin_reload_translit,
		admin_reload_shops,
		admin_reload_static,
		admin_reload_doors,
		admin_reload_pkt_logger,
		admin_reload_pets,
		admin_reload_locale,
		admin_reload_instances,
		admin_reload_hwid_bonus,
		admin_reload_nobles,
		admin_reload_vote,
		admin_reload_config
	}

	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanReload)
			return false;

		switch(command)
		{
			case admin_reload:
				break;
			case admin_reload_multisell:
			{
				try
				{
					L2Multisell.getInstance().reload();
				}
				catch(Exception e)
				{
					return false;
				}
				for(ScriptClassAndMethod handler : Scripts.onReloadMultiSell)
					activeChar.callScripts(handler.scriptClass, handler.method);
				activeChar.sendMessage("Multisell list reloaded!");
				break;
			}
			case admin_reload_gmaccess:
			{
				try
				{
					Config.loadGMAccess();
					for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
						if(!Config.EVERYBODY_HAS_ADMIN_RIGHTS)
							player.setPlayerAccess(Config.gmlist.get(player.getObjectId()));
						else
							player.setPlayerAccess(Config.gmlist.get(new Integer(0)));
				}
				catch(Exception e)
				{
					return false;
				}
				activeChar.sendMessage("GMAccess reloaded!");
				break;
			}
			case admin_reload_htm:
			{
				Files.cacheClean();
				activeChar.sendMessage("HTML cache clearned.");
				break;
			}
			case admin_reload_qs:
			{
				if(fullString.endsWith("all"))
					for(L2Player p : L2ObjectsStorage.getAllPlayersForIterate())
						reloadQuestStates(p);
				else
				{
					L2Object t = activeChar.getTarget();

					if(t != null && t.isPlayer())
					{
						L2Player p = (L2Player) t;
						reloadQuestStates(p);
					}
					else
						reloadQuestStates(activeChar);
				}
				break;
			}
			case admin_reload_qs_help:
			{
				activeChar.sendMessage("");
				activeChar.sendMessage("Quest Help:");
				activeChar.sendMessage("reload_qs_help - This Message.");
				activeChar.sendMessage("reload_qs <selected target> - reload all quest states for target.");
				activeChar.sendMessage("reload_qs <no target or target is not player> - reload quests for self.");
				activeChar.sendMessage("reload_qs all - reload quests for all players in world.");
				activeChar.sendMessage("");
				break;
			}
			case admin_reload_loc:
			{
				TerritoryTable.getInstance().reloadData();
				ZoneManager.getInstance().reload();
				GmListTable.broadcastMessageToGMs("Locations and zones reloaded.");
				break;
			}
			case admin_reload_skills:
			{
				SkillTable.getInstance().reload();
				GmListTable.broadcastMessageToGMs("Skill table reloaded by " + activeChar.getName() + ".");
				System.out.println("Skill table reloaded by " + activeChar.getName() + ".");
				break;
			}
			case admin_reload_npc:
			{
				NpcTable.getInstance().reloadAllNpc();
				GmListTable.broadcastMessageToGMs("Npc table reloaded.");
				break;
			}
			case admin_reload_spawn:
			{
				SpawnTable.getInstance().reloadAll();
				GmListTable.broadcastMessageToGMs("All npc respawned.");
				break;
			}
			case admin_reload_fish:
			{
				FishTable.getInstance().reload();
				GmListTable.broadcastMessageToGMs("Fish table reloaded.");
				break;
			}
			case admin_reload_abuse:
			{
				Config.abuseLoad();
				GmListTable.broadcastMessageToGMs("Abuse reloaded.");
				break;
			}
			case admin_reload_translit:
			{
				Strings.reload();
				GmListTable.broadcastMessageToGMs("Translit reloaded.");
				break;
			}
			case admin_reload_shops:
			{
				TradeController.reload();
				GmListTable.broadcastMessageToGMs("Shops reloaded.");
				break;
			}
			case admin_reload_static:
			{
				StaticObjectsTable.getInstance().reloadStaticObjects();
				GmListTable.broadcastMessageToGMs("Static objects table reloaded.");
				break;
			}
			case admin_reload_doors:
			{
				DoorTable.getInstance().respawn();
				GmListTable.broadcastMessageToGMs("Door table reloaded.");
				break;
			}
			case admin_reload_pkt_logger:
			{
				try
				{
					Config.reloadPacketLoggerConfig();
					activeChar.sendMessage("Packet Logger setting reloaded");
				}
				catch(Exception e)
				{
					activeChar.sendMessage("Failed reload Packet Logger setting. Check stdout for error!");
				}
				break;
			}
			case admin_reload_pets:
			{
				PetDataTable.reload();
				GmListTable.broadcastMessageToGMs("PetDataTable reloaded");
				break;
			}
			case admin_reload_locale:
			{
				CustomMessage.reload();
				GmListTable.broadcastMessageToGMs("Localization reloaded");
				break;
			}
			case admin_reload_instances: 
			{
				InstancedZoneManager.getInstance().reload();
				GmListTable.broadcastMessageToGMs("Instanced zones reloaded");

				Reflection r = ReflectionTable.SOD_REFLECTION_ID == 0 ? null : ReflectionTable.getInstance().get(ReflectionTable.SOD_REFLECTION_ID);
				if(r != null)
					r.collapse();
				ServerVariables.unset("SoD_id");
				break;
			}
			case admin_reload_hwid_bonus:
			{
				HWID.reloadBannedHWIDs();
				HWID.reloadBonusHWIDs();
				GmListTable.broadcastMessageToGMs("HWID bonus/bans reloaded");
				break;
			}
			case admin_reload_nobles:
			{
				OlympiadDatabase.loadNobles();
				OlympiadDatabase.loadNoblesRank();
				break;
			}
			case admin_reload_vote:
			{
				VoteManager.load();
				break;
			}
			case admin_reload_config:
			{
				Config.load();
				GmListTable.broadcastMessageToGMs("Configs reloaded");
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("data/html/admin/admserver.htm"));
				break;
			}
		}
		activeChar.sendPacket(new NpcHtmlMessage(5).setFile("data/html/admin/reload.htm"));
		return true;
	}

	private void reloadQuestStates(L2Player p)
	{
		for(QuestState qs : p.getAllQuestsStates())
			p.delQuestState(qs.getQuest().getName());
		Quest.playerEnter(p);
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