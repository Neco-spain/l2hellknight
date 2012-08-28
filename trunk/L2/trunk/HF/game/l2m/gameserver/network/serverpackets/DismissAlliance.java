package l2m.gameserver.network.serverpackets;

public class DismissAlliance extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(173);
  }
}