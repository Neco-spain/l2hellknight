package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javolution.text.TextBuilder;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Clan.SubPledge;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2PledgeSkillLearn;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.ClassType;
import net.sf.l2j.gameserver.model.base.PlayerClass;
import net.sf.l2j.gameserver.model.base.PlayerRace;
import net.sf.l2j.gameserver.model.base.SubClass;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.entity.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.AquireSkillList;
import net.sf.l2j.gameserver.network.serverpackets.AquireSkillList.SkillType;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.util.Util;

public final class L2VillageMasterInstance extends L2FolkInstance
{
  public L2VillageMasterInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (player.isMounted()) {
      player.sendActionFailed();
      return;
    }

    if (player.getActiveWarehouse() != null) {
      player.cancelActiveWarehouse();
      player.sendActionFailed();
      return;
    }

    String[] commandStr = command.split(" ");
    String actualCommand = commandStr[0];

    String cmdParams = "";
    String cmdParams2 = "";

    if (commandStr.length >= 2) {
      cmdParams = commandStr[1];
    }
    if (commandStr.length >= 3) {
      cmdParams2 = commandStr[2];
    }

    if (actualCommand.equalsIgnoreCase("create_clan")) {
      if (cmdParams.equals("")) {
        return;
      }

      ClanTable.getInstance().createClan(player, cmdParams);
    } else if (actualCommand.equalsIgnoreCase("create_academy")) {
      if (cmdParams.equals("")) {
        return;
      }

      createSubPledge(player, cmdParams, null, -1, 5);
    } else if (actualCommand.equalsIgnoreCase("create_royal")) {
      if (cmdParams.equals("")) {
        return;
      }

      createSubPledge(player, cmdParams, cmdParams2, 100, 6);
    } else if (actualCommand.equalsIgnoreCase("create_knight")) {
      if (cmdParams.equals("")) {
        return;
      }

      createSubPledge(player, cmdParams, cmdParams2, 1001, 7);
    } else if (actualCommand.equalsIgnoreCase("assign_subpl_leader")) {
      if (cmdParams.equals("")) {
        return;
      }

      assignSubPledgeLeader(player, cmdParams, cmdParams2);
    } else if (actualCommand.equalsIgnoreCase("create_ally")) {
      if (cmdParams.equals("")) {
        return;
      }

      if (!player.isClanLeader()) {
        player.sendPacket(Static.ONLY_CLAN_LEADER_CREATE_ALLIANCE);
        return;
      }
      player.getClan().createAlly(player, cmdParams);
    } else if (actualCommand.equalsIgnoreCase("dissolve_ally")) {
      if (!player.isClanLeader()) {
        player.sendPacket(Static.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
        return;
      }
      player.getClan().dissolveAlly(player);
    } else if (actualCommand.equalsIgnoreCase("dissolve_clan")) {
      dissolveClan(player, player.getClanId());
    } else if (actualCommand.equalsIgnoreCase("change_clan_leader")) {
      if (cmdParams.equals("")) {
        return;
      }

      changeClanLeader(player, cmdParams);
    } else if (actualCommand.equalsIgnoreCase("recover_clan")) {
      recoverClan(player, player.getClanId());
    } else if (actualCommand.equalsIgnoreCase("increase_clan_level")) {
      if (!player.isClanLeader()) {
        player.sendPacket(Static.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
        return;
      }
      player.getClan().levelUpClan(player);
    } else if (actualCommand.equalsIgnoreCase("learn_clan_skills")) {
      showPledgeSkillList(player);
    } else if (command.startsWith("Subclass")) {
      int cmdChoice = Integer.parseInt(command.substring(9, 10).trim());

      TextBuilder content = new TextBuilder("<html><body>");
      NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());

      int paramOne = 0;
      int paramTwo = 0;
      try
      {
        int endIndex = command.length();

        if (command.length() > 13) {
          endIndex = 13;
          paramTwo = Integer.parseInt(command.substring(13).trim());
        }

        paramOne = Integer.parseInt(command.substring(11, endIndex).trim());
      }
      catch (Exception NumberFormatException)
      {
      }
      Set subsAvailable;
      Iterator subList;
      switch (cmdChoice)
      {
      case 1:
        if (player.getTotalSubClasses() == Config.MAX_SUBCLASS) {
          player.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0434\u043E\u0431\u0430\u0432\u0438\u0442\u044C \u0431\u043E\u043B\u044C\u0448\u0435 " + Config.MAX_SUBCLASS + " \u0441\u0443\u0431-\u043A\u043B\u0430\u0441\u0441\u043E\u0432");
          return;
        }

        subsAvailable = getAvailableSubClasses(player);

        if ((subsAvailable != null) && (!subsAvailable.isEmpty())) {
          content.append("\u0414\u043E\u0431\u0430\u0432\u043B\u0435\u043D\u0438\u0435 \u0441\u0430\u0431\u0430:<br>\u041A\u0430\u043A\u043E\u0439 \u0441\u0430\u0431 \u0432\u044B \u0445\u043E\u0442\u0438\u0442\u0435 \u0434\u043E\u0431\u0430\u0432\u0438\u0442\u044C?<br>");

          for (PlayerClass subClass : subsAvailable) {
            content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 4 " + subClass.ordinal() + "\" msg=\"1268;" + formatClassForDisplay(subClass) + "\">" + formatClassForDisplay(subClass) + "</a><br>");
          }

        }
        else
        {
          player.sendMessage("\u041D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u043D\u044B\u0445 \u0441\u0430\u0431\u043E\u0432");
          return;
        }

      case 2:
        content.append("\u0421\u043C\u0435\u043D\u0430 \u0441\u0430\u0431\u0430:<br>");

        int baseClassId = player.getBaseClass();

        if (player.getSubClasses().isEmpty()) {
          content.append("\u0423 \u0432\u0430\u0441 \u043D\u0435\u0442 \u0441\u0430\u0431\u043E\u0432, \u0432\u044B \u043C\u043E\u0436\u0435\u0442\u0435 \u0435\u0433\u043E \u0434\u043E\u0431\u0430\u0432\u0438\u0442\u044C:<br><a action=\"bypass -h npc_" + getObjectId() + "_Subclass 1\">\u0414\u043E\u0431\u0430\u0432\u0438\u0442\u044C \u0441\u0430\u0431.</a>");
        }
        else
        {
          content.append("\u041D\u0430 \u043A\u0430\u043A\u043E\u0439 \u0441\u0430\u0431 \u0432\u044B \u0445\u043E\u0442\u0438\u0442\u0435 \u043F\u0435\u0440\u0435\u043A\u043B\u044E\u0447\u0438\u0442\u044C\u0441\u044F?<br>");

          if (baseClassId == player.getActiveClass()) {
            content.append(CharTemplateTable.getClassNameById(baseClassId) + "&nbsp;<font color=\"LEVEL\">(\u041C\u0435\u0439\u043D)</font><br><br>");
          }
          else {
            content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 5 0\">" + CharTemplateTable.getClassNameById(baseClassId) + "</a>&nbsp;" + "<font color=\"LEVEL\">(\u041C\u0435\u0439\u043D)</font><br><br>");
          }

          for (subList = iterSubClasses(player); subList.hasNext(); ) {
            SubClass subClass = (SubClass)subList.next();
            int subClassId = subClass.getClassId();

            if (subClassId == player.getActiveClass()) {
              content.append(CharTemplateTable.getClassNameById(subClassId) + "<br>");
            }
            else {
              content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 5 " + subClass.getClassIndex() + "\">" + CharTemplateTable.getClassNameById(subClassId) + "</a><br>");
            }
          }

        }

