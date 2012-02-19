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

package custom.TopPvpPk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Level;

import javolution.text.TextBuilder;

import l2.brick.Config;
import l2.brick.L2DatabaseFactory;
import l2.brick.gameserver.ThreadPoolManager;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.brick.util.L2FastList;

public class TopPvpPk extends Quest
{
	private static final int NPC_ID = Config.TOPID; //1
	private static final boolean DEBUG = Config.TOPDEBUG; //false
	private static List<TopPVP> TOP_PVP = new L2FastList<TopPVP>();
	private static List<TopPK> TOP_PK = new L2FastList<TopPK>();
	private static final long refreshTime = 5 * 60 * 1000;

	public TopPvpPk(int questid, String name, String descr)
	{
		super(questid, name, descr);
		addFirstTalkId(NPC_ID);
		addTalkId(NPC_ID);
		addStartNpc(NPC_ID);

		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new reloadTop(), 1000, refreshTime);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "main.htm";
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		return "main.htm";
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("toppvp"))
			sendTopPVP(player);
		else if (event.equalsIgnoreCase("toppk"))
			sendTopPK(player);
		return null;
	}

	private void sendTopPK(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>TOP 25 PK</title><body><br><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><table width=260>");

		for (TopPK pk : TOP_PK)
		{
			String name = pk.getName();
			int pk1 = pk.getpk();
			tb.append("<tr><td><font color=\"00C3FF\">" + name + "</color>:</td><td><font color=\"32C332\">" + pk1 + "</color></td></tr>");
		}

		tb.append("</table><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br></center></body></html>");

		NpcHtmlMessage msg = new NpcHtmlMessage(NPC_ID);
		msg.setHtml(tb.toString());
		player.sendPacket(msg);
	}

	private void sendTopPVP(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>TOP 25 PVP</title><body><br><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><table width=260>");

		for (TopPVP pvp : TOP_PVP)
		{
			String name = pvp.getName();
			int pvp1 = pvp.getpvp();
			tb.append("<tr><td><font color=\"00C3FF\">" + name + "</color>:</td><td><font color=\"32C332\">" + pvp1 + "</color></td></tr>");
		}

		tb.append("</table><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br></center></body></html>");

		NpcHtmlMessage msg = new NpcHtmlMessage(NPC_ID);
		msg.setHtml(tb.toString());
		player.sendPacket(msg);
	}

	private class reloadTop implements Runnable
	{
		public void run()
		{

			TOP_PK.clear();
			TOP_PVP.clear();

			Connection con = null;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT char_name,pvpkills FROM characters WHERE pvpkills>0 and accesslevel=0 order by pvpkills desc limit 25");

				ResultSet rset = statement.executeQuery();
				while (rset.next())
				{
					TopPVP pvp = new TopPVP();
					pvp.setTopPvp(rset.getString("char_name"), rset.getInt("pvpkills"));
					TOP_PVP.add(pvp);
				}

				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not restore top pvp: " + e.getMessage(), e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT char_name,pkkills FROM characters WHERE pkkills>0 and accesslevel=0 order by pkkills desc limit 25");

				ResultSet rset = statement.executeQuery();

				while (rset.next())
				{
					TopPK pk = new TopPK();
					pk.setTopPk(rset.getString("char_name"), rset.getInt("pkkills"));
					TOP_PK.add(pk);
				}

				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not restore top pk: " + e.getMessage(), e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
			if (DEBUG)
			{
				_log.info("Top Pvp/Pk Loaded: " + TOP_PVP.size() + " Pvps");
				_log.info("Top Pvp/Pk Loaded: " + TOP_PK.size() + " Pks");
			}
		}
	}

	private class TopPK
	{
		private String getcharName = null;
		private int gettopPk = 0;
		private void setTopPk(String char_name, int pkkills)
		{
			getcharName = char_name;
			gettopPk = pkkills;
		}

		private String getName()
		{
			return getcharName;
		}

		private int getpk()
		{
			return gettopPk;
		}
	}

	private class TopPVP
	{
		private String getcharName = null;
		private int gettopPvp = 0;

		private void setTopPvp(String char_name, int pvpkills)
		{
			getcharName = char_name;
			gettopPvp = pvpkills;
		}

		private String getName()
		{
			return getcharName;
		}

		private int getpvp()
		{
			return gettopPvp;
		}
	}

	public static void main(String[] args)
	{
		new TopPvpPk(-1, "TopPvpPk", "custom");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Custom: TopPvpPk");
	}
}