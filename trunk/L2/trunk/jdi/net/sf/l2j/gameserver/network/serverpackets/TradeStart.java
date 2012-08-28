package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public class TradeStart extends L2GameServerPacket
{
  private static final String _S__2E_TRADESTART = "[S] 1E TradeStart";
  private L2PcInstance _activeChar;
  private L2ItemInstance[] _itemList;

  public TradeStart(L2PcInstance player)
  {
    _activeChar = player;
    _itemList = _activeChar.getInventory().getAvailableItems(true);
  }

  protected final void writeImpl()
  {
    if ((_activeChar.getActiveTradeList() == null) || (_activeChar.getActiveTradeList().getPartner() == null)) {
      return;
    }
    writeC(30);
    writeD(_activeChar.getActiveTradeList().getPartner().getObjectId());

    writeH(_itemList.length);
    for (L2ItemInstance item : _itemList)
    {
      writeH(item.getItem().getType1());
      writeD(item.getObjectId());
      writeD(item.getItemId());
      writeD(item.getCount());
      writeH(item.getItem().getType2());
      writeH(0);

      writeD(item.getItem().getBodyPart());
      writeH(item.getEnchantLevel());
      writeH(0);
      writeH(0);
    }
  }

  public String getType()
  {
    return "[S] 1E TradeStart";
  }
}