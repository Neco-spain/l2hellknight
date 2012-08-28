package l2m.gameserver.handler.admincommands.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import l2p.commons.dbutils.DbUtils;
import l2m.gameserver.Announcements;
import l2m.gameserver.Config;
import l2m.gameserver.database.DatabaseFactory;
import l2m.gameserver.database.mysql;
import l2m.gameserver.handler.admincommands.IAdminCommandHandler;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.GameObjectsStorage;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.network.serverpackets.NpcHtmlMessage;
import l2m.gameserver.network.serverpackets.components.ChatType;
import l2m.gameserver.utils.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminChangeAccessLevel
  implements IAdminCommandHandler
{
  private static final Logger _log = LoggerFactory.getLogger(AdminChangeAccessLevel.class);

  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().CanGmEdit) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminChangeAccessLevel$Commands[command.ordinal()])
    {
    case 1:
      if (wordList.length == 2)
      {
        int lvl = Integer.parseInt(wordList[1]);
        if (activeChar.getTarget().isPlayer())
          ((Player)activeChar.getTarget()).setAccessLevel(lvl);
      } else {
        if (wordList.length != 3)
          break;
        int lvl = Integer.parseInt(wordList[2]);
        Player player = GameObjectsStorage.getPlayer(wordList[1]);
        if (player != null)
          player.setAccessLevel(lvl); 
      }
      break;
    case 2:
      showModersPannel(activeChar);
      break;
    case 3:
      if ((activeChar.getTarget() == null) || (!activeChar.getTarget().isPlayer()))
      {
        activeChar.sendMessage("Incorrect target. Please select a player.");
        showModersPannel(activeChar);
        return false;
      }

      Player modAdd = activeChar.getTarget().getPlayer();
      if (Config.gmlist.containsKey(Integer.valueOf(modAdd.getObjectId())))
      {
        activeChar.sendMessage(new StringBuilder().append("Error: Moderator ").append(modAdd.getName()).append(" already in server access list.").toString());
        showModersPannel(activeChar);
        return false;
      }

      String newFName = new StringBuilder().append("m").append(modAdd.getObjectId()).append(".xml").toString();
      if (!Files.copyFile("config/GMAccess.d/template/moderator.xml", new StringBuilder().append("config/GMAccess.d/").append(newFName).toString()))
      {
        activeChar.sendMessage("Error: Failed to copy access-file.");
        showModersPannel(activeChar);
        return false;
      }

      String res = "";
      try
      {
        BufferedReader in = new BufferedReader(new FileReader(new StringBuilder().append("config/GMAccess.d/").append(newFName).toString()));
        String str;
        while ((str = in.readLine()) != null)
          res = new StringBuilder().append(res).append(str).append("\n").toString();
        in.close();

        res = res.replaceFirst("ObjIdPlayer", new StringBuilder().append("").append(modAdd.getObjectId()).toString());
        Files.writeFile(new StringBuilder().append("config/GMAccess.d/").append(newFName).toString(), res);
      }
      catch (Exception e)
      {
        activeChar.sendMessage("Error: Failed to modify object ID in access-file.");
        File fDel = new File(new StringBuilder().append("config/GMAccess.d/").append(newFName).toString());
        if (fDel.exists())
          fDel.delete();
        showModersPannel(activeChar);
        return false;
      }

      File af = new File(new StringBuilder().append("config/GMAccess.d/").append(newFName).toString());
      if (!af.exists())
      {
        activeChar.sendMessage(new StringBuilder().append("Error: Failed to read access-file for ").append(modAdd.getName()).toString());
        showModersPannel(activeChar);
        return false;
      }

      Config.loadGMAccess(af);
      modAdd.setPlayerAccess((PlayerAccess)Config.gmlist.get(Integer.valueOf(modAdd.getObjectId())));

      activeChar.sendMessage(new StringBuilder().append("Moderator ").append(modAdd.getName()).append(" added.").toString());
      showModersPannel(activeChar);
      break;
    case 4:
      if (wordList.length < 2)
      {
        activeChar.sendMessage("Please specify moderator object ID to delete moderator.");
        showModersPannel(activeChar);
        return false;
      }

      int oid = Integer.parseInt(wordList[1]);

      if (Config.gmlist.containsKey(Integer.valueOf(oid))) {
        Config.gmlist.remove(Integer.valueOf(oid));
      }
      else {
        activeChar.sendMessage(new StringBuilder().append("Error: Moderator with object ID ").append(oid).append(" not found in server access lits.").toString());
        showModersPannel(activeChar);
        return false;
      }

      Player modDel = GameObjectsStorage.getPlayer(oid);
      if (modDel != null) {
        modDel.setPlayerAccess(null);
      }

      String fname = new StringBuilder().append("m").append(oid).append(".xml").toString();
      File f = new File(new StringBuilder().append("config/GMAccess.d/").append(fname).toString());
      if ((!f.exists()) || (!f.isFile()) || (!f.delete()))
      {
        activeChar.sendMessage(new StringBuilder().append("Error: Can't delete access-file: ").append(fname).toString());
        showModersPannel(activeChar);
        return false;
      }

      if (modDel != null)
        activeChar.sendMessage(new StringBuilder().append("Moderator ").append(modDel.getName()).append(" deleted.").toString());
      else {
        activeChar.sendMessage(new StringBuilder().append("Moderator with object ID ").append(oid).append(" deleted.").toString());
      }
      showModersPannel(activeChar);
      break;
    case 5:
      if (wordList.length < 2)
      {
        activeChar.sendMessage("USAGE: //penalty charName [count] [reason]");
        return false;
      }

      int count = 1;
      if (wordList.length > 2) {
        count = Integer.parseInt(wordList[2]);
      }
      String reason = "\u043D\u0435 \u0443\u043A\u0430\u0437\u0430\u043D\u0430";

      if (wordList.length > 3) {
        reason = wordList[3];
      }
      int oId = 0;

      Player player = GameObjectsStorage.getPlayer(wordList[1]);
      if ((player != null) && (player.getPlayerAccess().CanBanChat))
      {
        oId = player.getObjectId();
        int oldPenaltyCount = 0;
        String oldPenalty = player.getVar("penaltyChatCount");
        if (oldPenalty != null) {
          oldPenaltyCount = Integer.parseInt(oldPenalty);
        }
        player.setVar("penaltyChatCount", new StringBuilder().append("").append(oldPenaltyCount + count).toString(), -1L);
      }
      else
      {
        oId = mysql.simple_get_int("obj_Id", "characters", new StringBuilder().append("`char_name`='").append(wordList[1]).append("'").toString());
        if (oId > 0)
        {
          Integer oldCount = (Integer)mysql.get(new StringBuilder().append("SELECT `value` FROM character_variables WHERE `obj_id` = ").append(oId).append(" AND `name` = 'penaltyChatCount'").toString());
          mysql.set(new StringBuilder().append("REPLACE INTO character_variables (obj_id, type, name, value, expire_time) VALUES (").append(oId).append(",'user-var','penaltyChatCount','").append(oldCount.intValue() + count).append("',-1)").toString());
        }
      }

      if (oId <= 0) break;
      if (Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
        Announcements.getInstance().announceToAll(new StringBuilder().append(activeChar).append(" \u043E\u0448\u0442\u0440\u0430\u0444\u043E\u0432\u0430\u043B \u043C\u043E\u0434\u0435\u0440\u0430\u0442\u043E\u0440\u0430 ").append(wordList[1]).append(" \u043D\u0430 ").append(count).append(", \u043F\u0440\u0438\u0447\u0438\u043D\u0430: ").append(reason).append(".").toString());
      else {
        Announcements.shout(activeChar, new StringBuilder().append(activeChar).append(" \u043E\u0448\u0442\u0440\u0430\u0444\u043E\u0432\u0430\u043B \u043C\u043E\u0434\u0435\u0440\u0430\u0442\u043E\u0440\u0430 ").append(wordList[1]).append(" \u043D\u0430 ").append(count).append(", \u043F\u0440\u0438\u0447\u0438\u043D\u0430: ").append(reason).append(".").toString(), ChatType.CRITICAL_ANNOUNCE);
      }

    }

    return true;
  }

  private static void showModersPannel(Player activeChar)
  {
    NpcHtmlMessage reply = new NpcHtmlMessage(5);
    String html = "Moderators managment panel.<br>";

    File dir = new File("config/GMAccess.d/");
    if ((!dir.exists()) || (!dir.isDirectory()))
    {
      html = new StringBuilder().append(html).append("Error: Can't open permissions folder.").toString();
      reply.setHtml(html);
      activeChar.sendPacket(reply);
      return;
    }

    html = new StringBuilder().append(html).append("<p align=right>").toString();
    html = new StringBuilder().append(html).append("<button width=120 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h admin_moders_add\" value=\"Add modrator\">").toString();
    html = new StringBuilder().append(html).append("</p><br>").toString();

    html = new StringBuilder().append(html).append("<center><font color=LEVEL>Moderators:</font></center>").toString();
    html = new StringBuilder().append(html).append("<table width=285>").toString();
    for (File f : dir.listFiles())
    {
      if ((f.isDirectory()) || (!f.getName().startsWith("m")) || (!f.getName().endsWith(".xml")))
      {
        continue;
      }
      int oid = Integer.parseInt(f.getName().substring(1, 10));
      String pName = getPlayerNameByObjId(oid);
      boolean on = false;

      if ((pName == null) || (pName.isEmpty()))
        pName = new StringBuilder().append("").append(oid).toString();
      else {
        on = GameObjectsStorage.getPlayer(pName) != null;
      }
      html = new StringBuilder().append(html).append("<tr>").toString();
      html = new StringBuilder().append(html).append("<td width=140>").append(pName).toString();
      html = new StringBuilder().append(html).append(on ? " <font color=\"33CC66\">(on)</font>" : "").toString();
      html = new StringBuilder().append(html).append("</td>").toString();
      html = new StringBuilder().append(html).append("<td width=45><button width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h admin_moders_log ").append(oid).append("\" value=\"Logs\"></td>").toString();
      html = new StringBuilder().append(html).append("<td width=45><button width=20 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h admin_moders_del ").append(oid).append("\" value=\"X\"></td>").toString();
      html = new StringBuilder().append(html).append("</tr>").toString();
    }
    html = new StringBuilder().append(html).append("</table>").toString();

    reply.setHtml(html);
    activeChar.sendPacket(reply);
  }

  private static String getPlayerNameByObjId(int oid)
  {
    String pName = null;
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement(new StringBuilder().append("SELECT `char_name` FROM `characters` WHERE `obj_Id`=\"").append(oid).append("\" LIMIT 1").toString());
      rset = statement.executeQuery();
      if (rset.next())
        pName = rset.getString(1);
    }
    catch (Exception e)
    {
      _log.warn(new StringBuilder().append("SQL Error: ").append(e).toString());
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    return pName;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_changelvl, 
    admin_moders, 
    admin_moders_add, 
    admin_moders_del, 
    admin_penalty;
  }
}