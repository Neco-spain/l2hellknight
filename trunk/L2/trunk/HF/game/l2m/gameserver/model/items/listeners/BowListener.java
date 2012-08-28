package l2m.gameserver.model.items.listeners;

import l2m.gameserver.listener.inventory.OnEquipListener;
import l2m.gameserver.model.Playable;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.templates.item.WeaponTemplate.WeaponType;

public final class BowListener
  implements OnEquipListener
{
  private static final BowListener _instance = new BowListener();

  public static BowListener getInstance()
  {
    return _instance;
  }

  public void onUnequip(int slot, ItemInstance item, Playable actor)
  {
    if ((!item.isEquipable()) || (slot != 7)) {
      return;
    }
    Player player = (Player)actor;

    if ((item.getItemType() == WeaponTemplate.WeaponType.BOW) || (item.getItemType() == WeaponTemplate.WeaponType.CROSSBOW) || (item.getItemType() == WeaponTemplate.WeaponType.ROD))
      player.getInventory().setPaperdollItem(8, null);
  }

  public void onEquip(int slot, ItemInstance item, Playable actor)
  {
    if ((!item.isEquipable()) || (slot != 7)) {
      return;
    }
    Player player = (Player)actor;

    if (item.getItemType() == WeaponTemplate.WeaponType.BOW)
    {
      ItemInstance arrow = player.getInventory().findArrowForBow(item.getTemplate());
      if (arrow != null)
        player.getInventory().setPaperdollItem(8, arrow);
    }
    if (item.getItemType() == WeaponTemplate.WeaponType.CROSSBOW)
    {
      ItemInstance bolt = player.getInventory().findArrowForCrossbow(item.getTemplate());
      if (bolt != null)
        player.getInventory().setPaperdollItem(8, bolt);
    }
    if (item.getItemType() == WeaponTemplate.WeaponType.ROD)
    {
      ItemInstance bait = player.getInventory().findEquippedLure();
      if (bait != null)
        player.getInventory().setPaperdollItem(8, bait);
    }
  }
}