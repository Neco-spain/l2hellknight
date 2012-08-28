package l2m.gameserver.network.serverpackets;

public class ExNotifyBirthDay extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new ExNotifyBirthDay();

  protected void writeImpl()
  {
    writeEx(143);
  }
}