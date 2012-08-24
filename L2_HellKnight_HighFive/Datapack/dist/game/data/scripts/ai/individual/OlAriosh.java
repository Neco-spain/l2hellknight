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

import javolution.util.FastSet;
import gnu.trove.map.hash.TIntObjectHashMap;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.NpcStringId;
import l2.hellknight.gameserver.network.serverpackets.NpcSay;

import ai.group_template.L2AttackableAIScript;


public class OlAriosh extends L2AttackableAIScript
{
  private static final int ARIOSH = 18555;
  private static final int GUARD = 18556;
  private static L2Npc _guard = null;
  private FastSet<Integer> _lockedSpawns = new FastSet<Integer>();
  private TIntObjectHashMap<Integer> _spawnedGuards = new TIntObjectHashMap<Integer>();

  public OlAriosh(int questId, String name, String descr)
  {
    super(questId, name, descr);

    addAttackId(ARIOSH);
    addKillId(ARIOSH);
    addKillId(GUARD);
  }

  @Override
public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
  {
    if (event.equalsIgnoreCase("time_to_spawn"))
    {
      int objId = npc.getObjectId();
      if (!this._spawnedGuards.contains(objId))
      {
        npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), NpcStringId.WHAT_ARE_YOU_DOING_HURRY_UP_AND_HELP_ME));
        _guard = addSpawn(GUARD, npc.getX() + 100, npc.getY() + 100, npc.getZ(), 0, false, 0L, false, npc.getInstanceId());
        this._lockedSpawns.remove(Integer.valueOf(objId));
        this._spawnedGuards.put(_guard.getObjectId(), Integer.valueOf(objId));
      }
    }
    return super.onAdvEvent(event, npc, player);
  }

  @Override
public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
  {
    if (npc.getNpcId() == ARIOSH)
    {
      int objId = npc.getObjectId();
      if (!this._spawnedGuards.contains(objId))
      {
        if (!this._lockedSpawns.contains(Integer.valueOf(objId)))
        {
          startQuestTimer("time_to_spawn", 60000L, npc, player);
          this._lockedSpawns.add(Integer.valueOf(objId));
        }
      }
    }
    return super.onAttack(npc, player, damage, isPet);
  }

  @Override
public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
  {
    switch (npc.getNpcId())
    {
    case GUARD:
      this._spawnedGuards.remove(npc.getObjectId());
      break;
    case ARIOSH:
      this._spawnedGuards.remove(_guard.getObjectId());
      _guard.decayMe();
      cancelQuestTimer("time_to_spawn", npc, killer);
    }

    return super.onKill(npc, killer, isPet);
  }

  public static void main(String[] args)
  {
    new OlAriosh(-1, "OlAriosh", "ai");
  }
}