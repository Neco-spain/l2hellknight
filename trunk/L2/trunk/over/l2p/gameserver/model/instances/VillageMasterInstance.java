package l2p.gameserver.model.instances;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.SubClass;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.base.ClassType;
import l2p.gameserver.model.base.PlayerClass;
import l2p.gameserver.model.base.Race;
import l2p.gameserver.model.entity.events.impl.SiegeEvent;
import l2p.gameserver.model.entity.olympiad.Olympiad;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.model.entity.residence.Dominion;
import l2p.gameserver.model.entity.residence.Residence;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.pledge.Alliance;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.pledge.SubUnit;
import l2p.gameserver.model.pledge.UnitMember;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.serverpackets.PledgeReceiveSubPledgeCreated;
import l2p.gameserver.serverpackets.PledgeShowInfoUpdate;
import l2p.gameserver.serverpackets.PledgeShowMemberListUpdate;
import l2p.gameserver.serverpackets.PledgeStatusChanged;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.tables.ClanTable;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.CertificationFunctions;
import l2p.gameserver.utils.HtmlUtils;
import l2p.gameserver.utils.SiegeUtils;
import l2p.gameserver.utils.Util;

public final class VillageMasterInstance extends NpcInstance
{
  public VillageMasterInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(Player player, String command)
  {
    if (!canBypassCheck(player, this)) {
      return;
    }
    if ((command.startsWith("create_clan")) && (command.length() > 12))
    {
      String val = command.substring(12);
      createClan(player, val);
    }
    else if ((command.startsWith("create_academy")) && (command.length() > 15))
    {
      String sub = command.substring(15, command.length());
      createSubPledge(player, sub, -1, 5, "");
    }
    else if ((command.startsWith("create_royal")) && (command.length() > 15))
    {
      String[] sub = command.substring(13, command.length()).split(" ", 2);
      if (sub.length == 2)
        createSubPledge(player, sub[1], 100, 6, sub[0]);
    }
    else if ((command.startsWith("create_knight")) && (command.length() > 16))
    {
      String[] sub = command.substring(14, command.length()).split(" ", 2);
      if (sub.length == 2)
        createSubPledge(player, sub[1], 1001, 7, sub[0]);
    }
    else if ((command.startsWith("assign_subpl_leader")) && (command.length() > 22))
    {
      String[] sub = command.substring(20, command.length()).split(" ", 2);
      if (sub.length == 2)
        assignSubPledgeLeader(player, sub[1], sub[0]);
    }
    else if ((command.startsWith("assign_new_clan_leader")) && (command.length() > 23))
    {
      String val = command.substring(23);
      setLeader(player, val);
    }
    if ((command.startsWith("create_ally")) && (command.length() > 12))
    {
      String val = command.substring(12);
      createAlly(player, val);
    }
    else if (command.startsWith("dissolve_ally")) {
      dissolveAlly(player);
    } else if (command.startsWith("dissolve_clan")) {
      dissolveClan(player);
    } else if (command.startsWith("increase_clan_level")) {
      levelUpClan(player);
    } else if (command.startsWith("learn_clan_skills")) {
      showClanSkillList(player);
    } else if (command.startsWith("ShowCouponExchange"))
    {
      if ((Functions.getItemCount(player, 8869) > 0L) || (Functions.getItemCount(player, 8870) > 0L))
        command = "Multisell 800";
      else
        command = "Link villagemaster/reflect_weapon_master_noticket.htm";
      super.onBypassFeedback(player, command);
    }
    else if (command.equalsIgnoreCase("CertificationList"))
    {
      CertificationFunctions.showCertificationList(this, player);
    }
    else if (command.equalsIgnoreCase("GetCertification65"))
    {
      CertificationFunctions.getCertification65(this, player);
    }
    else if (command.equalsIgnoreCase("GetCertification70"))
    {
      CertificationFunctions.getCertification70(this, player);
    }
    else if (command.equalsIgnoreCase("GetCertification80"))
    {
      CertificationFunctions.getCertification80(this, player);
    }
    else if (command.equalsIgnoreCase("GetCertification75List"))
    {
      CertificationFunctions.getCertification75List(this, player);
    }
    else if (command.equalsIgnoreCase("GetCertification75C"))
    {
      CertificationFunctions.getCertification75(this, player, true);
    }
    else if (command.equalsIgnoreCase("GetCertification75M"))
    {
      CertificationFunctions.getCertification75(this, player, false);
    }
    else if (command.startsWith("Subclass"))
    {
      if (player.getPet() != null)
      {
        player.sendPacket(SystemMsg.A_SUBCLASS_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SERVITOR_OR_PET_IS_SUMMONED);
        return;
      }

      if ((player.isActionsDisabled()) || (player.getTransformation() != 0))
      {
        player.sendPacket(SystemMsg.SUBCLASSES_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SKILL_IS_IN_USE);
        return;
      }

      if (player.getWeightPenalty() >= 3)
      {
        player.sendPacket(SystemMsg.A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_WHILE_YOU_ARE_OVER_YOUR_WEIGHT_LIMIT);
        return;
      }

      if (player.getInventoryLimit() * 0.8D < player.getInventory().getSize())
      {
        player.sendPacket(SystemMsg.A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_BECAUSE_YOU_HAVE_EXCEEDED_YOUR_INVENTORY_LIMIT);
        return;
      }

      StringBuilder content = new StringBuilder("<html><body>");
      NpcHtmlMessage html = new NpcHtmlMessage(player, this);

      Map playerClassList = player.getSubClasses();

      if (player.getLevel() < 40)
      {
        content.append("You must be level 40 or more to operate with your sub-classes.");
        content.append("</body></html>");
        html.setHtml(content.toString());
        player.sendPacket(html);
        return;
      }

      int classId = 0;
      int newClassId = 0;
      int intVal = 0;
      try
      {
        for (String id : command.substring(9, command.length()).split(" "))
        {
          if (intVal == 0)
          {
            intVal = Integer.parseInt(id);
          }
          else if (classId > 0)
          {
            newClassId = Integer.parseInt(id);
          }
          else
            classId = Integer.parseInt(id);
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      Set subsAvailable;
      switch (intVal)
      {
      case 1:
        subsAvailable = getAvailableSubClasses(player, true);

        if ((subsAvailable != null) && (!subsAvailable.isEmpty()))
        {
          content.append("Add Subclass:<br>Which subclass do you wish to add?<br>");

          for (PlayerClass subClass : subsAvailable)
            content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 4 ").append(subClass.ordinal()).append("\">").append(HtmlUtils.htmlClassName(subClass.ordinal())).append("</a><br>");
        }
        else
        {
          player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.NoSubAtThisTime", player, new Object[0]));
          return;
        }

      case 2:
        content.append("Change Subclass:<br>");

        int baseClassId = player.getBaseClassId();

        if (playerClassList.size() < 2) {
          content.append("You can't change subclasses when you don't have a subclass to begin with.<br><a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 1\">Add subclass.</a>");
        }
        else {
          content.append("Which class would you like to switch to?<br>");

          if (baseClassId == player.getActiveClassId())
            content.append(HtmlUtils.htmlClassName(baseClassId)).append(" <font color=\"LEVEL\">(Base Class)</font><br><br>");
          else {
            content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 5 ").append(baseClassId).append("\">").append(HtmlUtils.htmlClassName(baseClassId)).append("</a> <font color=\"LEVEL\">(Base Class)</font><br><br>");
          }
          for (SubClass subClass : playerClassList.values())
          {
            if (subClass.isBase())
              continue;
            int subClassId = subClass.getClassId();

            if (subClassId == player.getActiveClassId())
              content.append(HtmlUtils.htmlClassName(subClassId)).append("<br>");
            else
              content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 5 ").append(subClassId).append("\">").append(HtmlUtils.htmlClassName(subClassId)).append("</a><br>");
          }
        }
        break;
      case 3:
        content.append("Change Subclass:<br>Which of the following sub-classes would you like to change?<br>");

        for (SubClass sub : playerClassList.values())
        {
          content.append("<br>");
          if (!sub.isBase()) {
            content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 6 ").append(sub.getClassId()).append("\">").append(HtmlUtils.htmlClassName(sub.getClassId())).append("</a><br>");
          }
        }
        content.append("<br>If you change a sub-class, you'll start at level 40 after the 2nd class transfer.");
        break;
      case 4:
        boolean allowAddition = true;

        if (player.getLevel() < Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS)
        {
          player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.NoSubBeforeLevel", player, new Object[0]).addNumber(Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS));
          allowAddition = false;
        }

        if (!playerClassList.isEmpty()) {
          for (SubClass subClass : playerClassList.values())
            if (subClass.getLevel() < Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS)
            {
              player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.NoSubBeforeLevel", player, new Object[0]).addNumber(Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS));
              allowAddition = false;
              break;
            }
        }
        if ((Config.ENABLE_OLYMPIAD) && (Olympiad.isRegisteredInComp(player)))
        {
          player.sendPacket(Msg.YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER);
          return;
        }

        if ((!Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS) && (!playerClassList.isEmpty()) && (playerClassList.size() < 2 + Config.ALT_GAME_SUB_ADD)) {
          if (player.isQuestCompleted("_234_FatesWhisper"))
          {
            if (player.getRace() == Race.kamael)
            {
              allowAddition = player.isQuestCompleted("_236_SeedsOfChaos");
              if (!allowAddition)
                player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.QuestSeedsOfChaos", player, new Object[0]));
            }
            else
            {
              allowAddition = player.isQuestCompleted("_235_MimirsElixir");
              if (!allowAddition)
                player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.QuestMimirsElixir", player, new Object[0]));
            }
          }
          else
          {
            player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.QuestFatesWhisper", player, new Object[0]));
            allowAddition = false;
          }
        }
        if (allowAddition)
        {
          if (!player.addSubClass(classId, true, 0))
          {
            player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.SubclassCouldNotBeAdded", player, new Object[0]));
            return;
          }

          content.append("Add Subclass:<br>The subclass of <font color=\"LEVEL\">").append(HtmlUtils.htmlClassName(classId)).append("</font> has been added.");
          player.sendPacket(SystemMsg.THE_NEW_SUBCLASS_HAS_BEEN_ADDED);
        }
        else {
          html.setFile("villagemaster/SubClass_Fail.htm");
        }break;
      case 5:
        if ((Config.ENABLE_OLYMPIAD) && (Olympiad.isRegisteredInComp(player)))
        {
          player.sendPacket(Msg.YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER);
          return;
        }

        player.setActiveSubClass(classId, true);

        content.append("Change Subclass:<br>Your active subclass is now a <font color=\"LEVEL\">").append(HtmlUtils.htmlClassName(player.getActiveClassId())).append("</font>.");

        player.sendPacket(SystemMsg.YOU_HAVE_SUCCESSFULLY_SWITCHED_TO_YOUR_SUBCLASS);

        break;
      case 6:
        content.append("Please choose a subclass to change to. If the one you are looking for is not here, please seek out the appropriate master for that class.<br><font color=\"LEVEL\">Warning!</font> All classes and skills for this class will be removed.<br><br>");

        subsAvailable = getAvailableSubClasses(player, false);

        if (!subsAvailable.isEmpty()) {
          for (PlayerClass subClass : subsAvailable)
            content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 7 ").append(classId).append(" ").append(subClass.ordinal()).append("\">").append(HtmlUtils.htmlClassName(subClass.ordinal())).append("</a><br>");
        }
        else {
          player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.NoSubAtThisTime", player, new Object[0]));
          return;
        }

