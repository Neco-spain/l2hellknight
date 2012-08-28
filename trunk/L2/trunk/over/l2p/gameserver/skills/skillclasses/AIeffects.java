package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Skill;
import l2p.gameserver.templates.StatsSet;

public class AIeffects extends Skill
{
  public AIeffects(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets) {
      if (target != null)
        getEffects(activeChar, target, getActivateRate() > 0, false);
    }
    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}