package net.sf.l2j.gameserver.network.serverpackets;

public class TutorialShowQuestionMark extends L2GameServerPacket
{
  private static final String _S__A1_TUTORIALSHOWQUESTIONMARK = "[S] a1 TutorialShowQuestionMark";
  private int _blink;

  public TutorialShowQuestionMark(int blink)
  {
    _blink = blink;
  }

  protected void writeImpl()
  {
    writeC(161);
    writeD(_blink);
  }

  public String getType()
  {
    return "[S] a1 TutorialShowQuestionMark";
  }
}