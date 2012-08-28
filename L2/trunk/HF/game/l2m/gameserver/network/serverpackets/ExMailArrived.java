package l2m.gameserver.network.serverpackets;

public class ExMailArrived extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new ExMailArrived();

  protected final void writeImpl()
  {
    writeEx(46);
  }
}