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
package ai.group_template;

import ai.group_template.L2AttackableAIScript;

import com.l2js.gameserver.model.actor.L2Npc;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.network.serverpackets.NpcSay;
import com.l2js.util.Rnd;

/**
 * ¼w¾|»h»i¤H«ü´§©x
 */
public class Delu_Lizardman_Special_Commander extends L2AttackableAIScript
{
	private static final int Delu_Lizardman_Special_Commander = 21107;

	private static boolean _FirstAttacked;

	private static String[] text =
	{
		"Come on, Ill take you on!",
		"Is this the death of him!",
		"Despicable person! Go to hell!",
		"Foul! We mean by fast for me executed!",
		"Contest is over! We attack!"
	};
	
	private static String[] text1 =
	{
		"Come on, Ill take you on!",
		"How dare you interrupt a sacred duel! You must be taught a lesson!"
	};
	
	public Delu_Lizardman_Special_Commander(int questId, String name, String descr)
	{
		super(questId, name, descr);
		int[] mobs = {Delu_Lizardman_Special_Commander};
		registerMobs(mobs);
		_FirstAttacked = false;
	}

	@Override
	public String onAttack (L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if (npc.getNpcId() == Delu_Lizardman_Special_Commander)
		{
			if (_FirstAttacked)
			{
				if (Rnd.get(100) == 50)
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(),0,npc.getNpcId(),text[Rnd.get(4)]));
				}
				if (Rnd.get(100) == 50)
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(),0,npc.getNpcId(),player.getName() + text1[Rnd.get(1)]));
				}
			}
			_FirstAttacked = true;
		}
		return super.onAttack(npc, player, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == Delu_Lizardman_Special_Commander)
		{
			_FirstAttacked = false;
		}
		return super.onKill(npc,killer,isPet);
	}

	public static void main(String[] args)
	{
		new Delu_Lizardman_Special_Commander(-1, "Delu_Lizardman_Special_Commander", "ai");
	}
}
