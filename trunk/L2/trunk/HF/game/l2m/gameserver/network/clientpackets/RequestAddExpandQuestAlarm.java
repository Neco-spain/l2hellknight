package l2m.gameserver.network.clientpackets;

import l2m.gameserver.instancemanager.QuestManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.quest.Quest;
import l2m.gameserver.model.quest.QuestState;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExQuestNpcLogList;

public class RequestAddExpandQuestAlarm extends L2GameClientPacket
{
  private int _questId;

  protected void readImpl()
    throws Exception
  {
    _questId = readD();
  }

  protected void runImpl()
    throws Exception
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    Quest quest = QuestManager.getQuest(_questId);
    if (quest == null) {
      return;
    }
    QuestState state = player.getQuestState(quest.getClass());
    if (state == null) {
      return;
    }
    player.sendPacket(new ExQuestNpcLogList(state));
  }
}