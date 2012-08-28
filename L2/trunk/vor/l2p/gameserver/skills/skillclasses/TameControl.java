package l2p.gameserver.skills.skillclasses;

import java.util.List;
import java.util.Map;
import l2p.gameserver.Config;
import l2p.gameserver.ai.CharacterAI;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.instances.TamedBeastInstance;
import l2p.gameserver.templates.StatsSet;

public class TameControl extends Skill
{
  private final int _type;

  public TameControl(StatsSet set)
  {
    super(set);
    _type = set.getInteger("type", 0);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    if (isSSPossible()) {
      activeChar.unChargeShots(isMagic());
    }
    if (!activeChar.isPlayer()) {
      return;
    }
    Player player = activeChar.getPlayer();
    if (player.getTrainedBeasts() == null) {
      return;
    }
    if (_type == 0)
    {
      for (Creature target : targets)
        if ((target != null) && ((target instanceof TamedBeastInstance)) && 
          (player.getTrainedBeasts().get(Integer.valueOf(target.getObjectId())) != null))
          ((TamedBeastInstance)target).despawnWithDelay(1000);
    }
    else if (_type > 0)
    {
      if (_type == 1)
        for (TamedBeastInstance tamedBeast : player.getTrainedBeasts().values())
          tamedBeast.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player, Integer.valueOf(Config.FOLLOW_RANGE));
      else if (_type == 3)
        for (TamedBeastInstance tamedBeast : player.getTrainedBeasts().values())
          tamedBeast.buffOwner();
      else if (_type == 4)
        for (TamedBeastInstance tamedBeast : player.getTrainedBeasts().values())
          tamedBeast.doDespawn();
    }
  }
}