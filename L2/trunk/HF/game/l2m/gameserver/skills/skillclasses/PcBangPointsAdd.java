package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.templates.StatsSet;

public class PcBangPointsAdd extends Skill
{
  public PcBangPointsAdd(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    int points = (int)_power;

    for (Creature target : targets)
    {
      if (target.isPlayer())
      {
        Player player = target.getPlayer();
        player.addPcBangPoints(points, false);
      }
      getEffects(activeChar, target, getActivateRate() > 0, false);
    }

    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}