package l2m.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.quest.Quest;
import l2m.gameserver.model.quest.QuestState;

public class QuestList extends L2GameServerPacket
{
  private List<int[]> questlist;
  private static byte[] unk = new byte['\u0080'];

  public QuestList(Player player)
  {
    QuestState[] allQuestStates = player.getAllQuestsStates();
    questlist = new ArrayList(allQuestStates.length);
    for (QuestState quest : allQuestStates)
      if ((quest.getQuest().isVisible()) && (quest.isStarted()))
        questlist.add(new int[] { quest.getQuest().getQuestIntId(), quest.getInt("cond") });
  }

  protected final void writeImpl()
  {
    writeC(134);
    writeH(questlist.size());
    for (int[] q : questlist)
    {
      writeD(q[0]);
      writeD(q[1]);
    }
    writeB(unk);
  }
}