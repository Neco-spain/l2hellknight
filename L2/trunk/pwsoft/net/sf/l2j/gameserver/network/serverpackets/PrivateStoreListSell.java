package net.sf.l2j.gameserver.network.serverpackets;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.TradeList.TradeItem;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public class PrivateStoreListSell extends L2GameServerPacket
{
  private L2PcInstance _storePlayer;
  private L2PcInstance _activeChar;
  private int _playerAdena;
  private boolean _packageSale;
  private FastList<TradeList.TradeItem> _items;

  public PrivateStoreListSell(L2PcInstance player, L2PcInstance storePlayer)
  {
    _activeChar = player;
    _storePlayer = storePlayer;
    _playerAdena = _activeChar.getAdena();
    _items = _storePlayer.getSellList().getItems();
    _packageSale = _storePlayer.getSellList().isPackaged();
  }

  protected final void writeImpl()
  {
    writeC(155);
    writeD(_storePlayer.getObjectId());
    writeD(_packageSale ? 1 : 0);
    writeD(_playerAdena);

    writeD(_items.size());
    FastList.Node n = _items.head(); for (FastList.Node end = _items.tail(); (n = n.getNext()) != end; )
    {
      TradeList.TradeItem item = (TradeList.TradeItem)n.getValue();

      writeD(item.getItem().getType2());
      writeD(item.getObjectId());
      writeD(item.getItem().getItemId());
      writeD(item.getCount());
      writeH(0);
      writeH(item.getEnchant());
      writeH(0);
      writeD(item.getItem().getBodyPart());
      writeD(item.getPrice());
      writeD(item.getItem().getReferencePrice());
    }
  }

  public String getType()
  {
    return "S.PrivateStoreListSell";
  }
}