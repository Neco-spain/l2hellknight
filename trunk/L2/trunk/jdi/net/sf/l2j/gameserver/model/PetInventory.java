package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;

public class PetInventory extends Inventory
{
  private final L2PetInstance _owner;

  public PetInventory(L2PetInstance owner)
  {
    _owner = owner;
  }

  public L2PetInstance getOwner()
  {
    return _owner;
  }

  protected L2ItemInstance.ItemLocation getBaseLocation()
  {
    return L2ItemInstance.ItemLocation.PET;
  }

  protected L2ItemInstance.ItemLocation getEquipLocation()
  {
    return L2ItemInstance.ItemLocation.PET_EQUIP;
  }
}