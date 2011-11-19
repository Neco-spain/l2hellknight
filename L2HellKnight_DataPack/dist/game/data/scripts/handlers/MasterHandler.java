/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers;

import handlers.actionhandlers.L2ArtefactInstanceAction;
import handlers.actionhandlers.L2DecoyAction;
import handlers.actionhandlers.L2DoorInstanceAction;
import handlers.actionhandlers.L2DoorInstanceActionShift;
import handlers.actionhandlers.L2ItemInstanceAction;
import handlers.actionhandlers.L2ItemInstanceActionShift;
import handlers.actionhandlers.L2NpcAction;
import handlers.actionhandlers.L2NpcActionShift;
import handlers.actionhandlers.L2PcInstanceAction;
import handlers.actionhandlers.L2PcInstanceActionShift;
import handlers.actionhandlers.L2PetInstanceAction;
import handlers.actionhandlers.L2StaticObjectInstanceAction;
import handlers.actionhandlers.L2StaticObjectInstanceActionShift;
import handlers.actionhandlers.L2SummonAction;
import handlers.actionhandlers.L2SummonActionShift;
import handlers.actionhandlers.L2TrapAction;
import handlers.admincommandhandlers.AdminAdmin;
import handlers.admincommandhandlers.AdminAnnouncements;
import handlers.admincommandhandlers.AdminBBS;
import handlers.admincommandhandlers.AdminBan;
import handlers.admincommandhandlers.AdminBuffs;
import handlers.admincommandhandlers.AdminCHSiege;
import handlers.admincommandhandlers.AdminCache;
import handlers.admincommandhandlers.AdminCamera;
import handlers.admincommandhandlers.AdminChangeAccessLevel;
import handlers.admincommandhandlers.AdminClan;
import handlers.admincommandhandlers.AdminCreateItem;
import handlers.admincommandhandlers.AdminCursedWeapons;
import handlers.admincommandhandlers.AdminDebug;
import handlers.admincommandhandlers.AdminDelete;
import handlers.admincommandhandlers.AdminDisconnect;
import handlers.admincommandhandlers.AdminDoorControl;
import handlers.admincommandhandlers.AdminEditChar;
import handlers.admincommandhandlers.AdminEditNpc;
import handlers.admincommandhandlers.AdminEffects;
import handlers.admincommandhandlers.AdminElement;
import handlers.admincommandhandlers.AdminEnchant;
import handlers.admincommandhandlers.AdminEventEngine;
import handlers.admincommandhandlers.AdminEvents;
import handlers.admincommandhandlers.AdminExpSp;
import handlers.admincommandhandlers.AdminFightCalculator;
import handlers.admincommandhandlers.AdminFortSiege;
import handlers.admincommandhandlers.AdminGeoEditor;
import handlers.admincommandhandlers.AdminGeodata;
import handlers.admincommandhandlers.AdminGm;
import handlers.admincommandhandlers.AdminGmChat;
import handlers.admincommandhandlers.AdminGraciaSeeds;
import handlers.admincommandhandlers.AdminHeal;
import handlers.admincommandhandlers.AdminHellbound;
import handlers.admincommandhandlers.AdminHelpPage;
import handlers.admincommandhandlers.AdminInstance;
import handlers.admincommandhandlers.AdminInstanceZone;
import handlers.admincommandhandlers.AdminInvul;
import handlers.admincommandhandlers.AdminKick;
import handlers.admincommandhandlers.AdminKill;
import handlers.admincommandhandlers.AdminLevel;
import handlers.admincommandhandlers.AdminLogin;
import handlers.admincommandhandlers.AdminMammon;
import handlers.admincommandhandlers.AdminManor;
import handlers.admincommandhandlers.AdminMenu;
import handlers.admincommandhandlers.AdminMessages;
import handlers.admincommandhandlers.AdminMobGroup;
import handlers.admincommandhandlers.AdminMonsterRace;
import handlers.admincommandhandlers.AdminPForge;
import handlers.admincommandhandlers.AdminPathNode;
import handlers.admincommandhandlers.AdminPetition;
import handlers.admincommandhandlers.AdminPledge;
import handlers.admincommandhandlers.AdminPolymorph;
import handlers.admincommandhandlers.AdminPremium;
import handlers.admincommandhandlers.AdminQuest;
import handlers.admincommandhandlers.AdminRepairChar;
import handlers.admincommandhandlers.AdminRes;
import handlers.admincommandhandlers.AdminRide;
import handlers.admincommandhandlers.AdminShop;
import handlers.admincommandhandlers.AdminShowQuests;
import handlers.admincommandhandlers.AdminShutdown;
import handlers.admincommandhandlers.AdminSiege;
import handlers.admincommandhandlers.AdminSkill;
import handlers.admincommandhandlers.AdminSpawn;
import handlers.admincommandhandlers.AdminSummon;
import handlers.admincommandhandlers.AdminTarget;
import handlers.admincommandhandlers.AdminTeleport;
import handlers.admincommandhandlers.AdminTerritoryWar;
import handlers.admincommandhandlers.AdminTest;
import handlers.admincommandhandlers.AdminTvTEvent;
import handlers.admincommandhandlers.AdminUnblockIp;
import handlers.admincommandhandlers.AdminVitality;
import handlers.admincommandhandlers.AdminZone;
import handlers.bypasshandlers.ArenaBuff;
import handlers.bypasshandlers.Augment;
import handlers.bypasshandlers.BloodAlliance;
import handlers.bypasshandlers.Buy;
import handlers.bypasshandlers.BuyShadowItem;
import handlers.bypasshandlers.ChatLink;
import handlers.bypasshandlers.ClanWarehouse;
import handlers.bypasshandlers.DrawHenna;
import handlers.bypasshandlers.EventEngine;
import handlers.bypasshandlers.Festival;
import handlers.bypasshandlers.FortSiege;
import handlers.bypasshandlers.Freight;
import handlers.bypasshandlers.ItemAuctionLink;
import handlers.bypasshandlers.Leaderboard;
import handlers.bypasshandlers.Link;
import handlers.bypasshandlers.Loto;
import handlers.bypasshandlers.ManorManager;
import handlers.bypasshandlers.Multisell;
import handlers.bypasshandlers.Observation;
import handlers.bypasshandlers.OlympiadManagerLink;
import handlers.bypasshandlers.OlympiadObservation;
import handlers.bypasshandlers.PlayerHelp;
import handlers.bypasshandlers.PrivateWarehouse;
import handlers.bypasshandlers.QuestLink;
import handlers.bypasshandlers.QuestList;
import handlers.bypasshandlers.ReceivePremium;
import handlers.bypasshandlers.ReleaseAttribute;
import handlers.bypasshandlers.RemoveDeathPenalty;
import handlers.bypasshandlers.RemoveHennaList;
import handlers.bypasshandlers.RentPet;
import handlers.bypasshandlers.RideWyvern;
import handlers.bypasshandlers.Rift;
import handlers.bypasshandlers.SkillList;
import handlers.bypasshandlers.SupportBlessing;
import handlers.bypasshandlers.SupportMagic;
import handlers.bypasshandlers.TerritoryStatus;
import handlers.bypasshandlers.TerritoryWar;
import handlers.bypasshandlers.VoiceCommand;
import handlers.bypasshandlers.Wear;
import handlers.chathandlers.ChatAll;
import handlers.chathandlers.ChatAlliance;
import handlers.chathandlers.ChatBattlefield;
import handlers.chathandlers.ChatClan;
import handlers.chathandlers.ChatHeroVoice;
import handlers.chathandlers.ChatParty;
import handlers.chathandlers.ChatPartyMatchRoom;
import handlers.chathandlers.ChatPartyRoomAll;
import handlers.chathandlers.ChatPartyRoomCommander;
import handlers.chathandlers.ChatPetition;
import handlers.chathandlers.ChatShout;
import handlers.chathandlers.ChatTell;
import handlers.chathandlers.ChatTrade;
import handlers.itemhandlers.BeastSoulShot;
import handlers.itemhandlers.BeastSpice;
import handlers.itemhandlers.BeastSpiritShot;
import handlers.itemhandlers.BlessedSpiritShot;
import handlers.itemhandlers.Book;
import handlers.itemhandlers.Disguise;
import handlers.itemhandlers.Elixir;
import handlers.itemhandlers.EnchantAttribute;
import handlers.itemhandlers.EnchantScrolls;
import handlers.itemhandlers.EnergyStarStone;
import handlers.itemhandlers.EventItem;
import handlers.itemhandlers.ExtractableItems;
import handlers.itemhandlers.FishShots;
import handlers.itemhandlers.Harvester;
import handlers.itemhandlers.ItemSkills;
import handlers.itemhandlers.ItemSkillsTemplate;
import handlers.itemhandlers.ManaPotion;
import handlers.itemhandlers.Maps;
import handlers.itemhandlers.MercTicket;
import handlers.itemhandlers.NevitHourglass;
import handlers.itemhandlers.NicknameColor;
import handlers.itemhandlers.PaganKeys;
import handlers.itemhandlers.PetFood;
import handlers.itemhandlers.Recipes;
import handlers.itemhandlers.RollingDice;
import handlers.itemhandlers.ScrollOfResurrection;
import handlers.itemhandlers.Seed;
import handlers.itemhandlers.SevenSignsRecord;
import handlers.itemhandlers.SoulShots;
import handlers.itemhandlers.SpecialXMas;
import handlers.itemhandlers.SpiritShot;
import handlers.itemhandlers.SummonItems;
import handlers.itemhandlers.TeleportBookmark;
import handlers.skillhandlers.BalanceLife;
import handlers.skillhandlers.BallistaBomb;
import handlers.skillhandlers.BeastSkills;
import handlers.skillhandlers.Blow;
import handlers.skillhandlers.Cancel;
import handlers.skillhandlers.ChainHeal;
import handlers.skillhandlers.Charge;
import handlers.skillhandlers.CombatPointHeal;
import handlers.skillhandlers.Continuous;
import handlers.skillhandlers.CpDam;
import handlers.skillhandlers.CpDamPercent;
import handlers.skillhandlers.Craft;
import handlers.skillhandlers.DeluxeKey;
import handlers.skillhandlers.Detection;
import handlers.skillhandlers.Disablers;
import handlers.skillhandlers.Dummy;
import handlers.skillhandlers.Extractable;
import handlers.skillhandlers.Fishing;
import handlers.skillhandlers.FishingSkill;
import handlers.skillhandlers.GetPlayer;
import handlers.skillhandlers.GiveReco;
import handlers.skillhandlers.GiveSp;
import handlers.skillhandlers.GiveVitality;
import handlers.skillhandlers.Harvest;
import handlers.skillhandlers.Heal;
import handlers.skillhandlers.HealPercent;
import handlers.skillhandlers.InstantJump;
import handlers.skillhandlers.ManaHeal;
import handlers.skillhandlers.Manadam;
import handlers.skillhandlers.Mdam;
import handlers.skillhandlers.NornilsPower;
import handlers.skillhandlers.Pdam;
import handlers.skillhandlers.RefuelAirShip;
import handlers.skillhandlers.Resurrect;
import handlers.skillhandlers.ShiftTarget;
import handlers.skillhandlers.Soul;
import handlers.skillhandlers.Sow;
import handlers.skillhandlers.Spoil;
import handlers.skillhandlers.StealBuffs;
import handlers.skillhandlers.StrSiegeAssault;
import handlers.skillhandlers.SummonFriend;
import handlers.skillhandlers.Sweep;
import handlers.skillhandlers.TakeCastle;
import handlers.skillhandlers.TakeFort;
import handlers.skillhandlers.TransformDispel;
import handlers.skillhandlers.Trap;
import handlers.skillhandlers.Unlock;
import handlers.targethandlers.TargetAlly;
import handlers.targethandlers.TargetArea;
import handlers.targethandlers.TargetAreaCorpseMob;
import handlers.targethandlers.TargetAreaSummon;
import handlers.targethandlers.TargetAura;
import handlers.targethandlers.TargetAuraCorpseMob;
import handlers.targethandlers.TargetBehindArea;
import handlers.targethandlers.TargetBehindAura;
import handlers.targethandlers.TargetChainHeal;
import handlers.targethandlers.TargetClan;
import handlers.targethandlers.TargetClanMember;
import handlers.targethandlers.TargetCorpseAlly;
import handlers.targethandlers.TargetCorpseClan;
import handlers.targethandlers.TargetCorpseMob;
import handlers.targethandlers.TargetCorpsePet;
import handlers.targethandlers.TargetCorpsePlayer;
import handlers.targethandlers.TargetEnemySummon;
import handlers.targethandlers.TargetFlagPole;
import handlers.targethandlers.TargetFrontArea;
import handlers.targethandlers.TargetFrontAura;
import handlers.targethandlers.TargetGround;
import handlers.targethandlers.TargetHoly;
import handlers.targethandlers.TargetOne;
import handlers.targethandlers.TargetOwnerPet;
import handlers.targethandlers.TargetParty;
import handlers.targethandlers.TargetPartyClan;
import handlers.targethandlers.TargetPartyMember;
import handlers.targethandlers.TargetPartyNotMe;
import handlers.targethandlers.TargetPartyOther;
import handlers.targethandlers.TargetPet;
import handlers.targethandlers.TargetSelf;
import handlers.targethandlers.TargetSummon;
import handlers.targethandlers.TargetUnlockable;
import handlers.telnethandlers.ChatsHandler;
import handlers.telnethandlers.DebugHandler;
import handlers.telnethandlers.HelpHandler;
import handlers.telnethandlers.PlayerHandler;
import handlers.telnethandlers.ReloadHandler;
import handlers.telnethandlers.ServerHandler;
import handlers.telnethandlers.StatusHandler;
import handlers.telnethandlers.ThreadHandler;
import handlers.usercommandhandlers.Birthday;
import handlers.usercommandhandlers.ChannelDelete;
import handlers.usercommandhandlers.ChannelLeave;
import handlers.usercommandhandlers.ChannelListUpdate;
import handlers.usercommandhandlers.ClanPenalty;
import handlers.usercommandhandlers.ClanWarsList;
import handlers.usercommandhandlers.DisMount;
import handlers.usercommandhandlers.Escape;
import handlers.usercommandhandlers.InstanceZone;
import handlers.usercommandhandlers.Loc;
import handlers.usercommandhandlers.Mount;
import handlers.usercommandhandlers.OlympiadStat;
import handlers.usercommandhandlers.PartyInfo;
import handlers.usercommandhandlers.Time;
import handlers.voicedcommandhandlers.Banking;
import handlers.voicedcommandhandlers.BindingIP;
import handlers.voicedcommandhandlers.ChangePassword;
import handlers.voicedcommandhandlers.ChatAdmin;
import handlers.voicedcommandhandlers.Debug;
import handlers.voicedcommandhandlers.Experience;
import handlers.voicedcommandhandlers.Hellbound;
import handlers.voicedcommandhandlers.Lang;
import handlers.voicedcommandhandlers.Online;
import handlers.voicedcommandhandlers.TvTVoicedInfo;
import handlers.voicedcommandhandlers.Wedding;
import handlers.voicedcommandhandlers.stats;