        break;
      case 3:
        content.append("\u0421\u043C\u0435\u043D\u0430 \u0441\u0430\u0431\u0430:<br>\u041A\u0430\u043A\u043E\u0439 \u0438\u0437 \u0441\u0430\u0431\u043E\u0432 \u0432\u044B \u0445\u043E\u0442\u0438\u0442\u0435 \u043F\u043E\u043C\u0435\u043D\u044F\u0442\u044C?<br>");
        int classIndex = 1;

        for (Iterator subList = iterSubClasses(player); subList.hasNext(); ) {
          SubClass subClass = (SubClass)subList.next();

          content.append("\u0421\u0430\u0431\u044B " + classIndex + "<br1>");
          content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 6 " + subClass.getClassIndex() + "\">" + CharTemplateTable.getClassNameById(subClass.getClassId()) + "</a><br>");

          classIndex++;
        }

        content.append("<br>\u041D\u043E\u0432\u044B\u0439 \u0431\u0443\u0434\u0435\u0442 \u0434\u043E\u0431\u0430\u0432\u043B\u0435\u043D 40 \u0443\u0440\u043E\u0432\u043D\u044F \u0441 2-\u0439 \u043F\u0440\u043E\u0444\u043E\u0439.");
        break;
      case 4:
        boolean allowAddition = true;

        if (player.getLevel() < 75) {
          player.sendMessage("You may not add a new sub class before you are level 75 on your previous class.");
          allowAddition = false;
        }
        Iterator subList;
        if ((allowAddition) && 
          (!player.getSubClasses().isEmpty())) {
          for (subList = iterSubClasses(player); subList.hasNext(); ) {
            SubClass subClass = (SubClass)subList.next();

            if (subClass.getLevel() < 75) {
              player.sendMessage("You may not add a new sub class before you are level 75 on your previous sub class.");
              allowAddition = false;
              break;
            }

          }

        }

        if (!Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS)
        {
          QuestState qs = player.getQuestState("q235_MimirsElixir");
          if ((qs == null) || (!qs.isCompleted())) {
            player.sendMessage("You must have completed the Mimir's Elixir quest to continue adding your sub class.");
            return;
          }
          qs = player.getQuestState("q234_FatesWhisper");
          if ((qs == null) || (!qs.isCompleted())) {
            player.sendMessage("You must have completed the Fate's Whisper quest to continue adding your sub class.");
            return;
          }

        }

        if (allowAddition) {
          CharTemplateTable.getInstance(); String className = CharTemplateTable.getClassNameById(paramOne);

          if (!player.addSubClass(paramOne, player.getTotalSubClasses() + 1)) {
            player.sendMessage("\u041D\u0435\u043B\u044C\u0437\u044F \u0434\u043E\u0431\u0430\u0432\u0438\u0442\u044C \u044D\u0442\u043E\u0442 \u0441\u0430\u0431");
            return;
          }

          if (!ChangeSub(player, player.getTotalSubClasses())) {
            return;
          }

          content.append("Add Subclass:<br>\u0421\u0430\u0431 <font color=\"LEVEL\">" + className + "</font> \u0434\u043E\u0431\u0430\u0432\u043B\u0435\u043D");
          player.sendPacket(Static.CLASS_TRANSFER);
        } else {
          html.setFile("data/html/villagemaster/SubClass_Fail.htm");
        }
        break;
      case 5:
        if (!ChangeSub(player, paramOne)) {
          return;
        }

        content.append("\u0421\u043C\u0435\u043D\u0430 \u0441\u0430\u0431\u0430:<br>\u0422\u0435\u043F\u0435\u0440\u044C \u0432\u044B <font color=\"LEVEL\">" + CharTemplateTable.getClassNameById(player.getActiveClass()) + "</font>.");
        player.sendPacket(Static.SUBCLASS_TRANSFER_COMPLETED);
        break;
      case 6:
        content.append("Please choose a sub class to change to. If the one you are looking for is not here, please seek out the appropriate master for that class.<br><font color=\"LEVEL\">Warning!</font> All classes and skills for this class will be removed.<br><br>");

        subsAvailable = getAvailableSubClasses(player);

        if ((subsAvailable != null) && (!subsAvailable.isEmpty())) {
          for (PlayerClass subClass : subsAvailable) {
            content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 7 " + paramOne + " " + subClass.ordinal() + "\">" + formatClassForDisplay(subClass) + "</a><br>");
          }
        }
        else
        {
          player.sendMessage("There are no sub classes available at this time.");
          return;
        }

      case 7:
        if (!canChangeSub(player)) {
          return;
        }

        player.stopAllEffects();

        if (player.modifySubClass(paramOne, paramTwo)) {
          if (!ChangeSub(player, paramOne)) {
            return;
          }

          content.append("\u0421\u043C\u0435\u043D\u0430 \u0441\u0430\u0431\u0430:<br>\u0421\u0430\u0431 \u0438\u0437\u043C\u0435\u043D\u0435\u043D \u043D\u0430 <font color=\"LEVEL\">" + CharTemplateTable.getClassNameById(paramTwo) + "</font>.");
          player.sendPacket(Static.ADD_NEW_SUBCLASS);
        }
        else
        {
          player.setActiveClass(0);
          player.sendMessage("The sub class could not be added, you have been reverted to your base class.");
          return;
        }

      }

      content.append("</body></html>");

      if (content.length() > 26) {
        html.setHtml(content.toString());
      }

      player.sendPacket(html);
    }
    else
    {
      super.onBypassFeedback(player, command);
    }
  }

  public String getHtmlPath(int npcId, int val)
  {
    String pom = "";

    if (val == 0)
      pom = "" + npcId;
    else {
      pom = npcId + "-" + val;
    }

    return "data/html/villagemaster/" + pom + ".htm";
  }

  public void dissolveClan(L2PcInstance player, int clanId)
  {
    if (!player.isClanLeader()) {
      player.sendPacket(Static.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
      return;
    }
    L2Clan clan = player.getClan();
    if (clan.getAllyId() != 0) {
      player.sendPacket(Static.CANNOT_DISPERSE_THE_CLANS_IN_ALLY);
      return;
    }
    if (clan.isAtWar() != 0) {
      player.sendPacket(Static.CANNOT_DISSOLVE_WHILE_IN_WAR);
      return;
    }
    if ((clan.getHasCastle() != 0) || (clan.getHasHideout() != 0)) {
      player.sendPacket(Static.CANNOT_DISSOLVE_WHILE_OWNING_CLAN_HALL_OR_CASTLE);
      return;
    }
    for (Castle castle : CastleManager.getInstance().getCastles()) {
      if (SiegeManager.getInstance().checkIsRegistered(clan, castle.getCastleId())) {
        player.sendPacket(Static.CANNOT_DISSOLVE_CAUSE_CLAN_WILL_PARTICIPATE_IN_CASTLE_SIEGE);
        return;
      }
    }
    if (player.isInsideZone(4)) {
      player.sendPacket(Static.CANNOT_DISSOLVE_WHILE_IN_SIEGE);
      return;
    }
    if (clan.getDissolvingExpiryTime() > System.currentTimeMillis()) {
      player.sendPacket(Static.DISSOLUTION_IN_PROGRESS);
      return;
    }

    clan.setDissolvingExpiryTime(System.currentTimeMillis() + Config.ALT_CLAN_DISSOLVE_DAYS * 86400000L);
    clan.updateClanInDB();

    ClanTable.getInstance().scheduleRemoveClan(clan.getClanId());

    player.deathPenalty(false);
  }

  public void recoverClan(L2PcInstance player, int clanId)
  {
    if (!player.isClanLeader()) {
      player.sendPacket(Static.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
      return;
    }
    L2Clan clan = player.getClan();

    clan.setDissolvingExpiryTime(0L);
    clan.updateClanInDB();
  }

  public void changeClanLeader(L2PcInstance player, String target)
  {
    if (!player.isClanLeader()) {
      player.sendPacket(Static.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
      return;
    }
    if (player.getName().equalsIgnoreCase(target)) {
      return;
    }
    L2Clan clan = player.getClan();

    L2ClanMember member = clan.getClanMember(target);
    if (member == null) {
      player.sendPacket(SystemMessage.id(SystemMessageId.S1_DOES_NOT_EXIST).addString(target));
      return;
    }
    if (!member.isOnline()) {
      player.sendPacket(Static.INVITED_USER_NOT_ONLINE);
      return;
    }
    clan.setNewLeader(member);
  }

  public void createSubPledge(L2PcInstance player, String clanName, String leaderName, int pledgeType, int minClanLvl)
  {
    if (!player.isClanLeader()) {
      player.sendPacket(Static.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
      return;
    }

    L2Clan clan = player.getClan();
    if (clan.getLevel() < minClanLvl) {
      if (pledgeType == -1)
        player.sendPacket(Static.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN_ACADEMY);
      else {
        player.sendPacket(Static.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_MILITARY_UNIT);
      }
      return;
    }
    if ((!Util.isAlphaNumeric(clanName)) || (2 > clanName.length())) {
      player.sendPacket(Static.CLAN_NAME_INCORRECT);
      return;
    }
    if (clanName.length() > 16) {
      player.sendPacket(Static.CLAN_NAME_TOO_LONG);
      return;
    }

    FastTable cn = new FastTable();
    cn.addAll(ClanTable.getInstance().getClans());
    for (L2Clan tempClan : cn) {
      if (tempClan.getSubPledge(clanName) != null) {
        if (pledgeType == -1)
          player.sendPacket(SystemMessage.id(SystemMessageId.S1_ALREADY_EXISTS).addString(clanName));
        else {
          player.sendPacket(Static.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME);
        }
        return;
      }
    }

    if ((pledgeType != -1) && (
      (clan.getClanMember(leaderName) == null) || (clan.getClanMember(leaderName).getPledgeType() != 0))) {
      if (pledgeType >= 1001)
        player.sendPacket(Static.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED);
      else if (pledgeType >= 100) {
        player.sendPacket(Static.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
      }
      return;
    }

    if (clan.createSubPledge(player, pledgeType, leaderName, clanName) == null) {
      return;
    }

    SystemMessage sm = Static.CLAN_CREATED;
    if (pledgeType == -1)
      sm = SystemMessage.id(SystemMessageId.THE_S1S_CLAN_ACADEMY_HAS_BEEN_CREATED).addString(player.getClan().getName());
    else if (pledgeType >= 1001)
      sm = SystemMessage.id(SystemMessageId.THE_KNIGHTS_OF_S1_HAVE_BEEN_CREATED).addString(player.getClan().getName());
    else if (pledgeType >= 100) {
      sm = SystemMessage.id(SystemMessageId.THE_ROYAL_GUARD_OF_S1_HAVE_BEEN_CREATED).addString(player.getClan().getName());
    }
    player.sendPacket(sm);
    sm = null;

    if (pledgeType != -1) {
      L2ClanMember leaderSubPledge = clan.getClanMember(leaderName);
      if (leaderSubPledge.getPlayerInstance() == null) {
        return;
      }
      leaderSubPledge.getPlayerInstance().setPledgeClass(leaderSubPledge.calculatePledgeClass(leaderSubPledge.getPlayerInstance()));
      leaderSubPledge.getPlayerInstance().sendPacket(new UserInfo(leaderSubPledge.getPlayerInstance()));
    }
  }

  public void assignSubPledgeLeader(L2PcInstance player, String clanName, String leaderName)
  {
    if (!player.isClanLeader()) {
      player.sendPacket(Static.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
      return;
    }

    if (leaderName.length() > 16) {
      player.sendPacket(Static.NAMING_CHARNAME_UP_TO_16CHARS);
      return;
    }

    if (player.getName().equals(leaderName)) {
      player.sendPacket(Static.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
      return;
    }

    L2Clan clan = player.getClan();
    L2Clan.SubPledge subPledge = player.getClan().getSubPledge(clanName);

    if (null == subPledge) {
      player.sendPacket(Static.CLAN_NAME_INCORRECT);
      return;
    }
    if (subPledge.getId() == -1) {
      player.sendPacket(Static.CLAN_NAME_INCORRECT);
      return;
    }

    if ((clan.getClanMember(leaderName) == null) || (clan.getClanMember(leaderName).getPledgeType() != 0)) {
      if (subPledge.getId() >= 1001)
        player.sendPacket(Static.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED);
      else if (subPledge.getId() >= 100) {
        player.sendPacket(Static.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
      }
      return;
    }

    subPledge.setLeaderName(leaderName);
    clan.updateSubPledgeInDB(subPledge.getId());

    L2ClanMember leaderSubPledge = clan.getClanMember(leaderName);
    L2PcInstance leader = leaderSubPledge.getPlayerInstance();
    if ((leader != null) && (leader.isOnline() == 1)) {
      leader.setPledgeClass(leaderSubPledge.calculatePledgeClass(leader));
      leader.sendPacket(new UserInfo(leader));
    }
    clan.broadcastClanStatus();
    clan.broadcastToOnlineMembers(SystemMessage.id(SystemMessageId.S1_HAS_BEEN_SELECTED_AS_CAPTAIN_OF_S2).addString(leaderName).addString(clanName));
  }

  private final Set<PlayerClass> getAvailableSubClasses(L2PcInstance player) {
    int charClassId = player.getBaseClass();
    if (charClassId >= 88) {
      charClassId = ClassId.values()[charClassId].getParent().getId();
    }

    PlayerClass currClass = PlayerClass.values()[charClassId];

    Set availSubs = null;
    PlayerRace npcRace;
    ClassType npcTeachType;
    if ((Config.ALT_ANY_SUBCLASS) || ((Config.PREMIUM_ANY_SUBCLASS) && (player.isPremium()))) {
      availSubs = currClass.getAllSubclasses();
    } else {
      npcRace = getVillageMasterRace();
      npcTeachType = getVillageMasterTeachType();
      availSubs = currClass.getAvailableSubclasses(player);
      if (availSubs != null) {
        for (PlayerClass availSub : availSubs) {
          for (Iterator subList = iterSubClasses(player); subList.hasNext(); ) {
            SubClass prevSubClass = (SubClass)subList.next();
            int subClassId = prevSubClass.getClassId();
            if (subClassId >= 88) {
              subClassId = ClassId.values()[subClassId].getParent().getId();
            }

            if ((availSub.ordinal() == subClassId) || (availSub.ordinal() == charClassId)) {
              availSubs.remove(PlayerClass.values()[availSub.ordinal()]);
            }
          }

          if ((npcRace == PlayerRace.Human) || (npcRace == PlayerRace.LightElf))
          {
            if (!availSub.isOfType(npcTeachType)) {
              availSubs.remove(availSub);
            }
            else if ((!availSub.isOfRace(PlayerRace.Human)) && (!availSub.isOfRace(PlayerRace.LightElf))) {
              availSubs.remove(availSub);
            }

          }
          else if ((npcRace != PlayerRace.Human) && (npcRace != PlayerRace.LightElf) && (!availSub.isOfRace(npcRace))) {
            availSubs.remove(availSub);
          }
        }
      }
    }

    return availSubs;
  }

  public void showPledgeSkillList(L2PcInstance player)
  {
    if (player.getClan() == null) {
      return;
    }

    L2PledgeSkillLearn[] skills = SkillTreeTable.getInstance().getAvailablePledgeSkills(player);
    AquireSkillList asl = new AquireSkillList(AquireSkillList.SkillType.Clan);
    int counts = 0;

    for (L2PledgeSkillLearn s : skills) {
      int cost = s.getRepCost();
      counts++;

      asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
    }

    if (counts == 0) {
      asl = null;
      if (player.getClan().getLevel() < 8)
        player.sendPacket(SystemMessage.id(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN).addNumber(player.getClan().getLevel() + 1));
      else
        player.sendHtmlMessage("You've learned all skills available for your Clan.");
    }
    else {
      player.sendPacket(asl);
    }

    player.sendActionFailed();
  }

  private final String formatClassForDisplay(PlayerClass className) {
    String classNameStr = className.toString();
    char[] charArray = classNameStr.toCharArray();

    for (int i = 1; i < charArray.length; i++) {
      if (Character.isUpperCase(charArray[i])) {
        classNameStr = classNameStr.substring(0, i) + " " + classNameStr.substring(i);
      }
    }

    return classNameStr;
  }

  private final PlayerRace getVillageMasterRace() {
    String npcClass = getTemplate().getStatsSet().getString("jClass").toLowerCase();

    if (npcClass.indexOf("human") > -1) {
      return PlayerRace.Human;
    }

    if (npcClass.indexOf("darkelf") > -1) {
      return PlayerRace.DarkElf;
    }

    if (npcClass.indexOf("elf") > -1) {
      return PlayerRace.LightElf;
    }

    if (npcClass.indexOf("orc") > -1) {
      return PlayerRace.Orc;
    }

    return PlayerRace.Dwarf;
  }

  private final ClassType getVillageMasterTeachType() {
    String npcClass = getTemplate().getStatsSet().getString("jClass");

    if ((npcClass.indexOf("sanctuary") > -1) || (npcClass.indexOf("clergyman") > -1)) {
      return ClassType.Priest;
    }

    if ((npcClass.indexOf("mageguild") > -1) || (npcClass.indexOf("patriarch") > -1)) {
      return ClassType.Mystic;
    }

    return ClassType.Fighter;
  }

  private Iterator<SubClass> iterSubClasses(L2PcInstance player) {
    return player.getSubClasses().values().iterator();
  }

  public void sForces(L2PcInstance player)
  {
    player.stopSkillEffects(426);
    player.stopSkillEffects(427);
    player.stopSkillEffects(5104);
    player.stopSkillEffects(5105);
  }

  public boolean ChangeSub(L2PcInstance player, int sub)
  {
    if (System.currentTimeMillis() - player.gCPBF() < 2100L) {
      player.sendPacket(Static.PLEASE_WAIT);
      return false;
    }
    player.sCPBF();

    if (!canChangeSub(player)) {
      return false;
    }

    if (player.getFairy() != null) {
      player.getFairy().unSummon(player);
    }

    player.abortAttack();
    player.abortCast();
    player.setIsParalyzed(true);

    player.setActiveClass(sub);
    player.setCurrentCp(player.getMaxCp());
    player.setCurrentHp(player.getMaxHp());
    player.setCurrentMp(player.getMaxMp());
    sForces(player);
    player.setIsParalyzed(false);
    player.checkAllowedSkills();
    player.sendChanges();
    return true;
  }

  public boolean canChangeSub(L2PcInstance player)
  {
    if (player.getTarget() != this) {
      player.sendPacket(Static.SUBCLASS_NO_CHANGE_OR_CREATE_WHILE_SKILL_IN_USE);
      return false;
    }

    if ((player.isCastingNow()) || (player.isAllSkillsDisabled()) || (player.isAttackingNow())) {
      player.sendPacket(Static.SUBCLASS_NO_CHANGE_OR_CREATE_WHILE_SKILL_IN_USE);
      return false;
    }

    if (player.getPet() != null) {
      player.sendPacket(Static.CANT_CHANGE_SUB_SUMMON);
      return false;
    }

    if ((player.isDead()) || (player.isAlikeDead())) {
      player.sendPacket(Static.OOPS_ERROR);
      return false;
    }

    if ((Olympiad.isRegisteredInComp(player)) || (player.isInOlympiadMode()) || (player.getOlympiadGameId() > -1)) {
      player.sendPacket(Static.OOPS_ERROR);
      return false;
    }

    if ((!TvTEvent.isInactive()) && (TvTEvent.isPlayerParticipant(player.getName()))) {
      player.sendPacket(Static.OOPS_ERROR);
      return false;
    }

    if (player.inFClub()) {
      player.sendPacket(Static.OOPS_ERROR);
      return false;
    }

    player.abortAttack();
    player.abortCast();
    return true;
  }

  public boolean isL2VillageMaster()
  {
    return true;
  }
}