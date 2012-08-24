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
package retail.DragonVortex;

import ai.group_template.L2AttackableAIScript;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.util.Rnd;

/**
 * @author micr0
 */
public class DragonVortex extends L2AttackableAIScript
{
	private static final String qn = "DragonVortex";
  	private static final int _despawnDelay = 1800000;
  	private final int[] bosses = { 25718,25719,25720,25721,25722,25723,25724 };
  
  	@Override
  	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
  	{
  		if (event.equalsIgnoreCase("summon"))
  		{
  			if (!hasQuestItems(player, 17248))
  				return "no_item.html";
      
  			player.destroyItemByItemId(npc.getName(), 17248, 1, player, true);
  			int bossId = bosses[Rnd.get(bosses.length)];
  			addSpawn(bossId, player.getX(), player.getY() + (Rnd.get(100) - 50), player.getZ() , player.getHeading(), true, _despawnDelay);
  		}
  		return super.onAdvEvent(event, npc, player);
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
		addFirstTalkId(32871);
		addStartNpc(32871);
		addTalkId(32871);
	}
  
	public static void main(String[] args)
	{
		new DragonVortex(-1, qn, "retail");
	}
}