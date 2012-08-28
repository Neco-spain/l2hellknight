package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;

public class ConditionTargetUsesWeaponKind extends Condition
{
  private final int _weaponMask;

  public ConditionTargetUsesWeaponKind(int weaponMask)
  {
    _weaponMask = weaponMask;
  }

  public boolean testImpl(Env env)
  {
    if (env.target == null) {
      return false;
    }
    L2Weapon item = env.target.getActiveWeaponItem();

    if (item == null) {
      return false;
    }
    return (item.getItemType().mask() & _weaponMask) != 0;
  }
}