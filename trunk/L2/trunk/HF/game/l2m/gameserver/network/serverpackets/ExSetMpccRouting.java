package l2m.gameserver.network.serverpackets;

public class ExSetMpccRouting extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(55);
  }
}