package l2m.gameserver.handler.admincommands.impl;

import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import l2m.gameserver.Announcements;
import l2m.gameserver.Config;
import l2m.gameserver.aConfig;
import l2m.gameserver.handler.admincommands.IAdminCommandHandler;
import l2m.gameserver.instancemanager.ReflectionManager;
import l2m.gameserver.loginservercon.LoginServerCommunication;
import l2m.gameserver.loginservercon.gspackets.ChangeAccessLevel;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.World;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.model.items.ManufactureItem;
import l2m.gameserver.model.items.TradeItem;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.scripts.Functions;
import l2m.gameserver.network.serverpackets.NpcHtmlMessage;
import l2m.gameserver.network.serverpackets.components.ChatType;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.utils.AdminFunctions;
import l2m.gameserver.utils.AutoBan;
import l2m.gameserver.utils.Location;
import l2m.gameserver.utils.Log;

public class AdminBan
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    StringTokenizer st = new StringTokenizer(fullString);

    if (activeChar.getPlayerAccess().CanTradeBanUnban) {
      switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminBan$Commands[command.ordinal()])
      {
      case 1:
        return tradeBan(st, activeChar);
      case 2:
        return tradeUnban(st, activeChar);
      }
    }
    if (activeChar.getPlayerAccess().CanBan) {
      switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminBan$Commands[command.ordinal()])
      {
      case 3:
        ban(st, activeChar);
        break;
      case 4:
        unban(st, activeChar);
        break;
      case 5:
        st.nextToken();

        int level = 0;
        int banExpire = 0;

        String account = st.nextToken();

        if (st.hasMoreTokens())
          banExpire = (int)(System.currentTimeMillis() / 1000L) + Integer.parseInt(st.nextToken()) * 60;
        else {
          level = -100;
        }
        LoginServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(account, level, banExpire));
        GameClient client = LoginServerCommunication.getInstance().getAuthedClient(account);
        if (client == null)
          break;
        Player player = client.getActiveChar();
        if (player != null)
        {
          player.kick();
          activeChar.sendMessage(new StringBuilder().append("Player ").append(player.getName()).append(" kicked.").toString());
        }
        break;
      case 6:
        st.nextToken();
        String account = st.nextToken();
        LoginServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(account, 0, 0));
        break;
      case 1:
        return tradeBan(st, activeChar);
      case 2:
        return tradeUnban(st, activeChar);
      case 7:
        try
        {
          st.nextToken();
          String player = st.nextToken();
          String period = st.nextToken();
          String reason = st.nextToken();
          String bmsg = new StringBuilder().append("admin_chatban ").append(player).append(" ").append(period).append(" ").toString();
          String msg = fullString.substring(bmsg.length(), fullString.length());

          if (AutoBan.ChatBan(player, Integer.parseInt(period), msg, activeChar.getName())) {
            activeChar.sendMessage(new StringBuilder().append("You ban chat for ").append(player).append(".").toString());
          }
          else {
            activeChar.sendMessage(new StringBuilder().append("Can't find char ").append(player).append(".").toString());
          }
          if (aConfig.get("AnnounceChatBan", false)) {
            Announcements.getInstance().announceToAll(new StringBuilder().append(activeChar.getName()).append(" \u0437\u0430\u0431\u0430\u043D\u0438\u043B \u0447\u0430\u0442 \u0438\u0433\u0440\u043E\u043A\u0443 ").append(player).append(" \u043D\u0430 ").append(period).append(" \u043C\u0438\u043D\u0443\u0442, \u043F\u0440\u0438\u0447\u0438\u043D\u0430: ").append(reason).toString(), ChatType.CRITICAL_ANNOUNCE);
          }
        }
        catch (Exception e)
        {
          activeChar.sendMessage("Command syntax: //chatban char_name period reason");
        }

      case 8:
        try
        {
          st.nextToken();
          String player = st.nextToken();

          if (AutoBan.ChatUnBan(player, activeChar.getName()))
            activeChar.sendMessage(new StringBuilder().append("You unban chat for ").append(player).append(".").toString());
          else
            activeChar.sendMessage(new StringBuilder().append("Can't find char ").append(player).append(".").toString());
        }
        catch (Exception e)
        {
          activeChar.sendMessage("Command syntax: //chatunban char_name");
        }

      case 9:
        try
        {
          st.nextToken();
          String player = st.nextToken();
          int period = Integer.parseInt(st.nextToken());
          String reason = st.nextToken();

          Player target = World.getPlayer(player);

          if (target != null)
          {
            target.setVar("jailedFrom", new StringBuilder().append(target.getX()).append(";").append(target.getY()).append(";").append(target.getZ()).append(";").append(target.getReflectionId()).toString(), -1L);
            target.setVar("jailed", period * 60000 + System.currentTimeMillis(), -1L);
            target.startUnjailTask(target, period);
            target.teleToLocation(Location.findPointToStay(target, AdminFunctions.JAIL_SPAWN, 50, 200), ReflectionManager.JAIL);
            if (activeChar.isInStoreMode())
              activeChar.setPrivateStoreType(0);
            target.sitDown(null);
            target.block();
            target.sendMessage(new StringBuilder().append("You moved to jail, time to escape - ").append(period).append(" minutes, reason - ").append(reason).append(" .").toString());
            activeChar.sendMessage(new StringBuilder().append("You jailed ").append(player).append(".").toString());
            if (aConfig.get("AnnounceCharJail", false))
              Announcements.getInstance().announceToAll(new StringBuilder().append(activeChar.getName()).append(" \u043F\u043E\u0441\u0430\u0434\u0438\u043B \u0432 \u0442\u044E\u0440\u044C\u043C\u0443 ").append(player).append(" \u043D\u0430 ").append(period).append(" \u043C\u0438\u043D\u0443\u0442, \u043F\u0440\u0438\u0447\u0438\u043D\u0430: ").append(reason).toString(), ChatType.CRITICAL_ANNOUNCE);
          }
          else
          {
            activeChar.sendMessage(new StringBuilder().append("Can't find char ").append(player).append(".").toString());
          }
        }
        catch (Exception e) {
          activeChar.sendMessage("Command syntax: //jail char_name period reason");
        }

      case 10:
        try
        {
          st.nextToken();
          String player = st.nextToken();

          Player target = World.getPlayer(player);

          if ((target != null) && (target.getVar("jailed") != null))
          {
            String[] re = target.getVar("jailedFrom").split(";");
            target.teleToLocation(Integer.parseInt(re[0]), Integer.parseInt(re[1]), Integer.parseInt(re[2]));
            target.setReflection(re.length > 3 ? Integer.parseInt(re[3]) : 0);
            target.stopUnjailTask();
            target.unsetVar("jailedFrom");
            target.unsetVar("jailed");
            target.unblock();
            target.standUp();
            activeChar.sendMessage(new StringBuilder().append("You unjailed ").append(player).append(".").toString());
          }
          else {
            activeChar.sendMessage(new StringBuilder().append("Can't find char ").append(player).append(".").toString());
          }
        }
        catch (Exception e) {
          activeChar.sendMessage("Command syntax: //unjail char_name");
        }

      case 11:
        activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/cban.htm"));
        break;
      case 12:
        if ((activeChar.getTarget() == null) || (!activeChar.getTarget().isPlayer()))
        {
          Functions.sendDebugMessage(activeChar, "Target should be set and be a player instance");
          return false;
        }
        Player banned = activeChar.getTarget().getPlayer();
        String banaccount = banned.getAccountName();
        LoginServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(banaccount, -100, 0));
        if (banned.isInOfflineMode())
          banned.setOfflineMode(false);
        banned.kick();
        Functions.sendDebugMessage(activeChar, new StringBuilder().append("Player account ").append(banaccount).append(" is banned, player ").append(banned.getName()).append(" kicked.").toString());
      }
    }

    return true;
  }

  private boolean tradeBan(StringTokenizer st, Player activeChar)
  {
    if ((activeChar.getTarget() == null) || (!activeChar.getTarget().isPlayer()))
      return false;
    st.nextToken();
    Player targ = (Player)activeChar.getTarget();
    long days = -1L;
    long time = -1L;
    if (st.hasMoreTokens())
    {
      days = Long.parseLong(st.nextToken());
      time = days * 24L * 60L * 60L * 1000L + System.currentTimeMillis();
    }
    targ.setVar("tradeBan", String.valueOf(time), -1L);
    String msg = new StringBuilder().append(activeChar.getName()).append(" \u0437\u0430\u0431\u043B\u043E\u043A\u0438\u0440\u043E\u0432\u0430\u043B \u0442\u043E\u0440\u0433\u043E\u0432\u043B\u044E \u043F\u0435\u0440\u0441\u043E\u043D\u0430\u0436\u0443 ").append(targ.getName()).append(days == -1L ? " \u043D\u0430 \u0431\u0435\u0441\u0441\u0440\u043E\u0447\u043D\u044B\u0439 \u043F\u0435\u0440\u0438\u043E\u0434." : new StringBuilder().append(" \u043D\u0430 ").append(days).append(" \u0434\u043D\u0435\u0439.").toString()).toString();

    Log.add(new StringBuilder().append(targ.getName()).append(":").append(days).append(tradeToString(targ, targ.getPrivateStoreType())).toString(), "tradeBan", activeChar);

    if (targ.isInOfflineMode())
    {
      targ.setOfflineMode(false);
      targ.kick();
    }
    else if (targ.isInStoreMode())
    {
      targ.setPrivateStoreType(0);
      targ.standUp();
      targ.broadcastCharInfo();
      targ.getBuyList().clear();
    }

    if (Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
      Announcements.getInstance().announceToAll(msg);
    else
      Announcements.shout(activeChar, msg, ChatType.CRITICAL_ANNOUNCE);
    return true;
  }

  private boolean unban(StringTokenizer st, Player activeChar)
  {
    try {
      st.nextToken();
      String name = st.nextToken();
      AutoBan.Banned(name, 0, 0, "unban", activeChar.getName());
      activeChar.sendMessage(new StringBuilder().append("You unbanned ").append(name).toString());
    }
    catch (Exception e)
    {
      activeChar.sendMessage("Command syntax: //unban char_name");
    }
    return true;
  }

  private static String tradeToString(Player targ, int trade)
  {
    Collection list;
    String ret;
    switch (trade)
    {
    case 3:
      list = targ.getBuyList();
      if ((list == null) || (list.isEmpty()))
        return "";
      ret = ":buy:";
      for (TradeItem i : list)
        ret = new StringBuilder().append(ret).append(i.getItemId()).append(";").append(i.getCount()).append(";").append(i.getOwnersPrice()).append(":").toString();
      return ret;
    case 1:
    case 8:
      list = targ.getSellList();
      if ((list == null) || (list.isEmpty()))
        return "";
      ret = ":sell:";
      for (TradeItem i : list)
        ret = new StringBuilder().append(ret).append(i.getItemId()).append(";").append(i.getCount()).append(";").append(i.getOwnersPrice()).append(":").toString();
      return ret;
    case 5:
      list = targ.getCreateList();
      if ((list == null) || (list.isEmpty()))
        return "";
      ret = ":mf:";
      for (ManufactureItem i : list)
        ret = new StringBuilder().append(ret).append(i.getRecipeId()).append(";").append(i.getCost()).append(":").toString();
      return ret;
    case 2:
    case 4:
    case 6:
    case 7: } return "";
  }

  private boolean tradeUnban(StringTokenizer st, Player activeChar)
  {
    if ((activeChar.getTarget() == null) || (!activeChar.getTarget().isPlayer()))
      return false;
    Player targ = (Player)activeChar.getTarget();

    targ.unsetVar("tradeBan");

    if (Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
      Announcements.getInstance().announceToAll(new StringBuilder().append(activeChar).append(" \u0440\u0430\u0437\u0431\u043B\u043E\u043A\u0438\u0440\u043E\u0432\u0430\u043B \u0442\u043E\u0440\u0433\u043E\u0432\u043B\u044E \u043F\u0435\u0440\u0441\u043E\u043D\u0430\u0436\u0443 ").append(targ).append(".").toString());
    else {
      Announcements.shout(activeChar, new StringBuilder().append(activeChar).append(" \u0440\u0430\u0437\u0431\u043B\u043E\u043A\u0438\u0440\u043E\u0432\u0430\u043B \u0442\u043E\u0440\u0433\u043E\u0432\u043B\u044E \u043F\u0435\u0440\u0441\u043E\u043D\u0430\u0436\u0443 ").append(targ).append(".").toString(), ChatType.CRITICAL_ANNOUNCE);
    }
    Log.add(new StringBuilder().append(activeChar).append(" \u0440\u0430\u0437\u0431\u043B\u043E\u043A\u0438\u0440\u043E\u0432\u0430\u043B \u0442\u043E\u0440\u0433\u043E\u0432\u043B\u044E \u043F\u0435\u0440\u0441\u043E\u043D\u0430\u0436\u0443 ").append(targ).append(".").toString(), "tradeBan", activeChar);
    return true;
  }

  private boolean ban(StringTokenizer st, Player activeChar)
  {
    try
    {
      st.nextToken();

      String player = st.nextToken();

      int time = 0;
      String msg = "";

      if (st.hasMoreTokens()) {
        time = Integer.parseInt(st.nextToken());
      }
      if (st.hasMoreTokens())
      {
        msg = new StringBuilder().append("admin_ban ").append(player).append(" ").append(time).append(" ").toString();
        while (st.hasMoreTokens())
          msg = new StringBuilder().append(msg).append(st.nextToken()).append(" ").toString();
        msg.trim();
      }

      Player plyr = World.getPlayer(player);
      if (plyr != null)
      {
        plyr.sendMessage(new CustomMessage("admincommandhandlers.YoureBannedByGM", plyr, new Object[0]));
        plyr.setAccessLevel(-100);
        AutoBan.Banned(plyr, time, msg, activeChar.getName());
        plyr.kick();
        activeChar.sendMessage(new StringBuilder().append("You banned ").append(plyr.getName()).toString());
      }
      else if (AutoBan.Banned(player, -100, time, msg, activeChar.getName())) {
        activeChar.sendMessage(new StringBuilder().append("You banned ").append(player).toString());
      } else {
        activeChar.sendMessage(new StringBuilder().append("Can't find char: ").append(player).toString());
      }
    }
    catch (Exception e) {
      activeChar.sendMessage("Command syntax: //ban char_name days reason");
    }
    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_ban, 
    admin_unban, 
    admin_cban, 
    admin_chatban, 
    admin_chatunban, 
    admin_accban, 
    admin_accunban, 
    admin_trade_ban, 
    admin_trade_unban, 
    admin_jail, 
    admin_unjail, 
    admin_permaban;
  }
}