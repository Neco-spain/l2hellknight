package net.sf.l2j.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.CustomZoneManager;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.FriendList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

public final class Logout extends L2GameClientPacket
{
  private static final String _C__09_LOGOUT = "[C] 09 Logout";
  private static Logger _log = Logger.getLogger(Logout.class.getName());

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

    if (player == null) {
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

    if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player))
    {
      if (Config.DEBUG) _log.fine("Player " + player.getName() + " tried to logout while fighting");

      player.sendPacket(new SystemMessage(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING));
      player.sendPacket(new ActionFailed());
      return;
    }

    if (player.atEvent) {
      player.sendPacket(SystemMessage.sendString("A superior power doesn't allow you to leave the event"));
      player.sendPacket(new ActionFailed());
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

    if ((player.isInOlympiadMode()) || (Olympiad.getInstance().isRegisteredInComp(player)) || (player.getOlympiadGameId() > 0))
    {
      player.sendMessage("You cant logout in olympiad mode");
      return;
    }

    if (player.isFestivalParticipant()) {
      if (SevenSignsFestival.getInstance().isFestivalInitialized())
      {
        player.sendMessage("You cannot log out while you are a participant in a festival.");
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

    if (((player.isInStoreMode()) && (Config.OFFLINE_TRADE_ENABLE)) || ((player.isInCraftMode()) && (Config.OFFLINE_CRAFT_ENABLE)))
    {
      if (L2World.getInstance().getAllOfflineCount() > 500)
      {
        player.sendMessage("\u041F\u0440\u0435\u0432\u044B\u0448\u0435\u043D\u043E \u043A\u043E\u043B\u0438\u0447\u0435\u0441\u0442\u0432\u043E \u043E\u0444\u0442\u0440\u0435\u0439\u0434\u043E\u0432 \u043D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435");
        return;
      }

      player.setOffline(true);
      player.closeNetConnection(true);
      if (player.getOfflineStartTime() == 0L)
        player.setOfflineStartTime(System.currentTimeMillis());
      return;
    }

    RegionBBSManager.getInstance().changeCommunityBoard();

    player.deleteMe();
    notifyFriends(player);
  }

  private void notifyFriends(L2PcInstance cha)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("SELECT friend_name FROM character_friends WHERE char_id=?");
      statement.setInt(1, cha.getObjectId());
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        String friendName = rset.getString("friend_name");

        L2PcInstance friend = L2World.getInstance().getPlayer(friendName);

        if (friend == null)
          continue;
        friend.sendPacket(new FriendList(friend));
      }

      rset.close();
      statement.close();
    }
    catch (Exception e) {
      _log.warning("could not restore friend data:" + e);
    } finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
  }

  public String getType() {
    return "[C] 09 Logout";
  }
}