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

package other.BossRespawn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import javolution.text.TextBuilder;

import l2.hellknight.L2DatabaseFactory;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.instancemanager.GrandBossManager;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.hellknight.gameserver.templates.StatsSet;
import l2.hellknight.util.L2FastList;


public class BossRespawn extends Quest
{
	private static final int NPC_ID = 7777;
	private static final boolean GM_ONLY = true;
	private static final boolean DEBUG = false;
	private static List<Grandboss> GRAND_BOSSES = new L2FastList<Grandboss>();
	private static List<Raidboss> RAID_BOSSES = new L2FastList<Raidboss>();
	private static final long refreshTime = 5 * 60 * 1000;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	public BossRespawn(int questid, String name, String descr)
	{
		super(questid, name, descr);
		addFirstTalkId(NPC_ID);
		addTalkId(NPC_ID);
		addStartNpc(NPC_ID);
		
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new reloadBosses(), 1000, refreshTime);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "home.htm";
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{	
		return "main.htm";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("grandbosses"))
			sendGrandBosses(player);
		else if (event.equalsIgnoreCase("raidbosses"))
			return "raidbosses.htm";
		else if (event.startsWith("raidboss_"))
		{
			try
			{
				int level = Integer.parseInt(event.substring(9));
				sendRaidBosses(player, level);
			}
			catch (NumberFormatException nfe) {}
		}
		
		return null;
	}
	
	private void sendRaidBosses(L2PcInstance player, int bosslevel)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Grand Boss Info</title><body><br><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><table width=260>");
		
		for (Raidboss rb : RAID_BOSSES)
		{
			String name = rb.getName();
			int level = rb.getLevel();
			int min = bosslevel;
			int max = min + 10;
			if (level >= min && level <= max)
			{
				long delay = rb.getRespawn();
				long currentTime = System.currentTimeMillis();
				
				if (delay <= currentTime)
					tb.append("<tr><td><font color=\"00C3FF\">" + name + " (" + level + ")</color>:</td><td><font color=\"32C332\">Is Alive</color></td></tr>");
				
				else
					tb.append("<tr><td><font color=\"00C3FF\">" + name + " (" + level + ")</color>:</td><td><font color=\"9CC300\">" + (player.isGM() && GM_ONLY ? sdf.format(new Date(delay)) : "Dead") + "</color></td></tr>");
			}
		}
		
		tb.append("</table><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br></center></body></html>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(NPC_ID);
		msg.setHtml(tb.toString());
		player.sendPacket(msg);
	}
	
	private void sendGrandBosses(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Grand Boss Info</title><body><br><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><table width=260>");
		
		for (Grandboss boss : GRAND_BOSSES)
		{
			String name = boss.getName();
			StatsSet stats = GrandBossManager.getInstance().getStatsSet(boss.getId());
			if (stats == null)
			{
				player.sendMessage("Stats for GrandBoss " + boss.getId() + " not found!");
				continue;
			}
			
			long delay = stats.getLong("respawn_time");
			long currentTime = System.currentTimeMillis();
			
			if (delay <= currentTime)
				tb.append("<tr><td><font color=\"00C3FF\">" + name + "</color>:</td><td><font color=\"32C332\">Is Alive</color></td></tr>");
			
			else
				tb.append("<tr><td><font color=\"00C3FF\">" + name + "</color>:</td><td><font color=\"9CC300\">" + (player.isGM() && GM_ONLY ? sdf.format(new Date(delay)) : "Dead") + "</color></td></tr>");
		}
		
		tb.append("</table><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br></center></body></html>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(NPC_ID);
		msg.setHtml(tb.toString());
		player.sendPacket(msg);
	}
	
	private class reloadBosses implements Runnable
	{
		public void run()
		{
			
			RAID_BOSSES.clear();
			GRAND_BOSSES.clear();
			
			Connection con = null;
			
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT `n`.`name`, `g`.`boss_id` FROM `npc` as `n` CROSS JOIN `grandboss_data` AS `g` ON `n`.`id` = `g`.`boss_id` GROUP BY `n`.`name` ORDER BY `g`.`respawn_time` DESC");
				
				ResultSet rset = statement.executeQuery();
				while (rset.next())
				{
					Grandboss boss = new Grandboss();
					boss.setGrandboss(rset.getInt("boss_id"), rset.getString("name"));
					GRAND_BOSSES.add(boss);					
				}
				
				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not restore grand bosses: " + e.getMessage(), e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
			
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT `n`.`level`, `n`.`name`, `r`.`respawn_time` FROM `npc` as `n` CROSS JOIN `raidboss_spawnlist` AS `r` ON `n`.`id` = `r`.`boss_id` ORDER BY `n`.`level`");
				
				ResultSet rset = statement.executeQuery();
				
				while (rset.next())
				{
					Raidboss rb = new Raidboss();
					rb.setRaidboss(rset.getString("name"), rset.getInt("level") , rset.getLong("respawn_time"));
					RAID_BOSSES.add(rb);
				}
				
				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not restore raid bosses: " + e.getMessage(), e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
			if (DEBUG)
			{
				_log.info("Boss Respawn last update: " + sdf.format(new Date(System.currentTimeMillis())));
				_log.info("Boss Respawn Loaded: " + GRAND_BOSSES.size() + " Grand Bosses");
				_log.info("Boss Respawn Loaded: " + RAID_BOSSES.size() + " Raid Bosses");
				_log.info("Boss Respawn next update: " + sdf.format(new Date(System.currentTimeMillis() + refreshTime)));
			}
		}
	}
	
	private class Raidboss
	{
		private String bossName = null;
		private long bossRespawn = 0;
		private int bossLevel = 0;
		private void setRaidboss(String name, int level, long respawn)
		{
			bossName = name;
			bossLevel = level;
			bossRespawn = respawn;
		}
		
		private String getName()
		{
			return bossName;
		}
		
		private int getLevel()
		{
			return bossLevel;
		}
		
		private long getRespawn()
		{
			return bossRespawn;
		}
	}
	
	private class Grandboss
	{
		private int bossId = 0;
		private String bossName = null;
		
		private void setGrandboss(int id, String name)
		{
			bossId = id;
			bossName = name;
		}
		
		private int getId()
		{
			return bossId;
		}
		
		private String getName()
		{
			return bossName;
		}
	}
	
	public static void main(String[] args)
	{
		new BossRespawn(-1, "BossRespawn", "other");
	}
}