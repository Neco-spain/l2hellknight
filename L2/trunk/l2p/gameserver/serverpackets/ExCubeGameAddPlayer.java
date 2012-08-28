package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;

public class ExCubeGameAddPlayer extends L2GameServerPacket
{
  private int _objectId;
  private String _name;
  boolean _isRedTeam;

  public ExCubeGameAddPlayer(Player player, boolean isRedTeam)
  {
    _objectId = player.getObjectId();
    _name = player.getName();
    _isRedTeam = isRedTeam;
  }

  protected void writeImpl()
  {
    writeEx(151);
    writeD(1);

    writeD(-1);

    writeD(_isRedTeam ? 1 : 0);
    writeD(_objectId);
    writeS(_name);
  }
}