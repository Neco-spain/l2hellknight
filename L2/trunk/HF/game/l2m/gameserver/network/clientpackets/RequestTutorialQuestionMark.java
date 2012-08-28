package l2m.gameserver.network.clientpackets;

import l2m.gameserver.instancemanager.QuestManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.quest.Quest;
import l2m.gameserver.network.GameClient;

public class RequestTutorialQuestionMark extends L2GameClientPacket
{
  int _number = 0;

  protected void readImpl()
  {
    _number = readD();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    Quest q = QuestManager.getQuest(255);
    if (q != null)
      player.processQuestEvent(q.getName(), "QM" + _number, null);
  }
}