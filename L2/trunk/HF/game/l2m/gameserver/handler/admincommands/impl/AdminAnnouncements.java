package l2m.gameserver.handler.admincommands.impl;

import java.util.List;
import l2m.gameserver.Announcements;
import l2m.gameserver.Announcements.Announce;
import l2m.gameserver.handler.admincommands.IAdminCommandHandler;
import l2m.gameserver.model.GameObjectsStorage;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.network.serverpackets.ExShowScreenMessage;
import l2m.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2m.gameserver.network.serverpackets.NpcHtmlMessage;
import l2m.gameserver.network.serverpackets.components.ChatType;

public class AdminAnnouncements
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (fullString.length() < 8)
    {
      return false;
    }

    if (!activeChar.getPlayerAccess().CanAnnounce)
      return false;
    ExShowScreenMessage sm;
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminAnnouncements$Commands[command.ordinal()])
    {
    case 1:
      listAnnouncements(activeChar);
      break;
    case 2:
      Announcements.getInstance().announceToAll(fullString.substring(20));
      listAnnouncements(activeChar);
      break;
    case 3:
      for (Player player : GameObjectsStorage.getAllPlayersForIterate())
        Announcements.getInstance().showAnnouncements(player);
      listAnnouncements(activeChar);
      break;
    case 4:
      if (wordList.length < 3)
        return false;
      try
      {
        int time = Integer.parseInt(wordList[1]);
        StringBuilder builder = new StringBuilder();
        for (int i = 2; i < wordList.length; i++) {
          builder.append(" ").append(wordList[i]);
        }
        Announcements.getInstance().addAnnouncement(time, builder.toString(), true);
        listAnnouncements(activeChar);
      }
      catch (Exception e)
      {
      }
    case 5:
      if (wordList.length != 2)
        return false;
      int val = Integer.parseInt(wordList[1]);
      Announcements.getInstance().delAnnouncement(val);
      listAnnouncements(activeChar);
      break;
    case 6:
      Announcements.getInstance().announceToAll(fullString.substring(15));
      break;
    case 7:
      Announcements.getInstance().announceToAll(fullString.substring(8));
      break;
    case 8:
    case 9:
      if (wordList.length < 2)
        return false;
      Announcements.getInstance().announceToAll(new StringBuilder().append(activeChar.getName()).append(": ").append(fullString.replaceFirst("admin_crit_announce ", "").replaceFirst("admin_c ", "")).toString(), ChatType.CRITICAL_ANNOUNCE);
      break;
    case 10:
    case 11:
      if (wordList.length < 2)
        return false;
      String text = new StringBuilder().append(activeChar.getName()).append(": ").append(fullString.replaceFirst("admin_toscreen ", "").replaceFirst("admin_s ", "")).toString();
      int time = 3000 + text.length() * 100;
      sm = new ExShowScreenMessage(text, time, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, text.length() < 64);
      for (Player player : GameObjectsStorage.getAllPlayersForIterate())
        player.sendPacket(sm);
      break;
    case 12:
      Announcements.getInstance().loadAnnouncements();
      listAnnouncements(activeChar);
      activeChar.sendMessage("Announcements reloaded.");
    }

    return true;
  }

  public void listAnnouncements(Player activeChar)
  {
    List announcements = Announcements.getInstance().getAnnouncements();

    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

    StringBuilder replyMSG = new StringBuilder("<html><body>");
    replyMSG.append("<table width=260><tr>");
    replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("<td width=180><center>Announcement Menu</center></td>");
    replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("</tr></table>");
    replyMSG.append("<br><br>");
    replyMSG.append("<center>Add or announce a new announcement:</center>");
    replyMSG.append("<center><multiedit var=\"new_announcement\" width=240 height=30></center><br>");
    replyMSG.append("<center>Time(in seconds, 0 - only for start)<edit var=\"time\" width=40 height=20></center><br>");
    replyMSG.append("<center><table><tr><td>");
    replyMSG.append("<button value=\"Add\" action=\"bypass -h admin_add_announcement $time $new_announcement\" width=60 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"Announce\" action=\"bypass -h admin_announce_menu $new_announcement\" width=64 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"Reload\" action=\"bypass -h admin_reload_announcements\" width=60 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"Broadcast\" action=\"bypass -h admin_announce_announcements\" width=70 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
    replyMSG.append("</td></tr></table></center>");
    replyMSG.append("<br>");

    for (int i = 0; i < announcements.size(); i++)
    {
      Announcements.Announce announce = (Announcements.Announce)announcements.get(i);
      replyMSG.append(new StringBuilder().append("<table width=260><tr><td width=180>").append(announce.getAnnounce()).append("</td><td width=40>").append(announce.getTime()).append("</td><<td width=40>").toString());
      replyMSG.append(new StringBuilder().append("<button value=\"Delete\" action=\"bypass -h admin_del_announcement ").append(i).append("\" width=60 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr></table>").toString());
    }

    replyMSG.append("</body></html>");

    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_list_announcements, 
    admin_announce_announcements, 
    admin_add_announcement, 
    admin_del_announcement, 
    admin_announce, 
    admin_a, 
    admin_announce_menu, 
    admin_crit_announce, 
    admin_c, 
    admin_toscreen, 
    admin_s, 
    admin_reload_announcements;
  }
}