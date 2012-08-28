package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class Moderator
  implements IVoicedCommandHandler
{
  private static final Logger _log = Logger.getLogger(Moderator.class.getName());
  private static final String[] _voicedCommands = { "moder" };

  public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
  {
    if (!Config.MODER_ENABLE) return false;

    if (command.equalsIgnoreCase("moder"))
    {
      return showModerPanel(activeChar);
    }
    if (command.startsWith("moder_panel_"))
    {
      String cmd = command.substring(12).trim();
      return actionWithPlayer(activeChar, cmd);
    }
    if (command.startsWith("moder_chatban_"))
    {
      String cmd = command.substring(14).trim();
      return banChat(activeChar, cmd);
    }
    if (command.startsWith("moder_chatunban_"))
    {
      String cmd = command.substring(16).trim();
      return unBunChat(activeChar, cmd);
    }

    if (command.startsWith("moder_teleto_"))
    {
      String cmd = command.substring(13).trim();
      return teleTo(activeChar, cmd);
    }
    return false;
  }

  public boolean teleTo(L2PcInstance activeChar, String cmd)
  {
    L2PcInstance target = L2World.getInstance().getPlayer(cmd);
    if ((target == null) || (activeChar == target)) return false;

    if ((activeChar.isGM()) || (canUseTp(activeChar)))
    {
      _log.info(new StringBuilder().append(activeChar.getName()).append(" telepro to ").append(target.getName()).toString());
      activeChar.teleToLocation(target.getX(), target.getY(), target.getZ(), false);

      CreatureSay cs = new CreatureSay(0, 10, "", new StringBuilder().append("moderator ").append(activeChar.getName()).append(" teleport to ").append(target.getName()).toString());

      for (L2PcInstance player : L2World.getInstance().getAllPlayers())
      {
        player.sendPacket(cs);
      }
      showModerPanel(activeChar);
      return true;
    }
    return false;
  }

  public boolean unBunChat(L2PcInstance activeChar, String cmd)
  {
    L2PcInstance target = L2World.getInstance().getPlayer(cmd);
    if ((target == null) || (!target.isChatBanned())) return false;

    if ((activeChar.isGM()) || (canUseChat(activeChar)))
    {
      _log.info(new StringBuilder().append(activeChar.getName()).append(" unban chat user ").append(target.getName()).toString());
      target.setChatBanned(false, 0);
      showModerPanel(activeChar);
      return true;
    }
    return false;
  }

  public boolean banChat(L2PcInstance activeChar, String cmd)
  {
    String[] args = cmd.split("_");
    int time = 10;
    String total_ann;
    String total_ann;
    if (args.length == 2)
    {
      total_ann = args[1];
    }
    else if (args.length == 3)
    {
      String total_ann = args[1];
      try
      {
        time = Integer.parseInt(args[2].trim());
      }
      catch (NumberFormatException e)
      {
        e.printStackTrace();
        return false;
      }
    }
    else {
      total_ann = "";
    }if (activeChar.getName() == args[0].trim()) return false;
    L2PcInstance target = L2World.getInstance().getPlayer(args[0].trim());
    if ((target == null) || (target.isChatBanned())) return false;

    if ((activeChar.isGM()) || (canUseChat(activeChar)))
    {
      _log.info(new StringBuilder().append(activeChar.getName()).append(" ban chat user ").append(target.getName()).append(" on ").append(time).append(" minutes : ").append(total_ann).toString());
      target.setChatBanned(true, time);

      CreatureSay cs = new CreatureSay(0, 10, "", new StringBuilder().append("\u043C\u043E\u0434\u0435\u0440\u0430\u0442\u043E\u0440 ").append(activeChar.getName()).append(" \u0437\u0430\u0431\u0430\u043D\u0438\u043B \u0447\u0430\u0442 ").append(target.getName()).append(" \u043D\u0430 ").append(time).append(" \u043C\u0438\u043D\u0443\u0442 : ").append(total_ann).toString());

      for (L2PcInstance player : L2World.getInstance().getAllPlayers())
      {
        player.sendPacket(cs);
      }

      showModerPanel(activeChar);
      return true;
    }
    return false;
  }

  public boolean actionWithPlayer(L2PcInstance activeChar, String cmd)
  {
    if (activeChar.getName() == cmd) return false;
    boolean isChat = false;
    boolean isTp = false;
    NpcHtmlMessage messageHtml = new NpcHtmlMessage(0);
    L2PcInstance target = L2World.getInstance().getPlayer(cmd);
    if (target == null)
    {
      return false;
    }

    if (activeChar.isGM())
    {
      isChat = true;
      isTp = true;
    }
    else
    {
      isChat = canUseChat(activeChar);
      isTp = canUseTp(activeChar);
    }

    StringBuilder html = new StringBuilder();
    html.append("<html><body><br1><table width=\"100%\"><tr><td><font color=\"LEVEL\">");
    html.append(target.getName());
    html.append("</font></td></tr></table>");

    if (isChat)
    {
      if (target.isChatBanned()) {
        html.append(new StringBuilder().append("<table width=\"100%\"><tr><td><button value=\"UnBan Chat\" action=\"bypass moder_chatunban_").append(cmd).append("\" width=\"100\" height=\"20\" back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>").toString());
      }
      else {
        html.append("<table width=\"100%\"><tr><td>");
        html.append("<font color=\"LEVEL\">Time:</font><br>");
        html.append("<edit var=\"time\" width=\"70\"><br><br>");
        html.append("<font color=\"LEVEL\">Announcement:</font><br>");
        html.append("<edit var=\"ann\" width=\"230\"><br>");
        html.append(new StringBuilder().append("<button value=\"Ban Chat\" action=\"bypass moder_chatban_").append(cmd).append("_ $ann _ $time \" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\">").toString());
        html.append("</td></tr></table>");
      }

    }

    html.append("<hr><hr><hr>");
    if (isTp)
    {
      html.append(new StringBuilder().append("<table width=\"100%\"><tr><td><button value=\"Teleport to him\" action=\"bypass moder_teleto_").append(cmd).append("\" width=\"100\" height=\"20\" back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>").toString());
    }
    html.append("</body></html>");
    messageHtml.setHtml(html.toString());
    activeChar.sendPacket(messageHtml);
    return true;
  }

  public boolean showModerPanel(L2PcInstance activeChar) {
    boolean isChat = false;
    boolean isTp = false;
    NpcHtmlMessage messageHtml = new NpcHtmlMessage(0);
    if (activeChar.isGM())
    {
      isChat = true;
      isTp = true;
    }
    else
    {
      isChat = canUseChat(activeChar);
      isTp = canUseTp(activeChar);
    }
    if ((isChat) || (isTp))
    {
      StringBuilder html = new StringBuilder();
      html.append("<html><body><br1>Players:<br1>");
      html.append("<table border=\"0\" width=\"100%\"><tr>");

      int count = 0;
      for (L2PcInstance player : L2World.getInstance().getAllPlayers())
      {
        if (count > 5)
        {
          html.append("</tr>");
          html.append("<tr>");
          count = 0;
        }
        html.append("<td>");
        html.append("<a action=\"bypass moder_panel_");
        html.append(player.getName());

        html.append("\" >");
        html.append(player.getName());
        html.append("</a>");
        html.append("</td>");
        count++;
      }
      html.append("</tr></table></body></html>");

      messageHtml.setHtml(html.toString());
      activeChar.sendPacket(messageHtml);
      return true;
    }
    return false;
  }

  public boolean canUseChat(L2PcInstance activeChar)
  {
    for (int moder_id : Config.MODER_CHAT_ID)
    {
      if (moder_id == activeChar.getObjectId())
      {
        return true;
      }
    }
    return false;
  }

  public boolean canUseTp(L2PcInstance activeChar)
  {
    for (int moder_id : Config.MODER_TP_ID)
    {
      if (moder_id == activeChar.getObjectId())
      {
        return true;
      }
    }
    return false;
  }

  public String[] getVoicedCommandList()
  {
    return _voicedCommands;
  }
}