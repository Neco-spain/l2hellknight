package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestRecipeShopManageQuit extends L2GameClientPacket
{
  private static final String _C__B3_RequestRecipeShopManageQuit = "[C] b2 RequestRecipeShopManageQuit";

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    player.setPrivateStoreType(0);
    player.broadcastUserInfo();
    player.standUp();
  }

  public String getType()
  {
    return "[C] b2 RequestRecipeShopManageQuit";
  }
}