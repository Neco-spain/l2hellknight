package l2p.gameserver.clientpackets;

import l2p.gameserver.instancemanager.itemauction.ItemAuction;
import l2p.gameserver.instancemanager.itemauction.ItemAuctionInstance;
import l2p.gameserver.instancemanager.itemauction.ItemAuctionManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExItemAuctionInfo;

public final class RequestInfoItemAuction extends L2GameClientPacket
{
  private int _instanceId;

  protected final void readImpl()
  {
    _instanceId = readD();
  }

  protected final void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    activeChar.getAndSetLastItemAuctionRequest();

    ItemAuctionInstance instance = ItemAuctionManager.getInstance().getManagerInstance(_instanceId);
    if (instance == null) {
      return;
    }
    ItemAuction auction = instance.getCurrentAuction();
    NpcInstance broker = activeChar.getLastNpc();
    if ((auction == null) || (broker == null) || (broker.getNpcId() != _instanceId) || (activeChar.getDistance(broker.getX(), broker.getY()) > 200.0D)) {
      return;
    }
    activeChar.sendPacket(new ExItemAuctionInfo(true, auction, instance.getNextAuction()));
  }
}