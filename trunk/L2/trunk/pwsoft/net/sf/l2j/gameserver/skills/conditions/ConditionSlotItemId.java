package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Env;

public final class ConditionSlotItemId extends ConditionInventory
{
  private final int _itemId;
  private final int _enchantLevel;

  public ConditionSlotItemId(int slot, int itemId, int enchantLevel)
  {
    super(slot);
    _itemId = itemId;
    _enchantLevel = enchantLevel;
  }

  public boolean testImpl(Env env)
  {
    if (!env.cha.isPlayer()) {
      return false;
    }
    L2ItemInstance item = env.cha.getPlayer().getInventory().getPaperdollItem(_slot);
    if (item == null)
      return _itemId == 0;
    return (item.getItemId() == _itemId) && (item.getEnchantLevel() >= _enchantLevel);
  }
}