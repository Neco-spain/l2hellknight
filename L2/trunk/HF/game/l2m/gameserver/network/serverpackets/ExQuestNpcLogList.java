package l2m.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import l2m.gameserver.model.quest.Quest;
import l2m.gameserver.model.quest.QuestNpcLogInfo;
import l2m.gameserver.model.quest.QuestState;

public class ExQuestNpcLogList extends L2GameServerPacket
{
  private int _questId;
  private List<int[]> _logList = Collections.emptyList();

  public ExQuestNpcLogList(QuestState state)
  {
    _questId = state.getQuest().getQuestIntId();
    int cond = state.getCond();
    List vars = state.getQuest().getNpcLogList(cond);
    if (vars == null) {
      return;
    }
    _logList = new ArrayList(vars.size());
    for (QuestNpcLogInfo entry : vars)
    {
      int[] i = new int[2];
      i[0] = (entry.getNpcIds()[0] + 1000000);
      i[1] = state.getInt(entry.getVarName());
      _logList.add(i);
    }
  }

  protected void writeImpl()
  {
    writeEx(197);
    writeD(_questId);
    writeC(_logList.size());
    for (int i = 0; i < _logList.size(); i++)
    {
      int[] values = (int[])_logList.get(i);
      writeD(values[0]);
      writeC(0);
      writeD(values[1]);
    }
  }
}