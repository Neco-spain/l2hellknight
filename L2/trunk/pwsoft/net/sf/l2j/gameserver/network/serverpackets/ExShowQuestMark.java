package net.sf.l2j.gameserver.network.serverpackets;

public class ExShowQuestMark extends L2GameServerPacket
{
  private int _questId;

  public ExShowQuestMark(int questId)
  {
    _questId = questId;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(26);
    writeD(_questId);
  }
}