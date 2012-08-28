package l2m.gameserver.network.serverpackets;

public class FlySelfDestination extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(67);
  }
}