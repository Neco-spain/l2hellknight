package l2.brick.gameserver.network.clientpackets;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import l2.brick.Base64;
import l2.brick.Config;
import l2.brick.gameserver.Announcements;
import l2.brick.gameserver.GmListTable;
import l2.brick.gameserver.LoginServerThread;
import l2.brick.gameserver.SevenSigns;
import l2.brick.gameserver.TaskPriority;
import l2.brick.gameserver.ThreadPoolManager;
import l2.brick.gameserver.cache.HtmCache;
import l2.brick.gameserver.communityboard.RegionBBSManager;
import l2.brick.gameserver.datatables.AdminCommandAccessRights;
import l2.brick.gameserver.datatables.GMSkillTable;
import l2.brick.gameserver.datatables.SkillTable;
import l2.brick.gameserver.instancemanager.BotManager;
import l2.brick.gameserver.instancemanager.CHSiegeManager;
import l2.brick.gameserver.instancemanager.CastleManager;
import l2.brick.gameserver.instancemanager.ClanHallManager;
import l2.brick.gameserver.instancemanager.CoupleManager;
import l2.brick.gameserver.instancemanager.CursedWeaponsManager;
import l2.brick.gameserver.instancemanager.DimensionalRiftManager;
import l2.brick.gameserver.instancemanager.FortManager;
import l2.brick.gameserver.instancemanager.FortSiegeManager;
import l2.brick.gameserver.instancemanager.InstanceManager;
import l2.brick.gameserver.instancemanager.MailManager;
import l2.brick.gameserver.instancemanager.MapRegionManager;
import l2.brick.gameserver.instancemanager.PetitionManager;
import l2.brick.gameserver.instancemanager.QuestManager;
import l2.brick.gameserver.instancemanager.SiegeManager;
import l2.brick.gameserver.instancemanager.TerritoryWarManager;
import l2.brick.gameserver.model.L2Clan;
import l2.brick.gameserver.model.L2ItemInstance;
import l2.brick.gameserver.model.L2Object;
import l2.brick.gameserver.model.L2World;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.instance.L2ClassMasterInstance;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.entity.CTF;
import l2.brick.gameserver.model.entity.Couple;
import l2.brick.gameserver.model.entity.DM;
import l2.brick.gameserver.model.entity.Fort;
import l2.brick.gameserver.model.entity.FortSiege;
import l2.brick.gameserver.model.entity.L2Event;
import l2.brick.gameserver.model.entity.LMEvent;
import l2.brick.gameserver.model.entity.Siege;
import l2.brick.gameserver.model.entity.TvTEvent;
import l2.brick.gameserver.model.entity.TvTRoundEvent;
import l2.brick.gameserver.model.entity.clanhall.AuctionableHall;
import l2.brick.gameserver.model.entity.clanhall.SiegableHall;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.network.MultiBoxProtection;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.communityserver.CommunityServerThread;
import l2.brick.gameserver.network.communityserver.writepackets.WorldInfo;
import l2.brick.gameserver.network.serverpackets.CreatureSay;
import l2.brick.gameserver.network.serverpackets.Die;
import l2.brick.gameserver.network.serverpackets.EtcStatusUpdate;
import l2.brick.gameserver.network.serverpackets.ExAutoSoulShot;
import l2.brick.gameserver.network.serverpackets.ExBasicActionList;
import l2.brick.gameserver.network.serverpackets.ExBirthdayPopup;
import l2.brick.gameserver.network.serverpackets.ExGetBookMarkInfoPacket;
import l2.brick.gameserver.network.serverpackets.ExNoticePostArrived;
import l2.brick.gameserver.network.serverpackets.ExNotifyPremiumItem;
import l2.brick.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2.brick.gameserver.network.serverpackets.ExShowScreenMessage;
import l2.brick.gameserver.network.serverpackets.ExStorageMaxCount;
import l2.brick.gameserver.network.serverpackets.ExVoteSystemInfo;
import l2.brick.gameserver.network.serverpackets.FriendList;
import l2.brick.gameserver.network.serverpackets.HennaInfo;
import l2.brick.gameserver.network.serverpackets.ItemList;
import l2.brick.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.brick.gameserver.network.serverpackets.PledgeShowMemberListAll;
import l2.brick.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import l2.brick.gameserver.network.serverpackets.PledgeSkillList;
import l2.brick.gameserver.network.serverpackets.PledgeStatusChanged;
import l2.brick.gameserver.network.serverpackets.QuestList;
import l2.brick.gameserver.network.serverpackets.ShortCutInit;
import l2.brick.gameserver.network.serverpackets.SkillCoolTime;
import l2.brick.gameserver.network.serverpackets.SystemMessage;
import l2.brick.gameserver.templates.L2Item;