import java.util.logging.Level;
import java.util.logging.Logger;

import l2.hellknight.Config;
import l2.hellknight.gameserver.handler.ActionHandler;
import l2.hellknight.gameserver.handler.AdminCommandHandler;
import l2.hellknight.gameserver.handler.BypassHandler;
import l2.hellknight.gameserver.handler.ChatHandler;
import l2.hellknight.gameserver.handler.ItemHandler;
import l2.hellknight.gameserver.handler.SkillHandler;
import l2.hellknight.gameserver.handler.TargetHandler;
import l2.hellknight.gameserver.handler.TelnetHandler;
import l2.hellknight.gameserver.handler.UserCommandHandler;
import l2.hellknight.gameserver.handler.VoicedCommandHandler;

/**
 * @author  nBd
 */
public class MasterHandler
{
	private static Logger _log = Logger.getLogger(MasterHandler.class.getName());
	
	private static final ActionHandler ACTION = ActionHandler.getInstance();
	private static final AdminCommandHandler ADMIN = AdminCommandHandler.getInstance();
	private static final BypassHandler BYPASS = BypassHandler.getInstance();
	private static final ChatHandler CHAT = ChatHandler.getInstance();
	private static final ItemHandler ITEM = ItemHandler.getInstance();
	private static final SkillHandler SKILL = SkillHandler.getInstance();
	private static final UserCommandHandler USER = UserCommandHandler.getInstance();
	private static final VoicedCommandHandler VOICE = VoicedCommandHandler.getInstance();
	private static final TargetHandler TARGET = TargetHandler.getInstance();
	private static final TelnetHandler TELNET = TelnetHandler.getInstance();
	
