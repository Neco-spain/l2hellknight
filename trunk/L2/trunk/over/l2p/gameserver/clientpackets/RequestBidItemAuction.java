package l2p.gameserver.clientpackets;

import l2p.gameserver.instancemanager.itemauction.ItemAuction;
import l2p.gameserver.instancemanager.itemauction.ItemAuctionInstance;
import l2p.gameserver.instancemanager.itemauction.ItemAuctionManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.network.GameClient;

public final class RequestBidItemAuction extends L2GameClientPacket
{
  private int _instanceId;
  private long _bid;

  protected final void readImpl()
  {
    _instanceId = readD();
    _bid = readQ();
  }

  protected final void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    ItemInstance adena = activeChar.getInventory().getItemByItemId(57);
    if ((_bid < 0L) || (_bid > adena.getCount())) {
      return;
    }
    ItemAuctionInstance instance = ItemAuctionManager.getInstance().getManagerInstance(_instanceId);
    NpcInstance broker = activeChar.getLastNpc();
    if ((broker == null) || (broker.getNpcId() != _instanceId) || (activeChar.getDistance(broker.getX(), broker.getY()) > 200.0D))
      return;
    if (instance != null)
    {
      ItemAuction auction = instance.getCurrentAuction();
      if (auction != null)
        auction.registerBid(activeChar, _bid);
    }
  }
}