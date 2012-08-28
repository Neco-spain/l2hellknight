package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Player;

public class ExBR_GamePoint extends L2GameServerPacket
{
  private int _objectId;
  private long _points;

  public ExBR_GamePoint(Player player)
  {
    _objectId = player.getObjectId();
    _points = player.getPremiumPoints();
  }

  protected void writeImpl()
  {
    writeEx(213);
    writeD(_objectId);
    writeQ(_points);
    writeD(0);
  }
}