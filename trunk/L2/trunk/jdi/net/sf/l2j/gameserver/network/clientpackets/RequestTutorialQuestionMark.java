package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.L2GameClient;

public class RequestTutorialQuestionMark extends L2GameClientPacket
{
  int _number = 0;

  protected void readImpl()
  {
    _number = readD();
  }

  protected void runImpl() {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

    if (player == null) {
      return;
    }
    QuestState qs = player.getQuestState("255_Tutorial");
    if (qs != null)
      qs.getQuest().notifyEvent("QM" + _number + "", null, player);
  }

  public String getType()
  {
    return "[C] 7d RequestTutorialQuestionMark";
  }
}