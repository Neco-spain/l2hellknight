package l2m.gameserver.serverpackets;

public class TutorialShowHtml extends L2GameServerPacket
{
  private String _html;

  public TutorialShowHtml(String html)
  {
    _html = html;
  }

  protected final void writeImpl()
  {
    writeC(166);
    writeS(_html);
  }
}