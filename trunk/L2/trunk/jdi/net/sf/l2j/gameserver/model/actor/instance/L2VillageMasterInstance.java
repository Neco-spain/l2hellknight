package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.FortSiegeManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Clan.SubPledge;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PledgeSkillLearn;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.ClassType;
import net.sf.l2j.gameserver.model.base.PlayerClass;
import net.sf.l2j.gameserver.model.base.PlayerRace;
import net.sf.l2j.gameserver.model.base.SubClass;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.FortSiege;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.AquireSkillList;
import net.sf.l2j.gameserver.network.serverpackets.AquireSkillList.skillType;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.Ride;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.l2j.gameserver.util.Util;

public final class L2VillageMasterInstance extends L2FolkInstance
{
  public L2VillageMasterInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    String[] commandStr = command.split(" ");
    String actualCommand = commandStr[0];

    String cmdParams = "";
    String cmdParams2 = "";

    if (commandStr.length >= 2) cmdParams = commandStr[1];
    if (commandStr.length >= 3) cmdParams2 = commandStr[2];

    if (actualCommand.equalsIgnoreCase("create_clan"))
    {
      if (cmdParams.equals("")) return;

      ClanTable.getInstance().createClan(player, cmdParams);
    }
    else if (actualCommand.equalsIgnoreCase("create_academy"))
    {
      if (cmdParams.equals("")) return;

      createSubPledge(player, cmdParams, null, -1, 5);
    }
    else if (actualCommand.equalsIgnoreCase("create_royal"))
    {
      if (cmdParams.equals("")) return;

      createSubPledge(player, cmdParams, cmdParams2, 100, 6);
    }
    else if (actualCommand.equalsIgnoreCase("create_knight"))
    {
      if (cmdParams.equals("")) return;

      createSubPledge(player, cmdParams, cmdParams2, 1001, 7);
    }
    else if (actualCommand.equalsIgnoreCase("assign_subpl_leader"))
    {
      if (cmdParams.equals("")) return;

      assignSubPledgeLeader(player, cmdParams, cmdParams2);
    }
    else if (actualCommand.equalsIgnoreCase("create_ally"))
    {
      if (cmdParams.equals("")) return;

      if (!player.isClanLeader())
      {
        player.sendPacket(new SystemMessage(SystemMessageId.ONLY_CLAN_LEADER_CREATE_ALLIANCE));
        return;
      }
      player.getClan().createAlly(player, cmdParams);
    }
    else if (actualCommand.equalsIgnoreCase("dissolve_ally"))
    {
      if (!player.isClanLeader())
      {
        player.sendPacket(new SystemMessage(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER));
        return;
      }
      player.getClan().dissolveAlly(player);
    }
    else if (actualCommand.equalsIgnoreCase("dissolve_clan"))
    {
      dissolveClan(player, player.getClanId());
    }
    else if (actualCommand.equalsIgnoreCase("change_clan_leader"))
    {
      if (cmdParams.equals("")) return;
      Ride dismount = new Ride(player.getObjectId(), 0, 0);
      player.sendPacket(dismount);
      player.broadcastPacket(dismount);
      player.setMountType(dismount.getMountType());
      changeClanLeader(player, cmdParams);
    }
    else if (actualCommand.equalsIgnoreCase("recover_clan"))
    {
      recoverClan(player, player.getClanId());
    }
    else if (actualCommand.equalsIgnoreCase("increase_clan_level"))
    {
      if (!player.isClanLeader())
      {
        player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
        return;
      }
      player.getClan().levelUpClan(player);
    }
    else if (actualCommand.equalsIgnoreCase("learn_clan_skills"))
    {
      showPledgeSkillList(player);
    }
    else if (command.startsWith("Subclass"))
    {
      int cmdChoice = Integer.parseInt(command.substring(9, 10).trim());

      if ((player.isCastingNow()) || (player.isAllSkillsDisabled()))
      {
        player.sendPacket(new SystemMessage(SystemMessageId.SUBCLASS_NO_CHANGE_OR_CREATE_WHILE_SKILL_IN_USE));
        return;
      }

      TextBuilder content = new TextBuilder("<html><body>");
      NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

      int paramOne = 0;
      int paramTwo = 0;
      try
      {
        int endIndex = command.length();

        if (command.length() > 13)
        {
          endIndex = 13;
          paramTwo = Integer.parseInt(command.substring(13).trim());
        }

        paramOne = Integer.parseInt(command.substring(11, endIndex).trim());
      }
      catch (Exception NumberFormatException)
      {
      }

      L2Weapon weapon = player.getActiveWeaponItem();
      Set subsAvailable;
      Iterator subList;
      switch (cmdChoice)
      {
      case 1:
        if ((Olympiad.getInstance().isRegisteredInComp(player)) || (player.getOlympiadGameId() > 0))
        {
          player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT));

          return;
        }
        if (player.getTotalSubClasses() == Config.MAX_SUBCLASS)
        {
          player.sendMessage("You can now only change one of your current sub classes.");
          return;
        }