	private static void loadActionHandlers()
	{
		ACTION.registerActionHandler(new L2ArtefactInstanceAction());
		ACTION.registerActionHandler(new L2DecoyAction());
		ACTION.registerActionHandler(new L2DoorInstanceAction());
		ACTION.registerActionHandler(new L2ItemInstanceAction());
		ACTION.registerActionHandler(new L2NpcAction());
		ACTION.registerActionHandler(new L2PcInstanceAction());
		ACTION.registerActionHandler(new L2PetInstanceAction());
		ACTION.registerActionHandler(new L2StaticObjectInstanceAction());
		ACTION.registerActionHandler(new L2SummonAction());
		ACTION.registerActionHandler(new L2TrapAction());
		_log.log(Level.INFO, "Loaded " + ACTION.size() + " ActionHandlers");
	}
	
	private static void loadActionShiftHandlers()
	{
		ACTION.registerActionShiftHandler(new L2DoorInstanceActionShift());
		ACTION.registerActionShiftHandler(new L2ItemInstanceActionShift());
		ACTION.registerActionShiftHandler(new L2NpcActionShift());
		ACTION.registerActionShiftHandler(new L2PcInstanceActionShift());
		ACTION.registerActionShiftHandler(new L2StaticObjectInstanceActionShift());
		ACTION.registerActionShiftHandler(new L2SummonActionShift());
		_log.log(Level.INFO, "Loaded " + ACTION.sizeShift() + " ActionShiftHandlers");
	}
	
