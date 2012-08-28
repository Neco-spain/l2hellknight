package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2ManufactureList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopManageList;

public final class RequestRecipeShopManageList extends L2GameClientPacket
{
  private static final String _C__B0_RequestRecipeShopManageList = "[C] b0 RequestRecipeShopManageList";

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    if (player.isAlikeDead())
    {
      sendPacket(new ActionFailed());
      return;
    }
    if (player.getPrivateStoreType() != 0) {
      player.setPrivateStoreType(0);
      player.broadcastUserInfo();
      if (player.isSitting()) player.standUp();
    }
    if (player.getCreateList() == null)
    {
      player.setCreateList(new L2ManufactureList());
    }

    player.sendPacket(new RecipeShopManageList(player, true));
  }

  public String getType()
  {
    return "[C] b0 RequestRecipeShopManageList";
  }
}