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

package custom.DelevelNpc;

import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.instancemanager.QuestManager;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.network.serverpackets.CreatureSay;

public class Delevel extends Quest
{
	private final static int NPC = 50002;
	private final static int ADENA = 57;
	private final static int COST1 = 5000; // cost per level, price=5k*charlvl
	private final static int COST2 = 10000; // cost per vitality level, price=10k*vitlvl
	private final static int MINLVL = 40; // minimum level requirement
	private final static int KARMA = 0; // 0=don't allow karma, more=max value
	private final static boolean ENABLE = false; 

	String LVL_TOO_LOW = "<html><body><title>Delevel Manager</title><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>Well now, aren't you a little too young for this? Why don't you come back later when you have gained some levels? <font color=LEVEL>(minimum requirements: level %MINLVL%)</font><br><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center></body></html>";
	String WELCOME = "<html><body><title>Delevel Manager</title><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>I welcome You, young warrior! I am Delevel manager, and, as my name says, I offer players like You to decrease their <font color=LEVEL>character level</font> and <font color=LEVEL>vitality level</font>! Of course, this costs money, after all I have earn living somehow.<button value=\"I'm ready!\" action=\"bypass -h Quest Delevel talk\" width=180 height=24 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"/><br><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center></body></html>";
	String DONE = "<html><body><title>Delevel Manager</title><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>So You really want to do this? Alright then, as You wish.<br><font color=LEVEL>The requirements are as follows:</font> You must be at least <font color=LEVEL>level %MINLVL%</font> and must have no more than <font color=LEVEL>%KARMA% Karma</font>, otherwise I won't help You. <br1>You are now level <font color=LEVEL>%LEVEL%</font>, which means decreasing 1 level will cost You <font color=LEVEL>%PRICE1% Adena</font>.<br1>Your Vitality level is <font color=LEVEL>%VITLVL%</font>, decreasing 1 level of Vitality will cost you <font color=LEVEL>%PRICE2% Adena</font>.<br><button value=\"Decrease 1 level\" action=\"bypass -h Quest Delevel level\" width=264 height=24 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"/><br1><button value=\"Decrease 1 vitality level\" action=\"bypass -h Quest Delevel vitality\" width=264 height=24 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"/></center></body></html>";
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = "";
		
		final int PRICE1 = COST1 * player.getLevel();
		final int PRICE2 = COST2 * player.getVitalityLevel();
		final int VITALITY = player.getVitalityPoints();
		
		if (event.equalsIgnoreCase("talk"))
		{
			htmltext = DONE;
			htmltext = htmltext.replace("%MINLVL%", String.valueOf(MINLVL));
			htmltext = htmltext.replace("%KARMA%", String.valueOf(KARMA));
			htmltext = htmltext.replace("%LEVEL%", String.valueOf(player.getLevel()));
			htmltext = htmltext.replace("%PRICE1%", String.valueOf(PRICE1));
			htmltext = htmltext.replace("%VITLVL%", String.valueOf(player.getVitalityLevel()));
			htmltext = htmltext.replace("%PRICE2%", String.valueOf(PRICE2));
		}
		else if (event.equalsIgnoreCase("level"))
		{
			if (player.getKarma() > KARMA)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "I don't offer my services to Karma players!"));
			}
			else if (player.getLevel() < MINLVL)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Sorry, " + player.getName() + ", Your level is too low!"));
			}
			else if (st.getQuestItemsCount(ADENA) < PRICE1)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Sorry, " + player.getName() + ", You don't have enough money!"));
			}
			else
			{
				final int lvl = player.getLevel();
				st.takeItems(ADENA, PRICE1);
				player.removeExpAndSp((player.getStat().getExpForLevel(lvl) - player.getStat().getExpForLevel(lvl-1)), 0);
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Congratulations " + player.getName() + ", Your level has been decreased!"));
				if (player.getExpertiseLevel() == 3 && lvl < 52)
				{
					player.addSkill(SkillTable.getInstance().getInfo(239, 2));
				}
				player.checkPlayerSkills();
				htmltext = DONE;
				htmltext = htmltext.replace("%MINLVL%", String.valueOf(MINLVL));
				htmltext = htmltext.replace("%KARMA%", String.valueOf(KARMA));
				htmltext = htmltext.replace("%LEVEL%", String.valueOf(player.getLevel()));
				htmltext = htmltext.replace("%PRICE1%", String.valueOf(COST1 * player.getLevel()));
				htmltext = htmltext.replace("%VITLVL%", String.valueOf(player.getVitalityLevel()));
				htmltext = htmltext.replace("%PRICE2%", String.valueOf(COST2 * player.getVitalityLevel()));
			}
		}
		
		else if (event.equalsIgnoreCase("vitality"))
		{
			if (player.getKarma() > KARMA)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "I don't offer my services to Karma players!"));
				return "";
			}
			else if (player.getLevel() < MINLVL)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Sorry, " + player.getName() + ", Your level is too low!"));
				return "";
			}
			else if (st.getQuestItemsCount(ADENA) < PRICE2)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Sorry, " + player.getName() + ", You don't have enough money!"));
				return "";
			}
			else if (VITALITY < 240)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Sorry, " + player.getName() + ", Your Vitality level can't be decreased anymore!"));
				return "";
			}
			else if (VITALITY < 2000)
			{
				st.takeItems(ADENA, PRICE2);
				player.setVitalityPoints(1, true);
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Congratulations, " + player.getName() + ", Your Vitality has been decreased!"));
			}
			else if (VITALITY < 13000)
			{
				st.takeItems(ADENA, PRICE2);
				player.setVitalityPoints(241, true);
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Congratulations, " + player.getName() + ", Your Vitality has been decreased!"));
			}
			else if (VITALITY < 17000)
			{
				st.takeItems(ADENA, PRICE2);
				player.setVitalityPoints(2001, true);
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Congratulations, " + player.getName() + ", Your Vitality has been decreased!"));
			}
			else if (VITALITY > 17000)
			{
				st.takeItems(ADENA, PRICE2);
				player.setVitalityPoints(13001, true);
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, "Delevel Manager", "Congratulations, " + player.getName() + ", Your Vitality has been decreased!"));
			}
			htmltext = DONE;
			htmltext = htmltext.replace("%MINLVL%", String.valueOf(MINLVL));
			htmltext = htmltext.replace("%KARMA%", String.valueOf(KARMA));
			htmltext = htmltext.replace("%LEVEL%", String.valueOf(player.getLevel()));
			htmltext = htmltext.replace("%PRICE1%", String.valueOf(COST1 * player.getLevel()));
			htmltext = htmltext.replace("%VITLVL%", String.valueOf(player.getVitalityLevel()));
			htmltext = htmltext.replace("%PRICE2%", String.valueOf(COST2 * player.getVitalityLevel()));
		}
		st.exitQuest(true);
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		@SuppressWarnings("unused")
		QuestState st = player.getQuestState(getName());
		String htmltext = "";
		if (player.getLevel() < MINLVL)
		{
			htmltext = LVL_TOO_LOW;
			htmltext = htmltext.replace("%MINLVL%", String.valueOf(MINLVL));
		}
		else
		{
			final Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
			htmltext = WELCOME;
		}
		return htmltext;
	}
	
	public Delevel(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
		if (ENABLE)
		{
		  	this.addSpawn(NPC, 82168, 148356, -3475, 57343, false, 0);
		}
	}
	
	public static void main(String[] args)
	{
		new Delevel(-1, "Delevel", "custom");
		System.out.println("===================================");
		System.out.println("Delevel Manager successfully loaded");
		System.out.println("===================================");
	}
}