public class EnterWorld extends L2GameClientPacket
{
	private static final String _C__03_ENTERWORLD = "[C] 03 EnterWorld";
	
	private static Logger _log = Logger.getLogger(EnterWorld.class.getName());
	
	private int[][] tracert = new int[5][4];
	
	public TaskPriority getPriority()
	{
		return TaskPriority.PR_URGENT;
	}
	
	@Override
	protected void readImpl()
	{
		readB(new byte[32]);	// Unknown Byte Array
		readD();				// Unknown Value
		readD();				// Unknown Value
		readD();				// Unknown Value
		readD();				// Unknown Value
		readB(new byte[32]);	// Unknown Byte Array
		readD();				// Unknown Value
		for (int i = 0; i < 5; i++)
			for (int o = 0; o < 4; o++)
				tracert[i][o] = readC();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			_log.warning("EnterWorld failed! activeChar returned 'null'.");
			getClient().closeNow();
			return;
		}
		
		String[] adress = new String[5];
		for (int i = 0; i < 5; i++)
			adress[i] = tracert[i][0]+"."+tracert[i][1]+"."+tracert[i][2]+"."+tracert[i][3];
		
		LoginServerThread.getInstance().sendClientTracert(activeChar.getAccountName(), adress);
		
		getClient().setClientTracert(tracert);
		
		// Restore to instanced area if enabled
		if (Config.RESTORE_PLAYER_INSTANCE)
			activeChar.setInstanceId(InstanceManager.getInstance().getPlayerInstance(activeChar.getObjectId()));
		else
		{
			int instanceId = InstanceManager.getInstance().getPlayerInstance(activeChar.getObjectId());
			if (instanceId > 0)
				InstanceManager.getInstance().getInstance(instanceId).removePlayer(activeChar.getObjectId());
		}
		
		if (L2World.getInstance().findObject(activeChar.getObjectId()) != null)
		{
			if (Config.DEBUG)
				_log.warning("User already exists in Object ID map! User "+activeChar.getName()+" is a character clone.");
		}
		
		// Apply special GM properties to the GM when entering
		if (activeChar.isGM())
		{
			if (Config.GM_STARTUP_INVULNERABLE && AdminCommandAccessRights.getInstance().hasAccess("admin_invul", activeChar.getAccessLevel()))
				activeChar.setIsInvul(true);
			
			if (Config.GM_STARTUP_INVISIBLE && AdminCommandAccessRights.getInstance().hasAccess("admin_invisible", activeChar.getAccessLevel()))
				activeChar.getAppearance().setInvisible();
			
			if (Config.GM_STARTUP_SILENCE && AdminCommandAccessRights.getInstance().hasAccess("admin_silence", activeChar.getAccessLevel()))
				activeChar.setSilenceMode(true);
			
			if (Config.GM_STARTUP_DIET_MODE && AdminCommandAccessRights.getInstance().hasAccess("admin_diet", activeChar.getAccessLevel()))
			{
				activeChar.setDietMode(true);
				activeChar.refreshOverloaded();
			}
			
			if (Config.GM_STARTUP_AUTO_LIST && AdminCommandAccessRights.getInstance().hasAccess("admin_gmliston", activeChar.getAccessLevel()))
				GmListTable.getInstance().addGm(activeChar, false);
			else
				GmListTable.getInstance().addGm(activeChar, true);
			
			if (Config.GM_GIVE_SPECIAL_SKILLS)
				GMSkillTable.getInstance().addSkills(activeChar);
		}
		
		// Bot manager punishment
		if(Config.ENABLE_BOTREPORT)
			BotManager.getInstance().onEnter(activeChar);
		
		activeChar.loadSchemesFromDatabase();
		
		//set Hero character Color if enable 
		if(activeChar.isHero() && !activeChar.isGM() && Config.ALT_HERO_COLOR_ENABLED)
		activeChar.getAppearance().setNameColor(Config.ALT_HERO_COLOR);
		       
