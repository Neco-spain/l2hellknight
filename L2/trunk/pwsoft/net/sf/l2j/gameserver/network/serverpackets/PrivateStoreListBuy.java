package net.sf.l2j.gameserver.network.serverpackets;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.TradeList.TradeItem;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public class PrivateStoreListBuy extends L2GameServerPacket
{
  private L2PcInstance _storePlayer;
  private L2PcInstance _activeChar;
  private int _playerAdena;
  private FastList<TradeList.TradeItem> _items;

  public PrivateStoreListBuy(L2PcInstance player, L2PcInstance storePlayer)
  {
    _storePlayer = storePlayer;
    _activeChar = player;
    _playerAdena = _activeChar.getAdena();
    _storePlayer.getSellList().updateItems();
    _items = _storePlayer.getBuyList().getAvailableItems(_activeChar.getInventory());
  }

  protected final void writeImpl()
  {
    writeC(184);
    writeD(_storePlayer.getObjectId());
    writeD(_playerAdena);

    writeD(_items.size());
    FastList.Node n = _items.head(); for (FastList.Node end = _items.tail(); (n = n.getNext()) != end; )
    {
      TradeList.TradeItem item = (TradeList.TradeItem)n.getValue();

      writeD(item.getObjectId());
      writeD(item.getItem().getItemId());
      writeH(item.getEnchant());
      writeD(item.getCount());

      writeD(item.getItem().getReferencePrice());
      writeH(0);
      writeD(item.getItem().getBodyPart());
      writeH(item.getItem().getType2());
      writeD(item.getPrice());
      writeD(item.getCount());
    }
  }

  public String getType()
  {
    return "S.PrivateStoreListBuy";
  }
}