	private static void loadAdminHandlers()
	{
		ADMIN.registerAdminCommandHandler(new AdminAdmin());
		ADMIN.registerAdminCommandHandler(new AdminAnnouncements());
		ADMIN.registerAdminCommandHandler(new AdminBan());
		ADMIN.registerAdminCommandHandler(new AdminBBS());
		ADMIN.registerAdminCommandHandler(new AdminBuffs());
		ADMIN.registerAdminCommandHandler(new AdminCache());
		ADMIN.registerAdminCommandHandler(new AdminCamera());
		ADMIN.registerAdminCommandHandler(new AdminChangeAccessLevel());
		ADMIN.registerAdminCommandHandler(new AdminCHSiege());
		ADMIN.registerAdminCommandHandler(new AdminClan());
		ADMIN.registerAdminCommandHandler(new AdminCreateItem());
		ADMIN.registerAdminCommandHandler(new AdminCursedWeapons());
		ADMIN.registerAdminCommandHandler(new AdminDebug());
		ADMIN.registerAdminCommandHandler(new AdminDelete());
		ADMIN.registerAdminCommandHandler(new AdminDisconnect());
		ADMIN.registerAdminCommandHandler(new AdminDoorControl());
		ADMIN.registerAdminCommandHandler(new AdminEditChar());
		ADMIN.registerAdminCommandHandler(new AdminEditNpc());
		ADMIN.registerAdminCommandHandler(new AdminEffects());
		ADMIN.registerAdminCommandHandler(new AdminElement());
		ADMIN.registerAdminCommandHandler(new AdminEnchant());
		ADMIN.registerAdminCommandHandler(new AdminEventEngine());
		ADMIN.registerAdminCommandHandler(new AdminEvents());
		ADMIN.registerAdminCommandHandler(new AdminExpSp());
		ADMIN.registerAdminCommandHandler(new AdminFightCalculator());
		ADMIN.registerAdminCommandHandler(new AdminFortSiege());
		ADMIN.registerAdminCommandHandler(new AdminGeodata());
		ADMIN.registerAdminCommandHandler(new AdminGeoEditor());
		ADMIN.registerAdminCommandHandler(new AdminGm());
		ADMIN.registerAdminCommandHandler(new AdminGmChat());
		ADMIN.registerAdminCommandHandler(new AdminGraciaSeeds());
		ADMIN.registerAdminCommandHandler(new AdminHeal());
		ADMIN.registerAdminCommandHandler(new AdminHellbound());
		ADMIN.registerAdminCommandHandler(new AdminHelpPage());
		ADMIN.registerAdminCommandHandler(new AdminInstance());
		ADMIN.registerAdminCommandHandler(new AdminInstanceZone());
		ADMIN.registerAdminCommandHandler(new AdminInvul());
		ADMIN.registerAdminCommandHandler(new AdminKick());
		ADMIN.registerAdminCommandHandler(new AdminKill());
		ADMIN.registerAdminCommandHandler(new AdminLevel());
		ADMIN.registerAdminCommandHandler(new AdminLogin());
		ADMIN.registerAdminCommandHandler(new AdminMammon());
		ADMIN.registerAdminCommandHandler(new AdminManor());
		ADMIN.registerAdminCommandHandler(new AdminMenu());
		ADMIN.registerAdminCommandHandler(new AdminMessages());
		ADMIN.registerAdminCommandHandler(new AdminMobGroup());
		ADMIN.registerAdminCommandHandler(new AdminMonsterRace());
		ADMIN.registerAdminCommandHandler(new AdminPathNode());
		ADMIN.registerAdminCommandHandler(new AdminPetition());
		ADMIN.registerAdminCommandHandler(new AdminPForge());
		ADMIN.registerAdminCommandHandler(new AdminPledge());
		ADMIN.registerAdminCommandHandler(new AdminPremium());
		ADMIN.registerAdminCommandHandler(new AdminPolymorph());
		ADMIN.registerAdminCommandHandler(new AdminQuest());
		ADMIN.registerAdminCommandHandler(new AdminRepairChar());
		ADMIN.registerAdminCommandHandler(new AdminRes());
		ADMIN.registerAdminCommandHandler(new AdminRide());
		ADMIN.registerAdminCommandHandler(new AdminShop());
		ADMIN.registerAdminCommandHandler(new AdminShowQuests());
		ADMIN.registerAdminCommandHandler(new AdminShutdown());
		ADMIN.registerAdminCommandHandler(new AdminSiege());
		ADMIN.registerAdminCommandHandler(new AdminSkill());
		ADMIN.registerAdminCommandHandler(new AdminSpawn());
		ADMIN.registerAdminCommandHandler(new AdminSummon());
		ADMIN.registerAdminCommandHandler(new AdminTarget());
		ADMIN.registerAdminCommandHandler(new AdminTeleport());
		ADMIN.registerAdminCommandHandler(new AdminTerritoryWar());
		ADMIN.registerAdminCommandHandler(new AdminTest());
		ADMIN.registerAdminCommandHandler(new AdminTvTEvent());
		ADMIN.registerAdminCommandHandler(new AdminUnblockIp());
		ADMIN.registerAdminCommandHandler(new AdminVitality());
		ADMIN.registerAdminCommandHandler(new AdminZone());
		_log.log(Level.INFO, "Loaded " + ADMIN.size() + " AdminCommandHandlers");
	}
	
