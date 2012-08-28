package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import net.sf.l2j.gameserver.util.Util;

public final class SetPrivateStoreMsgBuy extends L2GameClientPacket
{
  private static final String _C__94_SETPRIVATESTOREMSGBUY = "[C] 94 SetPrivateStoreMsgBuy";
  private String _storeMsg;

  protected void readImpl()
  {
    _storeMsg = readS();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if ((player == null) || (player.getBuyList() == null)) return;
    if (_storeMsg.length() > 29)
    {
      Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to use overflow exploit!", Config.DEFAULT_PUNISH);
      return;
    }
    player.getBuyList().setTitle(_storeMsg);
    player.sendPacket(new PrivateStoreMsgBuy(player));
  }

  public String getType()
  {
    return "[C] 94 SetPrivateStoreMsgBuy";
  }
}