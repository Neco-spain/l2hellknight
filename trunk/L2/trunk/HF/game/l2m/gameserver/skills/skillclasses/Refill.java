package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.entity.boat.Boat;
import l2m.gameserver.model.entity.boat.ClanAirShip;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.templates.StatsSet;

public class Refill extends Skill
{
  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
    if ((target == null) || (!target.isPlayer()) || (!target.isInBoat()) || (!target.getPlayer().getBoat().isClanAirShip()))
    {
      activeChar.sendPacket(new SystemMessage(113).addSkillName(_id, _level));
      return false;
    }

    return super.checkCondition(activeChar, target, forceUse, dontMove, first);
  }

  public Refill(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets)
    {
      if ((target == null) || (target.isDead()) || (!target.isPlayer()) || (!target.isInBoat()) || (!target.getPlayer().getBoat().isClanAirShip())) {
        continue;
      }
      ClanAirShip airship = (ClanAirShip)target.getPlayer().getBoat();
      airship.setCurrentFuel(airship.getCurrentFuel() + (int)_power);
    }

    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}