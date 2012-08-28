package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestRecipeItemMakeSelf extends L2GameClientPacket
{
  private int _id;

  protected void readImpl()
  {
    _id = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (player.getPrivateStoreType() != 0)
    {
      player.sendMessage("Cannot make items while trading");
      return;
    }

    if (player.isInCraftMode())
    {
      player.sendMessage("Currently in Craft Mode");
      return;
    }

    RecipeController.getInstance().requestMakeItem(player, _id);
  }

  public String getType()
  {
    return "C.RecipeItemMakeSelf";
  }
}