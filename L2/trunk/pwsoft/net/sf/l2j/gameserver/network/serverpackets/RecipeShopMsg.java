package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2ManufactureList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class RecipeShopMsg extends L2GameServerPacket
{
  private int _chaObjectId;
  private String _chaStoreName;

  public RecipeShopMsg(L2PcInstance player)
  {
    if ((player.getCreateList() == null) || (player.getCreateList().getStoreName() == null)) {
      return;
    }
    _chaObjectId = player.getObjectId();
    _chaStoreName = player.getCreateList().getStoreName();
  }

  protected final void writeImpl()
  {
    writeC(219);
    writeD(_chaObjectId);
    writeS(_chaStoreName);
  }
}