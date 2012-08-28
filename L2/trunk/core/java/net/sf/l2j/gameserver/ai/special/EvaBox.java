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

package net.sf.l2j.gameserver.ai.special;

import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.util.Rnd;

public class EvaBox extends Quest
{	
	private final static int[] KISS_OF_EVA = {1073,3141,3252};
	private final static int BOX = 32342;
	private final static int[] REWARDS = {9692,9693};
	
	public EvaBox(int questId, String name, String descr)
	{
		super(questId,name,descr);
		
		addEventId(BOX, Quest.QuestEventType.ON_KILL);
	}

	public void dropItem(L2NpcInstance npc, int itemId, int count, L2PcInstance player)
	{
		L2ItemInstance ditem = ItemTable.getInstance().createItem("Loot", itemId, count, player);
		ditem.dropMe(npc, npc.getX(),npc.getY(),npc.getZ()); 
	}

	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
	{
		boolean found = false;
		for(L2Effect effect : killer.getAllEffects())
		{
			for(int i=0;i<3;i++)
			{
				if(effect.getSkill().getId() == KISS_OF_EVA[i])
					found = true;
			}
		}

		if(found == true)
		{
			int dropid = Rnd.get(1);
			if(dropid == 1)
				dropItem(npc,REWARDS[dropid],1,killer);
			else if(dropid == 0)
				dropItem(npc,REWARDS[dropid],1,killer);
		}

		return super.onKill(npc,killer,isPet);
	}
}