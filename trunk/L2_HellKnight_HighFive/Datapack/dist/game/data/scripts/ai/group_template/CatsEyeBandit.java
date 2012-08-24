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

import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.network.NpcStringId;
import l2.hellknight.gameserver.network.serverpackets.NpcSay;
import l2.hellknight.util.Rnd;

public class CatsEyeBandit extends Quest
{
    private boolean firstAttacked = false;

    public CatsEyeBandit(int questId, String name, String descr)
	{
		super(questId, name, descr);
        addKillId(27038);
        addAttackId(27038);
    }

    @Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
    {
        if (firstAttacked)
        {
           if (Rnd.get(100) < 40) return super.onAttack(npc,player,damage,isPet,skill);
           npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), NpcStringId.YOU_CHILDISH_FOOL_DO_YOU_THINK_YOU_CAN_CATCH_ME));
        }
        else
           firstAttacked = true;
        return super.onAttack(npc,player,damage,isPet,skill);
    }

    @Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (npc.getNpcId() == 27038)
        {
            if (Rnd.get(100)<80)
                npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), NpcStringId.I_MUST_DO_SOMETHING_ABOUT_THIS_SHAMEFUL_INCIDENT));
            firstAttacked = false;
        }
        else if (firstAttacked)
            addSpawn(npc.getNpcId(), npc);
        return super.onKill(npc,player,isPet);
    }

    public static void main(String[] args)
	{
		new CatsEyeBandit(-1, "CatsEyeBandit", "ai");
	}
}
