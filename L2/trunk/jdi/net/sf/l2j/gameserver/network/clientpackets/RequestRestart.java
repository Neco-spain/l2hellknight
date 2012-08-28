package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.LoginServerThread.SessionKey;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.CustomZoneManager;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.L2GameClient.GameClientState;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CharSelectInfo;
import net.sf.l2j.gameserver.network.serverpackets.RestartResponse;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

public final class RequestRestart extends L2GameClientPacket
{
  private static final String _C__46_REQUESTRESTART = "[C] 46 RequestRestart";
  private static Logger _log = Logger.getLogger(RequestRestart.class.getName());

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null)
    {
      _log.warning("[RequestRestart] activeChar null!?");
      return;
    }

    if ((Olympiad.getInstance().isRegisteredInComp(player)) || (player.getOlympiadGameId() > 0))
    {
      player.sendMessage("You cant logout in olympiad mode");
      return;
    }

    if (player.isTeleporting())
    {
      player.sendPacket(SystemMessage.sendString("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0432\u044B\u0439\u0442\u0438 \u043F\u043E\u043A\u0430 \u0442\u0435\u043B\u0435\u043F\u043E\u0440\u0442\u0438\u0440\u0443\u0435\u0442\u0435\u0441\u044C!"));
      return;
    }

    if (player.isMounted())
    {
      player.sendPacket(SystemMessage.sendString("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0432\u044B\u0439\u0442\u0438. \u0421\u043B\u0435\u0437\u0442\u0435 \u0441 \u043F\u0438\u0442\u043E\u043C\u0446\u0430!"));
      return;
    }

    if (player.getActiveTradeList() != null)
    {
      player.sendPacket(SystemMessage.sendString("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0432\u044B\u0439\u0442\u0438 \u043F\u043E\u043A\u0430 \u0442\u043E\u0440\u0433\u0443\u0435\u0442\u0435!"));
      return;
    }

    if (player.isCastingNow())
    {
      player.sendPacket(SystemMessage.sendString("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0432\u044B\u0439\u0442\u0438 \u043F\u043E\u043A\u0430 \u043A\u0430\u0441\u0442\u0443\u0435\u0442\u0435!"));
      return;
    }

    if (player.getActiveEnchantItem() != null)
    {
      player.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0432\u044B\u0439\u0442\u0438 \u043F\u043E\u043A\u0430 \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u0443\u0435\u0442\u0435 \u0437\u0430\u0442\u043E\u0447\u043A\u0443!");
      return;
    }

    if (!player.isGM())
    {
      if (CustomZoneManager.getInstance().checkIfInZone("NoEscape", player)) {
        player.sendPacket(SystemMessage.sendString("Not the best place to exit the game"));
        player.sendPacket(new ActionFailed());
        return;
      }
    }

    player.getInventory().updateDatabase();

    if (player.getPrivateStoreType() != 0)
    {
      player.sendMessage("Cannot restart while trading");
      return;
    }

    if (player.getActiveRequester() != null)
    {
      player.getActiveRequester().onTradeCancel(player);
      player.onTradeCancel(player.getActiveRequester());
    }

    if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player))
    {
      if (Config.DEBUG) {
        _log.fine("Player " + player.getName() + " tried to logout while fighting.");
      }
      player.sendPacket(new SystemMessage(SystemMessageId.CANT_RESTART_WHILE_FIGHTING));
      player.sendPacket(new ActionFailed());
      return;
    }

    if (player.isFestivalParticipant())
    {
      if (SevenSignsFestival.getInstance().isFestivalInitialized())
      {
        player.sendPacket(SystemMessage.sendString("You cannot restart while you are a participant in a festival."));
        player.sendPacket(new ActionFailed());
        return;
      }
      L2Party playerParty = player.getParty();

      if (playerParty != null) {
        player.getParty().broadcastToPartyMembers(SystemMessage.sendString(player.getName() + " has been removed from the upcoming festival."));
      }
    }

    if (player.isFlying())
    {
      player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
    }

    L2GameClient client = (L2GameClient)getClient();

    player.setClient(null);

    RegionBBSManager.getInstance().changeCommunityBoard();

    player.deleteMe();
    L2GameClient.saveCharToDisk(client.getActiveChar());

    ((L2GameClient)getClient()).setActiveChar(null);

    client.setState(L2GameClient.GameClientState.AUTHED);

    RestartResponse response = new RestartResponse();
    sendPacket(response);

    CharSelectInfo cl = new CharSelectInfo(client.getAccountName(), client.getSessionId().playOkID1);

    sendPacket(cl);
    client.setCharSelection(cl.getCharInfo());
  }

  public String getType()
  {
    return "[C] 46 RequestRestart";
  }
}