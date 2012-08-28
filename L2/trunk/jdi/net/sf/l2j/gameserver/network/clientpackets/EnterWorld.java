package net.sf.l2j.gameserver.network.clientpackets;

import gnu.trove.TIntIntHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.AnnouncementsOnline;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
import net.sf.l2j.gameserver.datatables.CharSchemesTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.FortSiegeManager;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.MacroList;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.Couple;
import net.sf.l2j.gameserver.model.entity.FortSiege;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.model.entity.L2Event;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.entity.events.CTF;
import net.sf.l2j.gameserver.model.entity.events.Heroes;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.Disconnection;
import net.sf.l2j.gameserver.network.HwidDisconnection;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ClientSetTime;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.Die;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ExStorageMaxCount;
import net.sf.l2j.gameserver.network.serverpackets.FriendList;
import net.sf.l2j.gameserver.network.serverpackets.GameGuardQuery;
import net.sf.l2j.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PledgeSkillList;
import net.sf.l2j.gameserver.network.serverpackets.PledgeStatusChanged;
import net.sf.l2j.gameserver.network.serverpackets.QuestList;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.network.serverpackets.SignsSky;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.protection.nProtect;
import net.sf.protection.nProtect.RestrictionType;

public class EnterWorld extends L2GameClientPacket
{
  private static final String _C__03_ENTERWORLD = "[C] 03 EnterWorld";
  private static Logger _log = Logger.getLogger(EnterWorld.class.getName());

  public TaskPriority getPriority() { return TaskPriority.PR_URGENT;
  }

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();

    if (activeChar == null)
    {
      _log.warning("EnterWorld failed! activeChar is null...");
      ((L2GameClient)getClient()).closeNow();
      return;
    }
    FloodProtector.getInstance().registerNewPlayer(activeChar.getObjectId());

