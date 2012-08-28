package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Skill.AddedSkill;
import l2p.gameserver.templates.StatsSet;

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