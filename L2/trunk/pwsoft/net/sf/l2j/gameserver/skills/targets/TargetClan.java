package net.sf.l2j.gameserver.skills.targets;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.util.Util;

public class TargetClan extends TargetList
{
  public final FastList<L2Object> getTargetList(FastList<L2Object> targets, L2Character activeChar, boolean onlyFirst, L2Character target, L2Skill skill)
  {
    if (!activeChar.isPlayer()) {
      return targets;
    }
    if (activeChar.isInOlympiadMode())
    {
      targets.add(activeChar);
      return targets;
    }

    L2PcInstance player = activeChar.getPlayer();
    if (onlyFirst)
    {
      targets.add(player);
      return targets;
    }
    targets.add(player);

    L2Clan clan = player.getClan();
    if (clan == null) {
      return targets;
    }
    L2PcInstance newTarget = null;
    for (L2ClanMember member : clan.getMembers())
    {
      if (member == null) {
        continue;
      }
      newTarget = member.getPlayerInstance();
      if (newTarget == null) {
        continue;
      }
      if (newTarget.isDead())
      {
        continue;
      }

      if (!player.checkPvpSkill(newTarget, skill)) {
        continue;
      }
      if (skill.getSkillRadius() > 0)
      {
        if (!Util.checkIfInRange(skill.getSkillRadius(), activeChar, newTarget, true)) {
          continue;
        }
        if (!player.canSeeTarget(newTarget)) {
          continue;
        }
      }
      targets.add(newTarget);
    }
    return targets;
  }
}