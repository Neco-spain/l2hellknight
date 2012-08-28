package net.sf.l2j.gameserver.skills.targets;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;

public class TargetAllyCorpse extends TargetList
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

    FastList objs = activeChar.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius());
    if ((objs == null) || (objs.isEmpty())) {
      return targets;
    }
    L2Character cha = null;
    FastList.Node n = objs.head(); for (FastList.Node end = objs.tail(); (n = n.getNext()) != end; )
    {
      cha = (L2Character)n.getValue();
      if ((cha == null) || 
        (!cha.isPlayer()) || (!cha.isDead()) || (
        (skill.getSkillType() == L2Skill.SkillType.RESURRECT) && (cha.isInsideZone(4)))) {
        continue;
      }
      L2PcInstance allyTarget = cha.getPlayer();
      if (((allyTarget.getAllyId() == 0) || (allyTarget.getAllyId() != player.getAllyId())) && ((allyTarget.getClan() == null) || (allyTarget.getClanId() != player.getClanId()) || 
        ((player.isInDuel()) && ((player.getDuel() != allyTarget.getDuel()) || ((player.getParty() != null) && (!player.getParty().getPartyMembers().contains(cha))))) || 
        (!player.checkPvpSkill(cha, skill)) || 
        (!activeChar.canSeeTarget(cha)))) {
        continue;
      }
      if (onlyFirst)
      {
        targets.add(cha);
        return targets;
      }
      targets.add(cha);
    }
    return targets;
  }
}