package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.TradeList.TradeItem;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public class PrivateStoreManageListBuy extends L2GameServerPacket
{
  private static final String _S__D0_PRIVATESELLLISTBUY = "[S] b7 PrivateSellListBuy";
  private L2PcInstance _activeChar;
  private int _playerAdena;
  private L2ItemInstance[] _itemList;
  private TradeList.TradeItem[] _buyList;

  public PrivateStoreManageListBuy(L2PcInstance player)
  {
    _activeChar = player;
    _playerAdena = _activeChar.getAdena();
    _itemList = _activeChar.getInventory().getUniqueItems(false, true);
    _buyList = _activeChar.getBuyList().getItems();
  }

  protected final void writeImpl()
  {
    writeC(183);

    writeD(_activeChar.getObjectId());
    writeD(_playerAdena);

    writeD(_itemList.length);
    for (L2ItemInstance item : _itemList)
    {
      if (item.isAugmented())
      {
        continue;
      }
      writeD(item.getItemId());
      writeH(item.getEnchantLevel());
      writeD(item.getCount());
      writeD(item.getReferencePrice());
      writeH(0);
      writeD(item.getItem().getBodyPart());
      writeH(item.getItem().getType2());
    }

    writeD(_buyList.length);
    for (TradeList.TradeItem item : _buyList)
    {
      writeD(item.getItem().getItemId());
      writeH(item.getEnchant());
      writeD(item.getCount());
      writeD(item.getItem().getReferencePrice());
      writeH(0);
      writeD(item.getItem().getBodyPart());
      writeH(item.getItem().getType2());
      writeD(item.getPrice());
      writeD(item.getItem().getReferencePrice());
    }
  }

  public String getType()
  {
    return "[S] b7 PrivateSellListBuy";
  }
}