	private static void loadBypassHandlers()
	{
		BYPASS.registerBypassHandler(new Augment());
		BYPASS.registerBypassHandler(new ArenaBuff());
		BYPASS.registerBypassHandler(new BloodAlliance());
		BYPASS.registerBypassHandler(new Buy());
		BYPASS.registerBypassHandler(new BuyShadowItem());
		BYPASS.registerBypassHandler(new ChatLink());
		BYPASS.registerBypassHandler(new ClanWarehouse());
		BYPASS.registerBypassHandler(new DrawHenna());
		BYPASS.registerBypassHandler(new EventEngine());
		BYPASS.registerBypassHandler(new Festival());
		BYPASS.registerBypassHandler(new FortSiege());
		BYPASS.registerBypassHandler(new Freight());
		BYPASS.registerBypassHandler(new ItemAuctionLink());
		BYPASS.registerBypassHandler(new Leaderboard());
		BYPASS.registerBypassHandler(new Link());
		BYPASS.registerBypassHandler(new Loto());
		BYPASS.registerBypassHandler(new ManorManager());
		BYPASS.registerBypassHandler(new Multisell());
		BYPASS.registerBypassHandler(new Observation());
		BYPASS.registerBypassHandler(new OlympiadObservation());
		BYPASS.registerBypassHandler(new OlympiadManagerLink());
		BYPASS.registerBypassHandler(new QuestLink());
		BYPASS.registerBypassHandler(new PlayerHelp());
		BYPASS.registerBypassHandler(new PrivateWarehouse());
		BYPASS.registerBypassHandler(new QuestList());
		BYPASS.registerBypassHandler(new ReceivePremium());
		BYPASS.registerBypassHandler(new ReleaseAttribute());
		BYPASS.registerBypassHandler(new RemoveDeathPenalty());
		BYPASS.registerBypassHandler(new RemoveHennaList());
		BYPASS.registerBypassHandler(new RentPet());
		BYPASS.registerBypassHandler(new RideWyvern());
		BYPASS.registerBypassHandler(new Rift());
		BYPASS.registerBypassHandler(new SkillList());
		BYPASS.registerBypassHandler(new SupportBlessing());
		BYPASS.registerBypassHandler(new SupportMagic());
		BYPASS.registerBypassHandler(new TerritoryStatus());
		BYPASS.registerBypassHandler(new TerritoryWar());
		BYPASS.registerBypassHandler(new VoiceCommand());
		BYPASS.registerBypassHandler(new Wear());
		_log.log(Level.INFO, "Loaded " + BYPASS.size() + " BypassHandlers");
	}
	