      case 7:
        if ((Config.ENABLE_OLYMPIAD) && (Olympiad.isRegisteredInComp(player)))
        {
          player.sendPacket(Msg.YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER);
          return;
        }

        if (player.modifySubClass(classId, newClassId))
        {
          content.append("Change Subclass:<br>Your subclass has been changed to <font color=\"LEVEL\">").append(HtmlUtils.htmlClassName(newClassId)).append("</font>.");
          player.sendPacket(SystemMsg.THE_NEW_SUBCLASS_HAS_BEEN_ADDED);
        }
        else
        {
          player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.SubclassCouldNotBeAdded", player, new Object[0]));
          return;
        }
      }

      content.append("</body></html>");

      if (content.length() > 26) {
        html.setHtml(content.toString());
      }
      player.sendPacket(html);
    }
    else {
      super.onBypassFeedback(player, command);
    }
  }

  public String getHtmlPath(int npcId, int val, Player player)
  {
    String pom;
    String pom;
    if (val == 0)
      pom = new StringBuilder().append("").append(npcId).toString();
    else {
      pom = new StringBuilder().append(npcId).append("-").append(val).toString();
    }
    return new StringBuilder().append("villagemaster/").append(pom).append(".htm").toString();
  }

  public void createClan(Player player, String clanName)
  {
    if (player.getLevel() < 10)
    {
      player.sendPacket(Msg.YOU_ARE_NOT_QUALIFIED_TO_CREATE_A_CLAN);
      return;
    }

    if (player.getClanId() != 0)
    {
      player.sendPacket(Msg.YOU_HAVE_FAILED_TO_CREATE_A_CLAN);
      return;
    }

    if (!player.canCreateClan())
    {
      player.sendPacket(Msg.YOU_MUST_WAIT_10_DAYS_BEFORE_CREATING_A_NEW_CLAN);
      return;
    }
    if (clanName.length() > 16)
    {
      player.sendPacket(Msg.CLAN_NAMES_LENGTH_IS_INCORRECT);
      return;
    }
    if (!Util.isMatchingRegexp(clanName, Config.CLAN_NAME_TEMPLATE))
    {
      player.sendPacket(Msg.CLAN_NAME_IS_INCORRECT);
      return;
    }

    Clan clan = ClanTable.getInstance().createClan(player, clanName);
    if (clan == null)
    {
      player.sendPacket(Msg.THIS_NAME_ALREADY_EXISTS);
      return;
    }

    player.sendPacket(clan.listAll());
    player.sendPacket(new IStaticPacket[] { new PledgeShowInfoUpdate(clan), Msg.CLAN_HAS_BEEN_CREATED });
    player.updatePledgeClass();
    player.broadcastCharInfo();
  }

  public void setLeader(Player leader, String newLeader)
  {
    if (!leader.isClanLeader())
    {
      leader.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
      return;
    }

    if (leader.getEvent(SiegeEvent.class) != null)
    {
      leader.sendMessage(new CustomMessage("scripts.services.Rename.SiegeNow", leader, new Object[0]));
      return;
    }

    Clan clan = leader.getClan();
    SubUnit mainUnit = clan.getSubUnit(0);
    UnitMember member = mainUnit.getUnitMember(newLeader);

    if (member == null)
    {
      showChatWindow(leader, "villagemaster/clan-20.htm", new Object[0]);
      return;
    }

    if (member.getLeaderOf() != -128)
    {
      leader.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.CannotAssignUnitLeader", leader, new Object[0]));
      return;
    }

    setLeader(leader, clan, mainUnit, member);
  }

  public static void setLeader(Player player, Clan clan, SubUnit unit, UnitMember newLeader)
  {
    player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.ClanLeaderWillBeChangedFromS1ToS2", player, new Object[0]).addString(clan.getLeaderName()).addString(newLeader.getName()));

    unit.setLeader(newLeader, true);

    clan.broadcastClanStatus(true, true, false);
  }

  public void createSubPledge(Player player, String clanName, int pledgeType, int minClanLvl, String leaderName)
  {
    UnitMember subLeader = null;

    Clan clan = player.getClan();

    if ((clan == null) || (!player.isClanLeader()))
    {
      player.sendPacket(Msg.YOU_HAVE_FAILED_TO_CREATE_A_CLAN);
      return;
    }

    if (!Util.isMatchingRegexp(clanName, Config.CLAN_NAME_TEMPLATE))
    {
      player.sendPacket(Msg.CLAN_NAME_IS_INCORRECT);
      return;
    }

    Collection subPledge = clan.getAllSubUnits();
    for (SubUnit element : subPledge) {
      if (element.getName().equals(clanName))
      {
        player.sendPacket(Msg.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME_PLEASE_ENTER_A_DIFFERENT_NAME);
        return;
      }
    }
    if (ClanTable.getInstance().getClanByName(clanName) != null)
    {
      player.sendPacket(Msg.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME_PLEASE_ENTER_A_DIFFERENT_NAME);
      return;
    }

    if (clan.getLevel() < minClanLvl)
    {
      player.sendPacket(Msg.THE_CONDITIONS_NECESSARY_TO_CREATE_A_MILITARY_UNIT_HAVE_NOT_BEEN_MET);
      return;
    }

    SubUnit unit = clan.getSubUnit(0);

    if (pledgeType != -1)
    {
      subLeader = unit.getUnitMember(leaderName);
      if (subLeader == null)
      {
        player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.PlayerCantBeAssignedAsSubUnitLeader", player, new Object[0]));
        return;
      }
      if (subLeader.getLeaderOf() != -128)
      {
        player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.ItCantBeSubUnitLeader", player, new Object[0]));
        return;
      }
    }

    pledgeType = clan.createSubPledge(player, pledgeType, subLeader, clanName);
    if (pledgeType == -128) {
      return;
    }
    clan.broadcastToOnlineMembers(new L2GameServerPacket[] { new PledgeReceiveSubPledgeCreated(clan.getSubUnit(pledgeType)) });
    SystemMessage sm;
    if (pledgeType == -1)
    {
      SystemMessage sm = new SystemMessage(1741);
      sm.addString(player.getClan().getName());
    }
    else if (pledgeType >= 1001)
    {
      SystemMessage sm = new SystemMessage(1794);
      sm.addString(player.getClan().getName());
    }
    else if (pledgeType >= 100)
    {
      SystemMessage sm = new SystemMessage(1795);
      sm.addString(player.getClan().getName());
    }
    else {
      sm = Msg.CLAN_HAS_BEEN_CREATED;
    }
    player.sendPacket(sm);

    if (subLeader != null)
    {
      clan.broadcastToOnlineMembers(new L2GameServerPacket[] { new PledgeShowMemberListUpdate(subLeader) });
      if (subLeader.isOnline())
      {
        subLeader.getPlayer().updatePledgeClass();
        subLeader.getPlayer().broadcastCharInfo();
      }
    }
  }

  public void assignSubPledgeLeader(Player player, String clanName, String leaderName)
  {
    Clan clan = player.getClan();

    if (clan == null)
    {
      player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.ClanDoesntExist", player, new Object[0]));
      return;
    }

    if (!player.isClanLeader())
    {
      player.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
      return;
    }

    SubUnit targetUnit = null;
    for (SubUnit unit : clan.getAllSubUnits())
    {
      if ((unit.getType() == 0) || (unit.getType() == -1))
        continue;
      if (unit.getName().equalsIgnoreCase(clanName)) {
        targetUnit = unit;
      }
    }
    if (targetUnit == null)
    {
      player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.SubUnitNotFound", player, new Object[0]));
      return;
    }
    SubUnit mainUnit = clan.getSubUnit(0);
    UnitMember subLeader = mainUnit.getUnitMember(leaderName);
    if (subLeader == null)
    {
      player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.PlayerCantBeAssignedAsSubUnitLeader", player, new Object[0]));
      return;
    }

    if (subLeader.getLeaderOf() != -128)
    {
      player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.ItCantBeSubUnitLeader", player, new Object[0]));
      return;
    }

    targetUnit.setLeader(subLeader, true);
    clan.broadcastToOnlineMembers(new L2GameServerPacket[] { new PledgeReceiveSubPledgeCreated(targetUnit) });

    clan.broadcastToOnlineMembers(new L2GameServerPacket[] { new PledgeShowMemberListUpdate(subLeader) });
    if (subLeader.isOnline())
    {
      subLeader.getPlayer().updatePledgeClass();
      subLeader.getPlayer().broadcastCharInfo();
    }

    player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.NewSubUnitLeaderHasBeenAssigned", player, new Object[0]));
  }

  private void dissolveClan(Player player)
  {
    if ((player == null) || (player.getClan() == null))
      return;
    Clan clan = player.getClan();

    if (!player.isClanLeader())
    {
      player.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
      return;
    }
    if (clan.getAllyId() != 0)
    {
      player.sendPacket(Msg.YOU_CANNOT_DISPERSE_THE_CLANS_IN_YOUR_ALLIANCE);
      return;
    }
    if (clan.isAtWar() > 0)
    {
      player.sendPacket(Msg.YOU_CANNOT_DISSOLVE_A_CLAN_WHILE_ENGAGED_IN_A_WAR);
      return;
    }
    if ((clan.getCastle() != 0) || (clan.getHasHideout() != 0) || (clan.getHasFortress() != 0))
    {
      player.sendPacket(Msg.UNABLE_TO_DISPERSE_YOUR_CLAN_OWNS_ONE_OR_MORE_CASTLES_OR_HIDEOUTS);
      return;
    }

    for (Residence r : ResidenceHolder.getInstance().getResidences())
    {
      if ((r.getSiegeEvent().getSiegeClan("attackers", clan) != null) || (r.getSiegeEvent().getSiegeClan("defenders", clan) != null) || (r.getSiegeEvent().getSiegeClan("defenders_waiting", clan) != null))
      {
        player.sendPacket(SystemMsg.UNABLE_TO_DISSOLVE_YOUR_CLAN_HAS_REQUESTED_TO_PARTICIPATE_IN_A_CASTLE_SIEGE);
        return;
      }
    }

    ClanTable.getInstance().dissolveClan(player);
  }

  public void levelUpClan(Player player)
  {
    Clan clan = player.getClan();
    if (clan == null)
      return;
    if (!player.isClanLeader())
    {
      player.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
      return;
    }

    boolean increaseClanLevel = false;

    switch (clan.getLevel())
    {
    case 0:
      if ((player.getSp() < 20000L) || (player.getAdena() < 650000L))
        break;
      player.setSp(player.getSp() - 20000L);
      player.reduceAdena(650000L, true);
      increaseClanLevel = true; break;
    case 1:
      if ((player.getSp() < 100000L) || (player.getAdena() < 2500000L))
        break;
      player.setSp(player.getSp() - 100000L);
      player.reduceAdena(2500000L, true);
      increaseClanLevel = true; break;
    case 2:
      if ((player.getSp() < 350000L) || (!player.getInventory().destroyItemByItemId(1419, 1L)))
        break;
      player.setSp(player.getSp() - 350000L);
      increaseClanLevel = true; break;
    case 3:
      if ((player.getSp() < 1000000L) || (!player.getInventory().destroyItemByItemId(3874, 1L)))
        break;
      player.setSp(player.getSp() - 1000000L);
      increaseClanLevel = true; break;
    case 4:
      if ((player.getSp() < 2500000L) || (!player.getInventory().destroyItemByItemId(3870, 1L)))
        break;
      player.setSp(player.getSp() - 2500000L);
      increaseClanLevel = true; break;
    case 5:
      if ((clan.getReputationScore() < 5000) || (clan.getAllSize() < 30))
        break;
      clan.incReputation(-5000, false, "LvlUpClan");
      increaseClanLevel = true; break;
    case 6:
      if ((clan.getReputationScore() < 10000) || (clan.getAllSize() < 50))
        break;
      clan.incReputation(-10000, false, "LvlUpClan");
      increaseClanLevel = true; break;
    case 7:
      if ((clan.getReputationScore() < 20000) || (clan.getAllSize() < 80))
        break;
      clan.incReputation(-20000, false, "LvlUpClan");
      increaseClanLevel = true; break;
    case 8:
      if ((clan.getReputationScore() < 40000) || (clan.getAllSize() < 120) || 
        (!player.getInventory().destroyItemByItemId(9910, 150L)))
        break;
      clan.incReputation(-40000, false, "LvlUpClan");
      increaseClanLevel = true; break;
    case 9:
      if ((clan.getReputationScore() < 40000) || (clan.getAllSize() < 140) || 
        (!player.getInventory().destroyItemByItemId(9911, 5L)))
        break;
      clan.incReputation(-40000, false, "LvlUpClan");
      increaseClanLevel = true; break;
    case 10:
      if ((clan.getReputationScore() < 75000) || (clan.getAllSize() < 170) || (clan.getCastle() <= 0))
        break;
      Castle castle = (Castle)ResidenceHolder.getInstance().getResidence(clan.getCastle());
      Dominion dominion = castle.getDominion();
      if (dominion.getLordObjectId() != player.getObjectId())
        break;
      clan.incReputation(-75000, false, "LvlUpClan");
      increaseClanLevel = true;
    }
    PledgeShowInfoUpdate pu;
    PledgeStatusChanged ps;
    if (increaseClanLevel)
    {
      clan.setLevel(clan.getLevel() + 1);
      clan.updateClanInDB();

      player.broadcastCharInfo();

      doCast(SkillTable.getInstance().getInfo(5103, 1), player, true);

      if (clan.getLevel() >= 4) {
        SiegeUtils.addSiegeSkills(player);
      }
      if (clan.getLevel() == 5) {
        player.sendPacket(Msg.NOW_THAT_YOUR_CLAN_LEVEL_IS_ABOVE_LEVEL_5_IT_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS);
      }

      pu = new PledgeShowInfoUpdate(clan);
      ps = new PledgeStatusChanged(clan);
      for (UnitMember mbr : clan)
        if (mbr.isOnline())
        {
          mbr.getPlayer().updatePledgeClass();
          mbr.getPlayer().sendPacket(new IStaticPacket[] { Msg.CLANS_SKILL_LEVEL_HAS_INCREASED, pu, ps });
          mbr.getPlayer().broadcastCharInfo();
        }
    }
    else {
      player.sendPacket(Msg.CLAN_HAS_FAILED_TO_INCREASE_SKILL_LEVEL);
    }
  }

  public void createAlly(Player player, String allyName)
  {
    if (!player.isClanLeader())
    {
      player.sendPacket(Msg.ONLY_CLAN_LEADERS_MAY_CREATE_ALLIANCES);
      return;
    }
    if (player.getClan().getAllyId() != 0)
    {
      player.sendPacket(Msg.YOU_ALREADY_BELONG_TO_ANOTHER_ALLIANCE);
      return;
    }
    if (allyName.length() > 16)
    {
      player.sendPacket(Msg.INCORRECT_LENGTH_FOR_AN_ALLIANCE_NAME);
      return;
    }
    if (!Util.isMatchingRegexp(allyName, Config.ALLY_NAME_TEMPLATE))
    {
      player.sendPacket(Msg.INCORRECT_ALLIANCE_NAME);
      return;
    }
    if (player.getClan().getLevel() < 5)
    {
      player.sendPacket(Msg.TO_CREATE_AN_ALLIANCE_YOUR_CLAN_MUST_BE_LEVEL_5_OR_HIGHER);
      return;
    }
    if (ClanTable.getInstance().getAllyByName(allyName) != null)
    {
      player.sendPacket(Msg.THIS_ALLIANCE_NAME_ALREADY_EXISTS);
      return;
    }
    if (!player.getClan().canCreateAlly())
    {
      player.sendPacket(Msg.YOU_CANNOT_CREATE_A_NEW_ALLIANCE_WITHIN_1_DAY_AFTER_DISSOLUTION);
      return;
    }

    Alliance alliance = ClanTable.getInstance().createAlliance(player, allyName);
    if (alliance == null) {
      return;
    }
    player.broadcastCharInfo();
    player.sendMessage(new StringBuilder().append("Alliance ").append(allyName).append(" has been created.").toString());
  }

  private void dissolveAlly(Player player)
  {
    if ((player == null) || (player.getAlliance() == null)) {
      return;
    }
    if (!player.isAllyLeader())
    {
      player.sendPacket(Msg.FEATURE_AVAILABLE_TO_ALLIANCE_LEADERS_ONLY);
      return;
    }

    if (player.getAlliance().getMembersCount() > 1)
    {
      player.sendPacket(Msg.YOU_HAVE_FAILED_TO_DISSOLVE_THE_ALLIANCE);
      return;
    }

    ClanTable.getInstance().dissolveAlly(player);
  }

  private Set<PlayerClass> getAvailableSubClasses(Player player, boolean isNew)
  {
    int charClassId = player.getBaseClassId();
    Race npcRace = getVillageMasterRace();
    ClassType npcTeachType = getVillageMasterTeachType();

    PlayerClass currClass = PlayerClass.values()[charClassId];

    Set availSubs = currClass.getAvailableSubclasses();
    if (availSubs == null) {
      return Collections.emptySet();
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
      if ((!availSub.isOfRace(Race.human)) && (!availSub.isOfRace(Race.elf)))
      {
        if (!availSub.isOfRace(npcRace))
          availSubs.remove(availSub);
      }
      else if (!availSub.isOfType(npcTeachType)) {
        availSubs.remove(availSub);
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

        if (availSub == PlayerClass.Inspector) if (player.getSubClasses().size() < (isNew ? 3 : 4))
            availSubs.remove(availSub);
      }
    }
    return availSubs;
  }

  private Race getVillageMasterRace()
  {
    switch (getTemplate().getRace())
    {
    case 14:
      return Race.human;
    case 15:
      return Race.elf;
    case 16:
      return Race.darkelf;
    case 17:
      return Race.orc;
    case 18:
      return Race.dwarf;
    case 25:
      return Race.kamael;
    case 19:
    case 20:
    case 21:
    case 22:
    case 23:
    case 24: } return null;
  }

  private ClassType getVillageMasterTeachType()
  {
    switch (getNpcId())
    {
    case 30031:
    case 30037:
    case 30070:
    case 30120:
    case 30141:
    case 30191:
    case 30289:
    case 30305:
    case 30358:
    case 30359:
    case 30857:
    case 30905:
    case 31336:
    case 32095:
      return ClassType.Priest;
    case 30115:
    case 30154:
    case 30174:
    case 30175:
    case 30176:
    case 30694:
    case 30854:
    case 31285:
    case 31288:
    case 31326:
    case 31331:
    case 31755:
    case 31977:
    case 31996:
    case 32098:
    case 32147:
    case 32150:
    case 32160:
      return ClassType.Mystic;
    }

    return ClassType.Fighter;
  }
}