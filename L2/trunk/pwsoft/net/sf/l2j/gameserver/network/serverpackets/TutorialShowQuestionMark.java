package net.sf.l2j.gameserver.network.serverpackets;

public class TutorialShowQuestionMark extends L2GameServerPacket
{
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
    return "S.TutorialShowQuestionMark";
  }
}