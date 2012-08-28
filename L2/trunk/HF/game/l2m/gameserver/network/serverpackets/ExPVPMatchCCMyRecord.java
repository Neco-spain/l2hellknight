package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.entity.events.objects.KrateisCubePlayerObject;

public class ExPVPMatchCCMyRecord extends L2GameServerPacket
{
  private int _points;

  public ExPVPMatchCCMyRecord(KrateisCubePlayerObject player)
  {
    _points = player.getPoints();
  }

  public void writeImpl()
  {
    writeEx(138);
    writeD(_points);
  }
}