package l2p.gameserver.serverpackets;

public class ServerClose extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new ServerClose();

  protected void writeImpl()
  {
    writeC(32);
  }
}