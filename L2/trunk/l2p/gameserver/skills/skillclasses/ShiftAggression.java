package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.gameserver.model.AggroList;
import l2p.gameserver.model.AggroList.AggroInfo;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.World;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.templates.StatsSet;

public class ShiftAggression extends Skill
{
  public ShiftAggression(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    if (activeChar.getPlayer() == null) {
      return;
    }
    for (Creature target : targets)
      if (target != null)
      {
        if (!target.isPlayer()) {
          continue;
        }
        player = (Player)target;

        for (NpcInstance npc : World.getAroundNpc(activeChar, getSkillRadius(), getSkillRadius()))
        {
          AggroList.AggroInfo ai = npc.getAggroList().get(activeChar);
          if (ai == null)
            continue;
          npc.getAggroList().addDamageHate(player, 0, ai.hate);
          npc.getAggroList().remove(activeChar, true);
        }
      }
    Player player;
    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}