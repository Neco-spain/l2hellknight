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

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.L2Spawn;
import l2.hellknight.gameserver.datatables.SpawnTable;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.util.Rnd;

import ai.group_template.L2AttackableAIScript;

import java.util.Calendar;
import java.util.Collection;

public class Enira extends L2AttackableAIScript
{
  private static final int ENIRA = 25625;

  public Enira(int questId, String name, String descr)
  {
    super(questId, name, descr);

    eniraSpawn();
  }

  private void eniraSpawn()
  {
    Calendar _date = Calendar.getInstance();
    int newSecond = _date.get(13);
    int newMinute = _date.get(12);
    int newHour = _date.get(10);

    int targetHour = (24 - newHour) * 60 * 60 * 1000;
    int extraMinutesAndSeconds = ((60 - newMinute) * 60 + (60 - newSecond)) * 1000;
    int timerDuration = targetHour + extraMinutesAndSeconds;

    startQuestTimer("enira_spawn", timerDuration, null, null);
  }

  private L2Npc findTemplate(int npcId)
  {
    L2Npc npc = null;
    Collection <L2Spawn>spawns = SpawnTable.getInstance().getSpawnTable();
    for (L2Spawn spawn : spawns)
    {
      if ((spawn != null) && (spawn.getNpcid() == npcId))
      {
        npc = spawn.getLastSpawn();
        break;
      }
    }
    return npc;
  }

  @Override
public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
  {
    if (event.equalsIgnoreCase("enira_spawn"))
    {
      if (Rnd.get(100) <= 40)
      {
        L2Npc eniraSpawn = findTemplate(ENIRA);
        if (eniraSpawn == null) {
          addSpawn(ENIRA, -181989, 208968, 4030, 0, false, 3600000L);
        }
      }
      eniraSpawn();
    }

    return null;
  }

  public static void main(String[] args)
  {
    new Enira(-1, "Enira", "ai");
  }
}
