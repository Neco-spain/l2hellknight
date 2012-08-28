package l2p.gameserver.serverpackets;

import l2p.gameserver.model.entity.events.objects.KrateisCubePlayerObject;

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