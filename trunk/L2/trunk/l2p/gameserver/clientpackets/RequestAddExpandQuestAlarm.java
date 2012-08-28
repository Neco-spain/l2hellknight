package l2p.gameserver.clientpackets;

import l2p.gameserver.instancemanager.QuestManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExQuestNpcLogList;

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