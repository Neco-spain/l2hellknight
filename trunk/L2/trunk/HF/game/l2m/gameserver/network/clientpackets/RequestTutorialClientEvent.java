package l2m.gameserver.network.clientpackets;

import l2m.gameserver.instancemanager.QuestManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.quest.Quest;
import l2m.gameserver.network.GameClient;

public class RequestTutorialClientEvent extends L2GameClientPacket
{
  int event = 0;

  protected void readImpl()
  {
    event = readD();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    Quest tutorial = QuestManager.getQuest(255);
    if (tutorial != null)
      player.processQuestEvent(tutorial.getName(), "CE" + event, null);
  }
}