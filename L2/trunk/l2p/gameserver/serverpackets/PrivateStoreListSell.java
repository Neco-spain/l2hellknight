package l2p.gameserver.serverpackets;

import java.util.List;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.TradeItem;

public class PrivateStoreListSell extends L2GameServerPacket
{
  private int _sellerId;
  private long _adena;
  private final boolean _package;
  private List<TradeItem> _sellList;

  public PrivateStoreListSell(Player buyer, Player seller)
  {
    _sellerId = seller.getObjectId();
    _adena = buyer.getAdena();
    _package = (seller.getPrivateStoreType() == 8);
    _sellList = seller.getSellList();
  }

  protected final void writeImpl()
  {
    writeC(161);
    writeD(_sellerId);
    writeD(_package ? 1 : 0);
    writeQ(_adena);
    writeD(_sellList.size());
    for (TradeItem si : _sellList)
    {
      writeItemInfo(si);
      writeQ(si.getOwnersPrice());
      writeQ(si.getStorePrice());
    }
  }
}