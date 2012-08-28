package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.RecipeShopSellList;

public class RequestRecipeShopSellList extends L2GameClientPacket
{
  int _manufacturerId;

  protected void readImpl()
  {
    _manufacturerId = readD();
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

    activeChar.sendPacket(new RecipeShopSellList(activeChar, manufacturer));
  }
}