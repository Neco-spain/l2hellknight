package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.LoginServerThread.SessionKey;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.L2GameClient.GameClientState;
import net.sf.l2j.gameserver.network.serverpackets.CharSelectInfo;
import net.sf.l2j.gameserver.network.serverpackets.RestartResponse;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

public final class RequestRestart extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestRestart.class.getName());

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (System.currentTimeMillis() - player.gCPAV() < 100L) {
      return;
    }
    player.sCPAV();

    if (player.isInOlympiadMode())
    {
      player.sendPacket(Static.CANT_LOGOUT_OLY);
      return;
    }

    if (player.isTeleporting())
    {
      player.abortCast();
      player.setIsTeleporting(false);
    }

    player.getInventory().updateDatabase();

    if (player.getPrivateStoreType() != 0)
    {
      if (player.isInOfflineMode())
        player.closeNetConnection();
      else
        player.sendPacket(Static.CANT_LOGOUT_TRADE);
      return;
    }

    if (player.getActiveTradeList() != null)
    {
      player.cancelActiveTrade();
      if (player.getTransactionRequester() != null)
        player.getTransactionRequester().setTransactionRequester(null);
      player.setTransactionRequester(null);
    }

    if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player))
    {
      player.sendPacket(Static.CANT_RESTART_WHILE_FIGHTING);
      player.sendActionFailed();
      return;
    }

    if (player.isFestivalParticipant())
    {
      if (SevenSignsFestival.getInstance().isFestivalInitialized())
      {
        player.sendPacket(SystemMessage.sendString("You cannot restart while you are a participant in a festival."));
        player.sendActionFailed();
        return;
      }
      L2Party playerParty = player.getParty();

      if (playerParty != null)
        player.getParty().broadcastToPartyMembers(SystemMessage.sendString(player.getName() + " has been removed from the upcoming festival."));
    }
    if (player.isFlying())
    {
      player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
    }

    L2GameClient client = (L2GameClient)getClient();

    player.setClient(null);

    TvTEvent.onLogout(player);

    player.deleteMe();
    L2GameClient.saveCharToDisk(client.getActiveChar());

    ((L2GameClient)getClient()).setActiveChar(null);

    client.setState(L2GameClient.GameClientState.AUTHED);

    sendPacket(new RestartResponse());

    CharSelectInfo cl = new CharSelectInfo(client.getAccountName(), client.getSessionId().playOkID1);
    sendPacket(cl);
    client.setCharSelection(cl.getCharInfo());
  }

  public String getType()
  {
    return "C.Restart";
  }
}