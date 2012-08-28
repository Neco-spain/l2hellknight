package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.Skill.AddedSkill;
import l2m.gameserver.templates.StatsSet;

public class EffectsFromSkills extends Skill
{
  public EffectsFromSkills(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets)
      if (target != null)
        for (Skill.AddedSkill as : getAddedSkills())
          as.getSkill().getEffects(activeChar, target, false, false);
  }
}