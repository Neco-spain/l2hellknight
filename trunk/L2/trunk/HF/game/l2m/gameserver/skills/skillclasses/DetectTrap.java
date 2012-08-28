package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.World;
import l2m.gameserver.model.instances.TrapInstance;
import l2m.gameserver.network.serverpackets.NpcInfo;
import l2m.gameserver.templates.StatsSet;

public class DetectTrap extends Skill
{
  public DetectTrap(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets)
      if ((target != null) && (target.isTrap()))
      {
        trap = (TrapInstance)target;
        if (trap.getLevel() <= getPower())
        {
          trap.setDetected(true);
          for (Player player : World.getAroundPlayers(trap))
            player.sendPacket(new NpcInfo(trap, player));
        }
      }
    TrapInstance trap;
    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}