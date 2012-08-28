package l2p.gameserver.serverpackets;

public class FlySelfDestination extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(67);
  }
}