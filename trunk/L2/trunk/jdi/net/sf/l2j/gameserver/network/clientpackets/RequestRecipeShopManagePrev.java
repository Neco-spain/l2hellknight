package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopSellList;

public final class RequestRecipeShopManagePrev extends L2GameClientPacket
{
  private static final String _C__B7_RequestRecipeShopPrev = "[C] b7 RequestRecipeShopPrev";

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if ((player == null) || (player.getTarget() == null)) {
      return;
    }

    if (player.isAlikeDead())
    {
      sendPacket(new ActionFailed());
      return;
    }

    if (!(player.getTarget() instanceof L2PcInstance))
      return;
    L2PcInstance target = (L2PcInstance)player.getTarget();
    player.sendPacket(new RecipeShopSellList(player, target));
  }

  public String getType()
  {
    return "[C] b7 RequestRecipeShopPrev";
  }
}