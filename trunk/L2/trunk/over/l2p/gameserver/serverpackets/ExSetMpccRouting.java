package l2p.gameserver.serverpackets;

public class ExSetMpccRouting extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(55);
  }
}