		//set Noble character Color if enable
		if(activeChar.isNoble() && !activeChar.isGM() && Config.ALT_NOBLE_COLOR_ENABLED)
		activeChar.getAppearance().setNameColor(Config.ALT_NOBLE_COLOR);
		
		// Clan Leader Color Name
		if (!activeChar.isGM() && (activeChar.getClan() != null && activeChar.isClanLeader() && Config.CLAN_LEADER_COLOR_ENABLED && activeChar.getClan().getLevel() >= Config.CLAN_LEADER_COLOR_CLAN_LEVEL))
		activeChar.getAppearance().setNameColor(Config.CLAN_LEADER_COLOR);
		
		// Set dead status if applies
		if (activeChar.getCurrentHp() < 0.5)
			activeChar.setIsDead(true);
		
		boolean showClanNotice = false;
		
		// Clan related checks are here
		if (activeChar.getClan() != null)
		{
			activeChar.sendPacket(new PledgeSkillList(activeChar.getClan()));
			
			notifyClanMembers(activeChar);
			
			notifySponsorOrApprentice(activeChar);
			
			AuctionableHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan());
			
			if (clanHall != null)
			{
				if (!clanHall.getPaid())
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW));
			}
			
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress())
					continue;
				
				if (siege.checkIsAttacker(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte)1);
					activeChar.setSiegeSide(siege.getCastle().getCastleId());
				}
				
				else if (siege.checkIsDefender(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte)2);
					activeChar.setSiegeSide(siege.getCastle().getCastleId());
				}
			}
			
			for (FortSiege siege : FortSiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress())
					continue;
				
				if (siege.checkIsAttacker(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte)1);
					activeChar.setSiegeSide(siege.getFort().getFortId());
				}
				
				else if (siege.checkIsDefender(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte)2);
					activeChar.setSiegeSide(siege.getFort().getFortId());
				}
			}
			for(SiegableHall hall : CHSiegeManager.getInstance().getConquerableHalls().values())
			{
				if(!hall.isInSiege())
					continue;
				
				if(hall.isRegistered(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte)1);
					activeChar.setSiegeSide(hall.getId());
				}
			}
			
			sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), activeChar));
			sendPacket(new PledgeStatusChanged(activeChar.getClan()));
			
			// Residential skills support
			if (activeChar.getClan().getHasCastle() > 0)
				CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).giveResidentialSkills(activeChar);
			
			if (activeChar.getClan().getHasFort() > 0)
				FortManager.getInstance().getFortByOwner(activeChar.getClan()).giveResidentialSkills(activeChar);
			
			showClanNotice = activeChar.getClan().isNoticeEnabled();
		}
		
		if (TerritoryWarManager.getInstance().getRegisteredTerritoryId(activeChar) > 0)
		{
			if (TerritoryWarManager.getInstance().isTWInProgress())
				activeChar.setSiegeState((byte)1);
			activeChar.setSiegeSide(TerritoryWarManager.getInstance().getRegisteredTerritoryId(activeChar));
		}
		
		// Updating Seal of Strife Buff/Debuff
		if (SevenSigns.getInstance().isSealValidationPeriod() && SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) != SevenSigns.CABAL_NULL)
		{
			int cabal = SevenSigns.getInstance().getPlayerCabal(activeChar.getObjectId());
			if (cabal != SevenSigns.CABAL_NULL)
			{
				if (cabal == SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE))
					activeChar.addSkill(SkillTable.FrequentSkill.THE_VICTOR_OF_WAR.getSkill());
				else
					activeChar.addSkill(SkillTable.FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill());
			}
		}
		else
		{
			activeChar.removeSkill(SkillTable.FrequentSkill.THE_VICTOR_OF_WAR.getSkill());
			activeChar.removeSkill(SkillTable.FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill());
		}
		
		if (Config.ENABLE_VITALITY && Config.RECOVER_VITALITY_ON_RECONNECT)
		{
			float points = Config.RATE_RECOVERY_ON_RECONNECT * (System.currentTimeMillis() - activeChar.getLastAccess()) / 60000;
			if (points > 0)
				activeChar.updateVitalityPoints(points, false, true);
		}
		
		activeChar.checkRecoBonusTask();
		
		activeChar.broadcastUserInfo();
		
		// Send Macro List
		activeChar.getMacroses().sendUpdate();
		
		// Send Item List
		sendPacket(new ItemList(activeChar, false));
		
		// Send GG check
		activeChar.queryGameGuard();
		
		// Send Teleport Bookmark List
		sendPacket(new ExGetBookMarkInfoPacket(activeChar));
		
		// Send Shortcuts
		sendPacket(new ShortCutInit(activeChar));
		
		// Send Action list
		activeChar.sendPacket(ExBasicActionList.getStaticPacket(activeChar));
		
		// Send Skill list
		activeChar.sendSkillList();
		
		// Send Dye Information
		activeChar.sendPacket(new HennaInfo(activeChar));

		//disable OfflineSupermode
		activeChar.setOfflineSuperMode(false);
  	
		Quest.playerEnter(activeChar);
		
		if (!Config.DISABLE_TUTORIAL)
			loadTutorial(activeChar);
		
		// PvP / Pk Color System
		if (activeChar.getPvpKills()>=(Config.PVP_AMOUNT1) && (Config.PVP_COLOR_SYSTEM_ENABLED)) activeChar.updatePvPColor(activeChar.getPvpKills());
		if (activeChar.getPkKills()>=(Config.PK_AMOUNT1) && (Config.PK_COLOR_SYSTEM_ENABLED)) activeChar.updatePkColor(activeChar.getPkKills());
		
		for (Quest quest : QuestManager.getInstance().getAllManagedScripts())
		{
			if (quest != null && quest.getOnEnterWorld())
				quest.notifyEnterWorld(activeChar);
		}
		activeChar.sendPacket(new QuestList());
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
			activeChar.setProtection(true);
		
		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		
		if (L2Event.active && L2Event.connectionLossData.containsKey(activeChar.getName()) && L2Event.isOnEvent(activeChar))
			L2Event.restoreChar(activeChar);
		else if (L2Event.connectionLossData.containsKey(activeChar.getName()))
			L2Event.restoreAndTeleChar(activeChar);
		
		// Wedding Checks
		if (Config.L2JMOD_ALLOW_WEDDING)
		{
			engage(activeChar);
			notifyPartner(activeChar,activeChar.getPartnerId());
		}
		
		if(Config.SHOW_WELCOME_PM)
		{
		        CreatureSay np = new CreatureSay(0, Say2.TELL,Config.PM_FROM,Config.PM_TEXT);  
		        activeChar.sendPacket(np); 
		}

		if (activeChar.isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().getCursedWeapon(activeChar.getCursedWeaponEquippedId()).cursedOnLogin();
		}
		
		activeChar.updateEffectIcons();
		
		if(Config.PC_BANG_ENABLED)
		{
			if (activeChar.getPcBangPoints() > 0)
				activeChar.sendPacket(new ExPCCafePointInfo(activeChar.getPcBangPoints(), 0, false, false, 1));
			else
				activeChar.sendPacket(new ExPCCafePointInfo());
		}
		
		activeChar.sendPacket(new EtcStatusUpdate(activeChar));
		
		//Expand Skill
		activeChar.sendPacket(new ExStorageMaxCount(activeChar));
		
		sendPacket(new FriendList(activeChar));
		
		activeChar.sendMessage("Server use L2Brick files " +
				"and developed by the L2Brick Team, " +
				"you can find our info about project on www.l2brick.funsite.cz");
		activeChar.sendMessage("L2Brick Team: RobikBobik, St3et, mjaniko, FFs");
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN);
		sm.addString(activeChar.getName());
		for (int id : activeChar.getFriendList())
		{
			L2Object obj = L2World.getInstance().findObject(id);
			if (obj != null)
				obj.sendPacket(sm);
		}
		
		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WELCOME_TO_LINEAGE));
		
		SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
		Announcements.getInstance().showAnnouncements(activeChar);
		
		if (showClanNotice)
		{
			NpcHtmlMessage notice = new NpcHtmlMessage(1);
			notice.setFile(activeChar.getHtmlPrefix(), "data/html/clanNotice.htm");
			notice.replace("%clan_name%", activeChar.getClan().getName());
			notice.replace("%notice_text%", activeChar.getClan().getNotice().replaceAll("\r\n", "<br>"));
			notice.disableValidation();
			sendPacket(notice);
		}
		else if (Config.SERVER_NEWS)
		{
			String serverNews = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/servnews.htm");
			if (serverNews != null)
				sendPacket(new NpcHtmlMessage(1, serverNews));
		}
		
		if (Config.PETITIONING_ALLOWED)
			PetitionManager.getInstance().checkPetitionMessages(activeChar);
		
		if (activeChar.isAlikeDead()) // dead or fake dead
		{
			// no broadcast needed since the player will already spawn dead to others
			sendPacket(new Die(activeChar));
		}
		
		activeChar.onPlayerEnter();
		
		sendPacket(new SkillCoolTime(activeChar));
		sendPacket(new ExVoteSystemInfo(activeChar));
		
		for (L2ItemInstance i : activeChar.getInventory().getItems())
		{
			if (i.isTimeLimitedItem())
				i.scheduleLifeTimeTask();
			if (i.isShadowItem() && i.isEquipped())
				i.decreaseMana(false);
		}
		
		for (L2ItemInstance i : activeChar.getWarehouse().getItems())
		{
			if (i.isTimeLimitedItem())
				i.scheduleLifeTimeTask();
		}
		
		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false))
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(activeChar);
		
		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED));
		
		// remove combat flag before teleporting
		if (activeChar.getInventory().getItemByItemId(9819) != null)
		{
			Fort fort = FortManager.getInstance().getFort(activeChar);
			
			if (fort != null)
				FortSiegeManager.getInstance().dropCombatFlag(activeChar, fort.getFortId());
			else
			{
				int slot = activeChar.getInventory().getSlotFromItem(activeChar.getInventory().getItemByItemId(9819));
				activeChar.getInventory().unEquipItemInBodySlot(slot);
				activeChar.destroyItem("CombatFlag", activeChar.getInventory().getItemByItemId(9819), null, true);
			}
		}
		
        // Remove Demonic Weapon if character is not Cursed Weapon Equipped 
		if (activeChar.getInventory().getItemByItemId(8190) != null && activeChar.isCursedWeaponEquipped() == false) 
			activeChar.destroyItem("Zariche", activeChar.getInventory().getItemByItemId(8190), null, true); 
		if (activeChar.getInventory().getItemByItemId(8689) != null && activeChar.isCursedWeaponEquipped() == false) 
			activeChar.destroyItem("Akamanah", activeChar.getInventory().getItemByItemId(8689), null, true); 
	 	
		// Attacker or spectator logging in to a siege zone. Actually should be checked for inside castle only?
		if (!activeChar.isGM()
				// inside siege zone
				&& activeChar.isInsideZone(L2Character.ZONE_SIEGE)
				// but non-participant or attacker
				&& (!activeChar.isInSiege() || activeChar.getSiegeState() < 2))
			activeChar.teleToLocation(MapRegionManager.TeleportWhereType.Town);
		
		if (Config.ALLOW_MAIL)
		{
			if (MailManager.getInstance().hasUnreadPost(activeChar))
				sendPacket(ExNoticePostArrived.valueOf(false));
		}
		
		RegionBBSManager.getInstance().changeCommunityBoard();
		CommunityServerThread.getInstance().sendPacket(new WorldInfo(activeChar, null, WorldInfo.TYPE_UPDATE_PLAYER_STATUS));
		
		TvTEvent.onLogin(activeChar);
		LMEvent.onLogin(activeChar);
		DM.onLogin(activeChar);
		TvTRoundEvent.onLogin(activeChar);
				
		if (Config.WELCOME_MESSAGE_ENABLED)
			activeChar.sendPacket(new ExShowScreenMessage(Config.WELCOME_MESSAGE_TEXT, Config.WELCOME_MESSAGE_TIME));
		
		if (Config.WELCOME_ADVANCE_MESSAGE_ENABLED)
			
            switch(activeChar.getRace()){
            
            case Human:
                
                activeChar.sendPacket(new ExShowScreenMessage(Config.WELCOME_ADVANCE_HUMAN_MESSAGE_TEXT, Config.WELCOME_ADVANCE_MESSAGE_TIME));           
                break;
                
            case DarkElf:
            
            	activeChar.sendPacket(new ExShowScreenMessage(Config.WELCOME_ADVANCE_DARK_ELF_MESSAGE_TEXT, Config.WELCOME_ADVANCE_MESSAGE_TIME));           
            	break;

            case Dwarf:
            
            	activeChar.sendPacket(new ExShowScreenMessage(Config.WELCOME_ADVANCE_DWARF_MESSAGE_TEXT, Config.WELCOME_ADVANCE_MESSAGE_TIME));
            	break;

            case Elf:
            
            	activeChar.sendPacket(new ExShowScreenMessage(Config.WELCOME_ADVANCE_ELF_MESSAGE_TEXT, Config.WELCOME_ADVANCE_MESSAGE_TIME));
            	break;

            case Kamael:
            
            	activeChar.sendPacket(new ExShowScreenMessage(Config.WELCOME_ADVANCE_KAMAEL_MESSAGE_TEXT, Config.WELCOME_ADVANCE_MESSAGE_TIME));
            	break;

            case Orc:
            
            	activeChar.sendPacket(new ExShowScreenMessage(Config.WELCOME_ADVANCE_ORC_MESSAGE_TEXT, Config.WELCOME_ADVANCE_MESSAGE_TIME));
            	break;

            }
		
		L2ClassMasterInstance.showQuestionMark(activeChar);
		
		if (CTF._savePlayers.contains(activeChar.getName()))
		{
			CTF.addDisconnectedPlayer(activeChar);
		}
		
		int birthday = activeChar.checkBirthDay();
		if (birthday == 0)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_BIRTHDAY_GIFT_HAS_ARRIVED));
			activeChar.sendPacket(new ExBirthdayPopup());
		}
		else if (birthday != -1)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.THERE_ARE_S1_DAYS_UNTIL_YOUR_CHARACTERS_BIRTHDAY);
			sm.addString(Integer.toString(birthday));
			activeChar.sendPacket(sm);
		}
			
		if (Config.ALLOW_MAX_PLAYERS_FROM_ONE_PC) 
		{
			if (Config.MAX_PLAYERS_FROM_ONE_PC > 0) 
			{
				if (!MultiBoxProtection.getInstance().registerNewConnection(getClient())) 
				{
					activeChar.sendMessage("Player "+ activeChar.getName()+ " Too many connections, please logout other characters first.");
					ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
					public void run() 
                           {
								activeChar.logout(false);
								}
					
							}, 
						400);
					return;
				}
			}
		}
		
		if(!activeChar.getPremiumItemList().isEmpty())
			activeChar.sendPacket(new ExNotifyPremiumItem());
		
		if (Config.AUTO_ACTIVATE_SHOTS)
			verifyAndLoadShots(activeChar);
	}
	
	/**
	 * @param activeChar
	 */
	private void engage(L2PcInstance cha)
	{
		int _chaid = cha.getObjectId();
		
		for(Couple cl: CoupleManager.getInstance().getCouples())
		{
			if (cl.getPlayer1Id()==_chaid || cl.getPlayer2Id()==_chaid)
			{
				if (cl.getMaried())
					cha.setMarried(true);
				
				cha.setCoupleId(cl.getId());
				
				if (cl.getPlayer1Id()==_chaid)
					cha.setPartnerId(cl.getPlayer2Id());
				
				else
					cha.setPartnerId(cl.getPlayer1Id());
			}
		}
	}
	
	/**
	 * @param activeChar partnerid
	 */
	private void notifyPartner(L2PcInstance cha, int partnerId)
	{
		if (cha.getPartnerId() != 0)
		{
			int objId = cha.getPartnerId();
			
			try
			{
				L2PcInstance partner = L2World.getInstance().getPlayer(objId);
				
				if (partner != null)
					partner.sendMessage("Your Partner has logged in.");
				
				partner = null;
			}
			catch (ClassCastException cce)
			{
				_log.warning("Wedding Error: ID "+objId+" is now owned by a(n) "+L2World.getInstance().findObject(objId).getClass().getSimpleName());
			}
		}
	}
	
	/**
	 * @param activeChar
	 */
	private void notifyClanMembers(L2PcInstance activeChar)
	{
		L2Clan clan = activeChar.getClan();
		
		// This null check may not be needed anymore since notifyClanMembers is called from within a null check already. Please remove if we're certain it's ok to do so.
		if (clan != null)
		{
			clan.getClanMember(activeChar.getObjectId()).setPlayerInstance(activeChar);
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN);
			msg.addString(activeChar.getName());
			clan.broadcastToOtherOnlineMembers(msg, activeChar);
			msg = null;
			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(activeChar), activeChar);
		}
	}
	
	/**
	 * @param activeChar
	 */
	private void notifySponsorOrApprentice(L2PcInstance activeChar)
	{
		if (activeChar.getSponsor() != 0)
		{
			L2PcInstance sponsor = L2World.getInstance().getPlayer(activeChar.getSponsor());
			
			if (sponsor != null)
			{
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				sponsor.sendPacket(msg);
			}
		}
		else if (activeChar.getApprentice() != 0)
		{
			L2PcInstance apprentice = L2World.getInstance().getPlayer(activeChar.getApprentice());
			
			if (apprentice != null)
			{
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOUR_SPONSOR_C1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				apprentice.sendPacket(msg);
			}
		}
	}
	
	/**
	 * @param string
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unused")
	private String getText(String string)
	{
		try
		{
			String result = new String(Base64.decode(string), "UTF-8");
			return result;
		}
		catch (UnsupportedEncodingException e)
		{
			return null;
		}
	}
	
	private void loadTutorial(L2PcInstance player)
	{
		QuestState qs = player.getQuestState("255_Tutorial");
		
		if (qs != null)
			qs.getQuest().notifyEvent("UC", null, player);
	}
	
	/**
	 * This method will get the correct soulshot/spirishot and activate it for the current weapon if it's over the minimum.
	 * @param activeChar
	 * @author Zoey76
	 */
	private void verifyAndLoadShots(L2PcInstance activeChar)
	{
		int soulId = -1;
		int spiritId = -1;
		int bspiritId = -1;
		
		if (!activeChar.isDead() && activeChar.getActiveWeaponItem() != null)
		{
			switch (activeChar.getActiveWeaponItem().getCrystalType())
			{
				case L2Item.CRYSTAL_NONE:
					soulId = 1835;
					spiritId = 2509;
					bspiritId = 3947;
					break;
				case L2Item.CRYSTAL_D:
					soulId = 1463;
					spiritId = 2510;
					bspiritId = 3948;
					break;
				case L2Item.CRYSTAL_C:
					soulId = 1464;
					spiritId = 2511;
					bspiritId = 3949;
					break;
				case L2Item.CRYSTAL_B:
					soulId = 1465;
					spiritId = 2512;
					bspiritId = 3950;
					break;
				case L2Item.CRYSTAL_A:
					soulId = 1466;
					spiritId = 2513;
					bspiritId = 3951;
					break;
				case L2Item.CRYSTAL_S:
				case L2Item.CRYSTAL_S80:
				case L2Item.CRYSTAL_S84:
					soulId = 1467;
					spiritId = 2514;
					bspiritId = 3952;
					break;
			}
			
			if ((soulId > -1) && activeChar.getInventory().getInventoryItemCount(soulId, -1) > Config.AUTO_ACTIVATE_SHOTS_MIN)
			{
				activeChar.addAutoSoulShot(soulId);
				activeChar.sendPacket(new ExAutoSoulShot(soulId, 1));
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO);
				sm.addItemName(activeChar.getInventory().getItemByItemId(soulId));
				activeChar.sendPacket(sm);
			}
			
			if ((bspiritId > -1) && activeChar.getInventory().getInventoryItemCount(bspiritId, -1) > Config.AUTO_ACTIVATE_SHOTS_MIN)
			{
				activeChar.addAutoSoulShot(bspiritId);
				activeChar.sendPacket(new ExAutoSoulShot(bspiritId, 1));
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO);
				sm.addItemName(activeChar.getInventory().getItemByItemId(bspiritId));
				activeChar.sendPacket(sm);
			}
			else if ((spiritId > -1) && activeChar.getInventory().getInventoryItemCount(spiritId, -1) > Config.AUTO_ACTIVATE_SHOTS_MIN)
			{
				activeChar.addAutoSoulShot(spiritId);
				activeChar.sendPacket(new ExAutoSoulShot(spiritId, 1));
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO);
				sm.addItemName(activeChar.getInventory().getItemByItemId(spiritId));
				activeChar.sendPacket(sm);
			}
			
			activeChar.rechargeAutoSoulShot(true, true, false);
		}
	}
	
	/* (non-Javadoc)
	 * @see l2.brick.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__03_ENTERWORLD;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
