package l2m.gameserver.network.clientpackets;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import l2m.gameserver.Announcements;
import l2m.gameserver.Config;
import l2m.gameserver.data.dao.MailDAO;
import l2m.gameserver.data.StringHolder;
import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.instancemanager.CoupleManager;
import l2m.gameserver.instancemanager.CursedWeaponsManager;
import l2m.gameserver.instancemanager.PetitionManager;
import l2m.gameserver.instancemanager.PlayerMessageStack;
import l2m.gameserver.instancemanager.QuestManager;
import l2m.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.EffectList;
import l2m.gameserver.model.GameObjectsStorage;
import l2m.gameserver.model.Party;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.Summon;
import l2m.gameserver.model.World;
import l2m.gameserver.model.actor.instances.player.FriendList;
import l2m.gameserver.model.actor.instances.player.MacroList;
import l2m.gameserver.model.actor.instances.player.NevitSystem;
import l2m.gameserver.model.actor.listener.PlayerListenerList;
import l2m.gameserver.model.base.InvisibleType;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.model.entity.SevenSigns;
import l2m.gameserver.model.entity.boat.Boat;
import l2m.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import l2m.gameserver.model.entity.residence.ClanHall;
import l2m.gameserver.model.items.ClanWarehouse;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.ItemInstance.ItemLocation;
import l2m.gameserver.model.mail.Mail;
import l2m.gameserver.model.mail.Mail.SenderType;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.model.pledge.SubUnit;
import l2m.gameserver.model.pledge.UnitMember;
import l2m.gameserver.model.quest.Quest;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ChangeWaitType;
import l2m.gameserver.network.serverpackets.ClientSetTime;
import l2m.gameserver.network.serverpackets.ConfirmDlg;
import l2m.gameserver.network.serverpackets.Die;
import l2m.gameserver.network.serverpackets.EtcStatusUpdate;
import l2m.gameserver.network.serverpackets.ExAutoSoulShot;
import l2m.gameserver.network.serverpackets.ExBR_PremiumState;
import l2m.gameserver.network.serverpackets.ExBasicActionList;
import l2m.gameserver.network.serverpackets.ExGoodsInventoryChangedNotify;
import l2m.gameserver.network.serverpackets.ExMPCCOpen;
import l2m.gameserver.network.serverpackets.ExNoticePostArrived;
import l2m.gameserver.network.serverpackets.ExNotifyPremiumItem;
import l2m.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2m.gameserver.network.serverpackets.ExReceiveShowPostFriend;
import l2m.gameserver.network.serverpackets.ExSetCompassZoneCode;
import l2m.gameserver.network.serverpackets.ExStorageMaxCount;
import l2m.gameserver.network.serverpackets.HennaInfo;
import l2m.gameserver.network.serverpackets.L2FriendList;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.MagicSkillLaunched;
import l2m.gameserver.network.serverpackets.MagicSkillUse;
import l2m.gameserver.network.serverpackets.PartySmallWindowAll;
import l2m.gameserver.network.serverpackets.PartySpelled;
import l2m.gameserver.network.serverpackets.PetInfo;
import l2m.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2m.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import l2m.gameserver.network.serverpackets.PledgeSkillList;
import l2m.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import l2m.gameserver.network.serverpackets.PrivateStoreMsgSell;
import l2m.gameserver.network.serverpackets.QuestList;
import l2m.gameserver.network.serverpackets.RecipeShopMsg;
import l2m.gameserver.network.serverpackets.RelationChanged;
import l2m.gameserver.network.serverpackets.Ride;
import l2m.gameserver.network.serverpackets.SSQInfo;
import l2m.gameserver.network.serverpackets.ShortCutInit;
import l2m.gameserver.network.serverpackets.SkillCoolTime;
import l2m.gameserver.network.serverpackets.SkillList;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.skills.AbnormalEffect;
import l2m.gameserver.data.tables.SkillTable;
import l2m.gameserver.utils.GameStats;
import l2m.gameserver.utils.ItemFunctions;
import l2m.gameserver.utils.TradeHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnterWorld extends L2GameClientPacket
{
  private static final Object _lock = new Object();

  private static final Logger _log = LoggerFactory.getLogger(EnterWorld.class);

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    GameClient client = (GameClient)getClient();
    Player activeChar = client.getActiveChar();

    if (activeChar == null)
    {
      client.closeNow(false);
      return;
    }

    int MyObjectId = activeChar.getObjectId();
    Long MyStoreId = activeChar.getStoredId();

    synchronized (_lock)
    {
      for (Player cha : GameObjectsStorage.getAllPlayersForIterate())
      {
        if (MyStoreId == cha.getStoredId())
          continue;
        try
        {
          if (cha.getObjectId() == MyObjectId)
          {
            _log.warn("Double EnterWorld for char: " + activeChar.getName());
            cha.kick();
          }
        }
        catch (Exception e)
        {
          _log.error("", e);
        }
      }
    }

    GameStats.incrementPlayerEnterGame();

    boolean first = activeChar.entering;

    if (first)
    {
      activeChar.setOnlineStatus(true);
      if ((activeChar.getPlayerAccess().GodMode) && (!Config.SHOW_GM_LOGIN)) {
        activeChar.setInvisibleType(InvisibleType.NORMAL);
      }
      activeChar.setNonAggroTime(9223372036854775807L);
      activeChar.spawnMe();

      if ((activeChar.isInStoreMode()) && 
        (!TradeHelper.checksIfCanOpenStore(activeChar, activeChar.getPrivateStoreType())))
      {
        activeChar.setPrivateStoreType(0);
        activeChar.standUp();
        activeChar.broadcastCharInfo();
      }

      activeChar.setRunning();
      activeChar.standUp();
      activeChar.startTimers();
    }

    activeChar.sendPacket(new ExBR_PremiumState(activeChar, activeChar.hasBonus()));

    activeChar.getMacroses().sendUpdate();
    activeChar.sendPacket(new IStaticPacket[] { new SSQInfo(), new HennaInfo(activeChar) });
    activeChar.sendItemList(false);
    activeChar.sendPacket(new IStaticPacket[] { new ShortCutInit(activeChar), new SkillList(activeChar), new SkillCoolTime(activeChar) });
    activeChar.sendPacket(SystemMsg.WELCOME_TO_THE_WORLD_OF_LINEAGE_II);

    Announcements.getInstance().showAnnouncements(activeChar);

    if (first) {
      activeChar.getListeners().onEnter();
    }
    SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);

    if ((first) && (activeChar.getCreateTime() > 0L))
    {
      Calendar create = Calendar.getInstance();
      create.setTimeInMillis(activeChar.getCreateTime());
      Calendar now = Calendar.getInstance();

      int day = create.get(5);
      if ((create.get(2) == 1) && (day == 29)) {
        day = 28;
      }
      int myBirthdayReceiveYear = activeChar.getVarInt("MyBirthdayReceiveYear", 0);
      if ((create.get(2) == now.get(2)) && (create.get(5) == day))
      {
        if (((myBirthdayReceiveYear == 0) && (create.get(1) != now.get(1))) || ((myBirthdayReceiveYear > 0) && (myBirthdayReceiveYear != now.get(1))))
        {
          Mail mail = new Mail();
          mail.setSenderId(1);
          mail.setSenderName(StringHolder.getInstance().getNotNull(activeChar, "birthday.npc"));
          mail.setReceiverId(activeChar.getObjectId());
          mail.setReceiverName(activeChar.getName());
          mail.setTopic(StringHolder.getInstance().getNotNull(activeChar, "birthday.title"));
          mail.setBody(StringHolder.getInstance().getNotNull(activeChar, "birthday.text"));

          ItemInstance item = ItemFunctions.createItem(21169);
          item.setLocation(ItemInstance.ItemLocation.MAIL);
          item.setCount(1L);
          item.save();

          mail.addAttachment(item);
          mail.setUnread(true);
          mail.setType(Mail.SenderType.BIRTHDAY);
          mail.setExpireTime(2592000 + (int)(System.currentTimeMillis() / 1000L));
          mail.save();

          activeChar.setVar("MyBirthdayReceiveYear", String.valueOf(now.get(1)), -1L);
        }
      }
    }

    if (activeChar.getClan() != null)
    {
      notifyClanMembers(activeChar);

      activeChar.sendPacket(activeChar.getClan().listAll());
      activeChar.sendPacket(new IStaticPacket[] { new PledgeShowInfoUpdate(activeChar.getClan()), new PledgeSkillList(activeChar.getClan()) });
    }

    if ((first) && (Config.ALLOW_WEDDING))
    {
      CoupleManager.getInstance().engage(activeChar);
      CoupleManager.getInstance().notifyPartner(activeChar);
    }

    if (first)
    {
      activeChar.getFriendList().notifyFriends(true);
      loadTutorial(activeChar);
      activeChar.restoreDisableSkills();
    }

    sendPacket(new L2GameServerPacket[] { new L2FriendList(activeChar), new ExStorageMaxCount(activeChar), new QuestList(activeChar), new ExBasicActionList(activeChar), new EtcStatusUpdate(activeChar) });

    activeChar.checkHpMessages(activeChar.getMaxHp(), activeChar.getCurrentHp());
    activeChar.checkDayNightMessages();

    if (Config.PETITIONING_ALLOWED) {
      PetitionManager.getInstance().checkPetitionMessages(activeChar);
    }
    if (!first)
    {
      if (activeChar.isCastingNow())
      {
        Creature castingTarget = activeChar.getCastingTarget();
        Skill castingSkill = activeChar.getCastingSkill();
        long animationEndTime = activeChar.getAnimationEndTime();
        if ((castingSkill != null) && (castingTarget != null) && (castingTarget.isCreature()) && (activeChar.getAnimationEndTime() > 0L)) {
          sendPacket(new MagicSkillUse(activeChar, castingTarget, castingSkill.getId(), castingSkill.getLevel(), (int)(animationEndTime - System.currentTimeMillis()), 0L));
        }
      }
      if (activeChar.isInBoat()) {
        activeChar.sendPacket(activeChar.getBoat().getOnPacket(activeChar, activeChar.getInBoatPosition()));
      }
      if ((activeChar.isMoving) || (activeChar.isFollow)) {
        sendPacket(activeChar.movePacket());
      }
      if (activeChar.getMountNpcId() != 0) {
        sendPacket(new Ride(activeChar));
      }
      if (activeChar.isFishing()) {
        activeChar.stopFishing();
      }
    }
    activeChar.entering = false;
    activeChar.sendUserInfo(true);

    if (activeChar.isSitting())
      activeChar.sendPacket(new ChangeWaitType(activeChar, 0));
    if (activeChar.getPrivateStoreType() != 0) {
      if (activeChar.getPrivateStoreType() == 3)
        sendPacket(new PrivateStoreMsgBuy(activeChar));
      else if ((activeChar.getPrivateStoreType() == 1) || (activeChar.getPrivateStoreType() == 8))
        sendPacket(new PrivateStoreMsgSell(activeChar));
      else if (activeChar.getPrivateStoreType() == 5)
        sendPacket(new RecipeShopMsg(activeChar));
    }
    if (activeChar.isDead()) {
      sendPacket(new Die(activeChar));
    }
    activeChar.unsetVar("offline");

    activeChar.sendActionFailed();

    if ((first) && (activeChar.isGM()) && (Config.SAVE_GM_EFFECTS) && (activeChar.getPlayerAccess().CanUseGMCommand))
    {
      if (activeChar.getVarB("gm_silence"))
      {
        activeChar.setMessageRefusal(true);
        activeChar.sendPacket(SystemMsg.MESSAGE_REFUSAL_MODE);
      }

      if (activeChar.getVarB("gm_invul"))
      {
        activeChar.setIsInvul(true);
        activeChar.startAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
        activeChar.sendMessage(activeChar.getName() + " is now immortal.");
      }

      try
      {
        int var_gmspeed = Integer.parseInt(activeChar.getVar("gm_gmspeed"));
        if ((var_gmspeed >= 1) && (var_gmspeed <= 4))
          activeChar.doCast(SkillTable.getInstance().getInfo(7029, var_gmspeed), activeChar, true);
      }
      catch (Exception E)
      {
      }
    }
    PlayerMessageStack.getInstance().CheckMessages(activeChar);

    sendPacket(new L2GameServerPacket[] { ClientSetTime.STATIC, new ExSetCompassZoneCode(activeChar) });

    Pair entry = activeChar.getAskListener(false);
    if ((entry != null) && ((entry.getValue() instanceof ReviveAnswerListener))) {
      sendPacket(((ConfirmDlg)new ConfirmDlg(SystemMsg.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU, 0).addString("Other player")).addString("some"));
    }
    if (activeChar.isCursedWeaponEquipped()) {
      CursedWeaponsManager.getInstance().showUsageTime(activeChar, activeChar.getCursedWeaponEquippedId());
    }
    if (!first)
    {
      if (activeChar.isInObserverMode())
      {
        if (activeChar.getObserverMode() == 2) {
          activeChar.returnFromObserverMode();
        }
        else if (activeChar.getOlympiadObserveGame() != null)
          activeChar.leaveOlympiadObserverMode(true);
        else
          activeChar.leaveObserverMode();
      }
      else if (activeChar.isVisible()) {
        World.showObjectsToPlayer(activeChar);
      }
      if (activeChar.getPet() != null) {
        sendPacket(new PetInfo(activeChar.getPet()));
      }
      if (activeChar.isInParty())
      {
        sendPacket(new PartySmallWindowAll(activeChar.getParty(), activeChar));

        for (Player member : activeChar.getParty().getPartyMembers()) {
          if (member != activeChar)
          {
            sendPacket(new PartySpelled(member, true));
            Summon member_pet;
            if ((member_pet = member.getPet()) != null) {
              sendPacket(new PartySpelled(member_pet, true));
            }
            sendPacket(RelationChanged.update(activeChar, member, activeChar));
          }
        }

        if (activeChar.getParty().isInCommandChannel()) {
          sendPacket(ExMPCCOpen.STATIC);
        }
      }
      for (Iterator i$ = activeChar.getAutoSoulShot().iterator(); i$.hasNext(); ) { int shotId = ((Integer)i$.next()).intValue();
        sendPacket(new ExAutoSoulShot(shotId, true));
      }
      for (Effect e : activeChar.getEffectList().getAllFirstEffects()) {
        if (e.getSkill().isToggle())
          sendPacket(new MagicSkillLaunched(activeChar.getObjectId(), e.getSkill().getId(), e.getSkill().getLevel(), activeChar));
      }
      activeChar.broadcastCharInfo();
    }
    else {
      activeChar.sendUserInfo();
    }
    activeChar.updateEffectIcons();
    activeChar.updateStats();

    if (Config.ALT_PCBANG_POINTS_ENABLED) {
      activeChar.sendPacket(new ExPCCafePointInfo(activeChar, 0, 1, 2, 12));
    }
    if (!activeChar.getPremiumItemList().isEmpty()) {
      activeChar.sendPacket(Config.GOODS_INVENTORY_ENABLED ? ExGoodsInventoryChangedNotify.STATIC : ExNotifyPremiumItem.STATIC);
    }
    activeChar.sendVoteSystemInfo();
    activeChar.sendPacket(new ExReceiveShowPostFriend(activeChar));
    activeChar.getNevitSystem().onEnterWorld();

    checkNewMail(activeChar);
  }

  private static void notifyClanMembers(Player activeChar)
  {
    Clan clan = activeChar.getClan();
    SubUnit subUnit = activeChar.getSubUnit();
    if ((clan == null) || (subUnit == null)) {
      return;
    }
    UnitMember member = subUnit.getUnitMember(activeChar.getObjectId());
    if (member == null) {
      return;
    }
    member.setPlayerInstance(activeChar, false);

    int sponsor = activeChar.getSponsor();
    int apprentice = activeChar.getApprentice();
    L2GameServerPacket msg = new SystemMessage2(SystemMsg.CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME).addName(activeChar);
    PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(activeChar);
    for (Player clanMember : clan.getOnlineMembers(activeChar.getObjectId()))
    {
      clanMember.sendPacket(memberUpdate);
      if (clanMember.getObjectId() == sponsor)
        clanMember.sendPacket(new SystemMessage2(SystemMsg.YOUR_APPRENTICE_C1_HAS_LOGGED_OUT).addName(activeChar));
      else if (clanMember.getObjectId() == apprentice)
        clanMember.sendPacket(new SystemMessage2(SystemMsg.YOUR_SPONSOR_C1_HAS_LOGGED_IN).addName(activeChar));
      else {
        clanMember.sendPacket(msg);
      }
    }
    if (!activeChar.isClanLeader()) {
      return;
    }
    ClanHall clanHall = clan.getHasHideout() > 0 ? (ClanHall)ResidenceHolder.getInstance().getResidence(ClanHall.class, clan.getHasHideout()) : null;
    if ((clanHall == null) || (clanHall.getAuctionLength() != 0)) {
      return;
    }
    if (clanHall.getSiegeEvent().getClass() != ClanHallAuctionEvent.class) {
      return;
    }
    if (clan.getWarehouse().getCountOf(57) < clanHall.getRentalFee())
      activeChar.sendPacket(new SystemMessage2(SystemMsg.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_ME_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW).addLong(clanHall.getRentalFee()));
  }

  private void loadTutorial(Player player)
  {
    Quest q = QuestManager.getQuest(255);
    if (q != null)
      player.processQuestEvent(q.getName(), "UC", null);
  }

  private void checkNewMail(Player activeChar)
  {
    for (Mail mail : MailDAO.getInstance().getReceivedMailByOwnerId(activeChar.getObjectId()))
      if (mail.isUnread())
      {
        sendPacket(ExNoticePostArrived.STATIC_FALSE);
        break;
      }
  }
}