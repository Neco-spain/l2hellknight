package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.L2GameClient;

public class RequestTutorialPassCmdToServer extends L2GameClientPacket
{
  String _bypass = null;

  protected void readImpl()
  {
    _bypass = readS();
  }

  protected void runImpl() {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

    if (player == null) {
      return;
    }
    QuestState qs = player.getQuestState("255_Tutorial");
    if (qs != null)
      qs.getQuest().notifyEvent(_bypass, null, player);
  }

  public String getType()
  {
    return "[C] 7c RequestTutorialPassCmdToServer";
  }
}