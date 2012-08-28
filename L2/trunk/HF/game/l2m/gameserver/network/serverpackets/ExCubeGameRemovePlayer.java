package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Player;

public class ExCubeGameRemovePlayer extends L2GameServerPacket
{
  private int _objectId;
  private boolean _isRedTeam;

  public ExCubeGameRemovePlayer(Player player, boolean isRedTeam)
  {
    _objectId = player.getObjectId();
    _isRedTeam = isRedTeam;
  }

  protected void writeImpl()
  {
    writeEx(151);
    writeD(2);

    writeD(-1);

    writeD(_isRedTeam ? 1 : 0);
    writeD(_objectId);
  }
}