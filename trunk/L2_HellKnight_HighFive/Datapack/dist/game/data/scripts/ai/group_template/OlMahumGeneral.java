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

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.network.NpcStringId;
import l2.hellknight.gameserver.network.serverpackets.NpcSay;
import l2.hellknight.util.Rnd;

public class OlMahumGeneral extends L2AttackableAIScript
{
  private static final int Ol_Mahum_General = 20438;
  private static boolean _FirstAttacked;

  public OlMahumGeneral(int questId, String name, String descr)
  {
    super(questId, name, descr);

    int[] mobs = { Ol_Mahum_General };
    registerMobs(mobs, new Quest.QuestEventType[] { Quest.QuestEventType.ON_ATTACK, Quest.QuestEventType.ON_KILL });
    _FirstAttacked = false;
  }

  @Override
public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
  {
    if (npc.getNpcId() == Ol_Mahum_General)
    {
      if (_FirstAttacked)
      {
        if (Rnd.get(100) == 50)
        {
          npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), NpcStringId.I_WILL_DEFINITELY_REPAY_THIS_HUMILIATION));
        }
        if (Rnd.get(100) == 50)
        {
          npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), NpcStringId.WE_SHALL_SEE_ABOUT_THAT));
        }
      }
      _FirstAttacked = true;
    }
    return super.onAttack(npc, attacker, damage, isPet);
  }

  @Override
public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
  {
    int npcId = npc.getNpcId();
    if (npcId == Ol_Mahum_General)
    {
      _FirstAttacked = false;
    }
    return super.onKill(npc, killer, isPet);
  }

  public static void main(String[] args)
  {
    new OlMahumGeneral(-1, "OlMahumGeneral", "ai");
  }
}