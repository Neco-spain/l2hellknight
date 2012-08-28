package l2m.gameserver.network.serverpackets;

public class ExOlympiadMatchEnd extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new ExOlympiadMatchEnd();

  protected void writeImpl()
  {
    writeEx(45);
  }
}