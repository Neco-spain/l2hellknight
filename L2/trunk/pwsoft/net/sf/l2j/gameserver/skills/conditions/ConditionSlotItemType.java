package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.templates.L2Item;

public final class ConditionSlotItemType extends ConditionInventory
{
  private final int _mask;

  public ConditionSlotItemType(int slot, int mask)
  {
    super(slot);
    _mask = mask;
  }

  public boolean testImpl(Env env)
  {
    if (!env.cha.isPlayer()) {
      return false;
    }
    L2ItemInstance item = env.cha.getPlayer().getInventory().getPaperdollItem(_slot);
    if (item == null) {
      return false;
    }
    return (item.getItem().getItemMask() & _mask) != 0;
  }
}