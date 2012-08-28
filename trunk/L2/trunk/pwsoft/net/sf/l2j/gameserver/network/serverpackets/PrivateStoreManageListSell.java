package net.sf.l2j.gameserver.network.serverpackets;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.TradeList.TradeItem;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public class PrivateStoreManageListSell extends L2GameServerPacket
{
  private L2PcInstance _activeChar;
  private int _playerAdena;
  private boolean _packageSale;
  private FastList<TradeList.TradeItem> _itemList;
  private FastList<TradeList.TradeItem> _sellList;

  public PrivateStoreManageListSell(L2PcInstance player)
  {
    _activeChar = player;
    _playerAdena = _activeChar.getAdena();
    _activeChar.getSellList().updateItems();
    _packageSale = _activeChar.getSellList().isPackaged();
    _itemList = _activeChar.getInventory().getAvailableItems(_activeChar.getSellList());
    _sellList = _activeChar.getSellList().getItems();
  }

  protected final void writeImpl()
  {
    writeC(154);

    writeD(_activeChar.getObjectId());
    writeD(_packageSale ? 1 : 0);
    writeD(_playerAdena);

    writeD(_itemList.size());
    FastList.Node n = _itemList.head(); for (FastList.Node end = _itemList.tail(); (n = n.getNext()) != end; )
    {
      TradeList.TradeItem item = (TradeList.TradeItem)n.getValue();

      L2ItemInstance InvItem = _activeChar.getInventory().getItemByObjectId(item.getObjectId());
      if (InvItem.isAugmented())
        continue;
      writeD(item.getItem().getType2());
      writeD(item.getObjectId());
      writeD(item.getItem().getItemId());
      writeD(item.getCount());
      writeH(0);
      writeH(item.getEnchant());
      writeH(0);
      writeD(item.getItem().getBodyPart());
      writeD(item.getPrice());
    }

    writeD(_sellList.size());
    FastList.Node n = _sellList.head(); for (FastList.Node end = _sellList.tail(); (n = n.getNext()) != end; )
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
    return "S.PrivateSellListSell";
  }
}