	private static void loadChatHandlers()
	{
		CHAT.registerChatHandler(new ChatAll());
		CHAT.registerChatHandler(new ChatAlliance());
		CHAT.registerChatHandler(new ChatBattlefield());
		CHAT.registerChatHandler(new ChatClan());
		CHAT.registerChatHandler(new ChatHeroVoice());
		CHAT.registerChatHandler(new ChatParty());
		CHAT.registerChatHandler(new ChatPartyMatchRoom());
		CHAT.registerChatHandler(new ChatPartyRoomAll());
		CHAT.registerChatHandler(new ChatPartyRoomCommander());
		CHAT.registerChatHandler(new ChatPetition());
		CHAT.registerChatHandler(new ChatShout());
		CHAT.registerChatHandler(new ChatTell());
		CHAT.registerChatHandler(new ChatTrade());
		_log.log(Level.INFO, "Loaded " + CHAT.size() + " ChatHandlers");
	}
	
	private static void loadItemHandlers()
	{
		ITEM.registerItemHandler(new ScrollOfResurrection());
		ITEM.registerItemHandler(new SoulShots());
		ITEM.registerItemHandler(new SpiritShot());
		ITEM.registerItemHandler(new BlessedSpiritShot());
		ITEM.registerItemHandler(new BeastSoulShot());
		ITEM.registerItemHandler(new BeastSpiritShot());
		ITEM.registerItemHandler(new PaganKeys());
		ITEM.registerItemHandler(new Maps());
		ITEM.registerItemHandler(new NicknameColor());
		ITEM.registerItemHandler(new Recipes());
		ITEM.registerItemHandler(new RollingDice());
		ITEM.registerItemHandler(new EnchantAttribute());
		ITEM.registerItemHandler(new EnchantScrolls());
		ITEM.registerItemHandler(new ExtractableItems());
		ITEM.registerItemHandler(new Book());
		ITEM.registerItemHandler(new SevenSignsRecord());
		ITEM.registerItemHandler(new ItemSkills());
		ITEM.registerItemHandler(new ItemSkillsTemplate());
		ITEM.registerItemHandler(new Seed());
		ITEM.registerItemHandler(new Harvester());
		ITEM.registerItemHandler(new MercTicket());
		ITEM.registerItemHandler(new NevitHourglass());
		ITEM.registerItemHandler(new FishShots());
		ITEM.registerItemHandler(new PetFood());
		ITEM.registerItemHandler(new SpecialXMas());
		ITEM.registerItemHandler(new SummonItems());
		ITEM.registerItemHandler(new BeastSpice());
		ITEM.registerItemHandler(new TeleportBookmark());
		ITEM.registerItemHandler(new Elixir());
		ITEM.registerItemHandler(new Disguise());
		ITEM.registerItemHandler(new ManaPotion());
		ITEM.registerItemHandler(new EnergyStarStone());
		ITEM.registerItemHandler(new EventItem());
		_log.log(Level.INFO, "Loaded " + ITEM.size() + " ItemHandlers");
	}
	
