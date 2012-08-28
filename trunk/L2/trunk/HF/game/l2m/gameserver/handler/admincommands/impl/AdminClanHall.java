package l2m.gameserver.handler.admincommands.impl;

import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.handler.admincommands.IAdminCommandHandler;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Zone;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.model.entity.residence.ClanHall;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.network.serverpackets.NpcHtmlMessage;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.data.tables.ClanTable;

public class AdminClanHall
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().CanEditNPC) {
      return false;
    }
    ClanHall clanhall = null;
    if (wordList.length > 1) {
      clanhall = (ClanHall)ResidenceHolder.getInstance().getResidence(ClanHall.class, Integer.parseInt(wordList[1]));
    }
    if (clanhall == null)
    {
      showClanHallSelectPage(activeChar);
      return true;
    }

    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminClanHall$Commands[command.ordinal()])
    {
    case 1:
      showClanHallSelectPage(activeChar);
      break;
    case 2:
      GameObject target = activeChar.getTarget();
      Player player = activeChar;
      if ((target != null) && (target.isPlayer()))
        player = (Player)target;
      if (player.getClan() == null) {
        activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
      }
      else {
        clanhall.changeOwner(player.getClan());
      }
      break;
    case 3:
      clanhall.changeOwner(null);
      break;
    case 4:
      Zone zone = clanhall.getZone();
      if (zone == null) break;
      activeChar.teleToLocation(zone.getSpawn());
    }

    showClanHallPage(activeChar, clanhall);
    return true;
  }

  public void showClanHallSelectPage(Player activeChar)
  {
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

    StringBuilder replyMSG = new StringBuilder("<html><body>");
    replyMSG.append("<table width=268><tr>");
    replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("<td width=180><center><font color=\"LEVEL\">Clan Halls:</font></center></td>");
    replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("</tr></table><br>");

    replyMSG.append("<table width=268>");
    replyMSG.append("<tr><td width=130>ClanHall Name</td><td width=58>Town</td><td width=80>Owner</td></tr>");

    replyMSG.append("</table>");
    replyMSG.append("</body></html>");

    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
  }

  public void showClanHallPage(Player activeChar, ClanHall clanhall)
  {
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
    StringBuilder replyMSG = new StringBuilder("<html><body>");
    replyMSG.append("<table width=260><tr>");
    replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("<td width=180><center>ClanHall Name</center></td>");
    replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_clanhall\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("</tr></table>");
    replyMSG.append("<center>");
    replyMSG.append(new StringBuilder().append("<br><br><br>ClanHall: ").append(clanhall.getName()).append("<br>").toString());
    replyMSG.append(new StringBuilder().append("Location: &^").append(clanhall.getId()).append(";<br>").toString());
    replyMSG.append("ClanHall Owner: ");
    Clan owner = clanhall.getOwnerId() == 0 ? null : ClanTable.getInstance().getClan(clanhall.getOwnerId());
    if (owner == null)
      replyMSG.append("none");
    else {
      replyMSG.append(owner.getName());
    }
    replyMSG.append("<br><br><br>");
    replyMSG.append("<table>");
    replyMSG.append(new StringBuilder().append("<tr><td><button value=\"Open Doors\" action=\"bypass -h admin_clanhallopendoors ").append(clanhall.getId()).append("\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>").toString());
    replyMSG.append(new StringBuilder().append("<td><button value=\"Close Doors\" action=\"bypass -h admin_clanhallclosedoors ").append(clanhall.getId()).append("\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>").toString());
    replyMSG.append("</table>");
    replyMSG.append("<br>");
    replyMSG.append("<table>");
    replyMSG.append(new StringBuilder().append("<tr><td><button value=\"Give ClanHall\" action=\"bypass -h admin_clanhallset ").append(clanhall.getId()).append("\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>").toString());
    replyMSG.append(new StringBuilder().append("<td><button value=\"Take ClanHall\" action=\"bypass -h admin_clanhalldel ").append(clanhall.getId()).append("\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>").toString());
    replyMSG.append("</table>");
    replyMSG.append("<br>");
    replyMSG.append("<table><tr>");
    replyMSG.append(new StringBuilder().append("<td><button value=\"Teleport self\" action=\"bypass -h admin_clanhallteleportself ").append(clanhall.getId()).append(" \" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>").toString());
    replyMSG.append("</table>");
    replyMSG.append("</center>");
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
    admin_clanhall, 
    admin_clanhallset, 
    admin_clanhalldel, 
    admin_clanhallteleportself;
  }
}