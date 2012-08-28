package net.sf.l2j.gameserver.skills.targets;

import javolution.util.FastList;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.util.Util;

public class TargetClanCorpse extends TargetList
{
  public final FastList<L2Object> getTargetList(FastList<L2Object> targets, L2Character activeChar, boolean onlyFirst, L2Character target, L2Skill skill)
  {
    if (!activeChar.isPlayer()) {
      return targets;
    }

    L2Clan clan = activeChar.getClan();
    if (clan == null) {
      return targets;
    }
    L2PcInstance player = activeChar.getPlayer();
    for (L2ClanMember member : clan.getMembers())
    {
      if (member == null) {
        continue;
      }
      L2PcInstance newTarget = member.getPlayerInstance();
      if (newTarget == null) {
        continue;
      }
      if (!newTarget.isDead()) {
        continue;
      }
      if ((skill.getSkillType() == L2Skill.SkillType.RESURRECT) && (newTarget.isInsideZone(4))) {
        continue;
      }
      if ((player.isInDuel()) && ((player.getDuel() != newTarget.getDuel()) || ((player.getParty() != null) && (!player.getParty().getPartyMembers().contains(newTarget)))))
      {
        continue;
      }
      if (!player.checkPvpSkill(newTarget, skill)) {
        continue;
      }
      if (!Util.checkIfInRange(skill.getSkillRadius(), activeChar, newTarget, true)) {
        continue;
      }
      if (!player.canSeeTarget(newTarget)) {
        continue;
      }
      if (onlyFirst)
      {
        targets.add(newTarget);
        return targets;
      }
      targets.add(newTarget);
    }
    return targets;
  }
}