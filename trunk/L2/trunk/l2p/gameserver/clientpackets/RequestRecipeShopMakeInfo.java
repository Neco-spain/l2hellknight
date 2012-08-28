package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ManufactureItem;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.RecipeShopItemInfo;

public class RequestRecipeShopMakeInfo extends L2GameClientPacket
{
  private int _manufacturerId;
  private int _recipeId;

  protected void readImpl()
  {
    _manufacturerId = readD();
    _recipeId = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (activeChar.isActionsDisabled())
    {
      activeChar.sendActionFailed();
      return;
    }

    Player manufacturer = (Player)activeChar.getVisibleObject(_manufacturerId);
    if ((manufacturer == null) || (manufacturer.getPrivateStoreType() != 5) || (!manufacturer.isInRangeZ(activeChar, 200L)))
    {
      activeChar.sendActionFailed();
      return;
    }

    long price = -1L;
    for (ManufactureItem i : manufacturer.getCreateList()) {
      if (i.getRecipeId() == _recipeId)
      {
        price = i.getCost();
        break;
      }
    }
    if (price == -1L)
    {
      activeChar.sendActionFailed();
      return;
    }

    activeChar.sendPacket(new RecipeShopItemInfo(activeChar, manufacturer, _recipeId, price, -1));
  }
}