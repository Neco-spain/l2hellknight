package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.instances.StaticObjectInstance;

public class ChairSit extends L2GameServerPacket
{
  private int _objectId;
  private int _staticObjectId;

  public ChairSit(Player player, StaticObjectInstance throne)
  {
    _objectId = player.getObjectId();
    _staticObjectId = throne.getUId();
  }

  protected final void writeImpl()
  {
    writeC(237);
    writeD(_objectId);
    writeD(_staticObjectId);
  }
}