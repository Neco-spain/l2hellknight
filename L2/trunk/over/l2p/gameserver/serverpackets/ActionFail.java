package l2p.gameserver.serverpackets;

public class ActionFail extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new ActionFail();

  protected final void writeImpl()
  {
    writeC(31);
  }
}