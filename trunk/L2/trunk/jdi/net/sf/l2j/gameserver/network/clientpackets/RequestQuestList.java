package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.network.serverpackets.QuestList;

public final class RequestQuestList extends L2GameClientPacket
{
  private static final String _C__63_REQUESTQUESTLIST = "[C] 63 RequestQuestList";

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    QuestList ql = new QuestList();
    sendPacket(ql);
  }

  public String getType()
  {
    return "[C] 63 RequestQuestList";
  }
}