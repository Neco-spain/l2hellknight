package l2p.gameserver.utils;

import l2p.gameserver.Announcements;
import l2p.gameserver.Config;
import l2p.gameserver.dao.CharacterDAO;
import l2p.gameserver.instancemanager.CursedWeaponsManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.serverpackets.components.ChatType;
import l2p.gameserver.serverpackets.components.CustomMessage;

public final class AdminFunctions
{
  public static final Location JAIL_SPAWN = new Location(-114648, -249384, -2984);

  public static boolean kick(String player, String reason)
  {
    Player plyr = World.getPlayer(player);
    if (plyr == null) {
      return false;
    }
    return kick(plyr, reason);
  }

  public static boolean kick(Player player, String reason)
  {
    if ((Config.ALLOW_CURSED_WEAPONS) && (Config.DROP_CURSED_WEAPONS_ON_KICK) && 
      (player.isCursedWeaponEquipped()))
    {
      player.setPvpFlag(0);
      CursedWeaponsManager.getInstance().dropPlayer(player);
    }

    player.kick();

    return true;
  }

  public static String banChat(Player adminChar, String adminName, String charName, int val, String reason)
  {
    Player player = World.getPlayer(charName);

    if (player != null)
      charName = player.getName();
    else if (CharacterDAO.getInstance().getObjectIdByName(charName) == 0) {
      return "\u0418\u0433\u0440\u043E\u043A " + charName + " \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D.";
    }
    if (((adminName == null) || (adminName.isEmpty())) && (adminChar != null)) {
      adminName = adminChar.getName();
    }
    if ((reason == null) || (reason.isEmpty())) {
      reason = "\u043D\u0435 \u0443\u043A\u0430\u0437\u0430\u043D\u0430";
    }
    String announce = null;
    String result;
    String result;
    if (val == 0)
    {
      if ((adminChar != null) && (!adminChar.getPlayerAccess().CanUnBanChat))
        return "\u0412\u044B \u043D\u0435 \u0438\u043C\u0435\u0435\u0442\u0435 \u043F\u0440\u0430\u0432 \u043D\u0430 \u0441\u043D\u044F\u0442\u0438\u0435 \u0431\u0430\u043D\u0430 \u0447\u0430\u0442\u0430.";
      if (Config.BANCHAT_ANNOUNCE)
        announce = "\u0421 \u0438\u0433\u0440\u043E\u043A\u0430 " + charName + " \u0441\u043D\u044F\u0442 \u0431\u0430\u043D \u0447\u0430\u0442\u0430.";
      Log.add(adminName + " \u0441\u043D\u044F\u043B \u0431\u0430\u043D \u0447\u0430\u0442\u0430 \u0441 \u0438\u0433\u0440\u043E\u043A\u0430 " + charName + ".", "banchat", adminChar);
      result = "\u0412\u044B \u0441\u043D\u044F\u043B\u0438 \u0431\u0430\u043D \u0447\u0430\u0442\u0430 \u0441 \u0438\u0433\u0440\u043E\u043A\u0430 " + charName + ".";
    }
    else
    {
      String result;
      if (val < 0)
      {
        if ((adminChar != null) && (adminChar.getPlayerAccess().BanChatMaxValue > 0))
          return "\u0412\u044B \u043C\u043E\u0436\u0435\u0442\u0435 \u0431\u0430\u043D\u0438\u0442\u044C \u043D\u0435 \u0431\u043E\u043B\u0435\u0435 \u0447\u0435\u043C \u043D\u0430 " + adminChar.getPlayerAccess().BanChatMaxValue + " \u043C\u0438\u043D\u0443\u0442.";
        if (Config.BANCHAT_ANNOUNCE)
          announce = "\u0417\u0430\u0431\u0430\u043D\u0435\u043D \u0447\u0430\u0442 \u0438\u0433\u0440\u043E\u043A\u0443 " + charName + " \u043D\u0430 \u0431\u0435\u0441\u0441\u0440\u043E\u0447\u043D\u044B\u0439 \u043F\u0435\u0440\u0438\u043E\u0434, \u043F\u0440\u0438\u0447\u0438\u043D\u0430: " + reason + ".";
        Log.add(adminName + " \u0437\u0430\u0431\u0430\u043D\u0438\u043B \u0447\u0430\u0442 \u0438\u0433\u0440\u043E\u043A\u0443 " + charName + " \u043D\u0430 \u0431\u0435\u0441\u0441\u0440\u043E\u0447\u043D\u044B\u0439 \u043F\u0435\u0440\u0438\u043E\u0434, \u043F\u0440\u0438\u0447\u0438\u043D\u0430: " + reason + ".", "banchat", adminChar);
        result = "\u0412\u044B \u0437\u0430\u0431\u0430\u043D\u0438\u043B\u0438 \u0447\u0430\u0442 \u0438\u0433\u0440\u043E\u043A\u0443 " + charName + " \u043D\u0430 \u0431\u0435\u0441\u0441\u0440\u043E\u0447\u043D\u044B\u0439 \u043F\u0435\u0440\u0438\u043E\u0434.";
      }
      else
      {
        if ((adminChar != null) && (!adminChar.getPlayerAccess().CanUnBanChat) && ((player == null) || (player.getNoChannel() != 0L)))
          return "\u0412\u044B \u043D\u0435 \u0438\u043C\u0435\u0435\u0442\u0435 \u043F\u0440\u0430\u0432\u0430 \u0438\u0437\u043C\u0435\u043D\u044F\u0442\u044C \u0432\u0440\u0435\u043C\u044F \u0431\u0430\u043D\u0430.";
        if ((adminChar != null) && (adminChar.getPlayerAccess().BanChatMaxValue != -1) && (val > adminChar.getPlayerAccess().BanChatMaxValue))
          return "\u0412\u044B \u043C\u043E\u0436\u0435\u0442\u0435 \u0431\u0430\u043D\u0438\u0442\u044C \u043D\u0435 \u0431\u043E\u043B\u0435\u0435 \u0447\u0435\u043C \u043D\u0430 " + adminChar.getPlayerAccess().BanChatMaxValue + " \u043C\u0438\u043D\u0443\u0442.";
        if (Config.BANCHAT_ANNOUNCE)
          announce = "\u0417\u0430\u0431\u0430\u043D\u0435\u043D \u0447\u0430\u0442 \u0438\u0433\u0440\u043E\u043A\u0443 " + charName + " \u043D\u0430 " + val + " \u043C\u0438\u043D\u0443\u0442, \u043F\u0440\u0438\u0447\u0438\u043D\u0430: " + reason + ".";
        Log.add(adminName + " \u0437\u0430\u0431\u0430\u043D\u0438\u043B \u0447\u0430\u0442 \u0438\u0433\u0440\u043E\u043A\u0443 " + charName + " \u043D\u0430 " + val + " \u043C\u0438\u043D\u0443\u0442, \u043F\u0440\u0438\u0447\u0438\u043D\u0430: " + reason + ".", "banchat", adminChar);
        result = "\u0412\u044B \u0437\u0430\u0431\u0430\u043D\u0438\u043B\u0438 \u0447\u0430\u0442 \u0438\u0433\u0440\u043E\u043A\u0443 " + charName + " \u043D\u0430 " + val + " \u043C\u0438\u043D\u0443\u0442.";
      }
    }
    if (player != null)
      updateNoChannel(player, val, reason);
    else {
      AutoBan.ChatBan(charName, val, reason, adminName);
    }
    if (announce != null) {
      if (Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
        Announcements.getInstance().announceToAll(announce);
      else
        Announcements.shout(adminChar, announce, ChatType.CRITICAL_ANNOUNCE);
    }
    return result;
  }

  private static void updateNoChannel(Player player, int time, String reason)
  {
    player.updateNoChannel(time * 60000);
    if (time == 0)
      player.sendMessage(new CustomMessage("common.ChatUnBanned", player, new Object[0]));
    else if (time > 0)
    {
      if ((reason == null) || (reason.isEmpty()))
        player.sendMessage(new CustomMessage("common.ChatBanned", player, new Object[0]).addNumber(time));
      else
        player.sendMessage(new CustomMessage("common.ChatBannedWithReason", player, new Object[0]).addNumber(time).addString(reason));
    }
    else if ((reason == null) || (reason.isEmpty()))
      player.sendMessage(new CustomMessage("common.ChatBannedPermanently", player, new Object[0]));
    else
      player.sendMessage(new CustomMessage("common.ChatBannedPermanentlyWithReason", player, new Object[0]).addString(reason));
  }
}