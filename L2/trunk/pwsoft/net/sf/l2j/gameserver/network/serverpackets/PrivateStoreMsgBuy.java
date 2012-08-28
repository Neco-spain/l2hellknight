package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class PrivateStoreMsgBuy extends L2GameServerPacket
{
  private L2PcInstance _activeChar;
  private String _storeMsg;

  public PrivateStoreMsgBuy(L2PcInstance player)
  {
    _activeChar = player;
    if (_activeChar.getBuyList() != null)
      _storeMsg = _activeChar.getBuyList().getTitle();
  }

  protected final void writeImpl()
  {
    writeC(185);
    writeD(_activeChar.getObjectId());
    writeS(_storeMsg);
  }

  public String getType()
  {
    return "S.PrivateStoreMsgBuy";
  }
}