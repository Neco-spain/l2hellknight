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
package ai.individual;

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.util.Rnd;

import ai.group_template.L2AttackableAIScript;

public class EtisEtina extends L2AttackableAIScript
{
  private static final int ETIS = 18949;
  private static final int GUARD1 = 18950;
  private static final int GUARD2 = 18951;
  private boolean _FirstAttacked = false;

  public EtisEtina(int id, String name, String descr)
  {
    super(id, name, descr);

    addAttackId(ETIS);
  }

  @Override
public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
  {
    if (npc.getNpcId() == ETIS)
    {
      int maxHp = npc.getMaxHp();
      double nowHp = npc.getStatus().getCurrentHp();

      if (nowHp < maxHp * 0.7D)
      {
        if (this._FirstAttacked) {
          return null;
        }

        L2Npc warrior = addSpawn(GUARD1, npc.getX() + Rnd.get(10, 50), npc.getY() + Rnd.get(10, 50), npc.getZ(), 0, false, 0L, false, npc.getInstanceId());
        warrior.setRunning();
        ((L2Attackable)warrior).addDamageHate(attacker, 1, 99999);
        warrior.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);

        L2Npc warrior1 = addSpawn(GUARD2, npc.getX() + Rnd.get(10, 80), npc.getY() + Rnd.get(10, 80), npc.getZ(), 0, false, 0L, false, npc.getInstanceId());
        warrior1.setRunning();
        ((L2Attackable)warrior1).addDamageHate(attacker, 1, 99999);
        warrior1.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
        this._FirstAttacked = true;
      }
    }

    return null;
  }

  public static void main(String[] args)
  {
    new EtisEtina(-1, "EtisEtina", "ai");
  }
}