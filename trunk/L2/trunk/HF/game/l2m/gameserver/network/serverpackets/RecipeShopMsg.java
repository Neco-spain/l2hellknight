package l2m.gameserver.serverpackets;

import l2m.gameserver.model.Player;

public class RecipeShopMsg extends L2GameServerPacket
{
  private int _objectId;
  private String _storeName;

  public RecipeShopMsg(Player player)
  {
    _objectId = player.getObjectId();
    _storeName = player.getManufactureName();
  }

  protected final void writeImpl()
  {
    writeC(225);
    writeD(_objectId);
    writeS(_storeName);
  }
}