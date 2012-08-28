package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
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
  private static final String _C__64_REQUESTQUESTABORT = "[C] 64 RequestQuestAbort";
  private static Logger _log = Logger.getLogger(RequestQuestAbort.class.getName());
  private int _questId;

  protected void readImpl()
  {
    _questId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    Quest qe = QuestManager.getInstance().getQuest(_questId);
    if (qe != null)
    {
      QuestState qs = activeChar.getQuestState(qe.getName());
      if (qs != null)
      {
        qs.exitQuest(true);
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
        sm.addString("Quest aborted.");
        activeChar.sendPacket(sm);
        sm = null;
        QuestList ql = new QuestList();
        activeChar.sendPacket(ql);
      }
      else if (Config.DEBUG) { _log.info("Player '" + activeChar.getName() + "' try to abort quest " + qe.getName() + " but he didn't have it started.");
      }

    }
    else if (Config.DEBUG) { _log.warning("Quest (id='" + _questId + "') not found.");
    }
  }

  public String getType()
  {
    return "[C] 64 RequestQuestAbort";
  }
}