package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public class RequestPrivateStoreQuitBuy extends L2GameClientPacket
{
  private static final String _C__93_REQUESTPRIVATESTOREQUITBUY = "[C] 93 RequestPrivateStoreQuitBuy";

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;

    player.setPrivateStoreType(0);
    player.standUp();
    player.broadcastUserInfo();
  }

  public String getType()
  {
    return "[C] 93 RequestPrivateStoreQuitBuy";
  }
}