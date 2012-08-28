package l2m.gameserver.network.serverpackets;

public class ExShowQuestInfo extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new ExShowQuestInfo();

  protected final void writeImpl()
  {
    writeEx(32);
  }
}