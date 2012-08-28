package l2p.gameserver.serverpackets;

public class ExMPCCOpen extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new ExMPCCOpen();

  protected void writeImpl()
  {
    writeEx(18);
  }
}