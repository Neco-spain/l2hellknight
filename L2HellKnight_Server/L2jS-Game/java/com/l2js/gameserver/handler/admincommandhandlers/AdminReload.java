/**
 * 
 */
package com.l2js.gameserver.handler.admincommandhandlers;

import java.util.logging.Logger;

import com.l2js.Config;
import com.l2js.config.events.ConfigDM;
import com.l2js.config.events.ConfigEvents;
import com.l2js.config.events.ConfigHitman;
import com.l2js.config.events.ConfigLM;
import com.l2js.config.events.ConfigTvT;
import com.l2js.config.main.*;
import com.l2js.config.mods.*;
import com.l2js.config.network.ConfigCommunityServer;
import com.l2js.config.network.ConfigGameServer;
import com.l2js.config.network.ConfigHexid;
import com.l2js.config.network.ConfigIPConfig;
import com.l2js.config.network.ConfigTelnet;
import com.l2js.config.scripts.ConfigBufferNpc;
import com.l2js.config.scripts.ConfigRankNpc;
import com.l2js.config.security.ConfigProtectionAdmin;
import com.l2js.config.security.ConfigProtectionBot;
import com.l2js.config.security.ConfigProtectionBox;
import com.l2js.gameserver.cache.HtmCache;
import com.l2js.gameserver.datatables.*;
import com.l2js.gameserver.handler.IAdminCommandHandler;
import com.l2js.gameserver.instancemanager.Manager;
import com.l2js.gameserver.instancemanager.QuestManager;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.network.clientpackets.Say2;

/**
 * @author L0ngh0rn
 */
public class AdminReload implements IAdminCommandHandler
{
	private static Logger			_log			= Logger.getLogger(AdminReload.class.getName());

