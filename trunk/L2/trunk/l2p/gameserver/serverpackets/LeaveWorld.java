package l2p.gameserver.serverpackets;

public class LeaveWorld extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new LeaveWorld();

  protected final void writeImpl()
  {
    writeC(132);
  }
}