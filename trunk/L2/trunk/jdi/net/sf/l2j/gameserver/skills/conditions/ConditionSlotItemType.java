package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
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
    if (!(env.player instanceof L2PcInstance))
      return false;
    Inventory inv = ((L2PcInstance)env.player).getInventory();
    L2ItemInstance item = inv.getPaperdollItem(_slot);
    if (item == null)
      return false;
    return (item.getItem().getItemMask() & _mask) != 0;
  }
}