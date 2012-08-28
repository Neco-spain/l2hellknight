package l2m.gameserver.serverpackets;

import l2m.gameserver.model.Player;
import org.apache.commons.lang3.StringUtils;

public class PrivateStoreMsgSell extends L2GameServerPacket
{
  private final int _objId;
  private final String _name;
  private boolean _pkg;

  public PrivateStoreMsgSell(Player player)
  {
    _objId = player.getObjectId();
    _pkg = (player.getPrivateStoreType() == 8);
    _name = StringUtils.defaultString(player.getSellStoreName());
  }

  protected final void writeImpl()
  {
    if (_pkg)
    {
      writeEx(128);
    }
    else
      writeC(162);
    writeD(_objId);
    writeS(_name);
  }
}