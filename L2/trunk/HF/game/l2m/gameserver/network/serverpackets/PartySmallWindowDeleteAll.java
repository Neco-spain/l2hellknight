package l2m.gameserver.serverpackets;

public class PartySmallWindowDeleteAll extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new PartySmallWindowDeleteAll();

  protected final void writeImpl()
  {
    writeC(80);
  }
}