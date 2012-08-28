package l2m.gameserver.network.serverpackets;

public class ExEventMatchFirecracker extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(5);
  }
}