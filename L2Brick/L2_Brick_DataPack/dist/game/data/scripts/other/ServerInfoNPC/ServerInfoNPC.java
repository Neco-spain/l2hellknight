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
package other.ServerInfoNPC;

import l2.brick.Config;
import l2.brick.gameserver.instancemanager.QuestManager;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;

public class ServerInfoNPC extends Quest
{

	private final static int NPC = Config.SERVERINFO_NPC_ID;

	public ServerInfoNPC(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(NPC);
		addStartNpc(NPC);
		addTalkId(NPC);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		final String eventSplit[] = event.split(" ");
		if (eventSplit[0].equalsIgnoreCase("redirect"))
		{
			if (eventSplit[1].equalsIgnoreCase("main"))
				htmltext = pageIndex("Hello, " + player.getName() + ".");
			if (eventSplit[1].equalsIgnoreCase("page"))
				htmltext = pageSub(Integer.valueOf(eventSplit[2]));
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			final Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		htmltext = pageIndex("Hello, " + player.getName() + ".");
		return htmltext;
	}

	public String pageIndex(String msg)
	{
		String htmltext = "";
		htmltext += htmlPage("Title");
		htmltext += msg;
		htmltext += "<center><table width=230>";
		if (disablePage(1) == 0)
			htmltext += "<tr><td align=center>" + button("Rate Server", "redirect page 1 0", 150, 20) + "</td></tr>";
		if (disablePage(2) == 0)
			htmltext += "<tr><td align=center>" + button("Enchants", "redirect page 2 0", 150, 20) + "</td></tr>";
		if (disablePage(5) == 0)
			htmltext += "<tr><td align=center>" + button("Custom Info", "redirect page 5 0", 150, 20) + "</td></tr>";
		if (disablePage(6) == 0)
			htmltext += "<tr><td align=center>" + button("Events Info", "redirect page 6 0", 150, 20) + "</td></tr>";
		htmltext += "</table></center>";
		htmltext += htmlPage("Footer");
		return htmltext;
	}

	public String pageSub(int pIndex)
	{
		String htmltext = "", title = "";
		switch (pIndex)
		{
			case 1:
				title = "Rate Server";
				htmltext += "Rate XP: <font color=\"LEVEL\">" + String.valueOf(Config.RATE_XP) + "x</font><br>";
				htmltext += "Rate Party XP: <font color=\"LEVEL\">" + String.valueOf(Config.RATE_PARTY_XP) + "x</font><br>";
				htmltext += "Rate SP: <font color=\"LEVEL\">" + String.valueOf(Config.RATE_SP) + "x</font><br>";
				htmltext += "Rate Party SP: <font color=\"LEVEL\">" + String.valueOf(Config.RATE_PARTY_SP) + "x</font><br>";
				htmltext += "Rate Drop Items: <font color=\"LEVEL\">" + String.valueOf(Config.RATE_DROP_ITEMS) + "x</font><br>";
				htmltext += "Rate Drop Raid: <font color=\"LEVEL\">" + String.valueOf(Config.RATE_DROP_ITEMS_BY_RAID) + "x</font><br>";
				htmltext += "Rate Drop Spoil: <font color=\"LEVEL\">" + String.valueOf(Config.RATE_DROP_SPOIL) + "x</font><br>";
				htmltext += "Rate Quest Reward: <font color=\"LEVEL\">" + String.valueOf(Config.RATE_QUEST_DROP) + "x</font><br>";
				break;
			case 2:
				title = "Enchants";
				//htmltext += "Enchant Max Weapon: <font color=\"LEVEL\">" + convEnchantMax(Config.ENCHANT_MAX_WEAPON) + "</font><br>";
				//htmltext += "Enchant Max Armor: <font color=\"LEVEL\">" + convEnchantMax(Config.ENCHANT_MAX_ARMOR) + "</font><br>";
				//htmltext += "Enchant Max Jewelry: <font color=\"LEVEL\">" + convEnchantMax(Config.ENCHANT_MAX_JEWELRY) + "</font><br>";
				htmltext += "Enchant Safe Max: <font color=\"LEVEL\">+" + String.valueOf(Config.ENCHANT_SAFE_MAX) + "</font><br>";
				htmltext += "Enchant Safe Max Full: <font color=\"LEVEL\">+" + String.valueOf(Config.ENCHANT_SAFE_MAX_FULL) + "</font><br>";
				//htmltext += "Enchant Chance Weapon Warrior: <font color=\"LEVEL\">" + String.valueOf(Config.ENCHANT_CHANCE_WEAPON) + "</font><br>";
				//htmltext += "Enchant Chance Armor: <font color=\"LEVEL\">" + String.valueOf(Config.ENCHANT_CHANCE_ARMOR) + "</font><br>";
				//htmltext += "Enchant Chance Jewelry: <font color=\"LEVEL\">" + String.valueOf(Config.ENCHANT_CHANCE_JEWELRY) + "</font><br>";
				break;
			case 5:
				title = "Custom Info";
				htmltext += "Banking System: <font color=\"LEVEL\">" + convBoolean(Config.BANKING_SYSTEM_ENABLED) + "</font><br>";
				htmltext += "Vitality System: <font color=\"LEVEL\">" + convBoolean(Config.ENABLE_VITALITY) + "</font><br>";
				htmltext += "Champion System: <font color=\"LEVEL\">" + convBoolean(Config.L2JMOD_CHAMPION_ENABLE) + "</font><br>";
				htmltext += "Wedding System: <font color=\"LEVEL\">" + convBoolean(Config.L2JMOD_ALLOW_WEDDING) + "</font><br>";
				htmltext += "Offline Trade: <font color=\"LEVEL\">" + convBoolean(Config.OFFLINE_TRADE_ENABLE) + "</font><br>";
				htmltext += "Offline Craft: <font color=\"LEVEL\">" + convBoolean(Config.OFFLINE_CRAFT_ENABLE) + "</font><br>";
				break;
			case 6:
				title = "Events Info";
				htmltext += "TvT Events: <font color=\"LEVEL\">" + convBoolean(Config.TVT_EVENT_ENABLED) + "</font><br>";
				htmltext += "Handy's Block Checker Event: <font color=\"LEVEL\">" + convBoolean(Config.ENABLE_BLOCK_CHECKER_EVENT) + "</font><br>";
				break;
			default:
				title = "Server Info NPC";
				htmltext = "Hello!";
				break;
		}
		return showText(title, htmltext);
	}

	public int disablePage(int page)
	{
		int p = 0;
		for (final String pIndex : Config.SERVERINFO_NPC_DISABLE_PAGE)
			if (pIndex.equalsIgnoreCase(String.valueOf(page)))
				p = 1;
		return p;
	}

	public String convBoolean(Boolean b)
	{
		String text = "<null>";
		if (b)
			text = "<font color=\"00FF00\">ON!</font>";
		else
			text = "<font color=\"FF0000\">OFF!</font>";
		return text;
	}

	public String convEnchantMax(int i)
	{
		String text = "<null>";
		if (i == 0)
			text = "No Limit!";
		else
			text = "Up to +" + String.valueOf(i);
		return text;
	}

	public String htmlPage(String op)
	{
		String texto = "";
		if (op == "Title")
			texto += "<html><body><title>Brick</title><center><br>" + "<b><font color=ffcc00>Server Info NPC</font></b>"
					+ "<br><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br></center>";
		else if (op == "Footer")
			texto += "<br><center><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"><br>" + "<br><font color=\"303030\">---</font></center></body></html>";
		else
			texto = "Hello!";
		return texto;
	}

	public String button(String name, String event, int w, int h)
	{
		return "<button value=\"" + name + "\" action=\"bypass -h Quest ServerInfoNPC " + event + "\" " + "width=\"" + Integer.toString(w) + "\" height=\"" + Integer.toString(h) + "\" "
				+ "back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
	}

	public String link(String name, String event, String color)
	{
		return "<a action=\"bypass -h Quest ServerInfoNPC " + event + "\">" + "<font color=\"" + color + "\">" + name + "</font></a>";
	}

	public String showText(String title, String text)
	{
		String htmltext = "";
		htmltext += htmlPage("Title");
		htmltext += "<center><font color=\"LEVEL\">" + title + "</font></center><br>";
		htmltext += text + "<br><br>";
		htmltext += "<center>" + button("Back", "redirect main 1 0", 120, 20) + "</center>";
		htmltext += htmlPage("Footer");
		return htmltext;
	}

	public static void main(String[] args)
	{
		new ServerInfoNPC(-1, "ServerInfoNPC", "other");
	}
}
