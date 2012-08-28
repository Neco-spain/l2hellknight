package net.sf.l2j.gameserver.model;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class PcFreight extends ItemContainer
{
  private L2PcInstance _owner;
  private int _activeLocationId;

  public PcFreight(L2PcInstance owner)
  {
    _owner = owner;
  }

  public L2PcInstance getOwner() {
    return _owner;
  }
  public L2ItemInstance.ItemLocation getBaseLocation() { return L2ItemInstance.ItemLocation.FREIGHT; } 
  public void setActiveLocation(int locationId) { _activeLocationId = locationId; } 
  public int getactiveLocation() { return _activeLocationId;
  }

  public int getSize()
  {
    int size = 0;
    for (L2ItemInstance item : _items)
    {
      if ((item.getEquipSlot() == 0) || (_activeLocationId == 0) || (item.getEquipSlot() == _activeLocationId))
        size++;
    }
    return size;
  }

  public L2ItemInstance[] getItems()
  {
    List list = new FastList();
    for (L2ItemInstance item : _items)
    {
      if ((item.getEquipSlot() == 0) || (item.getEquipSlot() == _activeLocationId)) list.add(item);
    }

    return (L2ItemInstance[])list.toArray(new L2ItemInstance[list.size()]);
  }

  public L2ItemInstance getItemByItemId(int itemId)
  {
    for (L2ItemInstance item : _items)
      if ((item.getItemId() == itemId) && ((item.getEquipSlot() == 0) || (_activeLocationId == 0) || (item.getEquipSlot() == _activeLocationId)))
      {
        return item;
      }
    return null;
  }

  protected void addItem(L2ItemInstance item)
  {
    super.addItem(item);
    if (_activeLocationId > 0) item.setLocation(item.getLocation(), _activeLocationId);
  }

  public void restore()
  {
    int locationId = _activeLocationId;
    _activeLocationId = 0;
    super.restore();
    _activeLocationId = locationId;
  }

  public boolean validateCapacity(int slots)
  {
    return getSize() + slots <= _owner.GetFreightLimit();
  }
}