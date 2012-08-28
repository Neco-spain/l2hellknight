package net.sf.l2j.gameserver.network.clientpackets;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.CrownManager;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.EventManager;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.MacroList;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Couple;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ClientSetTime;
import net.sf.l2j.gameserver.network.serverpackets.Die;
import net.sf.l2j.gameserver.network.serverpackets.ExPCCafePointInfo;
import net.sf.l2j.gameserver.network.serverpackets.ExStorageMaxCount;
import net.sf.l2j.gameserver.network.serverpackets.FriendList;
import net.sf.l2j.gameserver.network.serverpackets.FriendStatus;
import net.sf.l2j.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PledgeSkillList;
import net.sf.l2j.gameserver.network.serverpackets.PledgeStatusChanged;
import net.sf.l2j.gameserver.network.serverpackets.QuestList;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.network.serverpackets.SignsSky;
import net.sf.l2j.gameserver.network.serverpackets.SkillCoolTime;
import net.sf.l2j.gameserver.network.serverpackets.SkillList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.util.Online;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;

public class EnterWorld extends L2GameClientPacket
{
  private static final Logger _log = Logger.getLogger(EnterWorld.class.getName());

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      _log.warning("EnterWorld failed! player is null...");
      ((L2GameClient)getClient()).closeNow();
      return;
    }

    if (player.isInGame()) {
      player.closeNetConnection();
      return;
    }
    player.setChannel(1);

    if (player.isGM()) {
      if (Config.GM_STARTUP_INVISIBLE) {
        player.setChannel(0);
      }

      if (Config.GM_STARTUP_INVULNERABLE) {
        player.setIsInvul(true);
      }

      if (Config.GM_STARTUP_SILENCE) {
        player.setMessageRefusal(true);
      }

      if (Config.GM_STARTUP_AUTO_LIST)
        GmListTable.getInstance().addGm(player, false);
      else {
        GmListTable.getInstance().addGm(player, true);
      }
    }
    try
    {
      player.spawnMe(player.getX(), player.getY(), player.getZ());
    } catch (Exception e) {
      e.printStackTrace();
    }

    sendPacket(new HennaInfo(player));
    player.sendItems(false);
    sendPacket(new ShortCutInit(player));
    player.getMacroses().sendUpdate();
    sendPacket(new ClientSetTime());
    if (SevenSigns.getInstance().isSealValidationPeriod()) {
      sendPacket(new SignsSky());
    }
    sendPacket(Static.WELCOME_TO_LINEAGE);

    SevenSigns.getInstance().sendCurrentPeriodMsg(player);

    Announcements.getInstance().showAnnouncements(player);
    if (Config.SHOW_ENTER_WARNINGS) {
      Announcements.getInstance().showWarnings(player);
    }

    player.setOnlineStatus(true);

    checkPledgeClass(player);

    if ((player.getClanId() != 0) && (player.getClan() != null)) {
      notifyClanMembers(player);
      notifySponsorOrApprentice(player);
      sendPacket(new PledgeShowMemberListAll(player.getClan(), player));
      sendPacket(new PledgeStatusChanged(player.getClan()));
      player.sendPacket(new PledgeSkillList(player.getClan()));

      for (Siege siege : SiegeManager.getInstance().getSieges()) {
        if (!siege.getIsInProgress()) {
          continue;
        }
        if (siege.checkIsAttacker(player.getClan()))
          player.setSiegeState(1);
        else if (siege.checkIsDefender(player.getClan())) {
          player.setSiegeState(2);
        }
      }

    }

    sendPacket(new ExStorageMaxCount(player));

    Quest.playerEnter(player);
    sendPacket(new QuestList());

    if ((!player.isGM()) && (player.getSiegeState() < 2) && (player.isInsideZone(4)))
    {
      player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
    }

    if ((player.isDead()) || (player.isAlikeDead())) {
      sendPacket(new UserInfo(player));

      sendPacket(new Die(player));
    }

    player.restoreEffects(null);
    player.updateEffectIcons();

    CursedWeaponsManager.getInstance().checkPlayer(player);

    player.sendEtcStatusUpdate();

    player.checkHpMessages(player.getMaxHp(), player.getCurrentHp());
    player.checkDayNightMessages();

    if (DimensionalRiftManager.getInstance().checkIfInRiftZone(player.getX(), player.getY(), player.getZ(), false)) {
      DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
    }

    if (Config.PLAYER_SPAWN_PROTECTION > 0) {
      player.setProtection(true);
    }

    player.checkBanChat(false);

    CrownManager.getInstance().checkCrowns(player);

    L2ItemInstance wpn = player.getInventory().getPaperdollItem(7);
    if (wpn == null) {
      wpn = player.getInventory().getPaperdollItem(14);
    }
    if (wpn != null) {
      if (wpn.isAugmented()) {
        wpn.getAugmentation().applyBoni(player);
      }

      if (wpn.isShadowItem()) {
        player.sendCritMessage(wpn.getItemName() + ": \u043E\u0441\u0442\u0430\u043B\u043E\u0441\u044C " + wpn.getMana() + " \u043C\u0438\u043D\u0443\u0442.");
      }
    }

    Quest.playerEnter(player);

    PetitionManager.getInstance().checkPetitionMessages(player);

    player.onPlayerEnter();

    player.sendPacket(new SkillCoolTime(player));

    EventManager.getInstance().onLogin(player);

    sendPacket(new SkillList());
    player.checkAllowedSkills();
    player.sendItems(false);

    player.setCurrentHp(player.getEnterWorldHp());
    player.setCurrentMp(player.getEnterWorldMp());
    player.setCurrentCp(player.getEnterWorldCp());

    if (player.isInOlumpiadStadium()) {
      if ((player.isDead()) || (player.isAlikeDead())) {
        player.doRevive();
      }
      player.teleToClosestTown();
      player.sendMessage("You have been teleported to the nearest town due to you being in an Olympiad Stadium");
    }

    Online _online = Online.getInstance();
    _online.checkMaxOnline();

    if (Config.SONLINE_LOGIN_ONLINE) {
      TextBuilder onlineWelcome = new TextBuilder();
      onlineWelcome.append(Static.SHO_ONLINE_ALL + " ").append(_online.getCurrentOnline()).append("; ");
      if (Config.SONLINE_LOGIN_OFFLINE) {
        onlineWelcome.append(Static.SHO_ONLINE_TRD + " ").append(_online.getOfflineTradersOnline()).append(" ");
      }
      player.sendMessage(onlineWelcome.toString());
      onlineWelcome.clear();
      onlineWelcome = null;
      if (Config.SONLINE_LOGIN_MAX) {
        TextBuilder gWelcome = new TextBuilder();
        gWelcome.append(Static.SHO_ONLINE_REC + " ").append(_online.getMaxOnline()).append("; ");
        if (Config.SONLINE_LOGIN_DATE) {
          gWelcome.append(Static.SHO_ONLINE_WAS + " ").append(_online.getMaxOnlineDate()).append(" ");
        }
        player.sendMessage(gWelcome.toString());
        gWelcome.clear();
        gWelcome = null;
      }

    }

    player.checkDonateSkills();

    if (Config.L2JMOD_ALLOW_WEDDING) {
      engage(player);
      notifyPartner(player, player.getPartnerId());
    }
    notifyFriends(player, true);
    if ((Config.PREMIUM_ANOOUNCE) && (player.isPremium())) {
      Announcements.getInstance().announceToAll(Config.PREMIUM_ANNOUNCE_PHRASE.replace("%player%", player.getName()));
    }

    if ((Config.PC_CAFE_ENABLED) && (player.getPcPoints() > 0)) {
      sendPacket(new ExPCCafePointInfo(player, 0, false, false));
    }

    player.setInGame(true);
    player.sendTempMessages();

    player.sendActionFailed();
  }

  private void engage(L2PcInstance cha)
  {
    int _chaid = cha.getObjectId();
    for (Couple cl : CoupleManager.getInstance().getCouples())
      if ((cl.getPlayer1Id() == _chaid) || (cl.getPlayer2Id() == _chaid)) {
        if (cl.getMaried()) {
          cha.setMarried(true);
        }

        cha.setCoupleId(cl.getId());
        if (cl.getPlayer1Id() == _chaid)
          cha.setPartnerId(cl.getPlayer2Id());
        else {
          cha.setPartnerId(cl.getPlayer1Id());
        }

        L2Skill wedTP = SkillTable.getInstance().getInfo(7073, 1);
        if (wedTP != null)
          cha.addSkill(wedTP, false);
      }
  }

  private void notifyPartner(L2PcInstance cha, int partnerId)
  {
    if (cha.getPartnerId() != 0) {
      L2PcInstance partner = L2World.getInstance().getPlayer(cha.getPartnerId());
      if (partner != null) {
        if (!partner.getAppearance().getSex())
          partner.sendPacket(Static.WIFE_LOGIN);
        else {
          partner.sendPacket(Static.AUNT_LOGIN);
        }
      }
      partner = null;
    }
  }

  public static void notifyFriends(L2PcInstance cha, boolean login)
  {
    if (login) {
      cha.sendPacket(new FriendList(cha));
    }

    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT friend_id, friend_name FROM character_friends WHERE char_id=?");
      st.setInt(1, cha.getObjectId());
      rs = st.executeQuery();

      while (rs.next()) {
        int friendId = rs.getInt("friend_id");
        String friendName = rs.getString("friend_name");
        cha.storeFriend(friendId, friendName);
        L2PcInstance friend = L2World.getInstance().getPlayer(friendId);
        if (friend == null)
        {
          continue;
        }
        if (login) {
          friend.sendPacket(SystemMessage.id(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN).addString(cha.getName()));
          friend.sendPacket(new FriendStatus(cha, true)); continue;
        }
        friend.sendPacket(new FriendStatus(cha, false));
        cha.sendPacket(new FriendList(cha));
      }

      Close.SR(st, rs);
      checkMail(con, cha);
    } catch (Exception e) {
      _log.warning("could not restore friend data:" + e);
    } finally {
      Close.CSR(con, st, rs);
    }
  }

  private static void checkMail(Connect con, L2PcInstance player) {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      st = con.prepareStatement("SELECT id FROM `z_bbs_mail` WHERE `to` = ? AND `read` = ? LIMIT 1");
      st.setInt(1, player.getObjectId());
      st.setInt(2, 0);
      rs = st.executeQuery();
      if (rs.next()) {
        player.sendPacket(Static.ExMailArrived);
        player.sendPacket(Static.UNREAD_MAIL);
      }
    } catch (Exception e) {
      _log.warning("EnterWorld: checkMail() error: " + e);
    } finally {
      Close.SR(st, rs);
    }
  }

  private void notifyClanMembers(L2PcInstance player)
  {
    L2Clan clan = player.getClan();
    if ((clan != null) && (clan.getClanMember(player.getName()) != null)) {
      clan.getClanMember(player.getName()).setPlayerInstance(player);
      clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(player), player);
      clan.broadcastToOtherOnlineMembers(SystemMessage.id(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN).addString(player.getName()), player);
      clan.addBonusEffects(player, true);
    }
  }

  private void notifySponsorOrApprentice(L2PcInstance player)
  {
    if (player.getSponsor() != 0) {
      L2PcInstance sponsor = L2World.getInstance().getPlayer(player.getSponsor());
      if (sponsor != null)
        sponsor.sendPacket(SystemMessage.id(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN).addString(player.getName()));
    }
    else if (player.getApprentice() != 0) {
      L2PcInstance apprentice = L2World.getInstance().getPlayer(player.getApprentice());
      if (apprentice != null)
        apprentice.sendPacket(SystemMessage.id(SystemMessageId.YOUR_SPONSOR_S1_HAS_LOGGED_IN).addString(player.getName()));
    }
  }

  private void checkPledgeClass(L2PcInstance player)
  {
    int pledgeClass = 0;
    if ((player.getClan() != null) && (player.getClan().getClanMember(player.getObjectId()) != null)) {
      pledgeClass = player.getClan().getClanMember(player.getObjectId()).calculatePledgeClass(player);
    }

    if ((player.isNoble()) && (pledgeClass < 5)) {
      pledgeClass = 5;
    }

    if (player.isHero()) {
      pledgeClass = 8;
    }

    player.setPledgeClass(pledgeClass);
  }
}