package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.entity.events.impl.KrateisCubeEvent;
import l2m.gameserver.model.entity.events.objects.KrateisCubePlayerObject;

public class ExPVPMatchCCRecord extends L2GameServerPacket
{
  private final KrateisCubePlayerObject[] _players;

  public ExPVPMatchCCRecord(KrateisCubeEvent cube)
  {
    _players = cube.getSortedPlayers();
  }

  public void writeImpl()
  {
    writeEx(137);
    writeD(0);
    writeD(_players.length);
    for (KrateisCubePlayerObject p : _players)
    {
      writeS(p.getName());
      writeD(p.getPoints());
    }
  }
}