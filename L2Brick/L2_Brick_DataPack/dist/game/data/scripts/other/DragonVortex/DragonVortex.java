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

package other.DragonVortex;

import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.util.Rnd;

public class DragonVortex extends Quest
{
	private static final int NPC_VORTEX = 32871;

	private static final int ITEM = 17248;
	
	private static final int RAID_1 = 25718;
	private static final int RAID_2 = 25719;
	private static final int RAID_3 = 25720;
	private static final int RAID_4 = 25721;
	private static final int RAID_5 = 25722;
	private static final int RAID_6 = 25723;
	private static final int RAID_7 = 25724;

	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";

		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = newQuestState(player);

		if (event.equalsIgnoreCase("summon"))
		{
			if (!st.hasQuestItems(ITEM))
			{
				htmltext = "no_item.html";
			}
			else
			{
				int random = Rnd.get(1000);
				
				st.takeItems(ITEM, 1);
				
				if (random < 290)
				{
					addSpawn(RAID_1, player.getX(), player.getY(), player.getZ(), 0, true, 1800000);
				}
				else if (random < 520)
				{
					addSpawn(RAID_2, player.getX(), player.getY(), player.getZ(), 0, true, 1800000);
				}
				else if (random < 690)
				{
					addSpawn(RAID_3, player.getX(), player.getY(), player.getZ(), 0, true, 1800000);
				}
				else if (random < 850)
				{
					addSpawn(RAID_4, player.getX(), player.getY(), player.getZ(), 0, true, 1800000);
				}
				else if (random < 980)
				{
					addSpawn(RAID_5, player.getX(), player.getY(), player.getZ(), 0, true, 1800000);
				}
				else if (random < 995)
				{
					addSpawn(RAID_6, player.getX(), player.getY(), player.getZ(), 0, true, 1800000);
				}
				else
				{
					addSpawn(RAID_7, player.getX(), player.getY(), player.getZ(), 0, true, 1800000);
				}
			}
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = newQuestState(player);
		
			return "vortex.html";
	}

	public DragonVortex(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addFirstTalkId(NPC_VORTEX);
		addStartNpc(NPC_VORTEX);
		addTalkId(NPC_VORTEX);
	}

	public static void main(String[] args)
	{
		new DragonVortex(-1, "DragonVortex", "other");
	}
}