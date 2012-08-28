package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreMsgBuy;

public final class SetPrivateStoreMsgBuy extends L2GameClientPacket
{
  private String _storeMsg;

  protected void readImpl()
  {
    _storeMsg = readS();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if ((player == null) || (player.getBuyList() == null)) {
      return;
    }
    player.getBuyList().setTitle(_storeMsg);
    player.sendPacket(new PrivateStoreMsgBuy(player));
  }
}