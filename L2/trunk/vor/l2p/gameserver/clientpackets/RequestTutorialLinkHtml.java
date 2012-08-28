package l2p.gameserver.clientpackets;

import l2p.gameserver.instancemanager.QuestManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.network.GameClient;

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