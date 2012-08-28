package net.sf.l2j.gameserver.skills.targets;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;

public class TargetArea extends TargetList
{
  public final FastList<L2Object> getTargetList(FastList<L2Object> targets, L2Character activeChar, boolean onlyFirst, L2Character target, L2Skill skill)
  {
    if ((target == null) || (target.isAlikeDead())) {
      activeChar.sendPacket(Static.TARGET_IS_INCORRECT);
      return targets;
    }

    if (((skill.getCastRange() >= 0) && (target.equals(activeChar))) || ((!target.isL2Attackable()) && (!target.isL2Playable())))
    {
      activeChar.sendPacket(Static.TARGET_IS_INCORRECT);
      return targets;
    }
    L2Character cha;
    if (skill.getCastRange() >= 0) {
      L2Character cha = target;
      if (onlyFirst) {
        targets.add(cha);
        return targets;
      }
      targets.add(cha);
    } else {
      cha = activeChar;
    }

    L2PcInstance src = activeChar.getPlayer();

    boolean srcInArena = (cha.isInsidePvpZone()) && (!activeChar.isInsideZone(4));

    FastList objs = cha.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius());
    if ((objs == null) || (objs.isEmpty())) {
      return targets;
    }

    L2Character obj = null;
    L2PcInstance trg = null;
    FastList.Node n = objs.head(); for (FastList.Node end = objs.tail(); (n = n.getNext()) != end; ) {
      obj = (L2Character)n.getValue();
      if ((obj == null) || 
        (obj.equals(activeChar)) || (obj.isAlikeDead()) || 
        ((!obj.isL2Attackable()) && (!obj.isL2Playable())) || 
        (obj.isL2Guard()) || (
        (skill.isSkillTypeOffensive()) && (activeChar.isMonster()) && (obj.isMonster())))
      {
        continue;
      }
      if (src == null)
      {
        if ((cha.isL2Playable()) && (!obj.isL2Playable()))
        {
          continue;
        }
      } else if ((obj.isPlayer()) || (obj.isL2Summon())) {
        trg = obj.getPlayer();
        if ((trg.equals(src)) || 
          (!src.checkPvpSkill(trg, skill)) || 
          (trg.isInZonePeace()) || 
          ((src.getParty() != null) && (src.getParty().getPartyMembers().contains(trg))) || (
          (!srcInArena) && ((!trg.isInsidePvpZone()) || (trg.isInsideZone(4))) && (
          ((skill.isSkillTypeOffensive()) || ((skill.isPvpSkill()) && (skill.getId() != 452)) || (skill.isHeroSkill()) || (skill.isAOEpvp())) && (((src.getAllyId() != 0) && (src.getAllyId() == trg.getAllyId())) || (
          (src.getClanId() != 0) && (src.getClanId() == trg.getClanId()))))))
        {
          continue;
        }
      }

      if (!activeChar.canSeeTarget(obj))
      {
        continue;
      }
      targets.add(obj);
    }
    return targets;
  }
}