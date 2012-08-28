package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.LoginServerThread.SessionKey;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AdminHwid
  implements IAdminCommandHandler
{
  private static Log _log = LogFactory.getLog("AdminHwid");
  private static String[] ADMIN_COMMANDS = { "admin_hwid", "admin_hwidlist", "admin_hwidban", "admin_hwidunban" };

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    StringTokenizer st = new StringTokenizer(command);
    st.nextToken();
    String account_name = "";
    String player = "";
    L2PcInstance plyr = null;

    if (command.equals("admin_hwid"))
    {
      String html = HtmCache.getInstance().getHtm("data/html/protect/main.htm");
      NpcHtmlMessage msg = new NpcHtmlMessage(0);
      msg.setHtml(html);
      activeChar.sendPacket(msg);
    }
    else if (command.startsWith("admin_hwidlist"))
    {
      String html = HtmCache.getInstance().getHtm("data/html/protect/list.htm");
      String list = "";
      String next = "";
      String prev = "";
      Connection con = null;
      try
      {
        int startwith = 0;
        if (command.indexOf(" ") != -1)
          startwith = Integer.parseInt(command.split(" ")[1]);
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement preparedstatement1 = con.prepareStatement("SELECT c.char_name, s.hwid FROM ban_hwid AS s INNER JOIN characters c on c.obj_id = s.charId");
        ResultSet resultset1 = preparedstatement1.executeQuery();
        int i = 0;
        while (resultset1.next())
        {
          if (i++ < startwith)
            continue;
          list = new StringBuilder().append(list).append("<tr><td>").append(resultset1.getString(1) != null ? resultset1.getString(1) : "").append("</td><td>").append(resultset1.getString(2)).append("</td><td>").append("<button action=\"bypass -h admin_hwidunban ").append(resultset1.getString(2)).append(" \" value=\"Unban\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\"></td></tr>").toString();

          if (i - startwith <= 30)
            continue;
          next = new StringBuilder().append("<button action=\"bypass -h admin_hwidban ").append(i).append("\" value=\"Next\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\">").toString();
        }

        if (startwith != 0)
        {
          startwith -= 30;
          if (startwith < 0)
            startwith = 0;
          prev = new StringBuilder().append("<button action=\"bypass -h admin_hwidban ").append(startwith).append("\" value=\"Prev\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\">").toString();
        }

        resultset1.close();
        preparedstatement1.close();
      }
      catch (SQLException e1)
      {
        _log.warn(new StringBuilder().append("Protect: Unable load banned list :").append(e).toString());
      }
      finally
      {
        try
        {
          con.close();
        }
        catch (SQLException e1)
        {
          e1.printStackTrace();
        }
      }

      NpcHtmlMessage msg = new NpcHtmlMessage(0);
      msg.setHtml(html);
      msg.replace("%list%", list);
      msg.replace("%next%", next);
      msg.replace("%prev%", prev);
      activeChar.sendPacket(msg);
    } else {
      if (command.startsWith("admin_hwidban"))
      {
        Connection con = null;
        try
        {
          player = st.nextToken();
          L2PcInstance playerObj = L2World.getInstance().getPlayer(player);

          if (playerObj != null)
          {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement preparedstatement1 = con.prepareStatement("INSERT INTO ban_hwid (hwid, charId) values(?,?)");
            preparedstatement1.setString(1, String.format("%X", new Object[] { Integer.valueOf(playerObj.getClient().getSessionId().clientKey) }));
            preparedstatement1.setInt(2, playerObj.getObjectId());
            preparedstatement1.execute();
            playerObj.sendMessage("\u0412\u044B \u043F\u043E\u043B\u0443\u0447\u0438\u043B\u0438 \u0431\u0430\u043D \u043F\u043E HWID");
            activeChar.sendMessage(new StringBuilder().append("\u0412\u044B \u0434\u0430\u043B\u0438 \u0438\u0433\u0440\u043E\u043A\u0443 ").append(player).append(" \u0411\u0430\u043D \u043F\u043E HWID ").toString());
            playerObj.closeNetConnection(false);

            preparedstatement1.close();
          }
          else
          {
            activeChar.sendMessage("\u041F\u0435\u0440\u0441\u043E\u043D\u0430\u0436\u0430 \u043D\u0435\u0442 \u0432 \u0438\u0433\u0440\u0435");
          }
        }
        catch (NoSuchElementException e1)
        {
          activeChar.sendMessage("Specify a character name.");
        }
        catch (Exception e1)
        {
          e.printStackTrace();
        }
        finally
        {
          try
          {
            con.close();
          }
          catch (SQLException e1)
          {
            e1.printStackTrace();
          }
        }

        GMAudit.auditGMAction(activeChar.getName(), command, player, "");
        return true;
      }
      if (command.startsWith("admin_hwidunban"))
      {
        Connection con = null;
        try
        {
          player = st.nextToken();
          con = L2DatabaseFactory.getInstance().getConnection();
          PreparedStatement preparedstatement1 = con.prepareStatement("DELETE FROM ban_hwid WHERE hwid=?");
          preparedstatement1.setString(1, player);
          preparedstatement1.execute();
          preparedstatement1.close();
        }
        catch (Exception e1)
        {
          e.printStackTrace();
        }
        finally
        {
          try
          {
            con.close();
          }
          catch (SQLException e1)
          {
            e1.printStackTrace();
          }
        }
      }
    }
    return false;
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }
}