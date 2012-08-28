package l2p.gameserver.serverpackets;

public class DismissAlliance extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(173);
  }
}