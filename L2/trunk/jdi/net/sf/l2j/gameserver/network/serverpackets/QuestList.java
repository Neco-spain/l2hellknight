package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.L2GameClient;

public class QuestList extends L2GameServerPacket
{
  private static final String _S__98_QUESTLIST = "[S] 80 QuestList";
  private Quest[] _quests;
  private L2PcInstance _activeChar;

  public void runImpl()
  {
    if ((getClient() != null) && (((L2GameClient)getClient()).getActiveChar() != null))
    {
      _activeChar = ((L2GameClient)getClient()).getActiveChar();
      _quests = _activeChar.getAllActiveQuests();
    }
  }

  protected final void writeImpl()
  {
    if ((_quests == null) || (_quests.length == 0))
    {
      writeC(128);
      writeH(0);
      writeH(0);
      return;
    }

    writeC(128);
    writeH(_quests.length);
    for (Quest q : _quests)
    {
      writeD(q.getQuestIntId());
      QuestState qs = _activeChar.getQuestState(q.getName());
      if (qs == null)
      {
        writeD(0);
      }
      else
      {
        int states = qs.getInt("__compltdStateFlags");
        if (states != 0)
          writeD(states);
        else
          writeD(qs.getInt("cond"));
      }
    }
  }

  public String getType()
  {
    return "[S] 80 QuestList";
  }
}