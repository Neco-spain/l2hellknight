package l2p.gameserver.serverpackets;

public class ExShowQuestMark extends L2GameServerPacket
{
  private int _questId;

  public ExShowQuestMark(int questId)
  {
    _questId = questId;
  }

  protected void writeImpl()
  {
    writeEx(33);
    writeD(_questId);
  }
}