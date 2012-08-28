package net.sf.l2j.gameserver.skills.targets;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;

public class TargetAura extends TargetList
{
  public final FastList<L2Object> getTargetList(FastList<L2Object> targets, L2Character activeChar, boolean onlyFirst, L2Character target, L2Skill skill)
  {
    FastList objs = activeChar.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius());
    if ((objs == null) || (objs.isEmpty())) {
      if (skill.getId() == 347) {
        targets.add(activeChar);
      }

      return targets;
    }

    boolean srcInArena = (activeChar.isInsidePvpZone()) && (!activeChar.isInsideZone(4));
    L2PcInstance player = activeChar.getPlayer();

    L2Character cha = null;
    L2PcInstance trg = null;
    FastList.Node n = objs.head(); for (FastList.Node end = objs.tail(); (n = n.getNext()) != end; ) {
      cha = (L2Character)n.getValue();
      if ((cha == null) || 
        ((!cha.isL2Attackable()) && (!cha.isL2Playable())) || 
        (cha.isL2Guard()) || 
        (cha.isDead()) || (cha.equals(activeChar)) || (cha.equals(player)) || (
        (skill.isSkillTypeOffensive()) && (activeChar.isMonster()) && (cha.isMonster())))
      {
        continue;
      }
      if ((player != null) && ((cha.isPlayer()) || (cha.isL2Summon()))) {
        trg = cha.getPlayer();
        if ((player.equals(trg)) || 
          (!player.checkPvpSkill(trg, skill)) || 
          (trg.isInZonePeace()) || 
          ((player.getParty() != null) && (player.getParty().getPartyMembers().contains(trg))) || (
          (!srcInArena) && ((!trg.isInsidePvpZone()) || (trg.isInsideZone(4))) && (
          ((player.getClanId() != 0) && (player.getClanId() == trg.getClanId())) || (
          ((skill.isSkillTypeOffensive()) || (skill.isPvpSkill()) || (skill.isHeroSkill()) || (skill.isAOEpvp())) && (player.getAllyId() != 0) && (player.getAllyId() == trg.getAllyId())))))
        {
          continue;
        }
      }

      if (!activeChar.canSeeTarget(cha))
      {
        continue;
      }
      if (onlyFirst) {
        targets.add(cha);
        return targets;
      }
      targets.add(cha);
    }

    if ((targets.isEmpty()) && (skill.getId() == 347)) {
      targets.add(activeChar);
    }

    return targets;
  }
}