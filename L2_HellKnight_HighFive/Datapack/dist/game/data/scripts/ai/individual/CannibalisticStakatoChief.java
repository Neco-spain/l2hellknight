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

import java.util.List;

import gnu.trove.map.hash.TIntObjectHashMap;

import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.L2Party;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.util.Util;
import l2.hellknight.util.Rnd;

import ai.group_template.L2AttackableAIScript;

public class CannibalisticStakatoChief extends L2AttackableAIScript
{
  private static final int CANNIBALISTIC_CHIEF = 25667;
  private static final int[] BIZARRE_COCOONS = { 18795, 18798 };
  private static final int LARGE_COCOON = 14834;
  private static final int SMALL_COCOON = 14833;
  private static TIntObjectHashMap<Integer> _captainSpawn = new TIntObjectHashMap<Integer>();

  public CannibalisticStakatoChief(int questId, String name, String descr)
  {
    super(questId, name, descr);

    addKillId(CANNIBALISTIC_CHIEF);
    for (int i : BIZARRE_COCOONS)
      addSkillSeeId(i);
  }

  @Override
public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
  {
    int npcId = npc.getNpcId();
    if ((Util.contains(BIZARRE_COCOONS, npcId)) && (skill.getId() == 2905) && (caster.getTarget() == npc))
    {
      npc.getSpawn().stopRespawn();
      npc.doDie(npc);
      L2Npc captain = addSpawn(CANNIBALISTIC_CHIEF, npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), 0, false, 0L);
      _captainSpawn.put(captain.getObjectId(), Integer.valueOf(npc.getNpcId()));
      caster.getInventory().destroyItemByItemId("removeAccelerator", 14832, 1L, caster, caster);
    }
    return super.onSkillSee(npc, caster, skill, targets, isPet);
  }

  @Override
public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
  {
    L2Party partyK = killer.getParty();
    if (partyK != null)
    {
      List<L2PcInstance> party = partyK.getMembers();
      for (L2PcInstance member : party)
      {
        if (Rnd.get(100) > 80)
          member.addItem("BigCocoon", LARGE_COCOON, 1L, npc, true);
        else {
          member.addItem("SMALL_COCOON", SMALL_COCOON, 1L, npc, true);
        }
      }

    }
    else if (Rnd.get(100) > 80) {
      killer.addItem("BigCocoon", LARGE_COCOON, 1L, npc, true);
    } else {
      killer.addItem("SMALL_COCOON", SMALL_COCOON, 1L, npc, true);
    }

    addSpawn(_captainSpawn.get(npc.getObjectId()).intValue(), npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), 0, false, 0L);
    _captainSpawn.remove(npc.getObjectId());

    return super.onKill(npc, killer, isPet);
  }

  public static void main(String[] args)
  {
    new CannibalisticStakatoChief(-1, "CannibalisticStakatoChief", "ai");
  }
}