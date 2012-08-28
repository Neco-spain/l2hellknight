package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.EffectList;
import l2m.gameserver.model.Skill;
import l2m.gameserver.data.tables.SkillTable;
import l2m.gameserver.templates.StatsSet;

public class BuffCharger extends Skill
{
  private int _target;

  public BuffCharger(StatsSet set)
  {
    super(set);
    _target = set.getInteger("targetBuff", 0);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets)
    {
      int level = 0;
      List el = target.getEffectList().getEffectsBySkillId(_target);
      if (el != null) {
        level = ((Effect)el.get(0)).getSkill().getLevel();
      }
      Skill next = SkillTable.getInstance().getInfo(_target, level + 1);
      if (next != null)
        next.getEffects(activeChar, target, false, false);
    }
  }
}