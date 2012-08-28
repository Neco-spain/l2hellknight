package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopItemInfo;

public final class RequestRecipeShopMakeInfo extends L2GameClientPacket
{
  private static final String _C__B5_RequestRecipeShopMakeInfo = "[C] b5 RequestRecipeShopMakeInfo";
  private int _playerObjectId;
  private int _recipeId;

  protected void readImpl()
  {
    _playerObjectId = readD();
    _recipeId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    player.sendPacket(new RecipeShopItemInfo(_playerObjectId, _recipeId));
  }

  public String getType()
  {
    return "[C] b5 RequestRecipeShopMakeInfo";
  }
}