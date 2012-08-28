package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;

public class GMViewQuestList extends L2GameServerPacket
{
  private L2PcInstance _activeChar;

  public GMViewQuestList(L2PcInstance cha)
  {
    _activeChar = cha;
  }

  protected final void writeImpl()
  {
    writeC(147);
    writeS(_activeChar.getName());

    Quest[] questList = _activeChar.getAllActiveQuests();

    if (questList.length == 0)
    {
      writeC(0);
      writeH(0);
      writeH(0);
      return;
    }

    writeH(questList.length);

    for (Quest q : questList)
    {
      writeD(q.getQuestIntId());

      QuestState qs = _activeChar.getQuestState(q.getName());

      if (qs == null)
      {
        writeD(0);
      }
      else
      {
        writeD(qs.getInt("cond"));
      }
    }
  }
}