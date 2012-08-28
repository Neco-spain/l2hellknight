package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import org.apache.commons.lang3.StringUtils;

public class PrivateStoreMsgBuy extends L2GameServerPacket
{
  private int _objId;
  private String _name;

  public PrivateStoreMsgBuy(Player player)
  {
    _objId = player.getObjectId();
    _name = StringUtils.defaultString(player.getBuyStoreName());
  }

  protected final void writeImpl()
  {
    writeC(191);
    writeD(_objId);
    writeS(_name);
  }
}