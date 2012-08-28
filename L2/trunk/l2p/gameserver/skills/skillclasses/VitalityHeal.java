package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.templates.StatsSet;

public class VitalityHeal extends Skill
{
  public VitalityHeal(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    int fullPoints = l2p.gameserver.Config.VITALITY_LEVELS[4];
    double percent = _power;

    for (Creature target : targets)
    {
      if (target.isPlayer())
      {
        Player player = target.getPlayer();
        double points = fullPoints / 100 * percent;
        player.addVitality(points);
      }
      getEffects(activeChar, target, getActivateRate() > 0, false);
    }

    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}