	private static void loadSkillHandlers()
	{
		SKILL.registerSkillHandler(new Blow());
		SKILL.registerSkillHandler(new Pdam());
		SKILL.registerSkillHandler(new Mdam());
		SKILL.registerSkillHandler(new CpDam());
		SKILL.registerSkillHandler(new CpDamPercent());
		SKILL.registerSkillHandler(new Manadam());
		SKILL.registerSkillHandler(new Heal());
		SKILL.registerSkillHandler(new HealPercent());
		SKILL.registerSkillHandler(new CombatPointHeal());
		SKILL.registerSkillHandler(new ManaHeal());
		SKILL.registerSkillHandler(new BalanceLife());
		SKILL.registerSkillHandler(new Charge());
		SKILL.registerSkillHandler(new Continuous());
		SKILL.registerSkillHandler(new Detection());
		SKILL.registerSkillHandler(new Resurrect());
		SKILL.registerSkillHandler(new ShiftTarget());
		SKILL.registerSkillHandler(new Spoil());
		SKILL.registerSkillHandler(new Sweep());
		SKILL.registerSkillHandler(new StrSiegeAssault());
		SKILL.registerSkillHandler(new SummonFriend());
		SKILL.registerSkillHandler(new Disablers());
		SKILL.registerSkillHandler(new Cancel());
		SKILL.registerSkillHandler(new ChainHeal());
		SKILL.registerSkillHandler(new StealBuffs());
		SKILL.registerSkillHandler(new BallistaBomb());
		SKILL.registerSkillHandler(new TakeCastle());
		SKILL.registerSkillHandler(new TakeFort());
		SKILL.registerSkillHandler(new Unlock());
		SKILL.registerSkillHandler(new Craft());
		SKILL.registerSkillHandler(new Fishing());
		SKILL.registerSkillHandler(new FishingSkill());
		SKILL.registerSkillHandler(new BeastSkills());
		SKILL.registerSkillHandler(new DeluxeKey());
		SKILL.registerSkillHandler(new Sow());
		SKILL.registerSkillHandler(new Soul());
		SKILL.registerSkillHandler(new Harvest());
		SKILL.registerSkillHandler(new GetPlayer());
		SKILL.registerSkillHandler(new TransformDispel());
		SKILL.registerSkillHandler(new Trap());
		SKILL.registerSkillHandler(new GiveSp());
		SKILL.registerSkillHandler(new GiveReco());
		SKILL.registerSkillHandler(new GiveVitality());
		SKILL.registerSkillHandler(new InstantJump());
		SKILL.registerSkillHandler(new Dummy());
		SKILL.registerSkillHandler(new Extractable());
		SKILL.registerSkillHandler(new RefuelAirShip());
		SKILL.registerSkillHandler(new NornilsPower());
		_log.log(Level.INFO, "Loaded " + SKILL.size() + " SkillHandlers");
	}
	
	private static void loadUserHandlers()
	{
		USER.registerUserCommandHandler(new ClanPenalty());
		USER.registerUserCommandHandler(new ClanWarsList());
		USER.registerUserCommandHandler(new DisMount());
		USER.registerUserCommandHandler(new Escape());
		USER.registerUserCommandHandler(new InstanceZone());
		USER.registerUserCommandHandler(new Loc());
		USER.registerUserCommandHandler(new Mount());
		USER.registerUserCommandHandler(new PartyInfo());
		USER.registerUserCommandHandler(new Time());
		USER.registerUserCommandHandler(new OlympiadStat());
		USER.registerUserCommandHandler(new ChannelLeave());
		USER.registerUserCommandHandler(new ChannelDelete());
		USER.registerUserCommandHandler(new ChannelListUpdate());
		USER.registerUserCommandHandler(new Birthday());
		_log.log(Level.INFO, "Loaded " + USER.size() + " UserHandlers");
	}
	