	private static final String[]	ADMIN_COMMANDS	= {
			"admin_reload", "admin_reload_config"
													};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		String[] cmd = command.split(" ");
		if (cmd[0].equals("admin_reload_config"))
		{
			if ((cmd.length != 2))
				AdminHelpPage.showHelpPage(activeChar, "reload_config_01.htm");
			else
			{
				final String op = cmd[1];
				try
				{
					if (op.equals("all"))
					{
						Config.loadAll();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All configs reloaded!");
					}
					else if (op.equals("all_events"))
					{
						Config.loadEventsConfigs();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Events configs reloaded!");
					}
					else if (op.equals("all_main"))
					{
						Config.loadMainConfigs();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Main configs reloaded!");
					}
					else if (op.equals("all_mods"))
					{
						Config.loadModsConfigs();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Mods configs reloaded!");
					}
					else if (op.equals("all_network"))
					{
						Config.loadNetworkConfigs();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Network configs reloaded!");
					}
					else if (op.equals("all_scripts"))
					{
						Config.loadScriptConfigs();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Scripts configs reloaded!");
					}
					else if (op.equals("all_security"))
					{
						Config.loadSecurityConfigs();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Security configs reloaded!");
					}
					else if (op.equals("all_versionning"))
					{
						Config.loadVersionningConfigs();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Versionning configs reloaded!");
					}
					// Events Folder
					else if (op.equalsIgnoreCase("DM"))
					{
						ConfigDM.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All DM configs reloaded!");
					}
					else if (op.equalsIgnoreCase("Events"))
					{
						ConfigEvents.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Events configs reloaded!");
					}
					else if (op.equalsIgnoreCase("Hitman"))
					{
						ConfigHitman.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Hitman configs reloaded!");
					}
					else if (op.equalsIgnoreCase("LM"))
					{
						ConfigLM.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All LM configs reloaded!");
					}
					else if (op.equalsIgnoreCase("TvT"))
					{
						ConfigTvT.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All TvT configs reloaded!");
					}
					// Main Folder
					else if (op.equalsIgnoreCase("Character"))
					{
						ConfigCharacter.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Character configs reloaded!");
					}
					else if (op.equalsIgnoreCase("ChatFilter"))
					{
						ConfigChatFilter.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All ChatFilter configs reloaded!");
					}
					else if (op.equalsIgnoreCase("Feature"))
					{
						ConfigFeature.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Feature configs reloaded!");
					}
					else if (op.equalsIgnoreCase("FloodProtector"))
					{
						ConfigFloodProtector.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All FloodProtector configs reloaded!");
					}
					else if (op.equalsIgnoreCase("General"))
					{
						ConfigGeneral.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All General configs reloaded!");
					}
					else if (op.equalsIgnoreCase("GrandBoss"))
					{
						ConfigGrandBoss.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All GrandBoss configs reloaded!");
					}
					else if (op.equalsIgnoreCase("IDFactory"))
					{
						ConfigIDFactory.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All IDFactory configs reloaded!");
					}
					else if (op.equalsIgnoreCase("MMO"))
					{
						ConfigMMO.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All MMO configs reloaded!");
					}
					else if (op.equalsIgnoreCase("NPC"))
					{
						ConfigNPC.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All NPC configs reloaded!");
					}
					else if (op.equalsIgnoreCase("Olympiad"))
					{
						ConfigOlympiad.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Olympiad configs reloaded!");
					}
					else if (op.equalsIgnoreCase("PvP"))
					{
						ConfigPvP.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All PvP configs reloaded!");
					}
					else if (op.equalsIgnoreCase("Rates"))
					{
						ConfigRates.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Rates configs reloaded!");
					}
					else if (op.equalsIgnoreCase("Siege"))
					{
						ConfigSiege.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Siege configs reloaded!");
					}
					// Mods Folder
					else if (op.equalsIgnoreCase("Banking"))
					{
						ConfigBanking.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Banking configs reloaded!");
					}
					else if (op.equalsIgnoreCase("Champion"))
					{
						ConfigChampion.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Champion configs reloaded!");
					}
					else if (op.equalsIgnoreCase("Chars"))
					{
						ConfigChars.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Chars configs reloaded!");
					}
					else if (op.equalsIgnoreCase("Chat"))
					{
						ConfigChat.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Chat configs reloaded!");
					}
					else if (op.equalsIgnoreCase("Classes"))
					{
						ConfigClasses.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Classes configs reloaded!");
					}
					else if (op.equalsIgnoreCase("Custom"))
					{
						ConfigCustom.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Custom configs reloaded!");
					}
					else if (op.equalsIgnoreCase("GraciaSeeds"))
					{
						ConfigGraciaSeeds.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All GraciaSeeds configs reloaded!");
					}
					else if (op.equalsIgnoreCase("L2jMods"))
					{
						ConfigL2jMods.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All L2jMods configs reloaded!");
					}
					else if (op.equalsIgnoreCase("Message"))
					{
						ConfigMessage.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Message configs reloaded!");
					}
					else if (op.equalsIgnoreCase("OfflineTrade"))
					{
						ConfigOfflineTrade.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All OfflineTrade configs reloaded!");
					}
					else if (op.equalsIgnoreCase("Wedding"))
					{
						ConfigWedding.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Wedding configs reloaded!");
					}
					// Network Folder
					else if (op.equalsIgnoreCase("CommunityServer"))
					{
						ConfigCommunityServer.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All CommunityServer configs reloaded!");
					}
					else if (op.equalsIgnoreCase("GameServer"))
					{
						ConfigGameServer.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All GameServer configs reloaded!");
					}
					else if (op.equalsIgnoreCase("Hexid"))
					{
						ConfigHexid.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Hexid configs reloaded!");
					}
					else if (op.equalsIgnoreCase("IPConfig"))
					{
						ConfigIPConfig.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All IPConfig configs reloaded!");
					}
					else if (op.equalsIgnoreCase("Telnet"))
					{
						ConfigTelnet.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Telnet configs reloaded!");
					}
					// Scripts Folder
					else if (op.equalsIgnoreCase("BufferNpc"))
					{
						ConfigBufferNpc.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All BufferNpc configs reloaded!");
					}
					else if (op.equalsIgnoreCase("RankNpc"))
					{
						ConfigRankNpc.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All RankNpc configs reloaded!");
					}
					// Security Folder
					else if (op.equalsIgnoreCase("ProtectionAdmin"))
					{
						ConfigProtectionAdmin.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All ProtectionAdmin configs reloaded!");
					}
					else if (op.equalsIgnoreCase("ProtectionBot"))
					{
						ConfigProtectionBot.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All ProtectionBot configs reloaded!");
					}
					else if (op.equalsIgnoreCase("ProtectionBox"))
					{
						ConfigProtectionBox.loadConfig();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All ProtectionBox configs reloaded!");
					}
					else
					{
						activeChar.sendMessage("This command does not exist!");
						activeChar.sendMessage("Usage: //reload_config <Name File>");
						AdminHelpPage.showHelpPage(activeChar, "reload_config_01.htm");
					}

					activeChar
							.sendMessage("WARNING: There are several known issues regarding this feature. Reloading server data during runtime is STRONGLY NOT RECOMMENDED for live servers, just for developing environments.");
				}
				catch (Exception e)
				{
					AdminHelpPage.showHelpPage(activeChar, "reload_config_01.htm");
					activeChar.sendMessage("An error occured while reloading config " + op + " !");
					_log.warning("An error occured while reloading config " + cmd[0] + " " + op + ": " + e);
				}
			}
		}
		else if (cmd[0].equals("admin_reload"))
		{
			if ((cmd.length != 2))
				AdminHelpPage.showHelpPage(activeChar, "reload.htm");
			else
			{
				final String op = cmd[1];
				try
				{
					if (op.equals("access"))
					{
						AccessLevels.getInstance().reloadAccessLevels();
						AdminCommandAccessRights.getInstance().reloadAdminCommandAccessRights();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "Access Rights have been reloaded!");
					}
					else if (op.equals("config"))
					{
						Config.load();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Config Settings have been reloaded!");
					}
					else if (op.equals("door"))
					{
						DoorTable.getInstance().reloadAll();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Doors have been reloaded!");
					}
					else if (op.equals("htm"))
					{
						HtmCache.getInstance().reload();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " MB on "
								+ HtmCache.getInstance().getLoadedFiles() + " files loaded!");
					}
					else if (op.equals("instancemanager"))
					{
						Manager.reloadAll();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Instance Manager has been reloaded!");
					}
					else if (op.equals("item"))
					{
						ItemTable.getInstance().reload();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "Item Templates have been reloaded!");
					}
					else if (op.equals("modsbuffer"))
					{
						ModsBufferSkillTable.getInstance().reloadBufferSkillTable();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Buff Skill have been reloaded!");
					}
					else if (op.equals("multisell"))
					{
						MultiSell.getInstance().reload();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Multisells have been reloaded!");
					}
					else if (op.equals("npc"))
					{
						NpcTable.getInstance().reloadAllNpc();
						QuestManager.getInstance().reloadAllQuests();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All NPCs have been reloaded!");
					}
					else if (op.equals("npcwalkers"))
					{
						NpcWalkerRoutesTable.getInstance().load();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "NPC Walker Routes have been reloaded!");
					}
					else if (op.equals("quests"))
					{
						QuestManager.getInstance().reloadAllQuests();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Quests have been reloaded!");
					}
					else if (op.equals("skill"))
					{
						SkillTable.getInstance().reload();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "All Skills have been reloaded!");
					}
					else if (op.equals("teleport"))
					{
						TeleportLocationTable.getInstance().reloadAll();
						activeChar.sendChatMessage(0, Say2.ALL, "SYS", "Teleport Locations have been reloaded");
					}
					else
					{
						activeChar.sendMessage("This command does not exist!");
						activeChar
								.sendMessage("Usage: //reload <access|config|door|htm|instancemanager|item|modsbuffer|multisell|npc|npcwalkers|quests|skill|teleport>");
						AdminHelpPage.showHelpPage(activeChar, "reload.htm");
					}

					activeChar
							.sendMessage("WARNING: There are several known issues regarding this feature. Reloading server data during runtime is STRONGLY NOT RECOMMENDED for live servers, just for developing environments.");
				}
				catch (Exception e)
				{
					AdminHelpPage.showHelpPage(activeChar, "reload.htm");
					activeChar.sendMessage("An error occured while reloading " + op + " !");
					_log.warning("An error occured while reloading " + cmd[0] + " " + op + ": " + e);
				}
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
