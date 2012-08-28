package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Player;

public class ExCubeGameChangeTeam extends L2GameServerPacket
{
  private int _objectId;
  private boolean _fromRedTeam;

  public ExCubeGameChangeTeam(Player player, boolean fromRedTeam)
  {
    _objectId = player.getObjectId();
    _fromRedTeam = fromRedTeam;
  }

  protected void writeImpl()
  {
    writeEx(151);
    writeD(5);

    writeD(_objectId);
    writeD(_fromRedTeam ? 1 : 0);
    writeD(_fromRedTeam ? 0 : 1);
  }
}