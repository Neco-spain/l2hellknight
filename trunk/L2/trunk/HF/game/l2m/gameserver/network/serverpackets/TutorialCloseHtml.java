package l2m.gameserver.serverpackets;

public class TutorialCloseHtml extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new TutorialCloseHtml();

  protected final void writeImpl()
  {
    writeC(169);
  }
}