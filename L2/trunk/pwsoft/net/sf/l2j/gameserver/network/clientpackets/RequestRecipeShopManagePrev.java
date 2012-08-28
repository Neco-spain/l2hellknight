package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopSellList;

public final class RequestRecipeShopManagePrev extends L2GameClientPacket
{
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
      player.sendActionFailed();
      return;
    }

    if (!player.getTarget().isPlayer()) {
      return;
    }
    player.sendPacket(new RecipeShopSellList(player, player.getTarget().getPlayer()));
  }
}