        subsAvailable = getAvailableSubClasses(player);

        if ((subsAvailable != null) && (!subsAvailable.isEmpty()))
        {
          content.append("Add Subclass:<br>Which sub class do you wish to add?<br>");

          for (PlayerClass subClass : subsAvailable) {
            content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 4 " + subClass.ordinal() + "\" msg=\"1268;" + formatClassForDisplay(subClass) + "\">" + formatClassForDisplay(subClass) + "</a><br>");
          }

        }
        else
        {
          player.sendMessage("There are no sub classes available at this time.");
          return;
        }

      case 2:
        if ((Olympiad.getInstance().isRegisteredInComp(player)) || (player.getOlympiadGameId() > 0))
        {
          player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT));

          return;
        }
        content.append("Change Subclass:<br>");

        int baseClassId = player.getBaseClass();

        if (player.getSubClasses().isEmpty())
        {
          content.append("You can't change sub classes when you don't have a sub class to begin with.<br><a action=\"bypass -h npc_" + getObjectId() + "_Subclass 1\">Add subclass.</a>");
        }
        else
        {
          content.append("Which class would you like to switch to?<br>");

          if (baseClassId == player.getActiveClass()) content.append(CharTemplateTable.getClassNameById(baseClassId) + "&nbsp;<font color=\"LEVEL\">(Base Class)</font><br><br>");
          else {
            content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 5 0\">" + CharTemplateTable.getClassNameById(baseClassId) + "</a>&nbsp;" + "<font color=\"LEVEL\">(Base Class)</font><br><br>");
          }

          for (subList = iterSubClasses(player); subList.hasNext(); )
          {
            SubClass subClass = (SubClass)subList.next();
            int subClassId = subClass.getClassId();

            if (subClassId == player.getActiveClass()) content.append(CharTemplateTable.getClassNameById(subClassId) + "<br>");
            else {
              content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 5 " + subClass.getClassIndex() + "\">" + CharTemplateTable.getClassNameById(subClassId) + "</a><br>");
            }
          }
        }

        break;
      case 3:
        if ((Olympiad.getInstance().isRegisteredInComp(player)) || (player.getOlympiadGameId() > 0))
        {
          player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT));

          return;
        }
        content.append("Change Subclass:<br>Which of the following sub classes would you like to change?<br>");
        int classIndex = 1;

        for (Iterator subList = iterSubClasses(player); subList.hasNext(); )
        {
          SubClass subClass = (SubClass)subList.next();

          content.append("Sub-class " + classIndex + "<br1>");
          content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 6 " + subClass.getClassIndex() + "\">" + CharTemplateTable.getClassNameById(subClass.getClassId()) + "</a><br>");

          classIndex++;
        }

        content.append("<br>If you change a sub class, you'll start at level 40 after the 2nd class transfer.");
        break;
      case 4:
        boolean allowAddition = true;
        if (player.getLevel() < 75)
        {
          player.sendMessage("You may not add a new sub class before you are level 75 on your previous class.");
          allowAddition = false;
        }

        if (player._inEventCTF)
        {
          player.sendMessage("You can't add a subclass while in an event.");
          return;
        }

        if ((Olympiad.getInstance().isRegisteredInComp(player)) || (player.getOlympiadGameId() > 0))
        {
          player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT));

          return;
        }
        if (player.isCursedWeaponEquiped())
          return;
        Iterator subList;
        if (allowAddition)
        {
          if (!player.getSubClasses().isEmpty())
          {
            for (subList = iterSubClasses(player); subList.hasNext(); )
            {
              SubClass subClass = (SubClass)subList.next();

              if (subClass.getLevel() < 75)
              {
                player.sendMessage("You may not add a new sub class before you are level 75 on your previous sub class.");
                allowAddition = false;
                break;
              }
            }
          }
        }
        if (!Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS)
        {
          QuestState qs = player.getQuestState("235_MimirsElixir");
          if ((qs == null) || (!qs.isCompleted()))
          {
            player.sendMessage("You must have completed the Mimir's Elixir quest to continue adding your sub class.");
            return;
          }
          qs = player.getQuestState("234_FatesWhisper");
          if ((qs == null) || (!qs.isCompleted()))
          {
            player.sendMessage("You must have completed the Fate's Whisper quest to continue adding your sub class.");
            return;
          }
        }

        if (allowAddition)
        {
          String className = CharTemplateTable.getClassNameById(paramOne);

          if (!player.addSubClass(paramOne, player.getTotalSubClasses() + 1))
          {
            player.sendMessage("The sub class could not be added.");
            return;
          }
          removeWeapon(player);
          player.setActiveClass(player.getTotalSubClasses());
          player.broadcastUserInfo();
          player.abortCast();
          player.stopAllEffects();

          content.append("Add Subclass:<br>The sub class of <font color=\"LEVEL\">" + className + "</font> has been added.");

          player.sendPacket(new SystemMessage(SystemMessageId.CLASS_TRANSFER));
        }
        else
        {
          html.setFile("data/html/villagemaster/SubClass_Fail.htm");
        }
        break;
      case 5:
        if (!FloodProtector.getInstance().tryPerformAction(player.getObjectId(), 6))
        {
          player.sendMessage("Sorry for the inconvenience, but you'll have to wait a bit, too often you change your class.");
          return;
        }
        if (player._inEventCTF)
        {
          player.sendMessage("You can't add a subclass while in an event.");
          return;
        }
        if ((Olympiad.getInstance().isRegisteredInComp(player)) || (player.getOlympiadGameId() > 0))
        {
          player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT));

          return;
        }
        if (player.isCursedWeaponEquiped())
          return;
        removeWeapon(player);
        player.setActiveClass(paramOne);
        player.broadcastUserInfo();
        player.abortCast();
        player.stopAllEffects();

        content.append("Change Subclass:<br>Your active sub class is now a <font color=\"LEVEL\">" + CharTemplateTable.getClassNameById(player.getActiveClass()) + "</font>.");

        player.sendPacket(new SystemMessage(SystemMessageId.SUBCLASS_TRANSFER_COMPLETED));
        if ((Config.CHECK_SKILLS_ON_ENTER) && (!Config.ALT_GAME_SKILL_LEARN)) player.checkAllowedSkills();
        CoupleManager.getInstance().checkCouple(player);
        break;
      case 6:
        if ((Olympiad.getInstance().isRegisteredInComp(player)) || (player.getOlympiadGameId() > 0))
        {
          player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT));

          return;
        }
        if (player.isCursedWeaponEquiped())
          return;
        content.append("Please choose a sub class to change to. If the one you are looking for is not here, please seek out the appropriate master for that class.<br><font color=\"LEVEL\">Warning!</font> All classes and skills for this class will be removed.<br><br>");

        subsAvailable = getAvailableSubClasses(player);

        if ((subsAvailable != null) && (!subsAvailable.isEmpty()))
        {
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
        if (!FloodProtector.getInstance().tryPerformAction(player.getObjectId(), 6))
        {
          player.sendMessage("Sorry for the inconvenience, but you'll have to wait a bit, too often you change your class.");
          return;
        }
        if ((Olympiad.getInstance().isRegisteredInComp(player)) || (player.getOlympiadGameId() > 0))
        {
          player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT));

          return;
        }
        if (player.isCursedWeaponEquiped())
          return;
        if (player.modifySubClass(paramOne, paramTwo))
        {
          removeWeapon(player);
          player.setActiveClass(paramOne);
          player.broadcastUserInfo();
          player.abortCast();
          player.stopAllEffects();

          content.append("Change Subclass:<br>Your sub class has been changed to <font color=\"LEVEL\">" + CharTemplateTable.getClassNameById(paramTwo) + "</font>.");

          player.sendPacket(new SystemMessage(SystemMessageId.ADD_NEW_SUBCLASS));
          if ((Config.CHECK_SKILLS_ON_ENTER) && (!Config.ALT_GAME_SKILL_LEARN)) player.checkAllowedSkills();
          CoupleManager.getInstance().checkCouple(player);
        }
        else
        {
          removeWeapon(player);
          player.setActiveClass(0);
          player.broadcastUserInfo();
          player.abortCast();
          player.stopAllEffects();

          player.sendMessage("The sub class could not be added, you have been reverted to your base class.");
          return;
        }

      }

      content.append("</body></html>");

      if (content.length() > 26) html.setHtml(content.toString());

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

    if (val == 0) pom = "" + npcId; else {
      pom = npcId + "-" + val;
    }
    return "data/html/villagemaster/" + pom + ".htm";
  }

  public void dissolveClan(L2PcInstance player, int clanId)
  {
    if (Config.DEBUG) {
      _log.fine(player.getObjectId() + "(" + player.getName() + ") requested dissolve a clan from " + getObjectId() + "(" + getName() + ")");
    }

    if (!player.isClanLeader())
    {
      player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
      return;
    }
    L2Clan clan = player.getClan();
    if (clan.getAllyId() != 0)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISPERSE_THE_CLANS_IN_ALLY));
      return;
    }
    if (clan.isAtWar() != 0)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_WAR));
      return;
    }
    if ((clan.getHasCastle() != 0) || (clan.getHasHideout() != 0))
    {
      player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISSOLVE_WHILE_OWNING_CLAN_HALL_OR_CASTLE));
      return;
    }
    for (Castle castle : CastleManager.getInstance().getCastles())
    {
      if (SiegeManager.getInstance().checkIsRegistered(clan, castle.getCastleId()))
      {
        player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISSOLVE_CAUSE_CLAN_WILL_PARTICIPATE_IN_CASTLE_SIEGE));
        return;
      }
    }
    if (player.isInsideZone(4))
    {
      player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_SIEGE));
      return;
    }
    if (clan.getDissolvingExpiryTime() > System.currentTimeMillis())
    {
      player.sendPacket(new SystemMessage(SystemMessageId.DISSOLUTION_IN_PROGRESS));
      return;
    }

    clan.setDissolvingExpiryTime(System.currentTimeMillis() + Config.ALT_CLAN_DISSOLVE_DAYS * 86400000L);
    clan.updateClanInDB();

    ClanTable.getInstance().scheduleRemoveClan(clan.getClanId());

    player.deathPenalty(false);
  }

  public void recoverClan(L2PcInstance player, int clanId)
  {
    if (Config.DEBUG) {
      _log.fine(player.getObjectId() + "(" + player.getName() + ") requested recover a clan from " + getObjectId() + "(" + getName() + ")");
    }

    if (!player.isClanLeader())
    {
      player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
      return;
    }
    L2Clan clan = player.getClan();

    clan.setDissolvingExpiryTime(0L);
    clan.updateClanInDB();
  }

  public void changeClanLeader(L2PcInstance player, String target)
  {
    if (Config.DEBUG) {
      _log.fine(player.getObjectId() + "(" + player.getName() + ") requested change a clan leader from " + getObjectId() + "(" + getName() + ")");
    }

    if (!player.isClanLeader())
    {
      player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
      return;
    }
    if (player.getName().equalsIgnoreCase(target))
    {
      return;
    }
    L2Clan clan = player.getClan();

    L2ClanMember member = clan.getClanMember(target);
    if (member == null)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_DOES_NOT_EXIST);
      sm.addString(target);
      player.sendPacket(sm);
      sm = null;
      return;
    }
    if (!member.isOnline())
    {
      player.sendPacket(new SystemMessage(SystemMessageId.INVITED_USER_NOT_ONLINE));
      return;
    }

    for (Siege siege : SiegeManager.getInstance().getSieges())
    {
      if ((siege.getIsInProgress()) && ((siege.checkIsAttacker(player.getClan())) || (siege.checkIsDefender(player.getClan()))))
      {
        player.sendMessage("\u0432\u043E \u0432\u0440\u0435\u043C\u044F \u043E\u0441\u0430\u0434\u044B \u043D\u0435\u043B\u044C\u0437\u044F");
        return;
      }
    }
    for (FortSiege fortsiege : FortSiegeManager.getInstance().getSieges())
    {
      if ((fortsiege.getIsInProgress()) && ((fortsiege.checkIsAttacker(player.getClan())) || (fortsiege.checkIsDefender(player.getClan()))))
      {
        player.sendMessage("\u0432\u043E \u0432\u0440\u0435\u043C\u044F \u043E\u0441\u0430\u0434\u044B \u043D\u0435\u043B\u044C\u0437\u044F");
        return;
      }
    }
    clan.setNewLeader(member, player);
  }

  public void createSubPledge(L2PcInstance player, String clanName, String leaderName, int pledgeType, int minClanLvl)
  {
    if (Config.DEBUG) {
      _log.fine(player.getObjectId() + "(" + player.getName() + ") requested sub clan creation from " + getObjectId() + "(" + getName() + ")");
    }

    if (!player.isClanLeader())
    {
      player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
      return;
    }

    L2Clan clan = player.getClan();
    if (clan.getLevel() < minClanLvl)
    {
      if (pledgeType == -1)
      {
        player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN_ACADEMY));
      }
      else
      {
        player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_MILITARY_UNIT));
      }
      return;
    }
    if ((!Util.isAlphaNumeric(clanName)) || (2 > clanName.length()))
    {
      player.sendPacket(new SystemMessage(SystemMessageId.CLAN_NAME_INCORRECT));
      return;
    }
    if (clanName.length() > 16)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.CLAN_NAME_TOO_LONG));
      return;
    }
    for (L2Clan tempClan : ClanTable.getInstance().getClans())
    {
      if (tempClan.getSubPledge(clanName) == null)
        continue;
      if (pledgeType == -1)
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_ALREADY_EXISTS);
        sm.addString(clanName);
        player.sendPacket(sm);
        sm = null;
      }
      else
      {
        player.sendPacket(new SystemMessage(SystemMessageId.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME));
      }
      return;
    }

    if ((pledgeType != -1) && (
      (clan.getClanMember(leaderName) == null) || (clan.getClanMember(leaderName).getPledgeType() != 0)))
    {
      if (pledgeType >= 1001)
      {
        player.sendPacket(new SystemMessage(SystemMessageId.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED));
      }
      else if (pledgeType >= 100)
      {
        player.sendPacket(new SystemMessage(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED));
      }
      return;
    }

    if (clan.createSubPledge(player, pledgeType, leaderName, clanName) == null)
      return;
    SystemMessage sm;
    if (pledgeType == -1)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.THE_S1S_CLAN_ACADEMY_HAS_BEEN_CREATED);
      sm.addString(player.getClan().getName());
    }
    else if (pledgeType >= 1001)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.THE_KNIGHTS_OF_S1_HAVE_BEEN_CREATED);
      sm.addString(player.getClan().getName());
    }
    else if (pledgeType >= 100)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.THE_ROYAL_GUARD_OF_S1_HAVE_BEEN_CREATED);
      sm.addString(player.getClan().getName());
    }
    else {
      sm = new SystemMessage(SystemMessageId.CLAN_CREATED);
    }
    player.sendPacket(sm);
    if (pledgeType != -1)
    {
      L2ClanMember leaderSubPledge = clan.getClanMember(leaderName);
      if (leaderSubPledge.getPlayerInstance() == null) return;
      leaderSubPledge.getPlayerInstance().setPledgeClass(leaderSubPledge.calculatePledgeClass(leaderSubPledge.getPlayerInstance()));
      leaderSubPledge.getPlayerInstance().sendPacket(new UserInfo(leaderSubPledge.getPlayerInstance()));
    }
  }

  public void assignSubPledgeLeader(L2PcInstance player, String clanName, String leaderName)
  {
    if (Config.DEBUG) {
      _log.fine(player.getObjectId() + "(" + player.getName() + ") requested to assign sub clan" + clanName + "leader " + "(" + leaderName + ")");
    }

    if (!player.isClanLeader())
    {
      player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
      return;
    }

    if (leaderName.length() > 16)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.NAMING_CHARNAME_UP_TO_16CHARS));
      return;
    }

    if (player.getName().equals(leaderName))
    {
      player.sendPacket(new SystemMessage(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED));
      return;
    }

    L2Clan clan = player.getClan();
    L2Clan.SubPledge subPledge = player.getClan().getSubPledge(clanName);

    if (null == subPledge)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.CLAN_NAME_INCORRECT));
      return;
    }
    if (subPledge.getId() == -1)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.CLAN_NAME_INCORRECT));
      return;
    }

    if ((clan.getClanMember(leaderName) == null) || (clan.getClanMember(leaderName).getPledgeType() != 0))
    {
      if (subPledge.getId() >= 1001)
      {
        player.sendPacket(new SystemMessage(SystemMessageId.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED));
      }
      else if (subPledge.getId() >= 100)
      {
        player.sendPacket(new SystemMessage(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED));
      }
      return;
    }

    subPledge.setLeaderName(leaderName);
    clan.updateSubPledgeInDB(subPledge.getId());
    L2ClanMember leaderSubPledge = clan.getClanMember(leaderName);
    leaderSubPledge.getPlayerInstance().setPledgeClass(leaderSubPledge.calculatePledgeClass(leaderSubPledge.getPlayerInstance()));
    leaderSubPledge.getPlayerInstance().sendPacket(new UserInfo(leaderSubPledge.getPlayerInstance()));
    clan.broadcastClanStatus();
    SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_SELECTED_AS_CAPTAIN_OF_S2);
    sm.addString(leaderName);
    sm.addString(clanName);
    clan.broadcastToOnlineMembers(sm);
    sm = null;
  }

  private final Set<PlayerClass> getAvailableSubClasses(L2PcInstance player)
  {
    int charClassId = player.getBaseClass();

    if (charClassId >= 88) charClassId = player.getClassId().getParent().ordinal();

    PlayerRace npcRace = getVillageMasterRace();
    ClassType npcTeachType = getVillageMasterTeachType();

    PlayerClass currClass = PlayerClass.values()[charClassId];

    Set availSubs = currClass.getAvailableSubclasses(player);

    if (availSubs != null)
    {
      for (PlayerClass availSub : availSubs)
      {
        for (Iterator subList = iterSubClasses(player); subList.hasNext(); )
        {
          SubClass prevSubClass = (SubClass)subList.next();
          int subClassId = prevSubClass.getClassId();
          if (subClassId >= 88) subClassId = ClassId.values()[subClassId].getParent().getId();

          if ((availSub.ordinal() == subClassId) || (availSub.ordinal() == player.getBaseClass()))
          {
            availSubs.remove(PlayerClass.values()[availSub.ordinal()]);
          }
        }
        if ((npcRace == PlayerRace.Human) || (npcRace == PlayerRace.LightElf))
        {
          if (!availSub.isOfType(npcTeachType)) availSubs.remove(availSub);
          else if ((!availSub.isOfRace(PlayerRace.Human)) && (!availSub.isOfRace(PlayerRace.LightElf))) {
            availSubs.remove(availSub);
          }

        }
        else if ((npcRace != PlayerRace.Human) && (npcRace != PlayerRace.LightElf) && (!availSub.isOfRace(npcRace))) {
          availSubs.remove(availSub);
        }
      }
    }

    return availSubs;
  }

  public void showPledgeSkillList(L2PcInstance player)
  {
    if (Config.DEBUG)
      _log.fine("PledgeSkillList activated on: " + getObjectId());
    if (player.getClan() == null) return;

    L2PledgeSkillLearn[] skills = SkillTreeTable.getInstance().getAvailablePledgeSkills(player);
    AquireSkillList asl = new AquireSkillList(AquireSkillList.skillType.Clan);
    int counts = 0;

    for (L2PledgeSkillLearn s : skills)
    {
      int cost = s.getRepCost();
      counts++;

      asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
    }

    if (counts == 0)
    {
      NpcHtmlMessage html = new NpcHtmlMessage(1);

      if (player.getClan().getLevel() < 8)
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN);
        sm.addNumber(player.getClan().getLevel() + 1);
        player.sendPacket(sm);
      }
      else
      {
        TextBuilder sb = new TextBuilder();
        sb.append("<html><body>");
        sb.append("You've learned all skills available for your Clan.<br>");
        sb.append("</body></html>");
        html.setHtml(sb.toString());
        player.sendPacket(html);
      }
    }
    else
    {
      player.sendPacket(asl);
    }

    player.sendPacket(new ActionFailed());
  }

  private final String formatClassForDisplay(PlayerClass className)
  {
    String classNameStr = className.toString();
    char[] charArray = classNameStr.toCharArray();

    for (int i = 1; i < charArray.length; i++) {
      if (Character.isUpperCase(charArray[i]))
        classNameStr = classNameStr.substring(0, i) + " " + classNameStr.substring(i);
    }
    return classNameStr;
  }

  private final PlayerRace getVillageMasterRace()
  {
    String npcClass = getTemplate().getStatsSet().getString("jClass").toLowerCase();

    if (npcClass.indexOf("human") > -1) return PlayerRace.Human;

    if (npcClass.indexOf("darkelf") > -1) return PlayerRace.DarkElf;

    if (npcClass.indexOf("elf") > -1) return PlayerRace.LightElf;

    if (npcClass.indexOf("orc") > -1) return PlayerRace.Orc;

    return PlayerRace.Dwarf;
  }

  private final ClassType getVillageMasterTeachType()
  {
    String npcClass = getTemplate().getStatsSet().getString("jClass");

    if ((npcClass.indexOf("sanctuary") > -1) || (npcClass.indexOf("clergyman") > -1)) {
      return ClassType.Priest;
    }
    if ((npcClass.indexOf("mageguild") > -1) || (npcClass.indexOf("patriarch") > -1)) {
      return ClassType.Mystic;
    }
    return ClassType.Fighter;
  }

  private Iterator<SubClass> iterSubClasses(L2PcInstance player)
  {
    return player.getSubClasses().values().iterator();
  }

  public void removeWeapon(L2PcInstance player)
  {
    L2ItemInstance wpn = player.getInventory().getPaperdollItem(7);
    if (wpn == null) wpn = player.getInventory().getPaperdollItem(14);
    if (wpn != null)
    {
      L2ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
      InventoryUpdate iu = new InventoryUpdate();
      for (int i = 0; i < unequiped.length; i++)
        iu.addModifiedItem(unequiped[i]);
      player.sendPacket(iu);
      player.abortAttack();
      player.broadcastUserInfo();

      if (unequiped.length > 0)
      {
        if (unequiped[0].isWear())
          return;
        SystemMessage sm = null;
        if (unequiped[0].getEnchantLevel() > 0) {
          sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
          sm.addNumber(unequiped[0].getEnchantLevel());
          sm.addItemName(unequiped[0].getItemId());
        } else {
          sm = new SystemMessage(SystemMessageId.S1_DISARMED);
          sm.addItemName(unequiped[0].getItemId());
        }
        player.sendPacket(sm);
      }
    }
  }
}