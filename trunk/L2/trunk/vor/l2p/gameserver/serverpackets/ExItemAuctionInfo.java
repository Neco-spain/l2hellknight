package l2p.gameserver.serverpackets;

import l2p.gameserver.instancemanager.itemauction.ItemAuction;
import l2p.gameserver.instancemanager.itemauction.ItemAuctionBid;
import l2p.gameserver.instancemanager.itemauction.ItemAuctionState;

public class ExItemAuctionInfo extends L2GameServerPacket
{
  private boolean _refresh;
  private int _timeRemaining;
  private ItemAuction _currentAuction;
  private ItemAuction _nextAuction;

  public ExItemAuctionInfo(boolean refresh, ItemAuction currentAuction, ItemAuction nextAuction)
  {
    if (currentAuction == null) {
      throw new NullPointerException();
    }
    if (currentAuction.getAuctionState() != ItemAuctionState.STARTED)
      _timeRemaining = 0;
    else {
      _timeRemaining = (int)(currentAuction.getFinishingTimeRemaining() / 1000L);
    }
    _refresh = refresh;
    _currentAuction = currentAuction;
    _nextAuction = nextAuction;
  }

  protected void writeImpl()
  {
    writeEx(104);
    writeC(_refresh ? 0 : 1);
    writeD(_currentAuction.getInstanceId());

    ItemAuctionBid highestBid = _currentAuction.getHighestBid();
    writeQ(highestBid != null ? highestBid.getLastBid() : _currentAuction.getAuctionInitBid());

    writeD(_timeRemaining);
    writeItemInfo(_currentAuction.getAuctionItem());

    if (_nextAuction != null)
    {
      writeQ(_nextAuction.getAuctionInitBid());
      writeD((int)(_nextAuction.getStartingTime() / 1000L));
      writeItemInfo(_nextAuction.getAuctionItem());
    }
  }
}