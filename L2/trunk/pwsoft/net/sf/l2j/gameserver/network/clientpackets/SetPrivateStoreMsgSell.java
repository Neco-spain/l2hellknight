package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreMsgSell;

public class SetPrivateStoreMsgSell extends L2GameClientPacket
{
  private String _storeMsg;

  protected void readImpl()
  {
    _storeMsg = readS();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if ((player == null) || (player.getSellList() == null)) {
      return;
    }
    player.getSellList().setTitle(_storeMsg);
    sendPacket(new PrivateStoreMsgSell(player));
  }

  public String getType()
  {
    return "[C] SetPrivateStoreMsgSell";
  }
}