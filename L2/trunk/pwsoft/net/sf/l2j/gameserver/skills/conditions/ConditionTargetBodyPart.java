package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.templates.L2Armor;

public class ConditionTargetBodyPart extends Condition
{
  private L2Armor _armor;

  public ConditionTargetBodyPart(L2Armor armor)
  {
    _armor = armor;
  }

  public boolean testImpl(Env env)
  {
    if (env.target == null) return true;
    int bodypart = env.target.getAttackingBodyPart();
    int armor_part = _armor.getBodyPart();
    switch (bodypart)
    {
    case 10:
      return (armor_part & 0x8401) != 0;
    case 11:
      return (armor_part & 0x8800) != 0;
    case 6:
      return (armor_part & 0x40) != 0;
    case 12:
      return (armor_part & 0x1000) != 0;
    case 9:
      return (armor_part & 0x200) != 0;
    case 7:
    case 8: } return true;
  }
}