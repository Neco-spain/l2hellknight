package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.network.L2GameClient;

public class RequestTutorialLinkHtml extends L2GameClientPacket
{
  String _bypass;

  public void readImpl()
  {
    try
    {
      _bypass = readS();
    }
    catch (BufferUnderflowException e)
    {
      _bypass = "no";
    }
  }

  public void runImpl()
  {
    if (_bypass.equalsIgnoreCase("no")) {
      return;
    }
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (System.currentTimeMillis() - player.gCPAR() < 200L) {
      return;
    }
    player.sCPAR();

    Quest q = QuestManager.getInstance().getQuest(255);
    if (q != null)
      player.processQuestEvent(q.getName(), _bypass);
  }

  public String getType()
  {
    return "C.TutorialLinkHtml";
  }
}