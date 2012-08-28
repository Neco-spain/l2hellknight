package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.util.Util;

public final class RequestRecipeShopMakeItem extends L2GameClientPacket
{
  private int _id;
  private int _recipeId;
  private int _unknow;

  protected void readImpl()
  {
    _id = readD();
    _recipeId = readD();
    _unknow = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null)
      return;
    L2PcInstance manufacturer = L2World.getInstance().getPlayer(_id);
    if (manufacturer == null) {
      return;
    }
    if (player.getPrivateStoreType() != 0)
    {
      player.sendMessage("Cannot make items while trading");
      return;
    }
    if (manufacturer.getPrivateStoreType() != 5)
    {
      return;
    }

    if ((player.isInCraftMode()) || (manufacturer.isInCraftMode()))
    {
      player.sendMessage("Currently in Craft Mode");
      return;
    }
    if ((manufacturer.isInDuel()) || (player.isInDuel()))
    {
      player.sendPacket(Static.CANT_CRAFT_DURING_COMBAT);
      return;
    }
    if (Util.checkIfInRange(150, player, manufacturer, true))
      RecipeController.getInstance().requestManufactureItem(manufacturer, _recipeId, player);
  }
}