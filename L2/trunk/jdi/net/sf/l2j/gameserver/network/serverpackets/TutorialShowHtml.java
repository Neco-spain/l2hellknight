package net.sf.l2j.gameserver.network.serverpackets;

public class TutorialShowHtml extends L2GameServerPacket
{
  private static final String _S__A0_TUTORIALSHOWHTML = "[S] a0 TutorialShowHtml";
  private String _html;

  public TutorialShowHtml(String html)
  {
    _html = html;
  }

  protected void writeImpl()
  {
    writeC(160);
    writeS(_html);
  }

  public String getType()
  {
    return "[S] a0 TutorialShowHtml";
  }
}