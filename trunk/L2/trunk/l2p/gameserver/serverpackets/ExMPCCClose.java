package l2p.gameserver.serverpackets;

public class ExMPCCClose extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new ExMPCCClose();

  protected void writeImpl()
  {
    writeEx(19);
  }
}