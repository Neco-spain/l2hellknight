package net.sf.l2j.gameserver.model;

import java.util.concurrent.ConcurrentLinkedQueue;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public final class ClanWarehouse extends Warehouse
{
  private L2Clan _clan;

  public ClanWarehouse(L2Clan clan)
  {
    _clan = clan;
  }

  public int getOwnerId() {
    return _clan.getClanId();
  }
  public L2PcInstance getOwner() { return _clan.getLeader().getPlayerInstance(); } 
  public L2ItemInstance.ItemLocation getBaseLocation() {
    return L2ItemInstance.ItemLocation.CLANWH; } 
  public String getLocationId() { return "0"; } 
  public int getLocationId(boolean dummy) { return 0; } 
  public void setLocationId(L2PcInstance dummy) {
  }

  public boolean validateCapacity(int slots) {
    return _items.size() + slots <= Config.WAREHOUSE_SLOTS_CLAN;
  }
}