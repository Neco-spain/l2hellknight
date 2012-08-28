package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Skill;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.templates.StatsSet;

public class DrainSoul extends Skill
{
  public DrainSoul(StatsSet set)
  {
    super(set);
  }

  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
    if (!target.isMonster())
    {
      activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
      return false;
    }
    return super.checkCondition(activeChar, target, forceUse, dontMove, first);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    if (!activeChar.isPlayer())
      return;
  }
}