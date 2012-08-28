package scripts.skills.skillhandlers;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.util.Rnd;
import scripts.skills.ISkillHandler;

public class ClanGate
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.CLAN_GATE };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    if (!activeChar.isPlayer())
      return;
    L2PcInstance caster = (L2PcInstance)activeChar;

    if (!caster.canSummon()) {
      return;
    }
    if (CastleManager.getInstance().getCastleByOwner(caster.getClan()) == null)
    {
      caster.sendUserPacket(Static.ONLY_FOR_CASTLEOWNER);
      return;
    }

    if (!caster.isClanLeader())
    {
      caster.sendUserPacket(Static.ONLY_FOR_CLANLEADER);
      return;
    }

    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
    {
      L2Object object = (L2Object)n.getValue();
      if ((object == null) || (!object.isPlayer())) {
        continue;
      }
      L2PcInstance target = (L2PcInstance)object;
      if (target == caster) {
        continue;
      }
      if (target.canBeSummoned(caster))
        target.teleToLocation(caster.getX() + Rnd.get(50), caster.getY() + Rnd.get(50), caster.getZ(), true);
    }
    L2Effect effect = activeChar.getFirstEffect(skill.getId());

    if ((effect != null) && (effect.isSelfEffect()))
      effect.exit();
    skill.getEffectsSelf(activeChar);
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}