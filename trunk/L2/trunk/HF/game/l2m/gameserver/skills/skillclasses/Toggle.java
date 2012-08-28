package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.EffectList;
import l2m.gameserver.model.Skill;
import l2m.gameserver.templates.StatsSet;

public class Toggle extends Skill
{
  public boolean isFakeDeath;

  public Toggle(StatsSet set)
  {
    super(set);
    isFakeDeath = set.getBool("isFakeDeath", false);
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