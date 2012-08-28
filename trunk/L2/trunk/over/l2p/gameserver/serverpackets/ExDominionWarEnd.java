package l2p.gameserver.serverpackets;

public class ExDominionWarEnd extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new ExDominionWarEnd();

  public void writeImpl()
  {
    writeEx(164);
  }
}