    if (L2World.getInstance().findObject(activeChar.getObjectId()) != null)
    {
      if (Config.DEBUG) {
        _log.warning("User already exist in OID map! User " + activeChar.getName() + " is character clone");
      }
    }
    if (activeChar.isGM())
    {
      if ((Config.GM_STARTUP_INVULNERABLE) && (((!Config.ALT_PRIVILEGES_ADMIN) && (activeChar.getAccessLevel() >= Config.GM_GODMODE)) || ((Config.ALT_PRIVILEGES_ADMIN) && (AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_invul")))))
      {
        activeChar.setIsInvul(true);
      }
      if ((Config.GM_STARTUP_INVISIBLE) && (((!Config.ALT_PRIVILEGES_ADMIN) && (activeChar.getAccessLevel() >= Config.GM_GODMODE)) || ((Config.ALT_PRIVILEGES_ADMIN) && (AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_invisible")))))
      {
        activeChar.getAppearance().setInvisible();
      }
      if ((Config.GM_STARTUP_SILENCE) && (((!Config.ALT_PRIVILEGES_ADMIN) && (activeChar.getAccessLevel() >= Config.GM_MENU)) || ((Config.ALT_PRIVILEGES_ADMIN) && (AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_silence")))))
      {
        activeChar.setMessageRefusal(true);
      }
      if ((Config.GM_STARTUP_AUTO_LIST) && (((!Config.ALT_PRIVILEGES_ADMIN) && (activeChar.getAccessLevel() >= Config.GM_MENU)) || ((Config.ALT_PRIVILEGES_ADMIN) && (AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_gmliston")))))
      {
        GmListTable.getInstance().addGm(activeChar, false);
      }
      else GmListTable.getInstance().addGm(activeChar, true);
    }

    if ((activeChar.getClanId() != 0) && (activeChar.getClan() != null))
    {
      sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), activeChar));
      sendPacket(new PledgeStatusChanged(activeChar.getClan()));
    }

    if ((Hero.getInstance().getHeroes() != null) && (Hero.getInstance().getHeroes().containsKey(Integer.valueOf(activeChar.getObjectId())))) {
      activeChar.setHero(true);
    }
    activeChar.getInventory().reloadEquippedItems();

    setPledgeClass(activeChar);

    sendPacket(new UserInfo(activeChar));
    sendPacket(new ValidateLocation(activeChar));
    activeChar.getMacroses().sendUpdate();

    sendPacket(new ItemList(activeChar, false));

    if (Config.GAMEGUARD_ENFORCE)
    {
      GameGuardQuery ggq = new GameGuardQuery();
      activeChar.sendPacket(ggq);
    }

    sendPacket(new ShortCutInit(activeChar));

    activeChar.sendSkillList();

    activeChar.sendPacket(new HennaInfo(activeChar));

    Quest.playerEnter(activeChar);
    activeChar.sendPacket(new QuestList());
    loadTutorial(activeChar);

    if (Config.PLAYER_SPAWN_PROTECTION > 0) {
      activeChar.setProtection(true);
    }
    activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());

    if (activeChar.getClan() != null)
    {
      activeChar.sendPacket(new PledgeSkillList(activeChar.getClan()));
      for (Siege siege : SiegeManager.getInstance().getSieges())
      {
        if (!siege.getIsInProgress())
        {
          continue;
        }

        if (siege.checkIsAttacker(activeChar.getClan()))
        {
          activeChar.setSiegeState(1);
          break;
        }
        if (siege.checkIsDefender(activeChar.getClan()))
        {
          activeChar.setSiegeState(2);
          break;
        }
      }

      for (FortSiege fortsiege : FortSiegeManager.getInstance().getSieges())
      {
        if (!fortsiege.getIsInProgress())
        {
          continue;
        }

        if (fortsiege.checkIsAttacker(activeChar.getClan()))
        {
          activeChar.setSiegeState(1);
          break;
        }
        if (fortsiege.checkIsDefender(activeChar.getClan()))
        {
          activeChar.setSiegeState(2);
          break;
        }
      }

      ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan());
      if (clanHall != null)
      {
        if (!clanHall.getPaid())
        {
          activeChar.sendPacket(new SystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW));
        }
      }
    }

    activeChar.broadcastUserInfo();

    if ((Config.ON_ENTER_BUFFS) && (Config.ON_ENTER_BUFFS_LVL >= activeChar.getLevel()))
    {
      if (activeChar.isMageClass())
      {
        if ((Config.ON_ENTER_M_BUFFS != null) && (!Config.ON_ENTER_M_BUFFS.isEmpty()))
        {
          for (int i : Config.ON_ENTER_M_BUFFS.keys())
          {
            L2Skill skill = SkillTable.getInstance().getInfo(i, Config.ON_ENTER_M_BUFFS.get(i));
            if (skill == null)
              continue;
            skill.getEffects(activeChar, activeChar);
          }
        }

      }
      else if ((Config.ON_ENTER_F_BUFFS != null) && (!Config.ON_ENTER_F_BUFFS.isEmpty()))
      {
        for (int i : Config.ON_ENTER_F_BUFFS.keys())
        {
          L2Skill skill = SkillTable.getInstance().getInfo(i, Config.ON_ENTER_F_BUFFS.get(i));
          if (skill == null)
            continue;
          skill.getEffects(activeChar, activeChar);
        }
      }

    }

    if ((activeChar.getZ() < -15000) || (activeChar.getZ() > 15000))
    {
      activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
      activeChar.setTarget(activeChar);
      activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
    }

    if ((Config.USE_PREMIUMSERVICE) && (activeChar.getPremiumService() == 1))
    {
      activeChar.getAppearance().setTitleColor(Config.PREMIUM_TITLE_COLOR);
      activeChar.getAppearance().setNameColor(Config.PREMIUM_NICK_COLOR, false);
    }

    if ((L2Event.active) && (L2Event.connectionLossData.containsKey(activeChar.getName())) && (L2Event.isOnEvent(activeChar)))
      L2Event.restoreChar(activeChar);
    else if (L2Event.connectionLossData.containsKey(activeChar.getName())) {
      L2Event.restoreAndTeleChar(activeChar);
    }
    if (Config.pccafe_event) {
      activeChar.showPcCafeWnd();
    }
    if (SevenSigns.getInstance().isSealValidationPeriod()) {
      sendPacket(new SignsSky());
    }
    if (Config.STORE_SKILL_COOLTIME) {
      activeChar.restoreEffects();
    }
    if (Config.L2JMOD_ALLOW_WEDDING)
    {
      engage(activeChar);
      notifyPartner(activeChar, activeChar.getPartnerId());
      CoupleManager.getInstance().checkCouple(activeChar);
    }

    L2Effect[] effects = activeChar.getAllEffects();

    if (effects != null)
    {
      for (L2Effect e : effects)
      {
        if (e.getEffectType() == L2Effect.EffectType.HEAL_OVER_TIME)
        {
          activeChar.stopEffects(L2Effect.EffectType.HEAL_OVER_TIME);
          activeChar.removeEffect(e);
        }

        if (e.getEffectType() == L2Effect.EffectType.MANA_HEAL_OVER_TIME)
        {
          activeChar.stopEffects(L2Effect.EffectType.MANA_HEAL_OVER_TIME);
          activeChar.removeEffect(e);
        }

        if (e.getEffectType() == L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME)
        {
          activeChar.stopEffects(L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME);
          activeChar.removeEffect(e);
        }

        if (e.getEffectType() != L2Effect.EffectType.CHARGE)
          continue;
        e.exit();
      }

    }

    activeChar.updateEffectIcons();

    activeChar.sendPacket(new EtcStatusUpdate(activeChar));

    ExStorageMaxCount esmc = new ExStorageMaxCount(activeChar);

    activeChar.sendPacket(esmc);

    sendPacket(new FriendList(activeChar));

    sendPacket(new ItemList(activeChar, false));

    for (L2ItemInstance temp : activeChar.getInventory().getAugmentedItems()) {
      if ((temp != null) && (temp.isEquipped()))
        temp.getAugmentation().applyBoni(activeChar);
    }
    if ((Config.CHECK_SKILLS_ON_ENTER) && (!Config.ALT_GAME_SKILL_LEARN)) {
      activeChar.checkAllowedSkills();
    }
    SystemMessage sm = new SystemMessage(SystemMessageId.WELCOME_TO_LINEAGE);
    sendPacket(sm);
    sm = null;

    sendPacket(new ClientSetTime());
    SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
    Announcements.getInstance().showAnnouncements(activeChar);

    if (Config.SAVE_BUFF_PROFILES) {
      CharSchemesTable.getInstance().onPlayerLogin(activeChar.getObjectId());
    }
    if (Config.SERVER_NEWS)
    {
      String serverNews = HtmCache.getInstance().getHtm("data/html/welcome.htm");
      if (serverNews != null) {
        sendPacket(new NpcHtmlMessage(1, serverNews));
      }
    }
    PetitionManager.getInstance().checkPetitionMessages(activeChar);

    if (Config.ALLOW_WATER) {
      activeChar.checkWaterState();
    }
    activeChar.setOnlineStatus(true);

    if (activeChar.isAlikeDead())
    {
      sendPacket(new Die(activeChar));
    }

    notifyFriends(activeChar);
    notifyClanMembers(activeChar);
    notifySponsorOrApprentice(activeChar);

    activeChar.onPlayerEnter();
    Heroes.getInstance().onEnterWorld(activeChar);

    if (Olympiad.getInstance().playerInStadia(activeChar))
    {
      activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
      activeChar.sendMessage("You teleported to the nearest town because of the Olympiad");
    }

    if (DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false))
    {
      DimensionalRiftManager.getInstance().teleportToWaitingRoom(activeChar);
    }

    if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED));
    }

    activeChar.setCurrentHpMp(L2PcInstance.curHp, L2PcInstance.curMp);
    activeChar.setCurrentCp(L2PcInstance.curCp);

    if ((Config.LEVEL_ON_ENTER > 0) && (activeChar.getLevel() <= 1))
    {
      byte byte0 = Config.LEVEL_ON_ENTER;
      if ((byte0 >= 1) && (byte0 <= 81))
      {
        activeChar.addExpAndSp(net.sf.l2j.gameserver.model.base.Experience.LEVEL[byte0], Config.SP_ON_ENTER);
      }
    }
    if (Config.SAVE_MAXONLINE_IN_DB) {
      AnnouncementsOnline.getInstance().checkMaxOnline();
    }

    if (Config.ONLINE_LOGIN_ONLINE)
    {
      activeChar.sendMessage("\u0422\u0435\u043A\u0443\u0449\u0438\u0439 \u043E\u043D\u043B\u0430\u0439\u043D: " + AnnouncementsOnline.getInstance().getCurrentOnline());
      if (Config.ONLINE_LOGIN_OFFLINE)
        activeChar.sendMessage("\u041E\u0444\u0444\u043B\u0430\u0439\u043D \u0442\u0440\u0435\u0439\u0434\u0435\u0440\u043E\u0432: " + AnnouncementsOnline.getInstance().getOfflineTradersOnline());
      if (Config.ONLINE_LOGIN_MAX)
      {
        if (Config.ONLINE_LOGIN_DATE)
          activeChar.sendMessage("\u041C\u0430\u043A\u0441\u0438\u043C\u0430\u043B\u044C\u043D\u044B\u0439 \u043E\u043D\u043B\u0430\u0439\u043D: " + AnnouncementsOnline.getInstance().getMaxOnline() + " \u0411\u044B\u043B: " + AnnouncementsOnline.getInstance().getMaxOnlineDate());
        else {
          activeChar.sendMessage("\u041C\u0430\u043A\u0441\u0438\u043C\u0430\u043B\u044C\u043D\u044B\u0439 \u043E\u043D\u043B\u0430\u0439\u043D: " + AnnouncementsOnline.getInstance().getMaxOnline());
        }
      }
    }
    if (Config.SHOW_WELCOME_PM)
    {
      CreatureSay np = new CreatureSay(0, 2, Config.PM_FROM, Config.PM_TEXT);
      activeChar.sendPacket(np);
    }

    if ((!activeChar.isGM()) && (activeChar.getSiegeState() < 2) && (activeChar.isInsideZone(4)))
    {
      activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
      activeChar.sendMessage("You teleported to the nearest town because of the Siege");
    }

    RegionBBSManager.getInstance().changeCommunityBoard();

    if (CTF._savePlayers.contains(activeChar.getName())) {
      CTF.addDisconnectedPlayer(activeChar);
    }
    if (!nProtect.getInstance().checkRestriction(activeChar, nProtect.RestrictionType.RESTRICT_ENTER, new Object[0]))
    {
      activeChar.setIsImobilised(true);
      activeChar.disableAllSkills();
      ThreadPoolManager.getInstance().scheduleGeneral(new Disconnection(activeChar), 20000L);
    }

    ThreadPoolManager.getInstance().scheduleGeneral(new HwidDisconnection(activeChar), 2000L);
  }

  private void engage(L2PcInstance cha) {
    int _chaid = cha.getObjectId();

    for (Couple cl : CoupleManager.getInstance().getCouples())
    {
      if ((cl.getPlayer1Id() == _chaid) || (cl.getPlayer2Id() == _chaid))
      {
        if (cl.getMaried()) {
          cha.setMarried(true);
        }
        cha.setCoupleId(cl.getId());

        if (cl.getPlayer1Id() == _chaid)
        {
          cha.setPartnerId(cl.getPlayer2Id());
        }
        else
        {
          cha.setPartnerId(cl.getPlayer1Id());
        }
      }
    }
  }

  private void notifyPartner(L2PcInstance cha, int partnerId)
  {
    if (cha == null)
      return;
    if (cha.getPartnerId() != 0)
    {
      L2PcInstance partner = (L2PcInstance)L2World.getInstance().findObject(cha.getPartnerId());
      if ((cha.isMarried()) && (Config.COLOR_WEDDING_NAME))
        cha.getAppearance().setNameColor(Config.COLOR_WEDDING_NAMES, true);
      if ((partner != null) && (partner.getAppearance().getSex() == cha.getAppearance().getSex()) && (cha.isMarried()) && (Config.COLOR_WEDDING_NAME))
      {
        if (cha.getAppearance().getSex())
        {
          cha.getAppearance().setNameColor(Config.COLOR_WEDDING_NAMES_LIZ, true);
          partner.getAppearance().setNameColor(Config.COLOR_WEDDING_NAMES_LIZ, true);
        }
        else {
          cha.getAppearance().setNameColor(Config.COLOR_WEDDING_NAMES_GEY, true);
          partner.getAppearance().setNameColor(Config.COLOR_WEDDING_NAMES_GEY, true);
        }
        partner.sendMessage("Your Partner has logged in");
        partner.broadcastUserInfo();
      }

      partner = null;
    }
  }

  private void notifyFriends(L2PcInstance cha)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("SELECT friend_name FROM character_friends WHERE char_id=?");
      statement.setInt(1, cha.getObjectId());
      ResultSet rset = statement.executeQuery();

      SystemMessage sm = new SystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN);
      sm.addString(cha.getName());

      while (rset.next())
      {
        String friendName = rset.getString("friend_name");

        L2PcInstance friend = L2World.getInstance().getPlayer(friendName);

        if (friend == null)
          continue;
        friend.sendPacket(new FriendList(friend));
        friend.sendPacket(sm);
      }

      sm = null;

      rset.close();
      statement.close();
    }
    catch (Exception e) {
      _log.warning("could not restore friend data:" + e);
    } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  private void notifyClanMembers(L2PcInstance activeChar) {
    L2Clan clan = activeChar.getClan();
    if (clan != null)
    {
      clan.getClanMember(activeChar.getName()).setPlayerInstance(activeChar);
      SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN);
      msg.addString(activeChar.getName());
      clan.broadcastToOtherOnlineMembers(msg, activeChar);
      msg = null;
      clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(activeChar));
      if (clan.isNoticeEnabled())
      {
        sendPacket(new NpcHtmlMessage(1, "<html><title>Clan Announcements</title><body><br><center><font color=\"CCAA00\">" + activeChar.getClan().getName() + "</font> <font color=\"6655FF\">Clan Alert Message</font></center><br>" + "<img src=\"L2UI.SquareWhite\" width=270 height=1><br>" + activeChar.getClan().getNotice().replaceAll("\r\n", "<br>") + "</body></html>"));
      }
    }
  }

  private void notifySponsorOrApprentice(L2PcInstance activeChar)
  {
    if (activeChar.getSponsor() != 0)
    {
      L2PcInstance sponsor = (L2PcInstance)L2World.getInstance().findObject(activeChar.getSponsor());

      if (sponsor != null)
      {
        SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
        msg.addString(activeChar.getName());
        sponsor.sendPacket(msg);
      }
    }
    else if (activeChar.getApprentice() != 0)
    {
      L2PcInstance apprentice = (L2PcInstance)L2World.getInstance().findObject(activeChar.getApprentice());

      if (apprentice != null)
      {
        SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_SPONSOR_S1_HAS_LOGGED_IN);
        msg.addString(activeChar.getName());
        apprentice.sendPacket(msg);
      }
    }
  }

  private void loadTutorial(L2PcInstance player)
  {
    QuestState qs = player.getQuestState("255_Tutorial");
    if (qs != null)
      qs.getQuest().notifyEvent("UC", null, player);
  }

  public String getType()
  {
    return "[C] 03 EnterWorld";
  }

  private void setPledgeClass(L2PcInstance activeChar)
  {
    int pledgeClass = 0;
    if (activeChar.getClan() != null) {
      pledgeClass = activeChar.getClan().getClanMember(activeChar.getObjectId()).calculatePledgeClass(activeChar);
    }
    if ((activeChar.isNoble()) && (pledgeClass < 5)) {
      pledgeClass = 5;
    }
    if (activeChar.isHero()) {
      pledgeClass = 8;
    }
    activeChar.setPledgeClass(pledgeClass);
  }
}