	private static void loadVoicedHandlers()
	{
		VOICE.registerVoicedCommandHandler(new Experience());
		if (Config.SHOW_ONLINE_PLAYERS)
			VOICE.registerVoicedCommandHandler(new Online());
		VOICE.registerVoicedCommandHandler(new BindingIP());
		VOICE.registerVoicedCommandHandler(new stats());
		if (Config.L2JMOD_ALLOW_WEDDING)
			VOICE.registerVoicedCommandHandler(new Wedding());
		if (Config.BANKING_SYSTEM_ENABLED)
			VOICE.registerVoicedCommandHandler(new Banking());
		if (Config.TVT_ALLOW_VOICED_COMMAND)
			VOICE.registerVoicedCommandHandler(new TvTVoicedInfo());
		if (Config.L2JMOD_CHAT_ADMIN)
			VOICE.registerVoicedCommandHandler(new ChatAdmin());
		if (Config.L2JMOD_MULTILANG_ENABLE && Config.L2JMOD_MULTILANG_VOICED_ALLOW)
			VOICE.registerVoicedCommandHandler(new Lang());
		if (Config.L2JMOD_DEBUG_VOICE_COMMAND)
			VOICE.registerVoicedCommandHandler(new Debug());
		if (Config.L2JMOD_ALLOW_CHANGE_PASSWORD)
			VOICE.registerVoicedCommandHandler(new ChangePassword());
		if (Config.L2JMOD_HELLBOUND_STATUS)
			VOICE.registerVoicedCommandHandler(new Hellbound());
		_log.log(Level.INFO, "Loaded " + VOICE.size() + " VoicedHandlers");
	}
	
	private static void loadTargetHandlers()
	{
		TARGET.registerTargetType(new TargetAlly());
		TARGET.registerTargetType(new TargetArea());
		TARGET.registerTargetType(new TargetAreaCorpseMob());
		TARGET.registerTargetType(new TargetAreaSummon());
		TARGET.registerTargetType(new TargetAura());
		TARGET.registerTargetType(new TargetAuraCorpseMob());
		TARGET.registerTargetType(new TargetBehindArea());
		TARGET.registerTargetType(new TargetBehindAura());
		TARGET.registerTargetType(new TargetClan());
		TARGET.registerTargetType(new TargetClanMember());
		TARGET.registerTargetType(new TargetCorpseAlly());
		TARGET.registerTargetType(new TargetCorpseClan());
		TARGET.registerTargetType(new TargetCorpseMob());
		TARGET.registerTargetType(new TargetCorpsePet());
		TARGET.registerTargetType(new TargetCorpsePlayer());
		TARGET.registerTargetType(new TargetEnemySummon());
		TARGET.registerTargetType(new TargetFlagPole());
		TARGET.registerTargetType(new TargetFrontArea());
		TARGET.registerTargetType(new TargetFrontAura());
		TARGET.registerTargetType(new TargetGround());
		TARGET.registerTargetType(new TargetChainHeal());
		TARGET.registerTargetType(new TargetHoly());
		TARGET.registerTargetType(new TargetOne());
		TARGET.registerTargetType(new TargetOwnerPet());
		TARGET.registerTargetType(new TargetParty());
		TARGET.registerTargetType(new TargetPartyClan());
		TARGET.registerTargetType(new TargetPartyMember());
		TARGET.registerTargetType(new TargetPartyNotMe());
		TARGET.registerTargetType(new TargetPartyOther());
		TARGET.registerTargetType(new TargetPet());
		TARGET.registerTargetType(new TargetSelf());
		TARGET.registerTargetType(new TargetSummon());
		TARGET.registerTargetType(new TargetUnlockable());
		_log.log(Level.INFO, "Loaded " + TARGET.size() + " Target Handlers");
	}
	
	public static void loadTelnetHandlers()
	{
		TELNET.registerCommandHandler(new ChatsHandler());
		TELNET.registerCommandHandler(new DebugHandler());
		TELNET.registerCommandHandler(new HelpHandler());
		TELNET.registerCommandHandler(new PlayerHandler());
		TELNET.registerCommandHandler(new ReloadHandler());
		TELNET.registerCommandHandler(new ServerHandler());
		TELNET.registerCommandHandler(new StatusHandler());
		TELNET.registerCommandHandler(new ThreadHandler());
		_log.log(Level.INFO, "Loaded " + TELNET.size() + " Telnet Handlers");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		_log.log(Level.INFO, "Loading Handlers...");
		loadActionHandlers();
		loadActionShiftHandlers();
		loadAdminHandlers();
		loadBypassHandlers();
		loadChatHandlers();
		loadItemHandlers();
		loadSkillHandlers();
		loadUserHandlers();
		loadVoicedHandlers();
		loadTargetHandlers();
		loadTelnetHandlers();
		_log.log(Level.INFO, "Handlers Loaded...");
	}
}