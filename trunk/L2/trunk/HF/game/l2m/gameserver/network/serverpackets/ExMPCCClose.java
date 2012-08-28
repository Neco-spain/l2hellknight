package l2m.gameserver.network.serverpackets;

public class ExMPCCClose extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new ExMPCCClose();

  protected void writeImpl()
  {
    writeEx(19);
  }
}