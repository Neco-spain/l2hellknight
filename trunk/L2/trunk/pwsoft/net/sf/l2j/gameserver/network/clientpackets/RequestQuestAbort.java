package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.QuestList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestQuestAbort extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestQuestAbort.class.getName());
  private int _questId;

  protected void readImpl()
  {
    _questId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    Quest qe = QuestManager.getInstance().getQuest(_questId);
    if (qe != null)
    {
      QuestState qs = player.getQuestState(qe.getName());
      if (qs != null)
      {
        qs.exitQuest(true);
        player.sendPacket(new QuestList());
        player.sendPacket(SystemMessage.id(SystemMessageId.S1_S2).addString("Quest aborted."));
      }
    }
  }

  public String getType()
  {
    return "[C] 64 RequestQuestAbort";
  }
}