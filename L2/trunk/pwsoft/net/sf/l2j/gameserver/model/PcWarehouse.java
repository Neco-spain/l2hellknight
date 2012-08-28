package net.sf.l2j.gameserver.model;

import java.util.concurrent.ConcurrentLinkedQueue;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class PcWarehouse extends Warehouse
{
  private L2PcInstance _owner;

  public PcWarehouse(L2PcInstance owner)
  {
    _owner = owner;
  }

  public L2PcInstance getOwner() {
    return _owner;
  }
  public L2ItemInstance.ItemLocation getBaseLocation() { return L2ItemInstance.ItemLocation.WAREHOUSE; } 
  public String getLocationId() { return "0"; } 
  public int getLocationId(boolean dummy) { return 0; }

  public void setLocationId(L2PcInstance dummy) {
  }

  public boolean validateCapacity(int slots) {
    return _items.size() + slots <= _owner.getWareHouseLimit();
  }
}