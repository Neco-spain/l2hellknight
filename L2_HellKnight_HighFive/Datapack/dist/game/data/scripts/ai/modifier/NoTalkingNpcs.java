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
package ai.modifier;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;

/**
 * @author Kazumi, Synerge
 */
public class NoTalkingNpcs extends Quest
{
	private final static int[] NO_TALKING_LIST =
	{
		18684, 18685, 18686, 18687, 18688, 18689, 18690, 19691, 18692, 31557, 31606, 
		31671, 31672, 31673, 31674, 32026, 32030, 32031, 32032, 32619, 32620, 32621
	};

	public NoTalkingNpcs(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (int _npcIds : NO_TALKING_LIST)
		{
			addStartNpc(_npcIds);
			addFirstTalkId(_npcIds);
		}
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (contains(NO_TALKING_LIST, npc.getNpcId()))
			return null;

		npc.showChatWindow(player);
		return null;
	}

	public static void main(String[] args)
	{
		new NoTalkingNpcs(-1, "NoTalkingNpcs", "ai");
	}
}
