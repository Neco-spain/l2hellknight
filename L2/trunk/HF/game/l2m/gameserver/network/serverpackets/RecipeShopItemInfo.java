package l2m.gameserver.serverpackets;

import l2m.gameserver.model.Player;

public class RecipeShopItemInfo extends L2GameServerPacket
{
  private int _recipeId;
  private int _shopId;
  private int _curMp;
  private int _maxMp;
  private int _success = -1;
  private long _price;

  public RecipeShopItemInfo(Player activeChar, Player manufacturer, int recipeId, long price, int success)
  {
    _recipeId = recipeId;
    _shopId = manufacturer.getObjectId();
    _price = price;
    _success = success;
    _curMp = (int)manufacturer.getCurrentMp();
    _maxMp = manufacturer.getMaxMp();
  }

  protected final void writeImpl()
  {
    writeC(224);
    writeD(_shopId);
    writeD(_recipeId);
    writeD(_curMp);
    writeD(_maxMp);
    writeD(_success);
    writeQ(_price);
  }
}