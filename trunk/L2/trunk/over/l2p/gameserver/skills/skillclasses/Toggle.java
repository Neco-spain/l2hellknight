package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.Skill;
import l2p.gameserver.templates.StatsSet;

public class Toggle extends Skill
{
  public Toggle(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    if (activeChar.getEffectList().getEffectsBySkillId(_id) != null)
    {
      activeChar.getEffectList().stopEffect(_id);
      activeChar.sendActionFailed();
      return;
    }

    getEffects(activeChar, activeChar, getActivateRate() > 0, false);
  }
}