package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Util;

public final class RequestRecipeShopMakeItem extends L2GameClientPacket
{
  private static final String _C__AF_REQUESTRECIPESHOPMAKEITEM = "[C] B6 RequestRecipeShopMakeItem";
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
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    L2PcInstance manufacturer = (L2PcInstance)L2World.getInstance().findObject(_id);
    if (manufacturer == null) {
      return;
    }
    if (activeChar.getPrivateStoreType() != 0)
    {
      activeChar.sendMessage("Cannot make items while trading");
      return;
    }
    if (manufacturer.getPrivateStoreType() != 5)
    {
      return;
    }

    if ((activeChar.isInCraftMode()) || (manufacturer.isInCraftMode()))
    {
      activeChar.sendMessage("Currently in Craft Mode");
      return;
    }
    if ((manufacturer.isInDuel()) || (activeChar.isInDuel()))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_CRAFT_DURING_COMBAT));
      return;
    }
    if (Util.checkIfInRange(150, activeChar, manufacturer, true))
      RecipeController.getInstance().requestManufactureItem(manufacturer, _recipeId, activeChar);
  }

  public String getType()
  {
    return "[C] B6 RequestRecipeShopMakeItem";
  }
}