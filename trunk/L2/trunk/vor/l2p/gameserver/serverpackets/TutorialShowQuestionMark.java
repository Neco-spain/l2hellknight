package l2p.gameserver.serverpackets;

public class TutorialShowQuestionMark extends L2GameServerPacket
{
  private int _number;

  public TutorialShowQuestionMark(int number)
  {
    _number = number;
  }

  protected final void writeImpl()
  {
    writeC(167);
    writeD(_number);
  }
}