package scripts.commands.admincommandhandlers;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CharInfo;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.util.Util;
import scripts.commands.IAdminCommandHandler;

public class AdminEditChar
  implements IAdminCommandHandler
{
  private static final Logger _log = Logger.getLogger(AdminEditChar.class.getName());
  private static final String[] ADMIN_COMMANDS = { "admin_edit_character", "admin_current_player", "admin_nokarma", "admin_setkarma", "admin_character_list", "admin_character_info", "admin_character_obj", "admin_show_characters", "admin_find_character", "admin_find_ip", "admin_find_account", "admin_save_modifications", "admin_rec", "admin_settitle", "admin_setname", "admin_setsex", "admin_sethero", "admin_setclass", "admin_fullfood", "admin_namecolor", "admin_titlecolor", "admin_negate", "admin_cleanse", "admin_spy", "admin_clearoly" };

  private static final int REQUIRED_LEVEL = Config.GM_CHAR_EDIT;
  private static final int REQUIRED_LEVEL2 = Config.GM_CHAR_EDIT_OTHER;
  private static final int REQUIRED_LEVEL_VIEW = Config.GM_CHAR_VIEW;

  public boolean useAdminCommand(String command, L2PcInstance activeChar) {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      ((!checkLevel(activeChar.getAccessLevel())) && (!checkLevel2(activeChar.getAccessLevel()))) || (!activeChar.isGM()))) {
      return false;
    }

    GMAudit.auditGMAction(activeChar.getName(), command, activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target", "");

    if (command.equals("admin_current_player")) {
      showCharacterInfo(activeChar, null);
    } else if ((command.startsWith("admin_character_list")) || (command.startsWith("admin_character_info"))) {
      try {
        String val = command.substring(21);
        val = val.replaceAll("&lt;", "<");
        val = val.replaceAll("&gt;", ">");
        L2PcInstance target = L2World.getInstance().getPlayer(val);
        if (target != null)
          showCharacterInfo(activeChar, target);
        else
          activeChar.sendPacket(SystemMessage.id(SystemMessageId.CHARACTER_DOES_NOT_EXIST));
      }
      catch (StringIndexOutOfBoundsException e) {
        activeChar.sendAdmResultMessage("Usage: //character_info <player_name>");
      }
    } else if (command.startsWith("admin_character_obj")) {
      try {
        Integer obj = Integer.valueOf(Integer.parseInt(command.substring(20)));
        L2PcInstance target = L2World.getInstance().getPlayer(obj.intValue());
        if (target != null)
          showCharacterInfo(activeChar, target);
        else
          activeChar.sendPacket(SystemMessage.id(SystemMessageId.CHARACTER_DOES_NOT_EXIST));
      }
      catch (StringIndexOutOfBoundsException e) {
        activeChar.sendAdmResultMessage("Usage: //character_info <player_name>");
      }
    } else if (command.startsWith("admin_show_characters")) {
      try {
        String val = command.substring(22);
        int page = Integer.parseInt(val);
        listCharacters(activeChar, page);
      }
      catch (StringIndexOutOfBoundsException e) {
        activeChar.sendAdmResultMessage("Usage: //show_characters <page_number>");
      }
    } else if (command.startsWith("admin_find_character")) {
      try {
        String val = command.substring(21);
        findCharacter(activeChar, val);
      } catch (StringIndexOutOfBoundsException e) {
        activeChar.sendAdmResultMessage("Usage: //find_character <character_name>");
        listCharacters(activeChar, 0);
      }
    } else if (command.startsWith("admin_find_ip")) {
      try {
        String val = command.substring(14);
        findCharactersPerIp(activeChar, val);
      } catch (Exception e) {
        activeChar.sendAdmResultMessage("Usage: //find_ip <www.xxx.yyy.zzz>");
        listCharacters(activeChar, 0);
      }
    }
    else if (command.startsWith("admin_sethero")) {
      L2Object target = activeChar.getTarget();
      L2PcInstance player = null;
      if ((activeChar != target) && (activeChar.getAccessLevel() < REQUIRED_LEVEL2)) {
        return false;
      }
      if (target.isPlayer())
        player = (L2PcInstance)target;
      else {
        return false;
      }
      player.setHero(!player.isHero());
      if (player.isHero()) {
        player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
      }
      player.sendAdmResultMessage("Admin changed your hero status");
      player.broadcastUserInfo();
    } else if (command.startsWith("admin_find_account")) {
      try {
        String val = command.substring(19);
        findCharactersPerAccount(activeChar, val);
      } catch (Exception e) {
        activeChar.sendAdmResultMessage("Usage: //find_account <player_name>");
        listCharacters(activeChar, 0);
      }
    } else if (command.equals("admin_edit_character")) {
      editCharacter(activeChar);
    }
    else if (command.equals("admin_nokarma")) {
      setTargetKarma(activeChar, 0);
    } else if (command.startsWith("admin_setkarma")) {
      try {
        String val = command.substring(15);
        int karma = Integer.parseInt(val);
        if ((activeChar == activeChar.getTarget()) || (activeChar.getAccessLevel() >= REQUIRED_LEVEL2)) {
          GMAudit.auditGMAction(activeChar.getName(), command, activeChar.getName(), "");
        }
        setTargetKarma(activeChar, karma);
      } catch (StringIndexOutOfBoundsException e) {
        if (Config.DEVELOPER) {
          System.out.println("Set karma error: " + e);
        }
        activeChar.sendAdmResultMessage("Usage: //setkarma <new_karma_value>");
      }
    } else if (command.startsWith("admin_save_modifications")) {
      try {
        String val = command.substring(24);
        if ((activeChar == activeChar.getTarget()) || (activeChar.getAccessLevel() >= REQUIRED_LEVEL2)) {
          GMAudit.auditGMAction(activeChar.getName(), command, activeChar.getName(), "");
        }
        adminModifyCharacter(activeChar, val);
      } catch (StringIndexOutOfBoundsException e) {
        activeChar.sendAdmResultMessage("Error while modifying character.");
        listCharacters(activeChar, 0);
      }
    } else if (command.startsWith("admin_rec")) {
      try {
        String val = command.substring(10);
        int recVal = Integer.parseInt(val);
        L2Object target = activeChar.getTarget();
        L2PcInstance player = null;
        if ((activeChar != target) && (activeChar.getAccessLevel() < REQUIRED_LEVEL2)) {
          return false;
        }
        if (target.isPlayer())
          player = (L2PcInstance)target;
        else {
          return false;
        }
        player.setRecomHave(recVal);
        player.sendAdmResultMessage("You have been recommended by a GM");
        player.broadcastUserInfo();
      } catch (Exception e) {
        activeChar.sendAdmResultMessage("Usage: //rec number");
      }
    } else if (command.startsWith("admin_setclass")) {
      try {
        String val = command.substring(15);
        int classidval = Integer.parseInt(val);
        L2Object target = activeChar.getTarget();
        L2PcInstance player = null;
        if ((activeChar != target) && (activeChar.getAccessLevel() < REQUIRED_LEVEL2)) {
          return false;
        }
        if (target.isPlayer())
          player = (L2PcInstance)target;
        else {
          return false;
        }
        boolean valid = false;
        for (ClassId classid : ClassId.values()) {
          if (classidval == classid.getId()) {
            valid = true;
          }
        }
        if ((valid) && (player.getClassId().getId() != classidval)) {
          player.setClassId(classidval);
          if (!player.isSubClassActive()) {
            player.setBaseClass(classidval);
          }
          String newclass = player.getTemplate().className;
          player.store();
          player.sendAdmResultMessage("A GM changed your class to " + newclass);
          player.broadcastUserInfo();
          activeChar.sendMessage(player.getName() + " is a " + newclass);
        }
        activeChar.sendAdmResultMessage("Usage: //setclass <valid_new_classid>");
      } catch (StringIndexOutOfBoundsException e) {
        AdminHelpPage.showHelpPage(activeChar, "charclasses.htm");
      }
    } else if (command.startsWith("admin_settitle")) {
      try {
        String val = command.substring(15);
        L2Object target = activeChar.getTarget();
        L2PcInstance player = null;
        if ((activeChar != target) && (activeChar.getAccessLevel() < REQUIRED_LEVEL2)) {
          return false;
        }
        if (target.isPlayer())
          player = (L2PcInstance)target;
        else {
          return false;
        }
        player.setTitle(val);
        player.sendAdmResultMessage("Your title has been changed by a GM");
        player.broadcastTitleInfo();
      } catch (StringIndexOutOfBoundsException e) {
        activeChar.sendAdmResultMessage("You need to specify the new title.");
      }
    } else if (command.startsWith("admin_setname")) {
      try {
        String val = command.substring(14);
        L2Object target = activeChar.getTarget();
        L2PcInstance player = null;
        if ((activeChar != target) && (activeChar.getAccessLevel() < REQUIRED_LEVEL2)) {
          return false;
        }
        if (target.isPlayer())
          player = (L2PcInstance)target;
        else {
          return false;
        }
        player.changeName(val);
        activeChar.sendAdmResultMessage("\u0421\u043C\u0435\u043D\u0438\u043B\u0438 \u0438\u0433\u0440\u043E\u043A\u0443 \u043D\u0438\u043A \u043D\u0430 " + val);
      } catch (StringIndexOutOfBoundsException e) {
        activeChar.sendAdmResultMessage("Usage: //setname new_name_for_target");
      }
    } else if (command.startsWith("admin_setsex")) {
      L2Object target = activeChar.getTarget();
      L2PcInstance player = null;
      if ((activeChar != target) && (activeChar.getAccessLevel() < REQUIRED_LEVEL2)) {
        return false;
      }
      if (target.isPlayer())
        player = (L2PcInstance)target;
      else {
        return false;
      }
      player.setSex(!player.getAppearance().getSex());
      player.sendAdmResultMessage("Your gender has been changed by a GM");
      player.store();
      player.broadcastUserInfo();
    } else if (command.startsWith("admin_namecolor"))
    {
      try {
        String val = command.substring(16);
        L2Object target = activeChar.getTarget();
        L2PcInstance player = null;

        if (target.isPlayer())
          player = (L2PcInstance)target;
        else {
          return false;
        }

        player.getAppearance().setNameColor(Integer.decode("0x" + convertColor(val)).intValue());
        player.sendAdmResultMessage("\u0426\u0432\u0435\u0442 \u043D\u0438\u043A\u0430 \u0438\u0437\u043C\u0435\u043D\u0435\u043D");
        player.broadcastUserInfo();
      } catch (StringIndexOutOfBoundsException e) {
        activeChar.sendAdmResultMessage("You need to specify the new color.");
      }
    } else if (command.startsWith("admin_titlecolor")) {
      try {
        String val = command.substring(17);
        L2Object target = activeChar.getTarget();
        L2PcInstance player = null;
        if (target.isPlayer())
          player = (L2PcInstance)target;
        else {
          return false;
        }

        player.getAppearance().setTitleColor(Integer.decode("0x" + convertColor(val)).intValue());
        player.sendAdmResultMessage("\u0426\u0432\u0435\u0442 \u0442\u0438\u0442\u0443\u043B\u0430 \u0438\u0437\u043C\u0435\u043D\u0435\u043D");
        player.broadcastUserInfo();
      } catch (StringIndexOutOfBoundsException e) {
        activeChar.sendAdmResultMessage("You need to specify the new color.");
      }
    } else if (command.startsWith("admin_fullfood")) {
      L2Object target = activeChar.getTarget();
      if (target.isPet()) {
        L2PetInstance targetPet = (L2PetInstance)target;
        targetPet.setCurrentFed(targetPet.getMaxFed());
      } else {
        activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
      }
    } else if (command.startsWith("admin_negate")) {
      L2Object target = activeChar.getTarget();
      L2Character player = null;
      if (target.isL2Character()) {
        player = (L2Character)target;
        player.stopAllEffects();
        player.broadcastPacket(new MagicSkillUser(player, player, 2243, 1, 1, 0));
        activeChar.sendAdmResultMessage("\u0421\u043D\u044F\u0442\u0438\u0435 \u0431\u0430\u0444\u0444\u043E\u0432 \u0438\u0433\u0440\u043E\u043A\u0443 " + player.getName());
      } else {
        activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
      }
    } else if (command.startsWith("admin_cleanse")) {
      L2Object target = activeChar.getTarget();
      L2Character player = null;
      if (target.isL2Character()) {
        player = (L2Character)target;
        player.stopAllDebuffs();
        player.broadcastPacket(new MagicSkillUser(player, player, 2242, 1, 1, 0));
        activeChar.sendAdmResultMessage("\u0421\u043D\u044F\u0442\u0438\u0435 \u0434\u0435\u0431\u0430\u0444\u0444\u043E\u0432 \u0438\u0433\u0440\u043E\u043A\u0443 " + player.getName());
      } else {
        activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
      }
    } else if (command.startsWith("admin_spy")) {
      L2Object target = activeChar.getTarget();
      L2PcInstance player = null;
      if (target.isPlayer()) {
        player = (L2PcInstance)target;
        player.setSpy(true);
      }
    } else if (command.equalsIgnoreCase("admin_clearoly")) {
      L2Object target = activeChar.getTarget();
      if (target != null) {
        target.olympiadClear();
      }
    }

    return true;
  }

  private String convertColor(String color)
  {
    return new TextBuilder(color).reverse().toString();
  }

  public String[] getAdminCommandList() {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }

  private boolean checkLevel2(int level) {
    return level >= REQUIRED_LEVEL_VIEW;
  }

  private void listCharacters(L2PcInstance activeChar, int page) {
    Collection allPlayers = L2World.getInstance().getAllPlayers();
    L2PcInstance[] players = (L2PcInstance[])allPlayers.toArray(new L2PcInstance[allPlayers.size()]);

    int MaxCharactersPerPage = 20;
    int MaxPages = players.length / MaxCharactersPerPage;

    if (players.length > MaxCharactersPerPage * MaxPages) {
      MaxPages++;
    }

    if (page > MaxPages) {
      page = MaxPages;
    }

    int CharactersStart = MaxCharactersPerPage * page;
    int CharactersEnd = players.length;
    if (CharactersEnd - CharactersStart > MaxCharactersPerPage) {
      CharactersEnd = CharactersStart + MaxCharactersPerPage;
    }

    NpcHtmlMessage htm = NpcHtmlMessage.id(5);
    htm.setFile("data/html/admin/charlist.htm");
    TextBuilder tb = new TextBuilder();
    for (int x = 0; x < MaxPages; x++) {
      int pagenr = x + 1;
      tb.append("<center><a action=\"bypass -h admin_show_characters " + x + "\">Page " + pagenr + "</a></center>");
    }
    htm.replace("%pages%", tb.toString());
    tb.clear();

    tb.append("<tr><td width=80><a action=\"bypass -h admin_character_obj " + activeChar.getObjectId() + "\">" + activeChar.getName() + "</a></td><td width=110>" + activeChar.getTemplate().className + "</td><td width=40>" + activeChar.getLevel() + "</td></tr>");
    tb.append("<tr><td width=80>#</td><td width=110>#</td><td width=40>#</td></tr>");
    String name = "";
    for (int i = CharactersStart; i < CharactersEnd; i++) {
      if (players[i] == null)
      {
        continue;
      }

      name = Util.htmlSpecialChars(players[i].getName());
      tb.append("<tr><td width=80><a action=\"bypass -h admin_character_obj " + players[i].getObjectId() + "\">" + name + "</a></td><td width=110>" + players[i].getTemplate().className + "</td><td width=40>" + players[i].getLevel() + "</td></tr>");
    }
    htm.replace("%players%", tb.toString());
    activeChar.sendUserPacket(htm);

    tb.clear();
    tb = null;
    htm = null;
  }

  private void showCharacterInfo(L2PcInstance activeChar, L2PcInstance player) {
    if (player == null) {
      L2Object target = activeChar.getTarget();
      if (target.isPlayer())
        player = (L2PcInstance)target;
      else
        return;
    }
    else {
      activeChar.setTarget(player);
    }
    gatherCharacterInfo(activeChar, player, "charinfo.htm");
  }

  private void gatherCharacterInfo(L2PcInstance activeChar, L2PcInstance player, String filename)
  {
    String ip = "N/A";
    String account = "N/A";
    try {
      StringTokenizer clientinfo = new StringTokenizer(player.getClient().toString(), " ]:-[");
      clientinfo.nextToken();
      clientinfo.nextToken();
      clientinfo.nextToken();
      account = clientinfo.nextToken();
      clientinfo.nextToken();
      ip = clientinfo.nextToken();
    }
    catch (Exception e) {
    }
    String name = Util.htmlSpecialChars(player.getName());
    NpcHtmlMessage htm = NpcHtmlMessage.id(5);
    htm.setFile("data/html/admin/" + filename);
    htm.replace("%name%", name);
    htm.replace("%level%", String.valueOf(player.getLevel()));
    htm.replace("%clan%", String.valueOf(ClanTable.getInstance().getClan(player.getClanId())));
    htm.replace("%xp%", String.valueOf(player.getExp()));
    htm.replace("%sp%", String.valueOf(player.getSp()));
    htm.replace("%class%", player.getTemplate().className);
    htm.replace("%ordinal%", String.valueOf(player.getClassId().ordinal()));
    htm.replace("%classid%", String.valueOf(player.getClassId()));
    htm.replace("%x%", String.valueOf(player.getX()));
    htm.replace("%y%", String.valueOf(player.getY()));
    htm.replace("%z%", String.valueOf(player.getZ()));
    htm.replace("%currenthp%", String.valueOf((int)player.getCurrentHp()));
    htm.replace("%maxhp%", String.valueOf(player.getMaxHp()));
    htm.replace("%karma%", String.valueOf(player.getKarma()));
    htm.replace("%currentmp%", String.valueOf((int)player.getCurrentMp()));
    htm.replace("%maxmp%", String.valueOf(player.getMaxMp()));
    htm.replace("%pvpflag%", String.valueOf(player.getPvpFlag()));
    htm.replace("%currentcp%", String.valueOf((int)player.getCurrentCp()));
    htm.replace("%maxcp%", String.valueOf(player.getMaxCp()));
    htm.replace("%pvpkills%", String.valueOf(player.getPvpKills()));
    htm.replace("%pkkills%", String.valueOf(player.getPkKills()));
    htm.replace("%currentload%", String.valueOf(player.getCurrentLoad()));
    htm.replace("%maxload%", String.valueOf(player.getMaxLoad()));
    htm.replace("%percent%", String.valueOf(Util.roundTo(player.getCurrentLoad() / player.getMaxLoad() * 100.0F, 2)));
    htm.replace("%patk%", String.valueOf(player.getPAtk(null)));
    htm.replace("%matk%", String.valueOf(player.getMAtk(null, null)));
    htm.replace("%pdef%", String.valueOf(player.getPDef(null)));
    htm.replace("%mdef%", String.valueOf(player.getMDef(null, null)));
    htm.replace("%accuracy%", String.valueOf(player.getAccuracy()));
    htm.replace("%evasion%", String.valueOf(player.getEvasionRate(null)));
    htm.replace("%critical%", String.valueOf(player.getCriticalHit(null, null)));
    htm.replace("%runspeed%", String.valueOf(player.getRunSpeed()));
    htm.replace("%patkspd%", String.valueOf(player.getPAtkSpd()));
    htm.replace("%matkspd%", String.valueOf(player.getMAtkSpd()));
    htm.replace("%access%", String.valueOf(player.getAccessLevel()));
    htm.replace("%account%", account);
    htm.replace("%ip%", ip);
    activeChar.sendPacket(htm);
    htm = null;
  }

  private void setTargetKarma(L2PcInstance activeChar, int newKarma)
  {
    L2Object target = activeChar.getTarget();
    L2PcInstance player = null;
    if (target.isPlayer())
      player = (L2PcInstance)target;
    else {
      return;
    }

    if (newKarma >= 0)
    {
      int oldKarma = player.getKarma();

      player.setKarma(newKarma);

      player.sendPacket(SystemMessage.id(SystemMessageId.YOUR_KARMA_HAS_BEEN_CHANGED_TO).addString(String.valueOf(newKarma)));

      activeChar.sendAdmResultMessage("Successfully Changed karma for " + player.getName() + " from (" + oldKarma + ") to (" + newKarma + ").");
      if (Config.DEBUG)
        _log.fine("[SET KARMA] [GM]" + activeChar.getName() + " Changed karma for " + player.getName() + " from (" + oldKarma + ") to (" + newKarma + ").");
    }
    else
    {
      activeChar.sendAdmResultMessage("You must enter a value for karma greater than or equal to 0.");
      if (Config.DEBUG)
        _log.fine("[SET KARMA] ERROR: [GM]" + activeChar.getName() + " entered an incorrect value for new karma: " + newKarma + " for " + player.getName() + ".");
    }
  }

  private void adminModifyCharacter(L2PcInstance activeChar, String modifications)
  {
    L2Object target = activeChar.getTarget();

    if (!target.isPlayer()) {
      return;
    }

    L2PcInstance player = (L2PcInstance)target;
    StringTokenizer st = new StringTokenizer(modifications);

    if (st.countTokens() != 6) {
      editCharacter(player);
      return;
    }

    String hp = st.nextToken();
    String mp = st.nextToken();
    String cp = st.nextToken();
    String pvpflag = st.nextToken();
    String pvpkills = st.nextToken();
    String pkkills = st.nextToken();

    int hpval = Integer.parseInt(hp);
    int mpval = Integer.parseInt(mp);
    int cpval = Integer.parseInt(cp);
    int pvpflagval = Integer.parseInt(pvpflag);
    int pvpkillsval = Integer.parseInt(pvpkills);
    int pkkillsval = Integer.parseInt(pkkills);

    player.sendAdmResultMessage("Admin has changed your stats.  HP: " + hpval + "  MP: " + mpval + "  CP: " + cpval + "  PvP Flag: " + pvpflagval + " PvP/PK " + pvpkillsval + "/" + pkkillsval);

    player.setCurrentHp(hpval);
    player.setCurrentMp(mpval);
    player.setCurrentCp(cpval);
    player.setPvpFlag(pvpflagval);
    player.setPvpKills(pvpkillsval);
    player.setPkKills(pkkillsval);

    player.store();

    StatusUpdate su = new StatusUpdate(player.getObjectId());
    su.addAttribute(9, hpval);
    su.addAttribute(10, player.getMaxHp());
    su.addAttribute(11, mpval);
    su.addAttribute(12, player.getMaxMp());
    su.addAttribute(33, cpval);
    su.addAttribute(34, player.getMaxCp());
    player.sendPacket(su);

    player.sendAdmResultMessage("Changed stats of " + player.getName() + "." + "  HP: " + hpval + "  MP: " + mpval + "  CP: " + cpval + "  PvP: " + pvpflagval + " / " + pvpkillsval);

    if (Config.DEBUG) {
      _log.fine("[GM]" + activeChar.getName() + " changed stats of " + player.getName() + ". " + " HP: " + hpval + " MP: " + mpval + " CP: " + cpval + " PvP: " + pvpflagval + " / " + pvpkillsval);
    }

    showCharacterInfo(activeChar, null);

    player.broadcastPacket(new CharInfo(player));
    player.sendPacket(new UserInfo(player));
    player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
    player.decayMe();
    player.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
  }

  private void editCharacter(L2PcInstance activeChar) {
    L2Object target = activeChar.getTarget();
    if (target == null)
    {
      activeChar.sendAdmResultMessage("\u041F\u0435\u0440\u0441\u043E\u043D\u0430\u0436 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D.");
      return;
    }
    if (!target.isPlayer()) {
      return;
    }
    L2PcInstance player = (L2PcInstance)target;
    gatherCharacterInfo(activeChar, player, "charedit.htm");
  }

  private void findCharacter(L2PcInstance activeChar, String charToFind)
  {
    TextBuilder tb = new TextBuilder();
    NpcHtmlMessage htm = NpcHtmlMessage.id(5);
    htm.setFile("data/html/admin/charfind.htm");

    int count = 0;

    String search = Util.htmlSpecialChars(charToFind.toLowerCase());
    for (L2PcInstance player : L2World.getInstance().getAllPlayers()) {
      if (player == null)
      {
        continue;
      }
      String name = Util.htmlSpecialChars(player.getName());
      if (name.toLowerCase().contains(search)) {
        count++;
        tb.append("<tr><td width=80><a action=\"bypass -h admin_character_list " + name + "\">" + name + "</a></td><td width=110>" + player.getTemplate().className + "</td><td width=40>" + player.getLevel() + "</td></tr>");
      }
      if (count > 20) {
        break;
      }
    }
    htm.replace("%results%", tb.toString());
    tb.clear();

    if (count == 0) {
      tb.append("s. Please try again.");
    } else if (count > 20) {
      htm.replace("%number%", " more than 20");
      tb.append("s.<br>Please refine your search to see all of the results.");
    } else if (count == 1) {
      tb.append(".");
    } else {
      tb.append("s.");
    }

    htm.replace("%number%", String.valueOf(count));
    htm.replace("%end%", tb.toString());
    activeChar.sendUserPacket(htm);

    tb.clear();
    tb = null;
    htm = null;
  }

  private void findCharactersPerIp(L2PcInstance activeChar, String IpAdress)
    throws IllegalArgumentException
  {
  }

  private void findCharactersPerAccount(L2PcInstance activeChar, String characterName)
    throws IllegalArgumentException
  {
    if (characterName.matches(Config.CNAME_TEMPLATE)) {
      String account = null;

      L2PcInstance player = L2World.getInstance().getPlayer(characterName);
      if (player == null) {
        throw new IllegalArgumentException("Player doesn't exist");
      }
      Map chars = player.getAccountChars();
      account = player.getAccountName();
      TextBuilder replyMSG = new TextBuilder();
      NpcHtmlMessage adminReply = NpcHtmlMessage.id(5);
      adminReply.setFile("data/html/admin/accountinfo.htm");
      for (String charname : chars.values()) {
        replyMSG.append(charname + "<br1>");
      }
      adminReply.replace("%characters%", replyMSG.toString());
      adminReply.replace("%account%", account);
      adminReply.replace("%player%", characterName);
      activeChar.sendPacket(adminReply);
    } else {
      throw new IllegalArgumentException("Malformed character name");
    }
  }
}