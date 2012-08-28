package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestRecipeItemMakeSelf extends L2GameClientPacket
{
  private static final String _C__AF_REQUESTRECIPEITEMMAKESELF = "[C] AF RequestRecipeItemMakeSelf";
  private int _id;

  protected void readImpl()
  {
    _id = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (activeChar.getPrivateStoreType() != 0)
    {
      activeChar.sendMessage("Cannot make items while trading");
      return;
    }

    if (activeChar.isInCraftMode())
    {
      activeChar.sendMessage("Currently in Craft Mode");
      return;
    }

    RecipeController.getInstance().requestMakeItem(activeChar, _id);
  }

  public String getType()
  {
    return "[C] AF RequestRecipeItemMakeSelf";
  }
}