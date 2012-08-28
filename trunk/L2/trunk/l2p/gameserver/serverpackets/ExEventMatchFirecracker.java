package l2p.gameserver.serverpackets;

public class ExEventMatchFirecracker extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(5);
  }
}