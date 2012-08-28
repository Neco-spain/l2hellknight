package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class PrivateStoreMsgSell extends L2GameServerPacket
{
  private L2PcInstance _activeChar;
  private String _storeMsg;

  public PrivateStoreMsgSell(L2PcInstance player)
  {
    _activeChar = player;
    if (_activeChar.getSellList() != null)
      _storeMsg = _activeChar.getSellList().getTitle();
  }

  protected final void writeImpl()
  {
    writeC(156);
    writeD(_activeChar.getObjectId());
    writeS(_storeMsg);
  }

  public String getType()
  {
    return "S.PrivateStoreMsgSell";
  }
}