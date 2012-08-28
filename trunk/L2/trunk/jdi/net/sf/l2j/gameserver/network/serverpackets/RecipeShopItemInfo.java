package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class RecipeShopItemInfo extends L2GameServerPacket
{
  private static final String _S__DA_RecipeShopItemInfo = "[S] da RecipeShopItemInfo";
  private int _shopId;
  private int _recipeId;

  public RecipeShopItemInfo(int shopId, int recipeId)
  {
    _shopId = shopId;
    _recipeId = recipeId;
  }

  protected final void writeImpl()
  {
    if (!(L2World.getInstance().findObject(_shopId) instanceof L2PcInstance)) {
      return;
    }
    L2PcInstance manufacturer = (L2PcInstance)L2World.getInstance().findObject(_shopId);
    writeC(218);
    writeD(_shopId);
    writeD(_recipeId);
    writeD(manufacturer != null ? (int)manufacturer.getCurrentMp() : 0);
    writeD(manufacturer != null ? manufacturer.getMaxMp() : 0);
    writeD(-1);
  }

  public String getType()
  {
    return "[S] da RecipeShopItemInfo";
  }
}