package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.L2GameClient;

public class RequestTutorialClientEvent extends L2GameClientPacket
{
  private static final String _C__7e_REQUESTTUTORIALCLIENTEVENT = "[C] 7e RequestTutorialClientEvent";
  int eventId = 0;

  protected void readImpl()
  {
    eventId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

    if (player == null) {
      return;
    }
    QuestState qs = player.getQuestState("255_Tutorial");
    if (qs != null)
      qs.getQuest().notifyEvent("CE" + eventId + "", null, player);
  }

  public String getType()
  {
    return "[C] 7e RequestTutorialClientEvent";
  }
}