package l2m.gameserver.network.clientpackets;

import l2m.gameserver.instancemanager.QuestManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.quest.Quest;
import l2m.gameserver.model.quest.QuestState;
import l2m.gameserver.network.GameClient;

public class RequestQuestAbort extends L2GameClientPacket
{
  private int _questID;

  protected void readImpl()
  {
    _questID = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    Quest quest = QuestManager.getQuest(_questID);
    if ((activeChar == null) || (quest == null)) {
      return;
    }
    if (!quest.canAbortByPacket()) {
      return;
    }
    QuestState qs = activeChar.getQuestState(quest.getClass());
    if ((qs != null) && (!qs.isCompleted()))
      qs.abortQuest();
  }
}