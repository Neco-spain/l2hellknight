package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.instances.TrapInstance;
import l2m.gameserver.templates.StatsSet;

public class DefuseTrap extends Skill
{
  public DefuseTrap(StatsSet set)
  {
    super(set);
  }

  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
    if ((target == null) || (!target.isTrap()))
    {
      activeChar.sendPacket(Msg.INVALID_TARGET);
      return false;
    }

    return super.checkCondition(activeChar, target, forceUse, dontMove, first);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets) {
      if ((target != null) && (target.isTrap()))
      {
        TrapInstance trap = (TrapInstance)target;
        if (trap.getLevel() <= getPower())
          trap.deleteMe();
      }
    }
    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}