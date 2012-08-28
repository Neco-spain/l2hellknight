package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.entity.boat.Boat;
import l2p.gameserver.model.entity.boat.ClanAirShip;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.templates.StatsSet;

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