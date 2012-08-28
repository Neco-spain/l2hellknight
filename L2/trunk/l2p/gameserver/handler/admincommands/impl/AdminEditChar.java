package l2p.gameserver.handler.admincommands.impl;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.database.mysql;
import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Playable;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.SubClass;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.base.PlayerClass;
import l2p.gameserver.model.base.Race;
import l2p.gameserver.model.entity.olympiad.Olympiad;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.serverpackets.ExPCCafePointInfo;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.serverpackets.SkillList;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.PlayerTemplate;
import l2p.gameserver.utils.ItemFunctions;
import l2p.gameserver.utils.Log;
import l2p.gameserver.utils.PositionUtils;
import l2p.gameserver.utils.Util;
import org.apache.commons.lang3.math.NumberUtils;

public class AdminEditChar
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (activeChar.getPlayerAccess().CanRename) {
      if (fullString.startsWith("admin_settitle"))
        try
        {
          String val = fullString.substring(15);
          GameObject target = activeChar.getTarget();
          Player player = null;
          if (target == null)
            return false;
          if (target.isPlayer())
          {
            player = (Player)target;
            player.setTitle(val);
            player.sendMessage("Your title has been changed by a GM");
            player.sendChanges();
          }
          else if (target.isNpc())
          {
            ((NpcInstance)target).setTitle(val);
            target.decayMe();
            target.spawnMe();
          }

          return true;
        }
        catch (StringIndexOutOfBoundsException e)
        {
          activeChar.sendMessage("You need to specify the new title.");
          return false;
        }
      if (fullString.startsWith("admin_setclass"))
        try
        {
          String val = fullString.substring(15);
          int id = Integer.parseInt(val.trim());
          GameObject target = activeChar.getTarget();

          if ((target == null) || (!target.isPlayer()))
            target = activeChar;
          if (id > 136)
          {
            activeChar.sendMessage("There are no classes over 136 id.");
            return false;
          }
          Player player = target.getPlayer();
          player.setClassId(id, false, false);
          player.sendMessage("Your class has been changed by a GM");
          player.broadcastCharInfo();

          return true;
        }
        catch (StringIndexOutOfBoundsException e)
        {
          activeChar.sendMessage("You need to specify the new class id.");
          return false;
        }
      if (fullString.startsWith("admin_setname"))
        try
        {
          String val = fullString.substring(14);
          GameObject target = activeChar.getTarget();
          Player player;
          if ((target != null) && (target.isPlayer()))
            player = (Player)target;
          else
            return false;
          Player player;
          if (mysql.simple_get_int("count(*)", "characters", new StringBuilder().append("`char_name` like '").append(val).append("'").toString()) > 0)
          {
            activeChar.sendMessage("Name already exist.");
            return false;
          }
          Log.add(new StringBuilder().append("Character ").append(player.getName()).append(" renamed to ").append(val).append(" by GM ").append(activeChar.getName()).toString(), "renames");
          player.reName(val);
          player.sendMessage("Your name has been changed by a GM");
          return true;
        }
        catch (StringIndexOutOfBoundsException e)
        {
          activeChar.sendMessage("You need to specify the new name.");
          return false;
        }
    }
    if ((!activeChar.getPlayerAccess().CanEditChar) && (!activeChar.getPlayerAccess().CanViewChar)) {
      return false;
    }
    if (fullString.equals("admin_current_player")) {
      showCharacterList(activeChar, null);
    } else if (fullString.startsWith("admin_character_list"))
    {
      try {
        String val = fullString.substring(21);
        Player target = GameObjectsStorage.getPlayer(val);
        showCharacterList(activeChar, target);
      }
      catch (StringIndexOutOfBoundsException e)
      {
      }
    }
    else if (fullString.startsWith("admin_show_characters"))
    {
      try {
        String val = fullString.substring(22);
        int page = Integer.parseInt(val);
        listCharacters(activeChar, page);
      }
      catch (StringIndexOutOfBoundsException e)
      {
      }
    }
    else if (fullString.startsWith("admin_find_character"))
    {
      try {
        String val = fullString.substring(21);
        findCharacter(activeChar, val);
      }
      catch (StringIndexOutOfBoundsException e)
      {
        activeChar.sendMessage("You didnt enter a character name to find.");

        listCharacters(activeChar, 0);
      }
    } else {
      if (!activeChar.getPlayerAccess().CanEditChar)
        return false;
      if (fullString.equals("admin_edit_character")) {
        editCharacter(activeChar);
      } else if (fullString.equals("admin_character_actions")) {
        showCharacterActions(activeChar);
      } else if (fullString.equals("admin_nokarma")) {
        setTargetKarma(activeChar, 0);
      } else if (fullString.startsWith("admin_setkarma"))
      {
        try {
          String val = fullString.substring(15);
          int karma = Integer.parseInt(val);
          setTargetKarma(activeChar, karma);
        }
        catch (StringIndexOutOfBoundsException e)
        {
          activeChar.sendMessage("Please specify new karma value.");
        }
      } else if (fullString.startsWith("admin_save_modifications"))
      {
        try {
          String val = fullString.substring(24);
          adminModifyCharacter(activeChar, val);
        }
        catch (StringIndexOutOfBoundsException e)
        {
          activeChar.sendMessage("Error while modifying character.");
          listCharacters(activeChar, 0);
        }
      } else if (fullString.equals("admin_rec"))
      {
        GameObject target = activeChar.getTarget();
        Player player = null;
        if ((target != null) && (target.isPlayer()))
          player = (Player)target;
        else
          return false;
        player.setRecomHave(player.getRecomHave() + 1);
        player.sendMessage("You have been recommended by a GM");
        player.broadcastCharInfo();
      }
      else if (fullString.startsWith("admin_rec"))
      {
        try {
          String val = fullString.substring(10);
          int recVal = Integer.parseInt(val);
          GameObject target = activeChar.getTarget();
          Player player = null;
          if ((target != null) && (target.isPlayer()))
            player = (Player)target;
          else
            return false;
          player.setRecomHave(player.getRecomHave() + recVal);
          player.sendMessage("You have been recommended by a GM");
          player.broadcastCharInfo();
        }
        catch (NumberFormatException e)
        {
          activeChar.sendMessage("Command format is //rec <number>");
        }
      } else if (fullString.startsWith("admin_sethero"))
      {
        GameObject target = activeChar.getTarget();

        if ((wordList.length > 1) && (wordList[1] != null))
        {
          Player player = GameObjectsStorage.getPlayer(wordList[1]);
          if (player == null)
          {
            activeChar.sendMessage(new StringBuilder().append("Character ").append(wordList[1]).append(" not found in game.").toString());
            return false;
          }
        }
        else
        {
          Player player;
          if ((target != null) && (target.isPlayer())) {
            player = (Player)target;
          }
          else {
            activeChar.sendMessage("You must specify the name or target character.");
            return false;
          }
        }
        Player player;
        if (player.isHero())
        {
          player.setHero(false);
          player.updatePledgeClass();
          player.removeSkill(SkillTable.getInstance().getInfo(395, 1));
          player.removeSkill(SkillTable.getInstance().getInfo(396, 1));
          player.removeSkill(SkillTable.getInstance().getInfo(1374, 1));
          player.removeSkill(SkillTable.getInstance().getInfo(1375, 1));
          player.removeSkill(SkillTable.getInstance().getInfo(1376, 1));
        }
        else
        {
          player.setHero(true);
          player.updatePledgeClass();
          player.addSkill(SkillTable.getInstance().getInfo(395, 1));
          player.addSkill(SkillTable.getInstance().getInfo(396, 1));
          player.addSkill(SkillTable.getInstance().getInfo(1374, 1));
          player.addSkill(SkillTable.getInstance().getInfo(1375, 1));
          player.addSkill(SkillTable.getInstance().getInfo(1376, 1));
        }

        player.sendPacket(new SkillList(player));

        player.sendMessage("Admin has changed your hero status.");
        player.broadcastUserInfo(true);
      }
      else if (fullString.startsWith("admin_setnoble"))
      {
        GameObject target = activeChar.getTarget();

        if ((wordList.length > 1) && (wordList[1] != null))
        {
          Player player = GameObjectsStorage.getPlayer(wordList[1]);
          if (player == null)
          {
            activeChar.sendMessage(new StringBuilder().append("Character ").append(wordList[1]).append(" not found in game.").toString());
            return false;
          }
        }
        else
        {
          Player player;
          if ((target != null) && (target.isPlayer())) {
            player = (Player)target;
          }
          else {
            activeChar.sendMessage("You must specify the name or target character.");
            return false;
          }
        }
        Player player;
        if (player.isNoble())
        {
          Olympiad.removeNoble(player);
          player.setNoble(false);
          player.sendMessage("Admin changed your noble status, now you are not nobless.");
        }
        else
        {
          Olympiad.addNoble(player);
          player.setNoble(true);
          player.sendMessage("Admin changed your noble status, now you are Nobless.");
        }

        player.updatePledgeClass();
        player.updateNobleSkills();
        player.sendPacket(new SkillList(player));
        player.broadcastUserInfo(true);
      }
      else if (fullString.startsWith("admin_setsex"))
      {
        GameObject target = activeChar.getTarget();
        Player player = null;
        if ((target != null) && (target.isPlayer()))
          player = (Player)target;
        else
          return false;
        player.changeSex();
        player.sendMessage("Your gender has been changed by a GM");
        player.broadcastUserInfo(true);
      }
      else if (fullString.startsWith("admin_setcolor"))
      {
        try {
          String val = fullString.substring(15);
          GameObject target = activeChar.getTarget();
          Player player = null;
          if ((target != null) && (target.isPlayer()))
            player = (Player)target;
          else
            return false;
          player.setNameColor(Integer.decode(new StringBuilder().append("0x").append(val).toString()).intValue());
          player.sendMessage("Your name color has been changed by a GM");
          player.broadcastUserInfo(true);
        }
        catch (StringIndexOutOfBoundsException e)
        {
          activeChar.sendMessage("You need to specify the new color.");
        }
      } else if (fullString.startsWith("admin_add_exp_sp_to_character")) {
        addExpSp(activeChar);
      } else if (fullString.startsWith("admin_add_exp_sp"))
      {
        try {
          String val = fullString.substring(16).trim();

          String[] vals = val.split(" ");
          long exp = NumberUtils.toLong(vals[0], 0L);
          int sp = vals.length > 1 ? NumberUtils.toInt(vals[1], 0) : 0;

          adminAddExpSp(activeChar, exp, sp);
        }
        catch (Exception e)
        {
          activeChar.sendMessage("Usage: //add_exp_sp <exp> <sp>");
        }
      } else if (fullString.startsWith("admin_trans"))
      {
        StringTokenizer st = new StringTokenizer(fullString);
        if (st.countTokens() > 1)
        {
          st.nextToken();
          int transformId = 0;
          try
          {
            transformId = Integer.parseInt(st.nextToken());
          }
          catch (Exception e)
          {
            activeChar.sendMessage("Specify a valid integer value.");
            return false;
          }
          if ((transformId != 0) && (activeChar.getTransformation() != 0))
          {
            activeChar.sendPacket(Msg.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
            return false;
          }
          activeChar.setTransformation(transformId);
          activeChar.sendMessage("Transforming...");
        }
        else {
          activeChar.sendMessage("Usage: //trans <ID>");
        }
      } else if (fullString.startsWith("admin_setsubclass"))
      {
        GameObject target = activeChar.getTarget();
        if ((target == null) || (!target.isPlayer()))
        {
          activeChar.sendPacket(Msg.SELECT_TARGET);
          return false;
        }
        Player player = (Player)target;

        StringTokenizer st = new StringTokenizer(fullString);
        if (st.countTokens() > 1)
        {
          st.nextToken();
          int classId = Short.parseShort(st.nextToken());
          if (!player.addSubClass(classId, true, 0))
          {
            activeChar.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.SubclassCouldNotBeAdded", activeChar, new Object[0]));
            return false;
          }
          player.sendPacket(Msg.CONGRATULATIONS_YOU_HAVE_TRANSFERRED_TO_A_NEW_CLASS);
        }
        else {
          setSubclass(activeChar, player);
        }
      } else if (fullString.startsWith("admin_setfame"))
      {
        try {
          String val = fullString.substring(14);
          int fame = Integer.parseInt(val);
          setTargetFame(activeChar, fame);
        }
        catch (StringIndexOutOfBoundsException e)
        {
          activeChar.sendMessage("Please specify new fame value.");
        }
      } else if (fullString.startsWith("admin_setbday"))
      {
        String msgUsage = "Usage: //setbday YYYY-MM-DD";
        String date = fullString.substring(14);
        if ((date.length() != 10) || (!Util.isMatchingRegexp(date, "[0-9]{4}-[0-9]{2}-[0-9]{2}")))
        {
          activeChar.sendMessage(msgUsage);
          return false;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try
        {
          dateFormat.parse(date);
        }
        catch (ParseException e)
        {
          activeChar.sendMessage(msgUsage);
        }

        if ((activeChar.getTarget() == null) || (!activeChar.getTarget().isPlayer()))
        {
          activeChar.sendMessage("Please select a character.");
          return false;
        }

        if (!mysql.set(new StringBuilder().append("update characters set createtime = UNIX_TIMESTAMP('").append(date).append("') where obj_Id = ").append(activeChar.getTarget().getObjectId()).toString()))
        {
          activeChar.sendMessage(msgUsage);
          return false;
        }

        activeChar.sendMessage(new StringBuilder().append("New Birthday for ").append(activeChar.getTarget().getName()).append(": ").append(date).toString());
        activeChar.getTarget().getPlayer().sendMessage(new StringBuilder().append("Admin changed your birthday to: ").append(date).toString());
      }
      else if (fullString.startsWith("admin_give_item"))
      {
        if (wordList.length < 3)
        {
          activeChar.sendMessage("Usage: //give_item id count <target>");
          return false;
        }
        int id = Integer.parseInt(wordList[1]);
        int count = Integer.parseInt(wordList[2]);
        if ((id < 1) || (count < 1) || (activeChar.getTarget() == null) || (!activeChar.getTarget().isPlayer()))
        {
          activeChar.sendMessage("Usage: //give_item id count <target>");
          return false;
        }
        ItemFunctions.addItem(activeChar.getTarget().getPlayer(), id, count, true);
      }
      else if (fullString.startsWith("admin_add_bang"))
      {
        if (!Config.ALT_PCBANG_POINTS_ENABLED)
        {
          activeChar.sendMessage("Error! Pc Bang Points service disabled!");
          return true;
        }
        if (wordList.length < 1)
        {
          activeChar.sendMessage("Usage: //add_bang count <target>");
          return false;
        }
        int count = Integer.parseInt(wordList[1]);
        if ((count < 1) || (activeChar.getTarget() == null) || (!activeChar.getTarget().isPlayer()))
        {
          activeChar.sendMessage("Usage: //add_bang count <target>");
          return false;
        }
        Player target = activeChar.getTarget().getPlayer();
        target.addPcBangPoints(count, false);
        activeChar.sendMessage(new StringBuilder().append("You have added ").append(count).append(" Pc Bang Points to ").append(target.getName()).toString());
      }
      else if (fullString.startsWith("admin_set_bang"))
      {
        if (!Config.ALT_PCBANG_POINTS_ENABLED)
        {
          activeChar.sendMessage("Error! Pc Bang Points service disabled!");
          return true;
        }
        if (wordList.length < 1)
        {
          activeChar.sendMessage("Usage: //set_bang count <target>");
          return false;
        }
        int count = Integer.parseInt(wordList[1]);
        if ((count < 1) || (activeChar.getTarget() == null) || (!activeChar.getTarget().isPlayer()))
        {
          activeChar.sendMessage("Usage: //set_bang count <target>");
          return false;
        }
        Player target = activeChar.getTarget().getPlayer();
        target.setPcBangPoints(count);
        target.sendMessage(new StringBuilder().append("Your Pc Bang Points count is now ").append(count).toString());
        target.sendPacket(new ExPCCafePointInfo(target, count, 1, 2, 12));
        activeChar.sendMessage(new StringBuilder().append("You have set ").append(target.getName()).append("'s Pc Bang Points to ").append(count).toString());
      }
    }
    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private void listCharacters(Player activeChar, int page)
  {
    List players = GameObjectsStorage.getAllPlayers();

    int MaxCharactersPerPage = 20;
    int MaxPages = players.size() / MaxCharactersPerPage;

    if (players.size() > MaxCharactersPerPage * MaxPages) {
      MaxPages++;
    }

    if (page > MaxPages) {
      page = MaxPages;
    }
    int CharactersStart = MaxCharactersPerPage * page;
    int CharactersEnd = players.size();
    if (CharactersEnd - CharactersStart > MaxCharactersPerPage) {
      CharactersEnd = CharactersStart + MaxCharactersPerPage;
    }
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

    StringBuilder replyMSG = new StringBuilder("<html><body>");
    replyMSG.append("<table width=260><tr>");
    replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
    replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("</tr></table>");
    replyMSG.append("<br><br>");
    replyMSG.append("<table width=270>");
    replyMSG.append("<tr><td width=270>You can find a character by writing his name and</td></tr>");
    replyMSG.append("<tr><td width=270>clicking Find bellow.<br></td></tr>");
    replyMSG.append("<tr><td width=270>Note: Names should be written case sensitive.</td></tr>");
    replyMSG.append("</table><br>");
    replyMSG.append("<center><table><tr><td>");
    replyMSG.append("<edit var=\"character_name\" width=80></td><td><button value=\"Find\" action=\"bypass -h admin_find_character $character_name\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
    replyMSG.append("</td></tr></table></center><br><br>");

    for (int x = 0; x < MaxPages; x++)
    {
      int pagenr = x + 1;
      replyMSG.append(new StringBuilder().append("<center><a action=\"bypass -h admin_show_characters ").append(x).append("\">Page ").append(pagenr).append("</a></center>").toString());
    }
    replyMSG.append("<br>");

    replyMSG.append("<table width=270>");
    replyMSG.append("<tr><td width=80>Name:</td><td width=110>Class:</td><td width=40>Level:</td></tr>");
    for (int i = CharactersStart; i < CharactersEnd; i++)
    {
      Player p = (Player)players.get(i);
      replyMSG.append(new StringBuilder().append("<tr><td width=80><a action=\"bypass -h admin_character_list ").append(p.getName()).append("\">").append(p.getName()).append("</a></td><td width=110>").append(p.getTemplate().className).append("</td><td width=40>").append(p.getLevel()).append("</td></tr>").toString());
    }
    replyMSG.append("</table>");
    replyMSG.append("</body></html>");

    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
  }

  public static void showCharacterList(Player activeChar, Player player)
  {
    if (player == null)
    {
      GameObject target = activeChar.getTarget();
      if ((target != null) && (target.isPlayer()))
        player = (Player)target;
      else
        return;
    }
    else {
      activeChar.setTarget(player);
    }
    String clanName = "No Clan";
    if (player.getClan() != null) {
      clanName = new StringBuilder().append(player.getClan().getName()).append("/").append(player.getClan().getLevel()).toString();
    }
    NumberFormat df = NumberFormat.getNumberInstance(Locale.ENGLISH);
    df.setMaximumFractionDigits(4);
    df.setMinimumFractionDigits(1);

    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

    StringBuilder replyMSG = new StringBuilder("<html><body>");
    replyMSG.append("<table width=260><tr>");
    replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
    replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_characters 0\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("</tr></table><br>");

    replyMSG.append("<table width=270>");
    replyMSG.append(new StringBuilder().append("<tr><td width=100>Account/IP:</td><td>").append(player.getAccountName()).append("/").append(player.getIP()).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td width=100>Name/Level:</td><td>").append(player.getName()).append("/").append(player.getLevel()).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td width=100>Class/Id:</td><td>").append(player.getTemplate().className).append("/").append(player.getClassId().getId()).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td width=100>Clan/Level:</td><td>").append(clanName).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td width=100>Exp/Sp:</td><td>").append(player.getExp()).append("/").append(player.getSp()).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td width=100>Cur/Max Hp:</td><td>").append((int)player.getCurrentHp()).append("/").append(player.getMaxHp()).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td width=100>Cur/Max Mp:</td><td>").append((int)player.getCurrentMp()).append("/").append(player.getMaxMp()).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td width=100>Cur/Max Load:</td><td>").append(player.getCurrentLoad()).append("/").append(player.getMaxLoad()).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td width=100>Patk/Matk:</td><td>").append(player.getPAtk(null)).append("/").append(player.getMAtk(null, null)).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td width=100>Pdef/Mdef:</td><td>").append(player.getPDef(null)).append("/").append(player.getMDef(null, null)).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td width=100>PAtkSpd/MAtkSpd:</td><td>").append(player.getPAtkSpd()).append("/").append(player.getMAtkSpd()).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td width=100>Acc/Evas:</td><td>").append(player.getAccuracy()).append("/").append(player.getEvasionRate(null)).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td width=100>Crit/MCrit:</td><td>").append(player.getCriticalHit(null, null)).append("/").append(df.format(player.getMagicCriticalRate(null, null))).append("%</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td width=100>Walk/Run:</td><td>").append(player.getWalkSpeed()).append("/").append(player.getRunSpeed()).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td width=100>Karma/Fame:</td><td>").append(player.getKarma()).append("/").append(player.getFame()).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td width=100>PvP/PK:</td><td>").append(player.getPvpKills()).append("/").append(player.getPkKills()).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td width=100>Coordinates:</td><td>").append(player.getX()).append(",").append(player.getY()).append(",").append(player.getZ()).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td width=100>Direction:</td><td>").append(PositionUtils.getDirectionTo(player, activeChar)).append("</td></tr>").toString());
    replyMSG.append("</table><br>");

    replyMSG.append("<table<tr>");
    replyMSG.append("<td><button value=\"Skills\" action=\"bypass -h admin_show_skills\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("<td><button value=\"Effects\" action=\"bypass -h admin_show_effects\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("<td><button value=\"Actions\" action=\"bypass -h admin_character_actions\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("</tr><tr>");
    replyMSG.append("<td><button value=\"Stats\" action=\"bypass -h admin_edit_character\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("<td><button value=\"Exp & Sp\" action=\"bypass -h admin_add_exp_sp_to_character\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("<td></td>");
    replyMSG.append("</tr></table></body></html>");

    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
  }

  private void setTargetKarma(Player activeChar, int newKarma)
  {
    GameObject target = activeChar.getTarget();
    if (target == null)
    {
      activeChar.sendPacket(Msg.INVALID_TARGET);
      return;
    }
    Player player;
    if (target.isPlayer())
      player = (Player)target;
    else
      return;
    Player player;
    if (newKarma >= 0)
    {
      int oldKarma = player.getKarma();
      player.setKarma(newKarma);

      player.sendMessage(new StringBuilder().append("Admin has changed your karma from ").append(oldKarma).append(" to ").append(newKarma).append(".").toString());
      activeChar.sendMessage(new StringBuilder().append("Successfully Changed karma for ").append(player.getName()).append(" from (").append(oldKarma).append(") to (").append(newKarma).append(").").toString());
    }
    else {
      activeChar.sendMessage("You must enter a value for karma greater than or equal to 0.");
    }
  }

  private void setTargetFame(Player activeChar, int newFame) {
    GameObject target = activeChar.getTarget();
    if (target == null)
    {
      activeChar.sendPacket(Msg.INVALID_TARGET);
      return;
    }
    Player player;
    if (target.isPlayer())
      player = (Player)target;
    else
      return;
    Player player;
    if (newFame >= 0)
    {
      int oldFame = player.getFame();
      player.setFame(newFame, "Admin");

      player.sendMessage(new StringBuilder().append("Admin has changed your fame from ").append(oldFame).append(" to ").append(newFame).append(".").toString());
      activeChar.sendMessage(new StringBuilder().append("Successfully Changed fame for ").append(player.getName()).append(" from (").append(oldFame).append(") to (").append(newFame).append(").").toString());
    }
    else {
      activeChar.sendMessage("You must enter a value for fame greater than or equal to 0.");
    }
  }

  private void adminModifyCharacter(Player activeChar, String modifications) {
    GameObject target = activeChar.getTarget();
    if ((target == null) || (!target.isPlayer()))
    {
      activeChar.sendPacket(Msg.SELECT_TARGET);
      return;
    }

    Player player = (Player)target;
    String[] strvals = modifications.split("&");
    Integer[] vals = new Integer[strvals.length];
    for (int i = 0; i < strvals.length; i++)
    {
      strvals[i] = strvals[i].trim();
      vals[i] = (strvals[i].isEmpty() ? null : Integer.valueOf(strvals[i]));
    }

    if (vals[0] != null) {
      player.setCurrentHp(vals[0].intValue(), false);
    }
    if (vals[1] != null) {
      player.setCurrentMp(vals[1].intValue());
    }
    if (vals[2] != null) {
      player.setKarma(vals[2].intValue());
    }
    if (vals[3] != null) {
      player.setPvpFlag(vals[3].intValue());
    }
    if (vals[4] != null) {
      player.setPvpKills(vals[4].intValue());
    }
    if (vals[5] != null) {
      player.setClassId(vals[5].intValue(), true, false);
    }
    editCharacter(activeChar);
    player.broadcastCharInfo();
    player.decayMe();
    player.spawnMe(activeChar.getLoc());
  }

  private void editCharacter(Player activeChar)
  {
    GameObject target = activeChar.getTarget();
    if ((target == null) || (!target.isPlayer()))
    {
      activeChar.sendPacket(Msg.SELECT_TARGET);
      return;
    }

    Player player = (Player)target;
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

    StringBuilder replyMSG = new StringBuilder("<html><body>");
    replyMSG.append("<table width=260><tr>");
    replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
    replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("</tr></table>");
    replyMSG.append("<br><br>");
    replyMSG.append(new StringBuilder().append("<center>Editing character: ").append(player.getName()).append("</center><br>").toString());
    replyMSG.append("<table width=250>");
    replyMSG.append("<tr><td width=40></td><td width=70>Curent:</td><td width=70>Max:</td><td width=70></td></tr>");
    replyMSG.append(new StringBuilder().append("<tr><td width=40>HP:</td><td width=70>").append(player.getCurrentHp()).append("</td><td width=70>").append(player.getMaxHp()).append("</td><td width=70>Karma: ").append(player.getKarma()).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td width=40>MP:</td><td width=70>").append(player.getCurrentMp()).append("</td><td width=70>").append(player.getMaxMp()).append("</td><td width=70>Pvp Kills: ").append(player.getPvpKills()).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td width=40>Load:</td><td width=70>").append(player.getCurrentLoad()).append("</td><td width=70>").append(player.getMaxLoad()).append("</td><td width=70>Pvp Flag: ").append(player.getPvpFlag()).append("</td></tr>").toString());
    replyMSG.append("</table>");
    replyMSG.append(new StringBuilder().append("<table width=270><tr><td>Class<?> Template Id: ").append(player.getClassId()).append("/").append(player.getClassId().getId()).append("</td></tr></table><br>").toString());
    replyMSG.append("<table width=270>");
    replyMSG.append("<tr><td>Note: Fill all values before saving the modifications.</td></tr>");
    replyMSG.append("</table><br>");
    replyMSG.append("<table width=270>");
    replyMSG.append("<tr><td width=50>Hp:</td><td><edit var=\"hp\" width=50></td><td width=50>Mp:</td><td><edit var=\"mp\" width=50></td></tr>");
    replyMSG.append("<tr><td width=50>Pvp Flag:</td><td><edit var=\"pvpflag\" width=50></td><td width=50>Karma:</td><td><edit var=\"karma\" width=50></td></tr>");
    replyMSG.append("<tr><td width=50>Class<?> Id:</td><td><edit var=\"classid\" width=50></td><td width=50>Pvp Kills:</td><td><edit var=\"pvpkills\" width=50></td></tr>");
    replyMSG.append("</table><br>");
    replyMSG.append("<center><button value=\"Save Changes\" action=\"bypass -h admin_save_modifications $hp & $mp & $karma & $pvpflag & $pvpkills & $classid &\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center><br>");
    replyMSG.append("</body></html>");

    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
  }

  private void showCharacterActions(Player activeChar)
  {
    GameObject target = activeChar.getTarget();
    Player player;
    if ((target != null) && (target.isPlayer()))
      player = (Player)target;
    else
      return;
    Player player;
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

    StringBuilder replyMSG = new StringBuilder("<html><body>");
    replyMSG.append("<table width=260><tr>");
    replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
    replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("</tr></table><br><br>");
    replyMSG.append(new StringBuilder().append("<center>Admin Actions for: ").append(player.getName()).append("</center><br>").toString());
    replyMSG.append("<center><table width=200><tr>");
    replyMSG.append("<td width=100>Argument(*):</td><td width=100><edit var=\"arg\" width=100></td>");
    replyMSG.append("</tr></table><br></center>");
    replyMSG.append("<table width=270>");

    replyMSG.append(new StringBuilder().append("<tr><td width=90><button value=\"Teleport\" action=\"bypass -h admin_teleportto ").append(player.getName()).append("\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>").toString());
    replyMSG.append(new StringBuilder().append("<td width=90><button value=\"Recall\" action=\"bypass -h admin_recall ").append(player.getName()).append("\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>").toString());
    replyMSG.append(new StringBuilder().append("<td width=90><button value=\"Quests\" action=\"bypass -h admin_quests ").append(player.getName()).append("\" width=85 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>").toString());

    replyMSG.append("</body></html>");

    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
  }

  private void findCharacter(Player activeChar, String CharacterToFind)
  {
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
    int CharactersFound = 0;

    StringBuilder replyMSG = new StringBuilder("<html><body>");
    replyMSG.append("<table width=260><tr>");
    replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
    replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_characters 0\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("</tr></table>");
    replyMSG.append("<br><br>");

    for (Player element : GameObjectsStorage.getAllPlayersForIterate()) {
      if (element.getName().startsWith(CharacterToFind))
      {
        CharactersFound += 1;
        replyMSG.append("<table width=270>");
        replyMSG.append("<tr><td width=80>Name</td><td width=110>Class</td><td width=40>Level</td></tr>");
        replyMSG.append(new StringBuilder().append("<tr><td width=80><a action=\"bypass -h admin_character_list ").append(element.getName()).append("\">").append(element.getName()).append("</a></td><td width=110>").append(element.getTemplate().className).append("</td><td width=40>").append(element.getLevel()).append("</td></tr>").toString());
        replyMSG.append("</table>");
      }
    }
    if (CharactersFound == 0)
    {
      replyMSG.append("<table width=270>");
      replyMSG.append("<tr><td width=270>Your search did not find any characters.</td></tr>");
      replyMSG.append("<tr><td width=270>Please try again.<br></td></tr>");
      replyMSG.append("</table><br>");
      replyMSG.append("<center><table><tr><td>");
      replyMSG.append("<edit var=\"character_name\" width=80></td><td><button value=\"Find\" action=\"bypass -h admin_find_character $character_name\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
      replyMSG.append("</td></tr></table></center>");
    }
    else
    {
      replyMSG.append(new StringBuilder().append("<center><br>Found ").append(CharactersFound).append(" character").toString());

      if (CharactersFound == 1)
        replyMSG.append(".");
      else if (CharactersFound > 1) {
        replyMSG.append("s.");
      }
    }
    replyMSG.append("</center></body></html>");

    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
  }

  private void addExpSp(Player activeChar)
  {
    GameObject target = activeChar.getTarget();
    Player player;
    if ((target != null) && (target.isPlayer()) && ((activeChar == target) || (activeChar.getPlayerAccess().CanEditCharAll))) {
      player = (Player)target;
    }
    else {
      activeChar.sendPacket(Msg.INVALID_TARGET);
      return;
    }
    Player player;
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

    StringBuilder replyMSG = new StringBuilder("<html><body>");
    replyMSG.append("<table width=260><tr>");
    replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
    replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("</tr></table>");
    replyMSG.append("<br><br>");
    replyMSG.append(new StringBuilder().append("<table width=270><tr><td>Name: ").append(player.getName()).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td>Lv: ").append(player.getLevel()).append(" ").append(player.getTemplate().className).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td>Exp: ").append(player.getExp()).append("</td></tr>").toString());
    replyMSG.append(new StringBuilder().append("<tr><td>Sp: ").append(player.getSp()).append("</td></tr></table>").toString());
    replyMSG.append("<br><table width=270><tr><td>Note: Dont forget that modifying players skills can</td></tr>");
    replyMSG.append("<tr><td>ruin the game...</td></tr></table><br>");
    replyMSG.append("<table width=270><tr><td>Note: Fill all values before saving the modifications.,</td></tr>");
    replyMSG.append("<tr><td>Note: Use 0 if no changes are needed.</td></tr></table><br>");
    replyMSG.append("<center><table><tr>");
    replyMSG.append("<td>Exp: <edit var=\"exp_to_add\" width=50></td>");
    replyMSG.append("<td>Sp:  <edit var=\"sp_to_add\" width=50></td>");
    replyMSG.append("<td>&nbsp;<button value=\"Save Changes\" action=\"bypass -h admin_add_exp_sp $exp_to_add $sp_to_add\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    replyMSG.append("</tr></table></center>");
    replyMSG.append("</body></html>");

    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
  }

  private void adminAddExpSp(Player activeChar, long exp, int sp)
  {
    if (!activeChar.getPlayerAccess().CanEditCharAll)
    {
      activeChar.sendMessage("You have not enough privileges, for use this function.");
      return;
    }

    GameObject target = activeChar.getTarget();
    if (target == null)
    {
      activeChar.sendPacket(Msg.SELECT_TARGET);
      return;
    }

    if (!target.isPlayable())
    {
      activeChar.sendPacket(Msg.INVALID_TARGET);
      return;
    }

    Playable playable = (Playable)target;
    playable.addExpAndSp(exp, sp);

    activeChar.sendMessage(new StringBuilder().append("Added ").append(exp).append(" experience and ").append(sp).append(" SP to ").append(playable.getName()).append(".").toString());
  }

  private void setSubclass(Player activeChar, Player player)
  {
    StringBuilder content = new StringBuilder("<html><body>");
    NpcHtmlMessage html = new NpcHtmlMessage(5);

    Set subsAvailable = getAvailableSubClasses(player);

    if ((subsAvailable != null) && (!subsAvailable.isEmpty()))
    {
      content.append("Add Subclass:<br>Which subclass do you wish to add?<br>");

      for (PlayerClass subClass : subsAvailable)
        content.append(new StringBuilder().append("<a action=\"bypass -h admin_setsubclass ").append(subClass.ordinal()).append("\">").append(formatClassForDisplay(subClass)).append("</a><br>").toString());
    }
    else
    {
      activeChar.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.NoSubAtThisTime", activeChar, new Object[0]));
      return;
    }
    content.append("</body></html>");
    html.setHtml(content.toString());
    activeChar.sendPacket(html);
  }

  private Set<PlayerClass> getAvailableSubClasses(Player player)
  {
    int charClassId = player.getBaseClassId();

    PlayerClass currClass = PlayerClass.values()[charClassId];

    Set availSubs = currClass.getAvailableSubclasses();
    if (availSubs == null) {
      return null;
    }

    availSubs.remove(currClass);

    for (PlayerClass availSub : availSubs)
    {
      for (SubClass subClass : player.getSubClasses().values())
      {
        if (availSub.ordinal() == subClass.getClassId())
        {
          availSubs.remove(availSub);
          continue;
        }

        ClassId parent = ClassId.VALUES[availSub.ordinal()].getParent(player.getSex());
        if ((parent != null) && (parent.getId() == subClass.getClassId()))
        {
          availSubs.remove(availSub);
          continue;
        }

        ClassId subParent = ClassId.VALUES[subClass.getClassId()].getParent(player.getSex());
        if ((subParent != null) && (subParent.getId() == availSub.ordinal())) {
          availSubs.remove(availSub);
        }
      }

      if (availSub.isOfRace(Race.kamael))
      {
        if (((currClass == PlayerClass.MaleSoulHound) || (currClass == PlayerClass.FemaleSoulHound) || (currClass == PlayerClass.FemaleSoulbreaker) || (currClass == PlayerClass.MaleSoulbreaker)) && ((availSub == PlayerClass.FemaleSoulbreaker) || (availSub == PlayerClass.MaleSoulbreaker))) {
          availSubs.remove(availSub);
        }

        if (((currClass == PlayerClass.Berserker) || (currClass == PlayerClass.Doombringer) || (currClass == PlayerClass.Arbalester) || (currClass == PlayerClass.Trickster)) && (
          ((player.getSex() == 1) && (availSub == PlayerClass.MaleSoulbreaker)) || ((player.getSex() == 0) && (availSub == PlayerClass.FemaleSoulbreaker)))) {
          availSubs.remove(availSub);
        }

        if (availSub == PlayerClass.Inspector)
        {
          if ((!player.getSubClasses().containsKey(Integer.valueOf(131))) && (!player.getSubClasses().containsKey(Integer.valueOf(127)))) {
            availSubs.remove(availSub);
          }
          else if ((!player.getSubClasses().containsKey(Integer.valueOf(132))) && (!player.getSubClasses().containsKey(Integer.valueOf(133))) && (!player.getSubClasses().containsKey(Integer.valueOf(128))) && (!player.getSubClasses().containsKey(Integer.valueOf(129)))) {
            availSubs.remove(availSub);
          }
          else if ((!player.getSubClasses().containsKey(Integer.valueOf(134))) && (!player.getSubClasses().containsKey(Integer.valueOf(130))))
            availSubs.remove(availSub); 
        }
      }
    }
    return availSubs;
  }

  private String formatClassForDisplay(PlayerClass className)
  {
    String classNameStr = className.toString();
    char[] charArray = classNameStr.toCharArray();

    for (int i = 1; i < charArray.length; i++) {
      if (Character.isUpperCase(charArray[i]))
        classNameStr = new StringBuilder().append(classNameStr.substring(0, i)).append(" ").append(classNameStr.substring(i)).toString();
    }
    return classNameStr;
  }

  private static enum Commands
  {
    admin_edit_character, 
    admin_character_actions, 
    admin_current_player, 
    admin_nokarma, 
    admin_setkarma, 
    admin_character_list, 
    admin_show_characters, 
    admin_find_character, 
    admin_save_modifications, 
    admin_rec, 
    admin_settitle, 
    admin_setclass, 
    admin_setname, 
    admin_setsex, 
    admin_setcolor, 
    admin_add_exp_sp_to_character, 
    admin_add_exp_sp, 
    admin_sethero, 
    admin_setnoble, 
    admin_trans, 
    admin_setsubclass, 
    admin_setfame, 
    admin_setbday, 
    admin_give_item, 
    admin_add_bang, 
    admin_set_bang;
  }
}