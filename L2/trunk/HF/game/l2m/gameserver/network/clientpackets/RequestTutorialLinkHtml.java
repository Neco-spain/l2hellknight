package l2m.gameserver.network.clientpackets;

import l2m.gameserver.instancemanager.QuestManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.quest.Quest;
import l2m.gameserver.network.GameClient;

public class RequestTutorialLinkHtml extends L2GameClientPacket
{
  String _bypass;

  protected void readImpl()
  {
    _bypass = readS();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    Quest q = QuestManager.getQuest(255);
    if (q != null)
      player.processQuestEvent(q.getName(), _bypass, null);
  }
}