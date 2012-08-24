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
package retail.SeparatedSoul;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;

/**
 * @author pmq
 * @author corbin12
 * @Updated by corbin12
 * @Updated in 28-10-2011
 */ 

public class SeparatedSoul extends Quest
{
	//Separated Soul
	private static final int[] SEPARATED_SOULS = { 32864, 32865, 32866, 32867, 32868, 32869, 32870, 32891 };
	private static final int WILL_OF_ANTHARAS       = 17266;
	private static final int SEALED_BLOOD_CRYSTAL   = 17267;
    private static final int ANTHARAS_BLOOD_CRYSTAL = 17268;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		
		if (st == null)
			return getNoQuestMsg(player);
		
		// hunter village
		if (event.equalsIgnoreCase("HuntersVillage"))
		{
			if (player.getLevel() >= 80)
			{
				player.teleToLocation(117046,76798,-2696);
			}
			else
			{
				return "no-level.htm";
			}
			
		}		
		// Antharas' Lair
		if (event.equalsIgnoreCase("AntharasLair"))
		{
			if (player.getLevel() >= 80)
			{
				player.teleToLocation(131116,114333,-3704);
			}
			else
			{
				return "no-level.htm";
			}
			
		}
		// Antharas' Lair Deep
		else if (event.equalsIgnoreCase("AntharasLairDeep"))
		{
			if (player.getLevel() >= 80)
			{
				player.teleToLocation(148447,110582,-3944);
			}
			else
			{
				return "no-level.htm";
			}

		}
		// Antharas' Lair-Magic Force Field Bridge
		else if (event.equalsIgnoreCase("AntharasLairMagicForceFieldBridge"))
		{
			if (player.getLevel() >= 80)
			{
				player.teleToLocation(146129,111232,-3568);
			}
			else
			{
				return "no-level.htm";
			}
			
		}
		// Dragon Valley
		else if (event.equalsIgnoreCase("DragonValley"))
		{
			if (player.getLevel() >= 80)
			{
				player.teleToLocation(73122,118351,-3714);
			}
			else
			{
				return "no-level.htm";
			}

		}
		// Dragon Valley Center
		else if (event.equalsIgnoreCase("DragonValleyCenter"))
		{
			if (player.getLevel() >= 80)
			{
				player.teleToLocation(99218,110283,-3696);
			}
			else
			{
				return "no-level.htm";
			}
			
		}
		// Dragon Valley North
		else if (event.equalsIgnoreCase("DragonValleyNorth"))
		{
			if (player.getLevel() >= 80)
			{
				player.teleToLocation(116992,113716,-3056);
			}
			else
			{
				return "no-level.htm";
			}

		}
		// Dragon Valley South
		else if (event.equalsIgnoreCase("DragonValleySouth"))
		{
			if (player.getLevel() >= 80)
			{
				player.teleToLocation(113203,121063,-3712);
			}
			else
			{
				return "no-level.htm";
			}

		}
		// Request Item Synthesis
	        else if (event.equalsIgnoreCase("Synthesis"))
		{
                      if (st.hasQuestItems(WILL_OF_ANTHARAS) && st.hasQuestItems(SEALED_BLOOD_CRYSTAL))
                      {
                             st.takeItems(WILL_OF_ANTHARAS, 1);
                             st.takeItems(SEALED_BLOOD_CRYSTAL, 1);
                             st.giveItems(ANTHARAS_BLOOD_CRYSTAL, 1);
                      }
                      else
                      {
                             return "no-items.htm";
                      }

		}
		return htmltext;
	}
	
    @Override
    public String onTalk(L2Npc npc, L2PcInstance player)
    {
        String htmltext = "";
        QuestState st = player.getQuestState(getName());
        
        if (st == null)
            return null;
            
        if (player.getLevel() < 80)
            return "no-level.htm";
                
        return htmltext;
    }
	
	public SeparatedSoul(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		for (int gatekeepers : SEPARATED_SOULS)
		{
			addStartNpc(gatekeepers);
			addTalkId(gatekeepers);
		}
	}
	
	public static void main(String[] args)
	{
		new SeparatedSoul(-1, SeparatedSoul.class.getSimpleName(), "retail/SeparatedSoul");
	}
}