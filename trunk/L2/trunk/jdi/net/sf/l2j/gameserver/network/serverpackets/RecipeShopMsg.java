package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2ManufactureList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class RecipeShopMsg extends L2GameServerPacket
{
  private static final String _S__DB_RecipeShopMsg = "[S] db RecipeShopMsg";
  private L2PcInstance _activeChar;

  public RecipeShopMsg(L2PcInstance player)
  {
    _activeChar = player;
  }

  protected final void writeImpl()
  {
    writeC(219);
    writeD(_activeChar.getObjectId());
    writeS(_activeChar.getCreateList().getStoreName());
  }

  public String getType()
  {
    return "[S] db RecipeShopMsg";
  }
}