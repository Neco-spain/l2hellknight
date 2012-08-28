package l2p.gameserver;

import gnu.trove.TIntIntHashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import l2p.commons.configuration.ExProperties;
import l2p.commons.net.nio.impl.SelectorConfig;
import l2p.gameserver.loginservercon.ServerType;
import l2p.gameserver.model.base.Experience;
import l2p.gameserver.model.base.PlayerAccess;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class Config
{
  private static final Logger _log = LoggerFactory.getLogger(Config.class);

  public static final int NCPUS = Runtime.getRuntime().availableProcessors();
  public static final String ITEM_FILE = "config/items.properties";
  public static double CRAFT_MASTERWORK_CHANCE;
  public static double CRAFT_DOUBLECRAFT_CHANCE;
  public static boolean ALT_GAME_UNREGISTER_RECIPE;
  public static int ALT_ADD_RECIPES;
  public static boolean ALT_ALLOW_AUGMENT_ALL;
  public static int ALT_MAMMON_UPGRADE;
  public static int ALT_MAMMON_EXCHANGE;
  public static boolean ALT_ALLOW_SELL_COMMON;
  public static boolean ALT_ALLOW_SHADOW_WEAPONS;
  public static boolean ALT_ALLOW_TATTOO;
  public static int[] ALT_DISABLED_MULTISELL;
  public static int[] ALT_SHOP_PRICE_LIMITS;
  public static int[] ALT_SHOP_UNALLOWED_ITEMS;
  public static int[] ALT_ALLOWED_PET_POTIONS;
  public static boolean ALT_ALLOW_DROP_AUGMENTED;
  public static boolean ALT_OPEN_CLOAK_SLOT;
  public static int ENCHANT_CHANCE_WEAPON;
  public static int ENCHANT_CHANCE_ARMOR;
  public static int ENCHANT_CHANCE_ACCESSORY;
  public static int ENCHANT_CHANCE_CRYSTAL_WEAPON;
  public static int ENCHANT_CHANCE_CRYSTAL_ARMOR;
  public static int ENCHANT_CHANCE_CRYSTAL_ACCESSORY;
  public static int SAFE_ENCHANT_COMMON;
  public static int SAFE_ENCHANT_FULL_BODY;
  public static int ENCHANT_MAX;
  public static int ARMOR_OVERENCHANT_HPBONUS_LIMIT;
  public static boolean SHOW_ENCHANT_EFFECT_RESULT;
  public static int ADD_FAIL_ENCHANT_VALUE;
  public static int ADD_BLESSED_ENCHANT_CHANCE_WEAPON;
  public static int ADD_BLESSED_ENCHANT_CHANCE_ARMOR;
  public static int ADD_BLESSED_ENCHANT_CHANCE_JEWEL;
  public static int ENCHANT_ATTRIBUTE_STONE_CHANCE;
  public static int ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE;
  public static boolean KARMA_DROP_GM;
  public static List<Integer> KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList();
  public static boolean DROP_ITEMS_AUGMENTED;
  public static boolean DROP_ITEMS_ON_DIE;
  public static double NORMAL_DROPCHANCE_BASE;
  public static double KARMA_DROPCHANCE_BASE;
  public static double KARMA_DROPCHANCE_MOD;
  public static int DROPCHANCE_EQUIPMENT;
  public static int DROPCHANCE_EQUIPPED_WEAPON;
  public static int DROPCHANCE_ITEM;
  public static int KARMA_DROP_ITEM_LIMIT;
  public static int KARMA_RANDOM_DROP_LOCATION_LIMIT;
  public static int MIN_PK_TO_ITEMS_DROP;
  public static boolean KARMA_NEEDED_TO_DROP;
  public static boolean ADD_REDUCE_ARROW;
  public static int[] ADD_CONSAMBLE_LIST;
  public static boolean ADD_EXPERTISE_PENALTY;
  public static boolean ADD_REDUCE_SHOT;
  public static final String OLYMPIAD = "config/olympiad.properties";
  public static boolean ENABLE_OLYMPIAD;
  public static boolean ENABLE_OLYMPIAD_SPECTATING;
  public static int OLYMPIAD_STADIAS_COUNT;
  public static int ALT_OLY_START_TIME;
  public static int ALT_OLY_MIN;
  public static long ALT_OLY_CPERIOD;
  public static long ALT_OLY_WPERIOD;
  public static long ALT_OLY_VPERIOD;
  public static int CLASS_GAME_MIN;
  public static int NONCLASS_GAME_MIN;
  public static int TEAM_GAME_MIN;
  public static int ALT_OLY_REG_DISPLAY;
  public static int ALT_OLY_BATTLE_REWARD_ITEM;
  public static int ALT_OLY_CLASSED_RITEM_C;
  public static int ALT_OLY_NONCLASSED_RITEM_C;
  public static int ALT_OLY_TEAM_RITEM_C;
  public static int ALT_OLY_COMP_RITEM;
  public static int ALT_OLY_GP_PER_POINT;
  public static int ALT_OLY_HERO_POINTS;
  public static int ALT_OLY_RANK1_POINTS;
  public static int ALT_OLY_RANK2_POINTS;
  public static int ALT_OLY_RANK3_POINTS;
  public static int ALT_OLY_RANK4_POINTS;
  public static int ALT_OLY_RANK5_POINTS;
  public static int GAME_MAX_LIMIT;
  public static int GAME_CLASSES_COUNT_LIMIT;
  public static int GAME_NOCLASSES_COUNT_LIMIT;
  public static int GAME_TEAM_COUNT_LIMIT;
  public static int OLYMPIAD_BATTLES_FOR_REWARD;
  public static int OLYMPIAD_POINTS_DEFAULT;
  public static int OLYMPIAD_POINTS_WEEKLY;
  public static boolean OLYMPIAD_OLDSTYLE_STAT;
  public static boolean OLY_ENCH_LIMIT_ENABLE;
  public static int OLY_ENCHANT_LIMIT_WEAPON;
  public static int OLY_ENCHANT_LIMIT_ARMOR;
  public static int OLY_ENCHANT_LIMIT_JEWEL;
  public static final String SKILLS_FILE = "config/skills.properties";
  public static boolean ALT_SAVE_UNSAVEABLE;
  public static int ALT_SAVE_EFFECTS_REMAINING_TIME;
  public static boolean ALT_SHOW_REUSE_MSG;
  public static boolean ALT_DELETE_SA_BUFFS;
  public static boolean SAVING_SPS;
  public static boolean MANAHEAL_SPS_BONUS;
  public static int ADD_MUSIC_LIMIT;
  public static int ADD_DEBUFF_LIMIT;
  public static int ADD_TRIGGER_LIMIT;
  public static boolean ADD_ENABLE_MODIFY_SKILL_DURATION;
  public static TIntIntHashMap ADD_SKILL_DURATION_LIST;
  public static boolean ADD_DANCE_MOD_ALLOW;
  public static boolean ALLOW_LEARN_TRANS_SKILLS_WO_QUEST;
  public static double CLANHALL_BUFFTIME_MODIFIER;
  public static double SONGDANCETIME_MODIFIER;
  public static boolean ALT_REMOVE_SKILLS_ON_DELEVEL;
  public static boolean ALT_ALL_PHYS_SKILLS_OVERHIT;
  public static int AUGMENTATION_NG_SKILL_CHANCE;
  public static int AUGMENTATION_NG_GLOW_CHANCE;
  public static int AUGMENTATION_MID_SKILL_CHANCE;
  public static int AUGMENTATION_MID_GLOW_CHANCE;
  public static int AUGMENTATION_HIGH_SKILL_CHANCE;
  public static int AUGMENTATION_HIGH_GLOW_CHANCE;
  public static int AUGMENTATION_TOP_SKILL_CHANCE;
  public static int AUGMENTATION_TOP_GLOW_CHANCE;
  public static int AUGMENTATION_BASESTAT_CHANCE;
  public static int AUGMENTATION_ACC_SKILL_CHANCE;
  public static double SKILLS_CHANCE_MOD;
  public static double SKILLS_CHANCE_MIN;
  public static double SKILLS_CHANCE_POW;
  public static double SKILLS_CHANCE_CAP;
  public static int SKILLS_CAST_TIME_MIN;
  public static double ALT_ABSORB_DAMAGE_MODIFIER;
  public static boolean UNSTUCK_SKILL;
  public static int ALT_BUFF_LIMIT;
  public static boolean DANCE_CANCEL_BUFF;
  public static boolean MAGICFAILURES;
  public static int MAGICFAILURES_DIFF;
  public static boolean MAGIC_SHIELD_BLOCK;
  public static int MAGIC_FULL_SHIELD_BLOCK;
  public static boolean SKILLS_SHOW_CHANCE;
  public static boolean SKILLS_SHOW_BLOW_CHANCE;
  public static boolean CLAN_SKILL_ITEM_ENABLED;
  public static final String CLAN_FILE = "config/clan.properties";
  public static int ALT_MAX_ALLY_SIZE;
  public static boolean ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE;
  public static boolean ALT_ALLOW_CLAN_COMMAND_ONLY_FOR_CLAN_LEADER;
  public static boolean ALLOW_CLANSKILLS;
  public static int ADD_MIN_ACADEM_POINT;
  public static int ADD_MAX_ACADEM_POINT;
  public static int FS_BLOOD_OATH_CYCLE;
  public static int FS_BLOOD_OATH_COUNT;
  public static int CLAN_PENALTY;
  public static int CLAN_MEMBERS_FOR_WAR;
  public static int CLAN_WAR_MAX;
  public static int CLAN_MIN_LVL_FOR_WAR;
  public static int ALLY_PENALTY;
  public static int QUEST_508_RATE;
  public static int QUEST_509_RATE;
  public static int QUEST_510_RATE;
  public static int CLAN_REP_FOR_TAKE_CASTLE;
  public static int CLAN_REP_FOR_DEFEND_CASTLE;
  public static int CLAN_REP_FOR_LOSE_CASTLE;
  public static int CLAN_BLOOD_ALLIANCE_REWARD;
  public static int CLAN_REP_FOR_FORT;
  public static int CLAN_REP_FOR_CH;
  public static int CLAN_WAR_REP;
  public static int CLAN_REP_FOR_LEVEL_6;
  public static int CLAN_REP_FOR_LEVEL_7;
  public static int CLAN_REP_FOR_LEVEL_8;
  public static int CLAN_REP_FOR_LEVEL_9;
  public static int CLAN_REP_FOR_LEVEL_10;
  public static int CLAN_REP_FOR_LEVEL_11;
  public static int CLAN_MEMBERS_FOR_LEVEL_6;
  public static int CLAN_MEMBERS_FOR_LEVEL_7;
  public static int CLAN_MEMBERS_FOR_LEVEL_8;
  public static int CLAN_MEMBERS_FOR_LEVEL_9;
  public static int CLAN_MEMBERS_FOR_LEVEL_10;
  public static int CLAN_MEMBERS_FOR_LEVEL_11;
  public static int CLAN_LVL_UP_ITEM_FOR_9;
  public static int CLAN_LVL_UP_ITEM_COUNT_FOR_9;
  public static int CLAN_LVL_UP_ITEM_FOR_10;
  public static int CLAN_LVL_UP_ITEM_COUNT_FOR_10;
  public static int CLAN_CREATE_ROYAL;
  public static int CLAN_CREATE_KNIGHT;
  public static int CLAN_MAX_ACADEM;
  public static int CLAN_MAX_MAIN0;
  public static int CLAN_MAX_MAIN1;
  public static int CLAN_MAX_MAIN2;
  public static int CLAN_MAX_MAIN3;
  public static int CLAN_MAX_MAIN4;
  public static int CLAN_MAX_MAIN5;
  public static int CLAN_MAX_MAIN6;
  public static int CLAN_MAX_MAIN7;
  public static int CLAN_MAX_MAIN8;
  public static int CLAN_MAX_MAIN9;
  public static int CLAN_MAX_MAIN10;
  public static int CLAN_MAX_MAIN11;
  public static int CLAN_MAX_ROYAL;
  public static int CLAN_MAX_ROYAL_IN_LEVEL_11;
  public static int CLAN_MAX_KNIGHT12;
  public static int CLAN_MAX_KNIGHT12_IN_LEVEL_9;
  public static int CLAN_MAX_KNIGHT34;
  public static int CLAN_MAX_KNIGHT34_IN_LEVEL_10;
  public static final String NPC_FILE = "config/npc.properties";
  public static boolean ALT_SIMPLE_SIGNS;
  public static boolean ALT_BS_CRYSTALLIZE;
  public static double ALT_RAID_RESPAWN_MULTIPLIER;
  public static double ALT_CHAMPION_CHANCE1;
  public static double ALT_CHAMPION_CHANCE2;
  public static boolean ALT_CHAMPION_CAN_BE_AGGRO;
  public static boolean ALT_CHAMPION_CAN_BE_SOCIAL;
  public static int NPC_CHAMPION_MAX_LEVEL;
  public static int NPC_CHAMPION_MIN_LEVEL;
  public static double ALT_NPC_PATK_MODIFIER;
  public static double ALT_NPC_MATK_MODIFIER;
  public static double ALT_NPC_MAXHP_MODIFIER;
  public static double ALT_NPC_MAXMP_MODIFIER;
  public static boolean DEEPBLUE_DROP_RULES;
  public static int DEEPBLUE_DROP_MAXDIFF;
  public static int DEEPBLUE_DROP_RAID_MAXDIFF;
  public static boolean ANNOUNCE_MAMMON_SPAWN;
  public static double RATE_RAID_REGEN;
  public static double RATE_RAID_DEFENSE;
  public static double RATE_RAID_ATTACK;
  public static double RATE_EPIC_DEFENSE;
  public static double RATE_EPIC_ATTACK;
  public static int RAID_MAX_LEVEL_DIFF;
  public static boolean PARALIZE_ON_RAID_DIFF;
  public static int MIN_NPC_ANIMATION;
  public static int MAX_NPC_ANIMATION;
  public static boolean SERVER_SIDE_NPC_NAME;
  public static boolean SERVER_SIDE_NPC_TITLE;
  public static final String QUESTS_FILE = "config/quests.properties";
  public static boolean ALT_NO_LASTHIT;
  public static final String SIEGES_FILE = "config/sieges.properties";
  public static float DOMINION_WAR_REWARD_RATE;
  public static int DOMINION_WAR_HOUR;
  public static Calendar CASTLE_VALIDATION_DATE;
  public static int[] CASTLE_SELECT_HOURS;
  public static int CASTLE_WAR_HOUR;
  public static final String COMMUNITY_FILE = "config/community.properties";
  public static boolean COMMUNITYBOARD_ENABLED;
  public static String BBS_DEFAULT;
  public static boolean ALLOW_BUFFER_CB;
  public static int MAX_SKILLS_IN_SCHEME_CB;
  public static int BUFF_TIME_CB;
  public static int BUFFER_MIN_LVL_CB;
  public static int BUFFER_MAX_LVL_CB;
  public static boolean ALLOW_SAVE_SCHEME_CB;
  public static boolean ALLOW_SHOP_CB;
  public static int[] ALLOWED_MULTISELL;
  public static int[] DISBLED_MULTISELL_WO_PA;
  public static boolean ALLOW_ENCHANT_CB;
  public static int ENCHANT_PRICE_ITEM_CB;
  public static int ENCHANT_MAX_CB;
  public static int[] ENCHANT_PRICE_WEAPON;
  public static int[] ENCHANT_PRICE_OTHER;
  public static int[] ENCHANT_LVL_CB;
  public static double CB_MUL_MOD;
  public static final String OTHER_CONFIG_FILE = "config/other.properties";
  public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
  public static boolean ALT_GAME_REQUIRE_CASTLE_DAWN;
  public static boolean ALT_GAME_ALLOW_ADENA_DAWN;
  public static int SS_ANNOUNCE_PERIOD;
  public static boolean ALT_DEATH_PENALTY;
  public static double ALT_PK_DEATH_RATE;
  public static int ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY;
  public static int ALT_DEATH_PENALTY_C5_KARMA_PENALTY;
  public static boolean ALT_TELE_TO_CATACOMBS;
  public static int FESTIVAL_MIN_PARTY_SIZE;
  public static double FESTIVAL_RATE_PRICE;
  public static int RIFT_MIN_PARTY_SIZE;
  public static int RIFT_SPAWN_DELAY;
  public static int RIFT_MAX_JUMPS;
  public static int RIFT_AUTO_JUMPS_TIME;
  public static int RIFT_AUTO_JUMPS_TIME_RAND;
  public static int RIFT_ENTER_COST_RECRUIT;
  public static int RIFT_ENTER_COST_SOLDIER;
  public static int RIFT_ENTER_COST_OFFICER;
  public static int RIFT_ENTER_COST_CAPTAIN;
  public static int RIFT_ENTER_COST_COMMANDER;
  public static int RIFT_ENTER_COST_HERO;
  public static boolean ALLOW_TALK_WHILE_SITTING;
  public static boolean ALLOW_NOBLE_TP_TO_ALL;
  public static boolean ALT_SOCIAL_ACTION_REUSE;
  public static double MAXLOAD_MODIFIER;
  public static double GATEKEEPER_MODIFIER;
  public static boolean ALT_IMPROVED_PETS_LIMITED_USE;
  public static int GATEKEEPER_FREE;
  public static int CRUMA_GATEKEEPER_LVL;
  public static boolean ALT_PET_HEAL_BATTLE_ONLY;
  public static int ALT_PARTY_DISTRIBUTION_RANGE;
  public static double[] ALT_PARTY_BONUS;
  public static boolean ALT_USE_BOW_REUSE_MODIFIER;
  public static boolean ALT_VITALITY_ENABLED;
  public static double ALT_VITALITY_RATE;
  public static double ALT_VITALITY_CONSUME_RATE;
  public static int ALT_VITALITY_RAID_BONUS;
  public static final int[] VITALITY_LEVELS = { 240, 2000, 13000, 17000, 20000 };
  public static boolean ALT_KAMALOKA_NIGHTMARES_PREMIUM_ONLY;
  public static boolean ALT_KAMALOKA_NIGHTMARE_REENTER;
  public static boolean ALT_KAMALOKA_ABYSS_REENTER;
  public static boolean ALT_KAMALOKA_LAB_REENTER;
  public static int ALT_PET_INVENTORY_LIMIT;
  public static int FOLLOW_RANGE;
  public static boolean ALT_SHOW_SERVER_TIME;
  public static boolean ALT_ITEM_AUCTION_ENABLED;
  public static boolean ALT_ITEM_AUCTION_CAN_REBID;
  public static boolean ALT_ITEM_AUCTION_START_ANNOUNCE;
  public static int ALT_ITEM_AUCTION_BID_ITEM_ID;
  public static long ALT_ITEM_AUCTION_MAX_BID;
  public static int ALT_ITEM_AUCTION_MAX_CANCEL_TIME_IN_MILLIS;
  public static boolean ALT_FISH_CHAMPIONSHIP_ENABLED;
  public static int ALT_FISH_CHAMPIONSHIP_REWARD_ITEM;
  public static int ALT_FISH_CHAMPIONSHIP_REWARD_1;
  public static int ALT_FISH_CHAMPIONSHIP_REWARD_2;
  public static int ALT_FISH_CHAMPIONSHIP_REWARD_3;
  public static int ALT_FISH_CHAMPIONSHIP_REWARD_4;
  public static int ALT_FISH_CHAMPIONSHIP_REWARD_5;
  public static boolean ALT_ENABLE_BLOCK_CHECKER_EVENT;
  public static int ALT_MIN_BLOCK_CHECKER_TEAM_MEMBERS;
  public static double ALT_RATE_COINS_REWARD_BLOCK_CHECKER;
  public static boolean ALT_HBCE_FAIR_PLAY;
  public static boolean PETITIONING_ALLOWED;
  public static int MAX_PETITIONS_PER_PLAYER;
  public static int MAX_PETITIONS_PENDING;
  public static boolean ALT_PCBANG_POINTS_ENABLED;
  public static double ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE;
  public static int ALT_PCBANG_POINTS_BONUS;
  public static int ALT_PCBANG_POINTS_DELAY;
  public static int ALT_PCBANG_POINTS_MIN_LVL;
  public static boolean ALT_DEBUG_ENABLED;
  public static boolean ALT_DEBUG_PVP_ENABLED;
  public static boolean ALT_DEBUG_PVP_DUEL_ONLY;
  public static boolean ALT_DEBUG_PVE_ENABLED;
  public static boolean EX_NEW_PETITION_SYSTEM;
  public static boolean EX_JAPAN_MINIGAME;
  public static boolean EX_LECTURE_MARK;
  public static boolean SENDSTATUS_TRADE_JUST_OFFLINE;
  public static double SENDSTATUS_TRADE_MOD;
  public static int MULTISELL_SIZE;
  public static int GAME_POINT_ITEM_ID;
  public static boolean GLOBAL_SHOUT;
  public static boolean GLOBAL_TRADE_CHAT;
  public static int CHAT_RANGE;
  public static int SHOUT_OFFSET;
  public static boolean PREMIUM_HEROCHAT;
  public static int CHAT_MESSAGE_MAX_LEN;
  public static boolean LOG_CHAT;
  public static boolean ABUSEWORD_BANCHAT;
  public static int[] BAN_CHANNEL_LIST = new int[18];
  public static boolean ABUSEWORD_REPLACE;
  public static String ABUSEWORD_REPLACE_STRING;
  public static int ABUSEWORD_BANTIME;
  public static boolean BANCHAT_ANNOUNCE;
  public static boolean BANCHAT_ANNOUNCE_FOR_ALL_WORLD;
  public static boolean BANCHAT_ANNOUNCE_NICK;
  public static int[] CHATFILTER_CHANNELS = new int[18];
  public static int CHATFILTER_MIN_LEVEL = 0;
  public static int CHATFILTER_WORK_TYPE = 1;
  public static boolean HIDE_GM_STATUS;
  public static boolean SHOW_GM_LOGIN;
  public static boolean SAVE_GM_EFFECTS;
  public static boolean ALLOW_CURSED_WEAPONS;
  public static boolean DROP_CURSED_WEAPONS_ON_KICK;
  public static boolean ALT_ARENA_EXP;
  public static boolean ENABLE_STARTING_ITEM;
  public static int[] STARTING_ITEM_ID = new int[15];
  public static int[] STARTING_ITEM_COUNT = new int[15];
  public static int STARTING_LEVEL;
  public static int STARTING_SP;
  public static boolean ENABLE_PROF_SOCIAL_ACTION;
  public static int PROF_SOCIAL_ACTION_ID;
  public static boolean NEW_CHARACTER_NOBL;
  public static final String CONFIGURATION_FILE = "config/server.properties";
  public static String GAMESERVER_HOSTNAME;
  public static int[] PORTS_GAME;
  public static String INTERNAL_HOSTNAME;
  public static String EXTERNAL_HOSTNAME;
  public static int GAME_SERVER_LOGIN_PORT;
  public static boolean GAME_SERVER_LOGIN_CRYPT;
  public static String GAME_SERVER_LOGIN_HOST;
  public static boolean ACCEPT_ALTERNATE_ID;
  public static int REQUEST_ID;
  public static String DATABASE_DRIVER;
  public static int DATABASE_MAX_CONNECTIONS;
  public static int DATABASE_MAX_IDLE_TIMEOUT;
  public static int DATABASE_IDLE_TEST_PERIOD;
  public static String DATABASE_URL;
  public static String DATABASE_LOGIN;
  public static String DATABASE_PASSWORD;
  public static boolean AUTOSAVE;
  public static String CNAME_TEMPLATE;
  public static int CNAME_MAXLEN = 32;
  public static String CLAN_NAME_TEMPLATE;
  public static String CLAN_TITLE_TEMPLATE;
  public static String ALLY_NAME_TEMPLATE;
  public static int LOGIN_SERVER_SERVER_TYPE;
  public static boolean LOGIN_SERVER_GM_ONLY;
  public static boolean LOGIN_SERVER_BRACKETS;
  public static boolean LOGIN_SERVER_IS_PVP;
  public static int LOGIN_SERVER_AGE_LIMIT;
  public static int MIN_PROTOCOL_REVISION;
  public static int MAX_PROTOCOL_REVISION;
  public static int SCHEDULED_THREAD_POOL_SIZE;
  public static int EXECUTOR_THREAD_POOL_SIZE;
  public static boolean ENABLE_RUNNABLE_STATS;
  public static SelectorConfig SELECTOR_CONFIG = new SelectorConfig();
  public static int EFFECT_TASK_MANAGER_COUNT;
  public static String DEFAULT_LANG;
  public static int DELETE_DAYS;
  public static File DATAPACK_ROOT;
  public static String RESTART_AT_TIME;
  public static int SHIFT_BY;
  public static int SHIFT_BY_Z;
  public static int MAP_MIN_Z;
  public static int MAP_MAX_Z;
  public static boolean DAMAGE_FROM_FALLING;
  public static boolean DONTLOADSPAWN;
  public static boolean DONTLOADQUEST;
  public static int MAX_REFLECTIONS_COUNT;
  public static int PURGE_BYPASS_TASK_FREQUENCY;
  public static int MOVE_PACKET_DELAY;
  public static int ATTACK_PACKET_DELAY;
  public static long USER_INFO_INTERVAL;
  public static boolean BROADCAST_STATS_INTERVAL;
  public static long BROADCAST_CHAR_INFO_INTERVAL;
  public static int MAXIMUM_ONLINE_USERS;
  public static int AUTODESTROY_ITEM_AFTER;
  public static int AUTODESTROY_PLAYER_ITEM_AFTER;
  public static boolean ALLOW_WATER;
  public static boolean ALLOW_MAIL;
  public static int WEAR_DELAY;
  public static boolean ALLOW_DISCARDITEM;
  public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
  public static int HTM_CACHE_MODE;
  public static boolean ALLOW_WAREHOUSE;
  public static final String CHAR_CONFIG_FILE = "config/character.properties";
  public static boolean ADD_ZONE_PVP_COUNT;
  public static boolean ADD_SIEGE_PVP_COUNT;
  public static boolean AUTO_LOOT;
  public static boolean AUTO_LOOT_HERBS;
  public static boolean AUTO_LOOT_INDIVIDUAL;
  public static boolean AUTO_LOOT_FROM_RAIDS;
  public static boolean AUTO_LOOT_PK;
  public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
  public static boolean ALT_GAME_DELEVEL;
  public static boolean ALT_DISABLE_SPELLBOOKS;
  public static boolean AUTO_LEARN_SKILLS;
  public static boolean AUTO_LEARN_FORGOTTEN_SKILLS;
  public static boolean CHAR_TITLE;
  public static String ADD_CHAR_TITLE;
  public static boolean ALLOW_NPC_SHIFTCLICK;
  public static boolean ALT_GAME_SHOW_DROPLIST;
  public static boolean ALT_FULL_NPC_STATS_PAGE;
  public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
  public static boolean ALT_ALLOW_SUBCLASS_WITHOUT_BAIUM;
  public static int ALT_GAME_LEVEL_TO_GET_SUBCLASS;
  public static int ALT_MAX_LEVEL;
  public static int ALT_MAX_SUB_LEVEL;
  public static int ALT_GAME_SUB_ADD;
  public static boolean ALLOW_DEATH_PENALTY_C5;
  public static int ALT_DEATH_PENALTY_C5_CHANCE;
  public static boolean ALT_DEATH_PENALTY_C5_CHAOTIC_RECOVERY;
  public static long NONOWNER_ITEM_PICKUP_DELAY;
  public static boolean PARTY_LEADER_ONLY_CAN_INVITE;
  public static int LIM_PATK;
  public static int LIM_MATK;
  public static int LIM_PDEF;
  public static int LIM_MDEF;
  public static int LIM_MATK_SPD;
  public static int LIM_PATK_SPD;
  public static int LIM_CRIT_DAM;
  public static int LIM_CRIT;
  public static int LIM_MCRIT;
  public static int LIM_ACCURACY;
  public static int LIM_EVASION;
  public static int LIM_MOVE;
  public static int LIM_FAME;
  public static double ALT_POLE_DAMAGE_MODIFIER;
  public static int STARTING_ADENA;
  public static int SWIMING_SPEED;
  public static int INVENTORY_MAXIMUM_NO_DWARF;
  public static int INVENTORY_MAXIMUM_DWARF;
  public static int INVENTORY_MAXIMUM_GM;
  public static int QUEST_INVENTORY_MAXIMUM;
  public static int WAREHOUSE_SLOTS_NO_DWARF;
  public static int WAREHOUSE_SLOTS_DWARF;
  public static int WAREHOUSE_SLOTS_CLAN;
  public static int FREIGHT_SLOTS;
  public static boolean REGEN_SIT_WAIT;
  public static double RESPAWN_RESTORE_CP;
  public static double RESPAWN_RESTORE_HP;
  public static double RESPAWN_RESTORE_MP;
  public static int MAX_PVTSTORE_SLOTS_DWARF;
  public static int MAX_PVTSTORE_SLOTS_OTHER;
  public static int MAX_PVTCRAFT_SLOTS;
  public static int GM_NAME_COLOUR;
  public static boolean GM_HERO_AURA;
  public static int NORMAL_NAME_COLOUR;
  public static int CLANLEADER_NAME_COLOUR;
  public static int KARMA_MIN_KARMA;
  public static int KARMA_SP_DIVIDER;
  public static int KARMA_LOST_BASE;
  public static int PVP_TIME;
  public static boolean HONOR_SYSTEM_ENABLE;
  public static int HONOR_SYSTEM_WON_ITEM_ID;
  public static int HONOR_SYSTEM_WON_ITEM_COUNT;
  public static int HONOR_SYSTEM_LOSE_ITEM_ID;
  public static int HONOR_SYSTEM_LOSE_ITEM_COUNT;
  public static int HONOR_SYSTEM_PVP_ITEM_ID;
  public static int HONOR_SYSTEM__PVP_ITEM_COUNT;
  public static boolean HONOR_SYSTEM__IN_PVP_ZONE;
  public static boolean DISABLE_ENCHANT_BOOKS;
  public static boolean DISABLE_ENCHANT_BOOKS_ALL;
  public static final String RESIDENCE_CONFIG_FILE = "config/residence.properties";
  public static boolean ALLOW_CH_DOOR_OPEN_ON_CLICK;
  public static boolean ALT_CH_ALL_BUFFS;
  public static boolean ALT_CH_ALLOW_1H_BUFFS;
  public static boolean ALT_CH_SIMPLE_DIALOG;
  public static int CH_BID_GRADE1_MINCLANLEVEL;
  public static int CH_BID_GRADE1_MINCLANMEMBERS;
  public static int CH_BID_GRADE1_MINCLANMEMBERSLEVEL;
  public static int CH_BID_GRADE2_MINCLANLEVEL;
  public static int CH_BID_GRADE2_MINCLANMEMBERS;
  public static int CH_BID_GRADE2_MINCLANMEMBERSLEVEL;
  public static int CH_BID_GRADE3_MINCLANLEVEL;
  public static int CH_BID_GRADE3_MINCLANMEMBERS;
  public static int CH_BID_GRADE3_MINCLANMEMBERSLEVEL;
  public static double RESIDENCE_LEASE_FUNC_MULTIPLIER;
  public static double RESIDENCE_LEASE_MULTIPLIER;
  public static final String RATES_FILE = "config/rates.properties";
  public static double RATE_XP;
  public static double RATE_SP;
  public static double RATE_QUESTS_REWARD;
  public static double RATE_QUESTS_DROP;
  public static double RATE_CLAN_REP_SCORE;
  public static int RATE_CLAN_REP_SCORE_MAX_AFFECTED;
  public static double RATE_DROP_ADENA;
  public static double RATE_DROP_ITEMS;
  public static double RATE_DROP_COMMON_ITEMS;
  public static double RATE_DROP_RAIDBOSS;
  public static double RATE_DROP_SPOIL;
  public static int[] NO_RATE_ITEMS;
  public static boolean NO_RATE_EQUIPMENT;
  public static boolean NO_RATE_KEY_MATERIAL;
  public static boolean NO_RATE_RECIPES;
  public static double RATE_DROP_SIEGE_GUARD;
  public static double RATE_MANOR;
  public static double RATE_FISH_DROP_COUNT;
  public static boolean RATE_PARTY_MIN;
  public static double RATE_HELLBOUND_CONFIDENCE;
  public static int RATE_MOB_SPAWN;
  public static int RATE_MOB_SPAWN_MIN_LEVEL;
  public static int RATE_MOB_SPAWN_MAX_LEVEL;
  public static final String EVENTS_CONFIG_FILE = "config/events.properties";
  public static double EVENT_CofferOfShadowsPriceRate;
  public static double EVENT_CofferOfShadowsRewardRate;
  public static int EVENT_LastHeroItemID;
  public static double EVENT_LastHeroItemCOUNT;
  public static int EVENT_LastHeroTime;
  public static boolean EVENT_LastHeroRate;
  public static double EVENT_LastHeroItemCOUNTFinal;
  public static boolean EVENT_LastHeroRateFinal;
  public static int EVENT_LastHeroChanceToStart;
  public static int EVENT_TvTItemID;
  public static double EVENT_TvTItemCOUNT;
  public static int EVENT_TvTTime;
  public static boolean EVENT_TvT_rate;
  public static int EVENT_TvTChanceToStart;
  public static int EVENT_CtFItemID;
  public static double EVENT_CtFItemCOUNT;
  public static int EVENT_CtFTime;
  public static boolean EVENT_CtF_rate;
  public static int EVENT_CtFChanceToStart;
  public static double EVENT_TFH_POLLEN_CHANCE;
  public static double EVENT_GLITTMEDAL_NORMAL_CHANCE;
  public static double EVENT_GLITTMEDAL_GLIT_CHANCE;
  public static double EVENT_L2DAY_LETTER_CHANCE;
  public static double EVENT_CHANGE_OF_HEART_CHANCE;
  public static double EVENT_APIL_FOOLS_DROP_CHANCE;
  public static boolean EVENT_BOUNTY_HUNTERS_ENABLED;
  public static long EVENT_SAVING_SNOWMAN_LOTERY_PRICE;
  public static int EVENT_SAVING_SNOWMAN_REWARDER_CHANCE;
  public static double EVENT_TRICK_OF_TRANS_CHANCE;
  public static double EVENT_MARCH8_DROP_CHANCE;
  public static double EVENT_MARCH8_PRICE_RATE;
  public static int ENCHANT_CHANCE_MASTER_YOGI_STAFF;
  public static int ENCHANT_MAX_MASTER_YOGI_STAFF;
  public static int SAFE_ENCHANT_MASTER_YOGI_STAFF;
  public static int TMEVENTINTERVAL;
  public static int TMTIME1;
  public static int TMWAVE1COUNT;
  public static int TMWAVE2;
  public static boolean ALLOW_WEDDING;
  public static int WEDDING_PRICE;
  public static boolean WEDDING_PUNISH_INFIDELITY;
  public static boolean WEDDING_TELEPORT;
  public static int WEDDING_TELEPORT_PRICE;
  public static int WEDDING_TELEPORT_INTERVAL;
  public static boolean WEDDING_SAMESEX;
  public static boolean WEDDING_FORMALWEAR;
  public static int WEDDING_DIVORCE_COSTS;
  public static final String SERVICES_FILE = "config/services.properties";
  public static List<Integer> ALLOW_CLASS_MASTERS_LIST = new ArrayList();
  public static String CLASS_MASTERS_PRICE;
  public static int CLASS_MASTERS_PRICE_ITEM;
  public static boolean SERVICES_CHANGE_NICK_ENABLED;
  public static int SERVICES_CHANGE_NICK_PRICE;
  public static int SERVICES_CHANGE_NICK_ITEM;
  public static boolean SERVICES_CHANGE_CLAN_NAME_ENABLED;
  public static int SERVICES_CHANGE_CLAN_NAME_PRICE;
  public static int SERVICES_CHANGE_CLAN_NAME_ITEM;
  public static boolean SERVICES_CHANGE_PET_NAME_ENABLED;
  public static int SERVICES_CHANGE_PET_NAME_PRICE;
  public static int SERVICES_CHANGE_PET_NAME_ITEM;
  public static boolean SERVICES_EXCHANGE_BABY_PET_ENABLED;
  public static int SERVICES_EXCHANGE_BABY_PET_PRICE;
  public static int SERVICES_EXCHANGE_BABY_PET_ITEM;
  public static boolean SERVICES_CHANGE_SEX_ENABLED;
  public static int SERVICES_CHANGE_SEX_PRICE;
  public static int SERVICES_CHANGE_SEX_ITEM;
  public static boolean SERVICES_CHANGE_BASE_ENABLED;
  public static int SERVICES_CHANGE_BASE_PRICE;
  public static int SERVICES_CHANGE_BASE_ITEM;
  public static boolean SERVICES_SEPARATE_SUB_ENABLED;
  public static int SERVICES_SEPARATE_SUB_PRICE;
  public static int SERVICES_SEPARATE_SUB_ITEM;
  public static boolean SERVICES_CHANGE_NICK_COLOR_ENABLED;
  public static int SERVICES_CHANGE_NICK_COLOR_PRICE;
  public static int SERVICES_CHANGE_NICK_COLOR_ITEM;
  public static String[] SERVICES_CHANGE_NICK_COLOR_LIST;
  public static boolean SERVICES_BASH_ENABLED;
  public static boolean SERVICES_BASH_SKIP_DOWNLOAD;
  public static int SERVICES_BASH_RELOAD_TIME;
  public static int SERVICES_RATE_TYPE;
  public static int[] SERVICES_RATE_BONUS_PRICE;
  public static int[] SERVICES_RATE_BONUS_ITEM;
  public static double[] SERVICES_RATE_BONUS_VALUE;
  public static int[] SERVICES_RATE_BONUS_DAYS;
  public static boolean SERVICES_NOBLESS_SELL_ENABLED;
  public static int SERVICES_NOBLESS_SELL_PRICE;
  public static int SERVICES_NOBLESS_SELL_ITEM;
  public static boolean SERVICES_EXPAND_INVENTORY_ENABLED;
  public static int SERVICES_EXPAND_INVENTORY_PRICE;
  public static int SERVICES_EXPAND_INVENTORY_ITEM;
  public static int SERVICES_EXPAND_INVENTORY_MAX;
  public static boolean SERVICES_EXPAND_WAREHOUSE_ENABLED;
  public static int SERVICES_EXPAND_WAREHOUSE_PRICE;
  public static int SERVICES_EXPAND_WAREHOUSE_ITEM;
  public static boolean SERVICES_EXPAND_CWH_ENABLED;
  public static int SERVICES_EXPAND_CWH_PRICE;
  public static int SERVICES_EXPAND_CWH_ITEM;
  public static String SERVICES_SELLPETS;
  public static boolean SERVICES_OFFLINE_TRADE_ALLOW;
  public static boolean SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE;
  public static int SERVICES_OFFLINE_TRADE_MIN_LEVEL;
  public static int SERVICES_OFFLINE_TRADE_NAME_COLOR;
  public static int SERVICES_OFFLINE_TRADE_PRICE;
  public static int SERVICES_OFFLINE_TRADE_PRICE_ITEM;
  public static long SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK;
  public static boolean SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART;
  public static boolean SERVICES_GIRAN_HARBOR_ENABLED;
  public static boolean SERVICES_PARNASSUS_ENABLED;
  public static boolean SERVICES_PARNASSUS_NOTAX;
  public static long SERVICES_PARNASSUS_PRICE;
  public static boolean SERVICES_ALLOW_LOTTERY;
  public static int SERVICES_LOTTERY_PRIZE;
  public static int SERVICES_ALT_LOTTERY_PRICE;
  public static int SERVICES_LOTTERY_TICKET_PRICE;
  public static double SERVICES_LOTTERY_5_NUMBER_RATE;
  public static double SERVICES_LOTTERY_4_NUMBER_RATE;
  public static double SERVICES_LOTTERY_3_NUMBER_RATE;
  public static int SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE;
  public static boolean SERVICES_ALLOW_ROULETTE;
  public static long SERVICES_ROULETTE_MIN_BET;
  public static long SERVICES_ROULETTE_MAX_BET;
  public static boolean SERVICES_NO_TRADE_ONLY_OFFLINE;
  public static double SERVICES_TRADE_TAX;
  public static double SERVICES_OFFSHORE_TRADE_TAX;
  public static boolean SERVICES_OFFSHORE_NO_CASTLE_TAX;
  public static boolean SERVICES_TRADE_TAX_ONLY_OFFLINE;
  public static boolean SERVICES_TRADE_ONLY_FAR;
  public static int SERVICES_TRADE_RADIUS;
  public static int SERVICES_TRADE_MIN_LEVEL;
  public static boolean SERVICES_ENABLE_NO_CARRIER;
  public static int SERVICES_NO_CARRIER_DEFAULT_TIME;
  public static int SERVICES_NO_CARRIER_MAX_TIME;
  public static int SERVICES_NO_CARRIER_MIN_TIME;
  public static boolean ITEM_BROKER_ITEM_SEARCH;
  public static int[] CLASS_MASTERS_PRICE_LIST = new int[4];
  public static boolean ALLOW_EVENT_GATEKEEPER;
  public static boolean ADD_ACTIVATE_SUB;
  public static int ADD_ACTIVATE_SUB_ITEM;
  public static int ADD_ACTIVATE_SUB_PRICE;
  public static boolean LEVEL_UP_ENABLED;
  public static int LEVEL_UP_PRICE;
  public static int LEVEL_UP_ITEM;
  public static int LEVEL_UP_MAX;
  public static boolean HERO_SELL_ENABLED;
  public static int HERO_SELL_ITEM;
  public static int HERO_SELL_PRICE;
  public static boolean CB_CLASS_ENABLED;
  public static final String SPOIL_CONFIG_FILE = "config/spoil.properties";
  public static double BASE_SPOIL_RATE;
  public static double MINIMUM_SPOIL_RATE;
  public static boolean ALT_SPOIL_FORMULA;
  public static double MANOR_SOWING_BASIC_SUCCESS;
  public static double MANOR_SOWING_ALT_BASIC_SUCCESS;
  public static double MANOR_HARVESTING_BASIC_SUCCESS;
  public static int MANOR_DIFF_PLAYER_TARGET;
  public static double MANOR_DIFF_PLAYER_TARGET_PENALTY;
  public static int MANOR_DIFF_SEED_TARGET;
  public static double MANOR_DIFF_SEED_TARGET_PENALTY;
  public static boolean ALLOW_MANOR;
  public static int MANOR_REFRESH_TIME;
  public static int MANOR_REFRESH_MIN;
  public static int MANOR_APPROVE_TIME;
  public static int MANOR_APPROVE_MIN;
  public static int MANOR_MAINTENANCE_PERIOD;
  public static final String GEODATA_CONFIG_FILE = "config/geodata.properties";
  public static int GEO_X_FIRST;
  public static int GEO_Y_FIRST;
  public static int GEO_X_LAST;
  public static int GEO_Y_LAST;
  public static String GEOFILES_PATTERN;
  public static boolean ALLOW_GEODATA;
  public static boolean ALLOW_FALL_FROM_WALLS;
  public static boolean ALLOW_KEYBOARD_MOVE;
  public static boolean COMPACT_GEO;
  public static int CLIENT_Z_SHIFT;
  public static int MAX_Z_DIFF;
  public static int MIN_LAYER_HEIGHT;
  public static int PATHFIND_BOOST;
  public static boolean PATHFIND_DIAGONAL;
  public static boolean PATH_CLEAN;
  public static int PATHFIND_MAX_Z_DIFF;
  public static long PATHFIND_MAX_TIME;
  public static String PATHFIND_BUFFERS;
  public static final String AI_CONFIG_FILE = "config/ai.properties";
  public static int AI_TASK_MANAGER_COUNT;
  public static long AI_TASK_ATTACK_DELAY;
  public static long AI_TASK_ACTIVE_DELAY;
  public static boolean BLOCK_ACTIVE_TASKS;
  public static boolean ALWAYS_TELEPORT_HOME;
  public static boolean RND_WALK;
  public static int RND_WALK_RATE;
  public static int RND_ANIMATION_RATE;
  public static int AGGRO_CHECK_INTERVAL;
  public static long NONAGGRO_TIME_ONTELEPORT;
  public static int MAX_DRIFT_RANGE;
  public static int MAX_PURSUE_RANGE;
  public static int MAX_PURSUE_UNDERGROUND_RANGE;
  public static int MAX_PURSUE_RANGE_RAID;
  public static final String TELNET_CONFIGURATION_FILE = "config/telnet.properties";
  public static boolean IS_TELNET_ENABLED;
  public static String TELNET_DEFAULT_ENCODING;
  public static String TELNET_PASSWORD;
  public static String TELNET_HOSTNAME;
  public static int TELNET_PORT;
  public static final String ANUSEWORDS_CONFIG_FILE = "config/abusewords.txt";
  public static Pattern[] ABUSEWORD_LIST = new Pattern[0];
  public static final String GM_PERSONAL_ACCESS_FILE = "config/GMAccess.xml";
  public static final String GM_ACCESS_FILES_DIR = "config/GMAccess.d/";
  public static Map<Integer, PlayerAccess> gmlist = new HashMap();
  public static boolean DEBUG;
  public static boolean GOODS_INVENTORY_ENABLED = false;

  public static void loadItemsConfig()
  {
    ExProperties itemsSettings = load("config/items.properties");

    CRAFT_MASTERWORK_CHANCE = itemsSettings.getProperty("CraftMasterworkChance", 3.0D);
    CRAFT_DOUBLECRAFT_CHANCE = itemsSettings.getProperty("CraftDoubleCraftChance", 3.0D);

    ALT_GAME_UNREGISTER_RECIPE = itemsSettings.getProperty("AltUnregisterRecipe", true);

    ALT_ADD_RECIPES = itemsSettings.getProperty("AltAddRecipes", 0);

    ALT_ALLOW_AUGMENT_ALL = itemsSettings.getProperty("AugmentAll", false);

    ALT_MAMMON_UPGRADE = itemsSettings.getProperty("MammonUpgrade", 6680500);

    ALT_MAMMON_EXCHANGE = itemsSettings.getProperty("MammonExchange", 10091400);

    ALT_ALLOW_SELL_COMMON = itemsSettings.getProperty("AllowSellCommon", true);

    ALT_ALLOW_SHADOW_WEAPONS = itemsSettings.getProperty("AllowShadowWeapons", true);

    ALT_ALLOW_TATTOO = itemsSettings.getProperty("AllowTattoo", false);

    ALT_DISABLED_MULTISELL = itemsSettings.getProperty("DisabledMultisells", ArrayUtils.EMPTY_INT_ARRAY);

    ALT_SHOP_PRICE_LIMITS = itemsSettings.getProperty("ShopPriceLimits", ArrayUtils.EMPTY_INT_ARRAY);

    ALT_SHOP_UNALLOWED_ITEMS = itemsSettings.getProperty("ShopUnallowedItems", ArrayUtils.EMPTY_INT_ARRAY);

    ALT_ALLOWED_PET_POTIONS = itemsSettings.getProperty("AllowedPetPotions", new int[] { 735, 1060, 1061, 1062, 1374, 1375, 1539, 1540, 6035, 6036 });

    ALT_ALLOW_DROP_AUGMENTED = itemsSettings.getProperty("AlowDropAugmented", false);

    ALT_OPEN_CLOAK_SLOT = itemsSettings.getProperty("OpenCloakSlot", false);

    ENCHANT_CHANCE_WEAPON = itemsSettings.getProperty("EnchantChance", 66);
    ENCHANT_CHANCE_ARMOR = itemsSettings.getProperty("EnchantChanceArmor", ENCHANT_CHANCE_WEAPON);
    ENCHANT_CHANCE_ACCESSORY = itemsSettings.getProperty("EnchantChanceAccessory", ENCHANT_CHANCE_ARMOR);
    ENCHANT_CHANCE_CRYSTAL_WEAPON = itemsSettings.getProperty("EnchantChanceCrystal", 66);
    ENCHANT_CHANCE_CRYSTAL_ARMOR = itemsSettings.getProperty("EnchantChanceCrystalArmor", ENCHANT_CHANCE_CRYSTAL_WEAPON);
    ENCHANT_CHANCE_CRYSTAL_ACCESSORY = itemsSettings.getProperty("EnchantChanceCrystalAccessory", ENCHANT_CHANCE_CRYSTAL_ARMOR);
    SAFE_ENCHANT_COMMON = itemsSettings.getProperty("SafeEnchantCommon", 3);
    SAFE_ENCHANT_FULL_BODY = itemsSettings.getProperty("SafeEnchantFullBody", 4);
    ENCHANT_MAX = itemsSettings.getProperty("EnchantMax", 20);
    ARMOR_OVERENCHANT_HPBONUS_LIMIT = itemsSettings.getProperty("ArmorOverEnchantHPBonusLimit", 10) - 3;
    SHOW_ENCHANT_EFFECT_RESULT = itemsSettings.getProperty("ShowEnchantEffectResult", false);
    ADD_FAIL_ENCHANT_VALUE = itemsSettings.getProperty("FailEnchantValue", 0);
    ADD_BLESSED_ENCHANT_CHANCE_WEAPON = itemsSettings.getProperty("EnchantChanceBlessedWeapon", 68);
    ADD_BLESSED_ENCHANT_CHANCE_ARMOR = itemsSettings.getProperty("EnchantChanceBlessedArmor", 52);
    ADD_BLESSED_ENCHANT_CHANCE_JEWEL = itemsSettings.getProperty("EnchantChanceBlessedAccessory", 54);

    ENCHANT_ATTRIBUTE_STONE_CHANCE = itemsSettings.getProperty("EnchantAttributeChance", 75);
    ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE = itemsSettings.getProperty("EnchantAttributeCrystalChance", 53);

    KARMA_DROP_GM = itemsSettings.getProperty("CanGMDropEquipment", false);

    KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList();
    for (int id : itemsSettings.getProperty("ListOfNonDroppableItems", new int[] { 57, 1147, 425, 1146, 461, 10, 2368, 7, 6, 2370, 2369, 3500, 3501, 3502, 4422, 4423, 4424, 2375, 6648, 6649, 6650, 6842, 6834, 6835, 6836, 6837, 6838, 6839, 6840, 5575, 7694, 6841, 8181 }))
    {
      KARMA_LIST_NONDROPPABLE_ITEMS.add(Integer.valueOf(id));
    }DROP_ITEMS_AUGMENTED = itemsSettings.getProperty("DropAugmented", false);

    DROP_ITEMS_ON_DIE = itemsSettings.getProperty("DropOnDie", false);
    NORMAL_DROPCHANCE_BASE = itemsSettings.getProperty("ChanceOfNormalDropBase", 1.0D);
    KARMA_DROPCHANCE_BASE = itemsSettings.getProperty("ChanceOfPKDropBase", 20.0D);
    KARMA_DROPCHANCE_MOD = itemsSettings.getProperty("ChanceOfPKsDropMod", 1.0D);
    DROPCHANCE_EQUIPPED_WEAPON = itemsSettings.getProperty("ChanceOfDropWeapon", 3);
    DROPCHANCE_EQUIPMENT = itemsSettings.getProperty("ChanceOfDropEquippment", 17);
    DROPCHANCE_ITEM = itemsSettings.getProperty("ChanceOfDropOther", 80);

    KARMA_DROP_ITEM_LIMIT = itemsSettings.getProperty("MaxItemsDroppable", 10);
    KARMA_RANDOM_DROP_LOCATION_LIMIT = itemsSettings.getProperty("MaxDropThrowDistance", 70);

    MIN_PK_TO_ITEMS_DROP = itemsSettings.getProperty("MinPKToDropItems", 5);
    KARMA_NEEDED_TO_DROP = itemsSettings.getProperty("KarmaNeededToDrop", true);

    ADD_REDUCE_ARROW = itemsSettings.getProperty("ArrowReduce", true);

    ADD_CONSAMBLE_LIST = itemsSettings.getProperty("ConsambleList", ArrayUtils.EMPTY_INT_ARRAY);

    ADD_EXPERTISE_PENALTY = itemsSettings.getProperty("ExpertisePenalty", true);

    ADD_REDUCE_SHOT = itemsSettings.getProperty("ShotReduce", true);
  }

  public static void loadOlympiadConfig()
  {
    ExProperties olympSettings = load("config/olympiad.properties");

    ENABLE_OLYMPIAD = olympSettings.getProperty("EnableOlympiad", true);
    ENABLE_OLYMPIAD_SPECTATING = olympSettings.getProperty("EnableOlympiadSpectating", true);

    OLYMPIAD_STADIAS_COUNT = olympSettings.getProperty("OlympiadStadiasCount", 160);

    ALT_OLY_START_TIME = olympSettings.getProperty("AltOlyStartTime", 18);
    ALT_OLY_MIN = olympSettings.getProperty("AltOlyMin", 0);

    ALT_OLY_CPERIOD = olympSettings.getProperty("AltOlyCPeriod", 21600000);
    ALT_OLY_WPERIOD = olympSettings.getProperty("AltOlyWPeriod", 604800000);
    ALT_OLY_VPERIOD = olympSettings.getProperty("AltOlyVPeriod", 43200000);

    CLASS_GAME_MIN = olympSettings.getProperty("ClassGameMin", 10);
    NONCLASS_GAME_MIN = olympSettings.getProperty("NonClassGameMin", 10);
    TEAM_GAME_MIN = olympSettings.getProperty("TeamGameMin", 6);

    ALT_OLY_REG_DISPLAY = olympSettings.getProperty("AltOlyRegistrationDisplayNumber", 100);

    ALT_OLY_BATTLE_REWARD_ITEM = olympSettings.getProperty("AltOlyBattleRewItem", 13722);
    ALT_OLY_CLASSED_RITEM_C = olympSettings.getProperty("AltOlyClassedRewItemCount", 50);
    ALT_OLY_NONCLASSED_RITEM_C = olympSettings.getProperty("AltOlyNonClassedRewItemCount", 40);
    ALT_OLY_TEAM_RITEM_C = olympSettings.getProperty("AltOlyTeamRewItemCount", 50);
    ALT_OLY_COMP_RITEM = olympSettings.getProperty("AltOlyCompRewItem", 13722);

    ALT_OLY_GP_PER_POINT = olympSettings.getProperty("AltOlyGPPerPoint", 1000);
    ALT_OLY_HERO_POINTS = olympSettings.getProperty("AltOlyHeroPoints", 200);
    ALT_OLY_RANK1_POINTS = olympSettings.getProperty("AltOlyRank1Points", 100);
    ALT_OLY_RANK2_POINTS = olympSettings.getProperty("AltOlyRank2Points", 75);
    ALT_OLY_RANK3_POINTS = olympSettings.getProperty("AltOlyRank3Points", 55);
    ALT_OLY_RANK4_POINTS = olympSettings.getProperty("AltOlyRank4Points", 40);
    ALT_OLY_RANK5_POINTS = olympSettings.getProperty("AltOlyRank5Points", 30);

    GAME_MAX_LIMIT = olympSettings.getProperty("GameMaxLimit", 70);
    GAME_CLASSES_COUNT_LIMIT = olympSettings.getProperty("GameClassesCountLimit", 30);
    GAME_NOCLASSES_COUNT_LIMIT = olympSettings.getProperty("GameNoClassesCountLimit", 60);
    GAME_TEAM_COUNT_LIMIT = olympSettings.getProperty("GameTeamCountLimit", 10);

    OLYMPIAD_BATTLES_FOR_REWARD = olympSettings.getProperty("OlympiadBattlesForReward", 15);

    OLYMPIAD_POINTS_DEFAULT = olympSettings.getProperty("OlympiadPointsDefault", 50);
    OLYMPIAD_POINTS_WEEKLY = olympSettings.getProperty("OlympiadPointsWeekly", 10);

    OLYMPIAD_OLDSTYLE_STAT = olympSettings.getProperty("OlympiadOldStyleStat", false);

    OLY_ENCH_LIMIT_ENABLE = olympSettings.getProperty("OlyEnchantLimit", false);
    OLY_ENCHANT_LIMIT_WEAPON = olympSettings.getProperty("OlyEnchantLimitWeapon", 0);
    OLY_ENCHANT_LIMIT_ARMOR = olympSettings.getProperty("OlyEnchantLimitArmor", 0);
    OLY_ENCHANT_LIMIT_JEWEL = olympSettings.getProperty("OlyEnchantLimitJewel", 0);
  }

  public static void loadSkillsConfig()
  {
    ExProperties skillsSettings = load("config/skills.properties");

    ALT_SAVE_UNSAVEABLE = skillsSettings.getProperty("AltSaveUnsaveable", false);

    ALT_SAVE_EFFECTS_REMAINING_TIME = skillsSettings.getProperty("AltSaveEffectsRemainingTime", 5);

    ALT_SHOW_REUSE_MSG = skillsSettings.getProperty("AltShowSkillReuseMessage", true);

    ALT_DELETE_SA_BUFFS = skillsSettings.getProperty("AltDeleteSABuffs", false);

    SAVING_SPS = skillsSettings.getProperty("SavingSpS", false);
    MANAHEAL_SPS_BONUS = skillsSettings.getProperty("ManahealSpSBonus", false);

    ALT_BUFF_LIMIT = skillsSettings.getProperty("BuffLimit", 20);
    ADD_MUSIC_LIMIT = skillsSettings.getProperty("MusicLimit", 12);
    ADD_DEBUFF_LIMIT = skillsSettings.getProperty("DebuffLimit", 8);
    ADD_TRIGGER_LIMIT = skillsSettings.getProperty("TriggerLimit", 12);

    ADD_ENABLE_MODIFY_SKILL_DURATION = skillsSettings.getProperty("EnableSkillDuration", false);
    ADD_DANCE_MOD_ALLOW = skillsSettings.getProperty("DanceModAllow", false);

    if (ADD_ENABLE_MODIFY_SKILL_DURATION)
    {
      String[] propertySplit = skillsSettings.getProperty("SkillDurationList", "").split(";");
      ADD_SKILL_DURATION_LIST = new TIntIntHashMap(propertySplit.length);
      for (String skill : propertySplit)
      {
        String[] skillSplit = skill.split(",");
        if (skillSplit.length != 2) {
          _log.warn("[SkillDurationList]: invalid config property -> SkillDurationList \"" + skill + "\"");
        }
        else {
          try
          {
            ADD_SKILL_DURATION_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
          }
          catch (NumberFormatException nfe)
          {
            if (skill.isEmpty())
              continue;
            _log.warn("[SkillDurationList]: invalid config property -> SkillList \"" + skillSplit[0] + "\"" + skillSplit[1]);
          }
        }
      }

    }

    ALLOW_LEARN_TRANS_SKILLS_WO_QUEST = skillsSettings.getProperty("AllowLearnTransSkillsWOQuest", false);

    CLANHALL_BUFFTIME_MODIFIER = skillsSettings.getProperty("ClanHallBuffTimeModifier", 1.0D);
    SONGDANCETIME_MODIFIER = skillsSettings.getProperty("SongDanceTimeModifier", 1.0D);

    ALT_REMOVE_SKILLS_ON_DELEVEL = skillsSettings.getProperty("AltRemoveSkillsOnDelevel", true);

    ALT_ALL_PHYS_SKILLS_OVERHIT = skillsSettings.getProperty("AltAllPhysSkillsOverhit", true);

    AUGMENTATION_NG_SKILL_CHANCE = skillsSettings.getProperty("AugmentationNGSkillChance", 15);
    AUGMENTATION_NG_GLOW_CHANCE = skillsSettings.getProperty("AugmentationNGGlowChance", 0);
    AUGMENTATION_MID_SKILL_CHANCE = skillsSettings.getProperty("AugmentationMidSkillChance", 30);
    AUGMENTATION_MID_GLOW_CHANCE = skillsSettings.getProperty("AugmentationMidGlowChance", 40);
    AUGMENTATION_HIGH_SKILL_CHANCE = skillsSettings.getProperty("AugmentationHighSkillChance", 45);
    AUGMENTATION_HIGH_GLOW_CHANCE = skillsSettings.getProperty("AugmentationHighGlowChance", 70);
    AUGMENTATION_TOP_SKILL_CHANCE = skillsSettings.getProperty("AugmentationTopSkillChance", 60);
    AUGMENTATION_TOP_GLOW_CHANCE = skillsSettings.getProperty("AugmentationTopGlowChance", 100);
    AUGMENTATION_BASESTAT_CHANCE = skillsSettings.getProperty("AugmentationBaseStatChance", 1);
    AUGMENTATION_ACC_SKILL_CHANCE = skillsSettings.getProperty("AugmentationAccSkillChance", 10);

    SKILLS_CHANCE_MOD = skillsSettings.getProperty("SkillsChanceMod", 11.0D);
    SKILLS_CHANCE_POW = skillsSettings.getProperty("SkillsChancePow", 0.5D);
    SKILLS_CHANCE_MIN = skillsSettings.getProperty("SkillsChanceMin", 5.0D);
    SKILLS_CHANCE_CAP = skillsSettings.getProperty("SkillsChanceCap", 95.0D);
    SKILLS_CAST_TIME_MIN = skillsSettings.getProperty("SkillsCastTimeMin", 333);

    ALT_ABSORB_DAMAGE_MODIFIER = skillsSettings.getProperty("AbsorbDamageModifier", 1.0D);

    UNSTUCK_SKILL = skillsSettings.getProperty("UnstuckSkill", true);

    DANCE_CANCEL_BUFF = skillsSettings.getProperty("SkillsAllowDispelDansSong", false);
    MAGICFAILURES = skillsSettings.getProperty("MagicFailures", false);
    MAGICFAILURES_DIFF = skillsSettings.getProperty("MinLvlDependMagicSuccess", 9);
    MAGIC_SHIELD_BLOCK = skillsSettings.getProperty("MagicShieldBlock", false);
    MAGIC_FULL_SHIELD_BLOCK = skillsSettings.getProperty("MagicPerfectShieldBlockRate", 5);

    SKILLS_SHOW_CHANCE = skillsSettings.getProperty("SkillsShowChance", false);
    SKILLS_SHOW_BLOW_CHANCE = skillsSettings.getProperty("SkillsShowBlowChance", false);

    CLAN_SKILL_ITEM_ENABLED = skillsSettings.getProperty("ClanSkillItem", true);
  }

  public static void loadClanConfig()
  {
    ExProperties clanSettings = load("config/clan.properties");

    ALT_MAX_ALLY_SIZE = clanSettings.getProperty("AltMaxAllySize", 3);

    ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE = clanSettings.getProperty("AltAllowOthersWithdrawFromClanWarehouse", false);

    ALT_ALLOW_CLAN_COMMAND_ONLY_FOR_CLAN_LEADER = clanSettings.getProperty("AltAllowClanCommandOnlyForClanLeader", true);

    ALLOW_CLANSKILLS = clanSettings.getProperty("AllowClanSkills", true);

    ADD_MIN_ACADEM_POINT = clanSettings.getProperty("MinAcademPoint", 190);

    ADD_MAX_ACADEM_POINT = clanSettings.getProperty("MaxAcademPoint", 650);

    CLAN_PENALTY = clanSettings.getProperty("HoursBeforeJoinAClan", 24);

    CLAN_MEMBERS_FOR_WAR = clanSettings.getProperty("ClanMembersForWar", 15);
    CLAN_WAR_MAX = clanSettings.getProperty("ClanWarMax", 30);
    CLAN_MIN_LVL_FOR_WAR = clanSettings.getProperty("MinClanLvlForWar", 3);

    ALLY_PENALTY = clanSettings.getProperty("HoursBeforeCreateNewAllyWhenDissolved", 1);

    QUEST_508_RATE = clanSettings.getProperty("Quest508RepScoreRate", 1);
    QUEST_509_RATE = clanSettings.getProperty("Quest509RepScoreRate", 1);
    QUEST_510_RATE = clanSettings.getProperty("Quest510RepScoreRate", 1);

    CLAN_REP_FOR_TAKE_CASTLE = clanSettings.getProperty("ClanRepNewCastle", 3000);
    CLAN_REP_FOR_DEFEND_CASTLE = clanSettings.getProperty("ClanRepDefendCastle", 1500);
    CLAN_REP_FOR_LOSE_CASTLE = clanSettings.getProperty("ClanRepCastleLoss", 3000);
    CLAN_BLOOD_ALLIANCE_REWARD = clanSettings.getProperty("BloodAllianceCount", 1);

    CLAN_REP_FOR_FORT = clanSettings.getProperty("ClanRepFortNewOwner", 1700);
    CLAN_REP_FOR_CH = clanSettings.getProperty("ClanRepCHOldOwner", 1700);

    CLAN_WAR_REP = clanSettings.getProperty("ReputationScorePerKill", 1);

    CLAN_REP_FOR_LEVEL_6 = clanSettings.getProperty("ClanRepForLevel6", 10000);
    CLAN_REP_FOR_LEVEL_7 = clanSettings.getProperty("ClanRepForLevel7", 20000);
    CLAN_REP_FOR_LEVEL_8 = clanSettings.getProperty("ClanRepForLevel8", 40000);
    CLAN_REP_FOR_LEVEL_9 = clanSettings.getProperty("ClanRepForLevel9", 40000);
    CLAN_REP_FOR_LEVEL_10 = clanSettings.getProperty("ClanRepForLevel10", 40000);
    CLAN_REP_FOR_LEVEL_11 = clanSettings.getProperty("ClanRepForLevel11", 75000);

    CLAN_MEMBERS_FOR_LEVEL_6 = clanSettings.getProperty("ClanMemberForLevel6", 30);
    CLAN_MEMBERS_FOR_LEVEL_7 = clanSettings.getProperty("ClanMemberForLevel7", 80);
    CLAN_MEMBERS_FOR_LEVEL_8 = clanSettings.getProperty("ClanMemberForLevel8", 120);
    CLAN_MEMBERS_FOR_LEVEL_9 = clanSettings.getProperty("ClanMemberForLevel9", 120);
    CLAN_MEMBERS_FOR_LEVEL_10 = clanSettings.getProperty("ClanMemberForLevel10", 140);
    CLAN_MEMBERS_FOR_LEVEL_11 = clanSettings.getProperty("ClanMemberForLevel11", 170);

    CLAN_LVL_UP_ITEM_FOR_9 = clanSettings.getProperty("ClanLvlUpItemForLevel9", 9910);
    CLAN_LVL_UP_ITEM_COUNT_FOR_9 = clanSettings.getProperty("ClanLvlUpItemCountForLevel9", 150);

    CLAN_LVL_UP_ITEM_FOR_10 = clanSettings.getProperty("ClanLvlUpItemForLevel10", 9911);
    CLAN_LVL_UP_ITEM_COUNT_FOR_10 = clanSettings.getProperty("ClanLvlUpItemCountForLevel10", 5);

    CLAN_CREATE_ROYAL = clanSettings.getProperty("ClanCreateRoyal", 5000);
    CLAN_CREATE_KNIGHT = clanSettings.getProperty("ClanCreateKnight", 10000);

    CLAN_MAX_ACADEM = clanSettings.getProperty("ClanMaxUserInAkadem", 20);

    CLAN_MAX_MAIN0 = clanSettings.getProperty("ClanMaxUserInClanLvl0", 10);
    CLAN_MAX_MAIN1 = clanSettings.getProperty("ClanMaxUserInClanLvl1", 15);
    CLAN_MAX_MAIN2 = clanSettings.getProperty("ClanMaxUserInClanLvl2", 20);
    CLAN_MAX_MAIN3 = clanSettings.getProperty("ClanMaxUserInClanLvl3", 30);
    CLAN_MAX_MAIN4 = clanSettings.getProperty("ClanMaxUserInClanLvl4", 40);
    CLAN_MAX_MAIN5 = clanSettings.getProperty("ClanMaxUserInClanLvl5", 40);
    CLAN_MAX_MAIN6 = clanSettings.getProperty("ClanMaxUserInClanLvl6", 40);
    CLAN_MAX_MAIN7 = clanSettings.getProperty("ClanMaxUserInClanLvl7", 40);
    CLAN_MAX_MAIN8 = clanSettings.getProperty("ClanMaxUserInClanLvl8", 40);
    CLAN_MAX_MAIN9 = clanSettings.getProperty("ClanMaxUserInClanLvl9", 40);
    CLAN_MAX_MAIN10 = clanSettings.getProperty("ClanMaxUserInClanLvl10", 40);
    CLAN_MAX_MAIN11 = clanSettings.getProperty("ClanMaxUserInClanLvl11", 40);

    CLAN_MAX_ROYAL = clanSettings.getProperty("ClanMaxUserInClanRoyal", 20);
    CLAN_MAX_ROYAL_IN_LEVEL_11 = clanSettings.getProperty("ClanMaxUserInClanRoyalLvl11", 30);

    CLAN_MAX_KNIGHT12 = clanSettings.getProperty("ClanMaxUserInKnight12InClan", 10);
    CLAN_MAX_KNIGHT12_IN_LEVEL_9 = clanSettings.getProperty("ClanMaxUserInKnight12InClanLvl9", 25);
    CLAN_MAX_KNIGHT34 = clanSettings.getProperty("ClanMaxUserInKnight34InClan", 10);
    CLAN_MAX_KNIGHT34_IN_LEVEL_10 = clanSettings.getProperty("ClanMaxUserInKnight34InClanLvl10", 25);

    FS_BLOOD_OATH_CYCLE = clanSettings.getProperty("FortressPeriodicUpdateFrequency", 360);
    FS_BLOOD_OATH_COUNT = clanSettings.getProperty("FortressBloodOathCount", 1);
  }

  public static void loadNpcConfig()
  {
    ExProperties NpcSettings = load("config/npc.properties");

    ALT_SIMPLE_SIGNS = NpcSettings.getProperty("PushkinSignsOptions", false);

    ALT_BS_CRYSTALLIZE = NpcSettings.getProperty("BSCrystallize", false);

    ALT_RAID_RESPAWN_MULTIPLIER = NpcSettings.getProperty("AltRaidRespawnMultiplier", 1.0D);

    ALT_CHAMPION_CHANCE1 = NpcSettings.getProperty("AltChampionChance1", 0.0D);
    ALT_CHAMPION_CHANCE2 = NpcSettings.getProperty("AltChampionChance2", 0.0D);
    ALT_CHAMPION_CAN_BE_AGGRO = NpcSettings.getProperty("AltChampionAggro", false);
    ALT_CHAMPION_CAN_BE_SOCIAL = NpcSettings.getProperty("AltChampionSocial", false);
    NPC_CHAMPION_MAX_LEVEL = NpcSettings.getProperty("ChampionMaxLevel", 75);
    NPC_CHAMPION_MIN_LEVEL = NpcSettings.getProperty("ChampionMinLevel", 20);

    ALT_NPC_PATK_MODIFIER = NpcSettings.getProperty("NpcPAtkModifier", 1.0D);
    ALT_NPC_MATK_MODIFIER = NpcSettings.getProperty("NpcMAtkModifier", 1.0D);
    ALT_NPC_MAXHP_MODIFIER = NpcSettings.getProperty("NpcMaxHpModifier", 1.0D);
    ALT_NPC_MAXMP_MODIFIER = NpcSettings.getProperty("NpcMapMpModifier", 1.0D);

    DEEPBLUE_DROP_RULES = NpcSettings.getProperty("UseDeepBlueDropRules", true);
    DEEPBLUE_DROP_MAXDIFF = NpcSettings.getProperty("DeepBlueDropMaxDiff", 8);
    DEEPBLUE_DROP_RAID_MAXDIFF = NpcSettings.getProperty("DeepBlueDropRaidMaxDiff", 2);

    ANNOUNCE_MAMMON_SPAWN = NpcSettings.getProperty("AnnounceMammonSpawn", true);

    RATE_RAID_REGEN = NpcSettings.getProperty("RateRaidRegen", 1.0D);
    RATE_RAID_DEFENSE = NpcSettings.getProperty("RateRaidDefense", 1.0D);
    RATE_RAID_ATTACK = NpcSettings.getProperty("RateRaidAttack", 1.0D);
    RATE_EPIC_DEFENSE = NpcSettings.getProperty("RateEpicDefense", RATE_RAID_DEFENSE);
    RATE_EPIC_ATTACK = NpcSettings.getProperty("RateEpicAttack", RATE_RAID_ATTACK);
    RAID_MAX_LEVEL_DIFF = NpcSettings.getProperty("RaidMaxLevelDiff", 8);
    PARALIZE_ON_RAID_DIFF = NpcSettings.getProperty("ParalizeOnRaidLevelDiff", true);

    MIN_NPC_ANIMATION = NpcSettings.getProperty("MinNPCAnimation", 10);
    MAX_NPC_ANIMATION = NpcSettings.getProperty("MaxNPCAnimation", 90);

    SERVER_SIDE_NPC_NAME = NpcSettings.getProperty("ServerSideNpcName", false);
    SERVER_SIDE_NPC_TITLE = NpcSettings.getProperty("ServerSideNpcTitle", false);
  }

  public static void loadQuestConfig()
  {
    ExProperties questSettings = load("config/quests.properties");

    ALT_NO_LASTHIT = questSettings.getProperty("NoLasthitOnRaid", false);
  }

  public static void loadSiegesConfig()
  {
    ExProperties siegeSettings = load("config/sieges.properties");

    DOMINION_WAR_REWARD_RATE = (float)siegeSettings.getProperty("BadgeRateForTW", 1.0D);
    DOMINION_WAR_HOUR = siegeSettings.getProperty("TerritoryWarSiegeHourOfDay", 20);

    CASTLE_SELECT_HOURS = siegeSettings.getProperty("CastleSelectHours", new int[] { 16, 20 });
    int[] tempCastleValidatonTime = siegeSettings.getProperty("CastleValidationDate", new int[] { 2, 4, 2003 });
    CASTLE_VALIDATION_DATE = Calendar.getInstance();
    CASTLE_VALIDATION_DATE.set(5, tempCastleValidatonTime[0]);
    CASTLE_VALIDATION_DATE.set(2, tempCastleValidatonTime[1] - 1);
    CASTLE_VALIDATION_DATE.set(1, tempCastleValidatonTime[2]);
    CASTLE_VALIDATION_DATE.set(11, 0);
    CASTLE_VALIDATION_DATE.set(12, 0);
    CASTLE_VALIDATION_DATE.set(13, 0);
    CASTLE_VALIDATION_DATE.set(14, 0);
    CASTLE_WAR_HOUR = siegeSettings.getProperty("CastleSiegeTime", 20);
  }

  public static void loadCommunityConfig()
  {
    ExProperties comSettings = load("config/community.properties");

    COMMUNITYBOARD_ENABLED = comSettings.getProperty("AllowCommunityBoard", true);
    BBS_DEFAULT = comSettings.getProperty("BBSDefault", "_bbshome");
    ALLOW_BUFFER_CB = comSettings.getProperty("AllowBuffer", true);
    MAX_SKILLS_IN_SCHEME_CB = comSettings.getProperty("BufferMaxSkillsInScheme", 30);
    BUFF_TIME_CB = comSettings.getProperty("BufferTime", 14400000);
    BUFFER_MIN_LVL_CB = comSettings.getProperty("BufferMinLvl", 1);
    BUFFER_MAX_LVL_CB = comSettings.getProperty("BufferMaxLvl", 85);
    ALLOW_SAVE_SCHEME_CB = comSettings.getProperty("BufferAllowSaveRestor", true);
    ALLOW_SHOP_CB = comSettings.getProperty("AllowShop", true);
    ALLOWED_MULTISELL = comSettings.getProperty("AllowMultisells", new int[] { 10061, 40011, 1008, 1009, 1010, 1006, 4001, 1005, 1007, 4002, 9998, 9999, 311262516, 400, 500, 9997 });
    DISBLED_MULTISELL_WO_PA = comSettings.getProperty("DisabledMultisellsWithoutPA", new int[] { 10061, 40011, 1008, 1009, 1010, 1006, 4001, 1005, 1007, 4002, 9998, 9999, 311262516, 400, 500, 9997 });
    ALLOW_ENCHANT_CB = comSettings.getProperty("AllowEnchant", true);
    ENCHANT_PRICE_ITEM_CB = comSettings.getProperty("EnchantPriceItem", 57);
    ENCHANT_MAX_CB = comSettings.getProperty("EnchantMax", 20);
    ENCHANT_PRICE_WEAPON = comSettings.getProperty("EnchantPriceWeapon", new int[] { 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1100, 1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000 });
    ENCHANT_PRICE_OTHER = comSettings.getProperty("EnchantPriceOther", new int[] { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160, 170, 180, 190, 200 });
    ENCHANT_LVL_CB = comSettings.getProperty("CBEnchantLvl", new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 });
    CB_MUL_MOD = comSettings.getProperty("CBEnchantMulMod", 1.2D);
  }

  public static void loadOtherConfig()
  {
    ExProperties otherSettings = load("config/other.properties");

    ALT_GAME_REQUIRE_CLAN_CASTLE = otherSettings.getProperty("AltRequireClanCastle", false);
    ALT_GAME_REQUIRE_CASTLE_DAWN = otherSettings.getProperty("AltRequireCastleDawn", true);
    ALT_GAME_ALLOW_ADENA_DAWN = otherSettings.getProperty("AltAllowAdenaDawn", true);

    SS_ANNOUNCE_PERIOD = otherSettings.getProperty("SSAnnouncePeriod", 0);

    ALT_DEATH_PENALTY = otherSettings.getProperty("EnableAltDeathPenalty", false);
    ALT_PK_DEATH_RATE = otherSettings.getProperty("AltPKDeathRate", 0.0D);

    ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY = otherSettings.getProperty("DeathPenaltyC5RateExpPenalty", 1);
    ALT_DEATH_PENALTY_C5_KARMA_PENALTY = otherSettings.getProperty("DeathPenaltyC5RateKarma", 1);

    ALT_TELE_TO_CATACOMBS = otherSettings.getProperty("TeleToCatacombs", false);

    FESTIVAL_MIN_PARTY_SIZE = otherSettings.getProperty("FestivalMinPartySize", 5);
    FESTIVAL_RATE_PRICE = otherSettings.getProperty("FestivalRatePrice", 1.0D);

    RIFT_MIN_PARTY_SIZE = otherSettings.getProperty("RiftMinPartySize", 5);
    RIFT_SPAWN_DELAY = otherSettings.getProperty("RiftSpawnDelay", 10000);
    RIFT_MAX_JUMPS = otherSettings.getProperty("MaxRiftJumps", 4);
    RIFT_AUTO_JUMPS_TIME = otherSettings.getProperty("AutoJumpsDelay", 8);
    RIFT_AUTO_JUMPS_TIME_RAND = otherSettings.getProperty("AutoJumpsDelayRandom", 120000);

    RIFT_ENTER_COST_RECRUIT = otherSettings.getProperty("RecruitFC", 18);
    RIFT_ENTER_COST_SOLDIER = otherSettings.getProperty("SoldierFC", 21);
    RIFT_ENTER_COST_OFFICER = otherSettings.getProperty("OfficerFC", 24);
    RIFT_ENTER_COST_CAPTAIN = otherSettings.getProperty("CaptainFC", 27);
    RIFT_ENTER_COST_COMMANDER = otherSettings.getProperty("CommanderFC", 30);
    RIFT_ENTER_COST_HERO = otherSettings.getProperty("HeroFC", 33);

    ALLOW_TALK_WHILE_SITTING = otherSettings.getProperty("AllowTalkWhileSitting", true);
    ALLOW_NOBLE_TP_TO_ALL = otherSettings.getProperty("AllowNobleTPToAll", false);

    ALT_SOCIAL_ACTION_REUSE = otherSettings.getProperty("AltSocialActionReuse", false);

    MAXLOAD_MODIFIER = otherSettings.getProperty("MaxLoadModifier", 1.0D);

    GATEKEEPER_MODIFIER = otherSettings.getProperty("GkCostMultiplier", 1.0D);

    ALT_IMPROVED_PETS_LIMITED_USE = otherSettings.getProperty("ImprovedPetsLimitedUse", false);

    ALT_PET_HEAL_BATTLE_ONLY = otherSettings.getProperty("PetsHealOnlyInBattle", true);

    ALT_PARTY_DISTRIBUTION_RANGE = otherSettings.getProperty("AltPartyDistributionRange", 1500);

    ALT_PARTY_BONUS = otherSettings.getProperty("AltPartyBonus", new double[] { 1.0D, 1.1D, 1.2D, 1.3D, 1.4D, 1.5D, 2.0D, 2.1D, 2.2D });

    ALT_USE_BOW_REUSE_MODIFIER = otherSettings.getProperty("AltUseBowReuseModifier", true);

    GATEKEEPER_FREE = otherSettings.getProperty("GkFree", 40);
    CRUMA_GATEKEEPER_LVL = otherSettings.getProperty("GkCruma", 56);

    ALT_VITALITY_ENABLED = otherSettings.getProperty("AltVitalityEnabled", true);
    ALT_VITALITY_RATE = otherSettings.getProperty("AltVitalityRate", 1.0D);
    ALT_VITALITY_CONSUME_RATE = otherSettings.getProperty("AltVitalityConsumeRate", 1.0D);
    ALT_VITALITY_RAID_BONUS = otherSettings.getProperty("AltVitalityRaidBonus", 2000);

    ALT_KAMALOKA_NIGHTMARES_PREMIUM_ONLY = otherSettings.getProperty("KamalokaNightmaresPremiumOnly", false);

    ALT_KAMALOKA_NIGHTMARE_REENTER = otherSettings.getProperty("SellReenterNightmaresTicket", true);
    ALT_KAMALOKA_ABYSS_REENTER = otherSettings.getProperty("SellReenterAbyssTicket", true);
    ALT_KAMALOKA_LAB_REENTER = otherSettings.getProperty("SellReenterLabyrinthTicket", true);

    ALT_PET_INVENTORY_LIMIT = otherSettings.getProperty("AltPetInventoryLimit", 12);

    FOLLOW_RANGE = otherSettings.getProperty("FollowRange", 100);

    ALT_SHOW_SERVER_TIME = otherSettings.getProperty("ShowServerTime", false);

    ALT_ITEM_AUCTION_ENABLED = otherSettings.getProperty("AltItemAuctionEnabled", true);
    ALT_ITEM_AUCTION_CAN_REBID = otherSettings.getProperty("AltItemAuctionCanRebid", false);
    ALT_ITEM_AUCTION_START_ANNOUNCE = otherSettings.getProperty("AltItemAuctionAnnounce", true);
    ALT_ITEM_AUCTION_BID_ITEM_ID = otherSettings.getProperty("AltItemAuctionBidItemId", 57);
    ALT_ITEM_AUCTION_MAX_BID = otherSettings.getProperty("AltItemAuctionMaxBid", 1000000L);
    ALT_ITEM_AUCTION_MAX_CANCEL_TIME_IN_MILLIS = otherSettings.getProperty("AltItemAuctionMaxCancelTimeInMillis", 604800000);

    ALT_FISH_CHAMPIONSHIP_ENABLED = otherSettings.getProperty("AltFishChampionshipEnabled", true);
    ALT_FISH_CHAMPIONSHIP_REWARD_ITEM = otherSettings.getProperty("AltFishChampionshipRewardItemId", 57);
    ALT_FISH_CHAMPIONSHIP_REWARD_1 = otherSettings.getProperty("AltFishChampionshipReward1", 800000);
    ALT_FISH_CHAMPIONSHIP_REWARD_2 = otherSettings.getProperty("AltFishChampionshipReward2", 500000);
    ALT_FISH_CHAMPIONSHIP_REWARD_3 = otherSettings.getProperty("AltFishChampionshipReward3", 300000);
    ALT_FISH_CHAMPIONSHIP_REWARD_4 = otherSettings.getProperty("AltFishChampionshipReward4", 200000);
    ALT_FISH_CHAMPIONSHIP_REWARD_5 = otherSettings.getProperty("AltFishChampionshipReward5", 100000);

    ALT_ENABLE_BLOCK_CHECKER_EVENT = otherSettings.getProperty("EnableBlockCheckerEvent", true);
    ALT_MIN_BLOCK_CHECKER_TEAM_MEMBERS = Math.min(Math.max(otherSettings.getProperty("BlockCheckerMinTeamMembers", 1), 1), 6);
    ALT_RATE_COINS_REWARD_BLOCK_CHECKER = otherSettings.getProperty("BlockCheckerRateCoinReward", 1.0D);

    ALT_HBCE_FAIR_PLAY = otherSettings.getProperty("HBCEFairPlay", false);

    PETITIONING_ALLOWED = otherSettings.getProperty("PetitioningAllowed", true);
    MAX_PETITIONS_PER_PLAYER = otherSettings.getProperty("MaxPetitionsPerPlayer", 5);
    MAX_PETITIONS_PENDING = otherSettings.getProperty("MaxPetitionsPending", 25);

    ALT_PCBANG_POINTS_ENABLED = otherSettings.getProperty("AltPcBangPointsEnabled", false);
    ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE = otherSettings.getProperty("AltPcBangPointsDoubleChance", 10.0D);
    ALT_PCBANG_POINTS_BONUS = otherSettings.getProperty("AltPcBangPointsBonus", 0);
    ALT_PCBANG_POINTS_DELAY = otherSettings.getProperty("AltPcBangPointsDelay", 20);
    ALT_PCBANG_POINTS_MIN_LVL = otherSettings.getProperty("AltPcBangPointsMinLvl", 1);

    ALT_DEBUG_ENABLED = otherSettings.getProperty("AltDebugEnabled", false);
    ALT_DEBUG_PVP_ENABLED = otherSettings.getProperty("AltDebugPvPEnabled", false);
    ALT_DEBUG_PVP_DUEL_ONLY = otherSettings.getProperty("AltDebugPvPDuelOnly", true);
    ALT_DEBUG_PVE_ENABLED = otherSettings.getProperty("AltDebugPvEEnabled", false);

    EX_NEW_PETITION_SYSTEM = otherSettings.getProperty("NewPetitionSystem", false);
    EX_JAPAN_MINIGAME = otherSettings.getProperty("JapanMinigame", false);
    EX_LECTURE_MARK = otherSettings.getProperty("LectureMark", false);

    SENDSTATUS_TRADE_JUST_OFFLINE = otherSettings.getProperty("SendStatusTradeJustOffline", false);
    SENDSTATUS_TRADE_MOD = otherSettings.getProperty("SendStatusTradeMod", 1.0D);

    MULTISELL_SIZE = otherSettings.getProperty("MultisellPageSize", 10);

    GAME_POINT_ITEM_ID = otherSettings.getProperty("GamePointItemId", -1);

    GLOBAL_SHOUT = otherSettings.getProperty("GlobalShout", false);
    GLOBAL_TRADE_CHAT = otherSettings.getProperty("GlobalTradeChat", false);
    CHAT_RANGE = otherSettings.getProperty("ChatRange", 1250);
    SHOUT_OFFSET = otherSettings.getProperty("ShoutOffset", 0);
    PREMIUM_HEROCHAT = otherSettings.getProperty("PremiumHeroChat", true);

    CHAT_MESSAGE_MAX_LEN = otherSettings.getProperty("ChatMessageLimit", 1000);

    LOG_CHAT = otherSettings.getProperty("LogChat", false);

    ABUSEWORD_BANCHAT = otherSettings.getProperty("ABUSEWORD_BANCHAT", false);
    int counter = 0;
    for (int id : otherSettings.getProperty("ABUSEWORD_BAN_CHANNEL", new int[] { 0 }))
    {
      BAN_CHANNEL_LIST[counter] = id;
      counter++;
    }
    ABUSEWORD_REPLACE = otherSettings.getProperty("ABUSEWORD_REPLACE", false);
    ABUSEWORD_REPLACE_STRING = otherSettings.getProperty("ABUSEWORD_REPLACE_STRING", "[censored]");
    BANCHAT_ANNOUNCE = otherSettings.getProperty("BANCHAT_ANNOUNCE", true);
    BANCHAT_ANNOUNCE_FOR_ALL_WORLD = otherSettings.getProperty("BANCHAT_ANNOUNCE_FOR_ALL_WORLD", true);
    BANCHAT_ANNOUNCE_NICK = otherSettings.getProperty("BANCHAT_ANNOUNCE_NICK", true);
    ABUSEWORD_BANTIME = otherSettings.getProperty("ABUSEWORD_UNBAN_TIMER", 30);

    CHATFILTER_MIN_LEVEL = otherSettings.getProperty("ChatFilterMinLevel", 0);
    counter = 0;
    for (int id : otherSettings.getProperty("ChatFilterChannels", new int[] { 1, 8 }))
    {
      CHATFILTER_CHANNELS[counter] = id;
      counter++;
    }
    CHATFILTER_WORK_TYPE = otherSettings.getProperty("ChatFilterWorkType", 1);

    HIDE_GM_STATUS = otherSettings.getProperty("HideGMStatus", false);
    SHOW_GM_LOGIN = otherSettings.getProperty("ShowGMLogin", true);
    SAVE_GM_EFFECTS = otherSettings.getProperty("SaveGMEffects", false);

    ALLOW_CURSED_WEAPONS = otherSettings.getProperty("AllowCursedWeapons", false);
    DROP_CURSED_WEAPONS_ON_KICK = otherSettings.getProperty("DropCursedWeaponsOnKick", false);

    ALT_ARENA_EXP = otherSettings.getProperty("ArenaExp", true);

    ENABLE_STARTING_ITEM = otherSettings.getProperty("EnableStartingItem", false);
    STARTING_ITEM_ID = otherSettings.getProperty("StartingItemId", new int[] { 1, 8 });
    STARTING_ITEM_COUNT = otherSettings.getProperty("StartingItemCount", new int[] { 1, 8 });

    STARTING_LEVEL = otherSettings.getProperty("StartingLevel", 1);
    STARTING_SP = otherSettings.getProperty("StartingSP", 0);

    ENABLE_PROF_SOCIAL_ACTION = otherSettings.getProperty("EnableProffSocialAction", true);
    PROF_SOCIAL_ACTION_ID = otherSettings.getProperty("ProffSocialActionID", 3);
  }

  public static void loadServerConfig()
  {
    ExProperties serverSettings = load("config/server.properties");

    GAMESERVER_HOSTNAME = serverSettings.getProperty("GameserverHostname");
    PORTS_GAME = serverSettings.getProperty("GameserverPort", new int[] { 7777 });

    EXTERNAL_HOSTNAME = serverSettings.getProperty("ExternalHostname", "*");
    INTERNAL_HOSTNAME = serverSettings.getProperty("InternalHostname", "*");

    GAME_SERVER_LOGIN_PORT = serverSettings.getProperty("LoginPort", 9013);
    GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHost", "127.0.0.1");
    GAME_SERVER_LOGIN_CRYPT = serverSettings.getProperty("LoginUseCrypt", true);

    REQUEST_ID = serverSettings.getProperty("RequestServerID", 0);
    ACCEPT_ALTERNATE_ID = serverSettings.getProperty("AcceptAlternateID", true);

    DATABASE_DRIVER = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
    DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
    DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
    DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
    DATABASE_MAX_CONNECTIONS = serverSettings.getProperty("MaximumDbConnections", 10);
    DATABASE_MAX_IDLE_TIMEOUT = serverSettings.getProperty("MaxIdleConnectionTimeout", 600);
    DATABASE_IDLE_TEST_PERIOD = serverSettings.getProperty("IdleConnectionTestPeriod", 60);

    AUTOSAVE = serverSettings.getProperty("Autosave", true);

    CNAME_TEMPLATE = serverSettings.getProperty("CnameTemplate", "[A-Za-z0-9\u0410-\u042F\u0430-\u044F]{2,16}");
    CLAN_NAME_TEMPLATE = serverSettings.getProperty("ClanNameTemplate", "[A-Za-z0-9\u0410-\u042F\u0430-\u044F]{3,16}");
    CLAN_TITLE_TEMPLATE = serverSettings.getProperty("ClanTitleTemplate", "[A-Za-z0-9\u0410-\u042F\u0430-\u044F \\p{Punct}]{1,16}");
    ALLY_NAME_TEMPLATE = serverSettings.getProperty("AllyNameTemplate", "[A-Za-z0-9\u0410-\u042F\u0430-\u044F]{3,16}");

    for (String a : serverSettings.getProperty("ServerType", ArrayUtils.EMPTY_STRING_ARRAY))
    {
      if (a.trim().isEmpty()) {
        continue;
      }
      ServerType t = ServerType.valueOf(a.toUpperCase());
      LOGIN_SERVER_SERVER_TYPE |= t.getMask();
    }
    LOGIN_SERVER_AGE_LIMIT = serverSettings.getProperty("ServerAgeLimit", 0);
    LOGIN_SERVER_GM_ONLY = serverSettings.getProperty("ServerGMOnly", false);
    LOGIN_SERVER_BRACKETS = serverSettings.getProperty("ServerBrackets", false);
    LOGIN_SERVER_IS_PVP = serverSettings.getProperty("PvPServer", false);

    MIN_PROTOCOL_REVISION = serverSettings.getProperty("MinProtocolRevision", 267);
    MAX_PROTOCOL_REVISION = serverSettings.getProperty("MaxProtocolRevision", 271);

    SCHEDULED_THREAD_POOL_SIZE = serverSettings.getProperty("ScheduledThreadPoolSize", NCPUS * 4);
    EXECUTOR_THREAD_POOL_SIZE = serverSettings.getProperty("ExecutorThreadPoolSize", NCPUS * 2);

    ENABLE_RUNNABLE_STATS = serverSettings.getProperty("EnableRunnableStats", false);

    SELECTOR_CONFIG.SLEEP_TIME = serverSettings.getProperty("SelectorSleepTime", 10L);
    SELECTOR_CONFIG.INTEREST_DELAY = serverSettings.getProperty("InterestDelay", 30L);
    SELECTOR_CONFIG.MAX_SEND_PER_PASS = serverSettings.getProperty("MaxSendPerPass", 32);
    SELECTOR_CONFIG.READ_BUFFER_SIZE = serverSettings.getProperty("ReadBufferSize", 65536);
    SELECTOR_CONFIG.WRITE_BUFFER_SIZE = serverSettings.getProperty("WriteBufferSize", 131072);
    SELECTOR_CONFIG.HELPER_BUFFER_COUNT = serverSettings.getProperty("BufferPoolSize", 64);
    EFFECT_TASK_MANAGER_COUNT = serverSettings.getProperty("EffectTaskManagers", 2);

    DEFAULT_LANG = serverSettings.getProperty("DefaultLang", "ru");
    DELETE_DAYS = serverSettings.getProperty("DeleteCharAfterDays", 7);
    try
    {
      DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();
    }
    catch (IOException e)
    {
      _log.error("", e);
    }

    RESTART_AT_TIME = serverSettings.getProperty("AutoRestartAt", "0 5 * * *");

    SHIFT_BY = serverSettings.getProperty("HShift", 12);
    SHIFT_BY_Z = serverSettings.getProperty("VShift", 11);
    MAP_MIN_Z = serverSettings.getProperty("MapMinZ", -32768);
    MAP_MAX_Z = serverSettings.getProperty("MapMaxZ", 32767);

    DAMAGE_FROM_FALLING = serverSettings.getProperty("DamageFromFalling", true);

    DONTLOADSPAWN = serverSettings.getProperty("StartWithoutSpawn", false);
    DONTLOADQUEST = serverSettings.getProperty("StartWithoutQuest", false);

    MAX_REFLECTIONS_COUNT = serverSettings.getProperty("MaxReflectionsCount", 300);

    PURGE_BYPASS_TASK_FREQUENCY = serverSettings.getProperty("PurgeTaskFrequency", 60);

    MOVE_PACKET_DELAY = serverSettings.getProperty("MovePacketDelay", 100);
    ATTACK_PACKET_DELAY = serverSettings.getProperty("AttackPacketDelay", 500);

    USER_INFO_INTERVAL = serverSettings.getProperty("UserInfoInterval", 100L);
    BROADCAST_STATS_INTERVAL = serverSettings.getProperty("BroadcastStatsInterval", true);
    BROADCAST_CHAR_INFO_INTERVAL = serverSettings.getProperty("BroadcastCharInfoInterval", 100L);

    MAXIMUM_ONLINE_USERS = serverSettings.getProperty("MaximumOnlineUsers", 3000);
    AUTODESTROY_ITEM_AFTER = serverSettings.getProperty("AutoDestroyDroppedItemAfter", 0);
    AUTODESTROY_PLAYER_ITEM_AFTER = serverSettings.getProperty("AutoDestroyPlayerDroppedItemAfter", 0);
    ALLOW_WAREHOUSE = serverSettings.getProperty("AllowWarehouse", true);
    ALLOW_MAIL = serverSettings.getProperty("AllowMail", true);
    WEAR_DELAY = serverSettings.getProperty("WearDelay", 5);
    ALLOW_DISCARDITEM = serverSettings.getProperty("AllowDiscardItem", true);
    ALLOW_WATER = serverSettings.getProperty("AllowWater", true);
    EVERYBODY_HAS_ADMIN_RIGHTS = serverSettings.getProperty("EverybodyHasAdminRights", false);
    HTM_CACHE_MODE = serverSettings.getProperty("HtmCacheMode", 1);
  }

  public static void loadCharConfig()
  {
    ExProperties charSettings = load("config/character.properties");

    ADD_SIEGE_PVP_COUNT = charSettings.getProperty("SiegePvpCount", false);
    ADD_ZONE_PVP_COUNT = charSettings.getProperty("ZonePvpCount", false);

    AUTO_LOOT = charSettings.getProperty("AutoLoot", false);
    AUTO_LOOT_HERBS = charSettings.getProperty("AutoLootHerbs", false);
    AUTO_LOOT_INDIVIDUAL = charSettings.getProperty("AutoLootIndividual", false);
    AUTO_LOOT_FROM_RAIDS = charSettings.getProperty("AutoLootFromRaids", false);
    AUTO_LOOT_PK = charSettings.getProperty("AutoLootPK", false);

    ALT_GAME_KARMA_PLAYER_CAN_SHOP = charSettings.getProperty("AltKarmaPlayerCanShop", false);

    ALT_GAME_DELEVEL = charSettings.getProperty("Delevel", true);

    ALT_DISABLE_SPELLBOOKS = charSettings.getProperty("AltDisableSpellbooks", false);
    AUTO_LEARN_SKILLS = charSettings.getProperty("AutoLearnSkills", false);
    AUTO_LEARN_FORGOTTEN_SKILLS = charSettings.getProperty("AutoLearnForgottenSkills", false);

    CHAR_TITLE = charSettings.getProperty("CharTitle", false);
    ADD_CHAR_TITLE = charSettings.getProperty("CharAddTitle", "");

    ALLOW_NPC_SHIFTCLICK = charSettings.getProperty("AllowShiftClick", true);
    ALT_GAME_SHOW_DROPLIST = charSettings.getProperty("AltShowDroplist", true);
    ALT_FULL_NPC_STATS_PAGE = charSettings.getProperty("AltFullStatsPage", false);

    ALT_GAME_SUBCLASS_WITHOUT_QUESTS = charSettings.getProperty("AltAllowSubClassWithoutQuest", false);
    ALT_ALLOW_SUBCLASS_WITHOUT_BAIUM = charSettings.getProperty("AltAllowSubClassWithoutBaium", true);
    ALT_GAME_LEVEL_TO_GET_SUBCLASS = charSettings.getProperty("AltLevelToGetSubclass", 75);
    ALT_GAME_SUB_ADD = charSettings.getProperty("AltSubAdd", 0);

    ALT_MAX_LEVEL = Math.min(charSettings.getProperty("AltMaxLevel", 85), Experience.LEVEL.length - 1);
    ALT_MAX_SUB_LEVEL = Math.min(charSettings.getProperty("AltMaxSubLevel", 80), Experience.LEVEL.length - 1);

    ALLOW_DEATH_PENALTY_C5 = charSettings.getProperty("EnableDeathPenaltyC5", true);
    ALT_DEATH_PENALTY_C5_CHANCE = charSettings.getProperty("DeathPenaltyC5Chance", 10);
    ALT_DEATH_PENALTY_C5_CHAOTIC_RECOVERY = charSettings.getProperty("ChaoticCanUseScrollOfRecovery", false);

    NONOWNER_ITEM_PICKUP_DELAY = charSettings.getProperty("NonOwnerItemPickupDelay", 15L) * 1000L;

    PARTY_LEADER_ONLY_CAN_INVITE = charSettings.getProperty("PartyLeaderOnlyCanInvite", true);

    LIM_PATK = charSettings.getProperty("LimitPatk", 20000);
    LIM_MATK = charSettings.getProperty("LimitMAtk", 25000);
    LIM_PDEF = charSettings.getProperty("LimitPDef", 15000);
    LIM_MDEF = charSettings.getProperty("LimitMDef", 15000);
    LIM_PATK_SPD = charSettings.getProperty("LimitPatkSpd", 1500);
    LIM_MATK_SPD = charSettings.getProperty("LimitMatkSpd", 1999);
    LIM_CRIT_DAM = charSettings.getProperty("LimitCriticalDamage", 2000);
    LIM_CRIT = charSettings.getProperty("LimitCritical", 500);
    LIM_MCRIT = charSettings.getProperty("LimitMCritical", 20);
    LIM_ACCURACY = charSettings.getProperty("LimitAccuracy", 200);
    LIM_EVASION = charSettings.getProperty("LimitEvasion", 200);
    LIM_MOVE = charSettings.getProperty("LimitMove", 250);
    LIM_FAME = charSettings.getProperty("LimitFame", 50000);

    ALT_POLE_DAMAGE_MODIFIER = charSettings.getProperty("PoleDamageModifier", 1.0D);

    STARTING_ADENA = charSettings.getProperty("StartingAdena", 0);

    SWIMING_SPEED = charSettings.getProperty("SwimingSpeedTemplate", 50);

    INVENTORY_MAXIMUM_NO_DWARF = charSettings.getProperty("MaximumSlotsForNoDwarf", 80);
    INVENTORY_MAXIMUM_DWARF = charSettings.getProperty("MaximumSlotsForDwarf", 100);
    INVENTORY_MAXIMUM_GM = charSettings.getProperty("MaximumSlotsForGMPlayer", 250);
    QUEST_INVENTORY_MAXIMUM = charSettings.getProperty("MaximumSlotsForQuests", 100);

    WAREHOUSE_SLOTS_NO_DWARF = charSettings.getProperty("BaseWarehouseSlotsForNoDwarf", 100);
    WAREHOUSE_SLOTS_DWARF = charSettings.getProperty("BaseWarehouseSlotsForDwarf", 120);
    WAREHOUSE_SLOTS_CLAN = charSettings.getProperty("MaximumWarehouseSlotsForClan", 200);
    FREIGHT_SLOTS = charSettings.getProperty("MaximumFreightSlots", 10);

    REGEN_SIT_WAIT = charSettings.getProperty("RegenSitWait", false);

    RESPAWN_RESTORE_CP = charSettings.getProperty("RespawnRestoreCP", 0.0D) / 100.0D;
    RESPAWN_RESTORE_HP = charSettings.getProperty("RespawnRestoreHP", 65.0D) / 100.0D;
    RESPAWN_RESTORE_MP = charSettings.getProperty("RespawnRestoreMP", 0.0D) / 100.0D;

    MAX_PVTSTORE_SLOTS_DWARF = charSettings.getProperty("MaxPvtStoreSlotsDwarf", 5);
    MAX_PVTSTORE_SLOTS_OTHER = charSettings.getProperty("MaxPvtStoreSlotsOther", 4);
    MAX_PVTCRAFT_SLOTS = charSettings.getProperty("MaxPvtManufactureSlots", 20);

    GM_NAME_COLOUR = Integer.decode("0x" + charSettings.getProperty("GMNameColour", "FFFFFF")).intValue();
    GM_HERO_AURA = charSettings.getProperty("GMHeroAura", false);
    NORMAL_NAME_COLOUR = Integer.decode("0x" + charSettings.getProperty("NormalNameColour", "FFFFFF")).intValue();
    CLANLEADER_NAME_COLOUR = Integer.decode("0x" + charSettings.getProperty("ClanleaderNameColour", "FFFFFF")).intValue();

    KARMA_MIN_KARMA = charSettings.getProperty("MinKarma", 240);
    KARMA_SP_DIVIDER = charSettings.getProperty("SPDivider", 7);
    KARMA_LOST_BASE = charSettings.getProperty("BaseKarmaLost", 0);

    PVP_TIME = charSettings.getProperty("PvPTime", 40000);

    HONOR_SYSTEM_ENABLE = charSettings.getProperty("HonorSystem", false);

    HONOR_SYSTEM_WON_ITEM_ID = charSettings.getProperty("HonorSystemWonItemId", 4674);
    HONOR_SYSTEM_WON_ITEM_COUNT = charSettings.getProperty("HonorSystemWonItemCount", 3);
    HONOR_SYSTEM_LOSE_ITEM_ID = charSettings.getProperty("HonorSystemLoseItemId", 4674);
    HONOR_SYSTEM_LOSE_ITEM_COUNT = charSettings.getProperty("HonorSystemLoseItemCount", 1);
    HONOR_SYSTEM_PVP_ITEM_ID = charSettings.getProperty("HonorSystemPvPItemId", 775);
    HONOR_SYSTEM__PVP_ITEM_COUNT = charSettings.getProperty("HonorSystemPvPItemCount", 3);
    HONOR_SYSTEM__IN_PVP_ZONE = charSettings.getProperty("HonorSystemInPvPZone", false);

    DISABLE_ENCHANT_BOOKS = charSettings.getProperty("DisableEnchantBooks", false);
    DISABLE_ENCHANT_BOOKS_ALL = charSettings.getProperty("DisableEnchantBooksAll", false);
  }

  public static void loadResidenceConfig()
  {
    ExProperties residenceSettings = load("config/residence.properties");

    ALLOW_CH_DOOR_OPEN_ON_CLICK = residenceSettings.getProperty("AllowChDoorOpenOnClick", true);
    ALT_CH_ALL_BUFFS = residenceSettings.getProperty("AltChAllBuffs", false);
    ALT_CH_ALLOW_1H_BUFFS = residenceSettings.getProperty("AltChAllowHourBuff", false);
    ALT_CH_SIMPLE_DIALOG = residenceSettings.getProperty("AltChSimpleDialog", false);

    CH_BID_GRADE1_MINCLANLEVEL = residenceSettings.getProperty("ClanHallBid_Grade1_MinClanLevel", 2);
    CH_BID_GRADE1_MINCLANMEMBERS = residenceSettings.getProperty("ClanHallBid_Grade1_MinClanMembers", 1);
    CH_BID_GRADE1_MINCLANMEMBERSLEVEL = residenceSettings.getProperty("ClanHallBid_Grade1_MinClanMembersAvgLevel", 1);
    CH_BID_GRADE2_MINCLANLEVEL = residenceSettings.getProperty("ClanHallBid_Grade2_MinClanLevel", 2);
    CH_BID_GRADE2_MINCLANMEMBERS = residenceSettings.getProperty("ClanHallBid_Grade2_MinClanMembers", 1);
    CH_BID_GRADE2_MINCLANMEMBERSLEVEL = residenceSettings.getProperty("ClanHallBid_Grade2_MinClanMembersAvgLevel", 1);
    CH_BID_GRADE3_MINCLANLEVEL = residenceSettings.getProperty("ClanHallBid_Grade3_MinClanLevel", 2);
    CH_BID_GRADE3_MINCLANMEMBERS = residenceSettings.getProperty("ClanHallBid_Grade3_MinClanMembers", 1);
    CH_BID_GRADE3_MINCLANMEMBERSLEVEL = residenceSettings.getProperty("ClanHallBid_Grade3_MinClanMembersAvgLevel", 1);
    RESIDENCE_LEASE_FUNC_MULTIPLIER = residenceSettings.getProperty("ResidenceLeaseFuncMultiplier", 1.0D);
    RESIDENCE_LEASE_MULTIPLIER = residenceSettings.getProperty("ResidenceLeaseMultiplier", 1.0D);
  }

  public static void loadRatesConfig()
  {
    ExProperties rateSettings = load("config/rates.properties");
    RATE_XP = rateSettings.getProperty("RateXp", 1.0D);
    RATE_SP = rateSettings.getProperty("RateSp", 1.0D);
    RATE_QUESTS_REWARD = rateSettings.getProperty("RateQuestsReward", 1.0D);
    RATE_QUESTS_DROP = rateSettings.getProperty("RateQuestsDrop", 1.0D);
    RATE_CLAN_REP_SCORE = rateSettings.getProperty("RateClanRepScore", 1.0D);
    RATE_CLAN_REP_SCORE_MAX_AFFECTED = rateSettings.getProperty("RateClanRepScoreMaxAffected", 2);
    RATE_DROP_ADENA = rateSettings.getProperty("RateDropAdena", 1.0D);
    RATE_DROP_ITEMS = rateSettings.getProperty("RateDropItems", 1.0D);
    RATE_DROP_COMMON_ITEMS = rateSettings.getProperty("RateDropCommonItems", 1.0D);
    RATE_DROP_RAIDBOSS = rateSettings.getProperty("RateRaidBoss", 1.0D);
    RATE_DROP_SPOIL = rateSettings.getProperty("RateDropSpoil", 1.0D);
    NO_RATE_ITEMS = rateSettings.getProperty("NoRateItemIds", new int[] { 6660, 6662, 6661, 6659, 6656, 6658, 8191, 6657, 10170, 10314, 16025, 16026 });

    NO_RATE_EQUIPMENT = rateSettings.getProperty("NoRateEquipment", true);
    NO_RATE_KEY_MATERIAL = rateSettings.getProperty("NoRateKeyMaterial", true);
    NO_RATE_RECIPES = rateSettings.getProperty("NoRateRecipes", true);
    RATE_DROP_SIEGE_GUARD = rateSettings.getProperty("RateSiegeGuard", 1.0D);
    RATE_MANOR = rateSettings.getProperty("RateManor", 1.0D);
    RATE_FISH_DROP_COUNT = rateSettings.getProperty("RateFishDropCount", 1.0D);
    RATE_PARTY_MIN = rateSettings.getProperty("RatePartyMin", false);
    RATE_HELLBOUND_CONFIDENCE = rateSettings.getProperty("RateHellboundConfidence", 1.0D);

    RATE_MOB_SPAWN = rateSettings.getProperty("RateMobSpawn", 1);
    RATE_MOB_SPAWN_MIN_LEVEL = rateSettings.getProperty("RateMobMinLevel", 1);
    RATE_MOB_SPAWN_MAX_LEVEL = rateSettings.getProperty("RateMobMaxLevel", 100);
  }

  public static void loadEventsConfig()
  {
    ExProperties eventSettings = load("config/events.properties");

    EVENT_CofferOfShadowsPriceRate = eventSettings.getProperty("CofferOfShadowsPriceRate", 1.0D);
    EVENT_CofferOfShadowsRewardRate = eventSettings.getProperty("CofferOfShadowsRewardRate", 1.0D);

    EVENT_LastHeroItemID = eventSettings.getProperty("LastHero_bonus_id", 57);
    EVENT_LastHeroItemCOUNT = eventSettings.getProperty("LastHero_bonus_count", 5000.0D);
    EVENT_LastHeroTime = eventSettings.getProperty("LastHero_time", 3);
    EVENT_LastHeroRate = eventSettings.getProperty("LastHero_rate", true);
    EVENT_LastHeroChanceToStart = eventSettings.getProperty("LastHero_ChanceToStart", 5);
    EVENT_LastHeroItemCOUNTFinal = eventSettings.getProperty("LastHero_bonus_count_final", 10000.0D);
    EVENT_LastHeroRateFinal = eventSettings.getProperty("LastHero_rate_final", true);

    EVENT_TvTItemID = eventSettings.getProperty("TvT_bonus_id", 57);
    EVENT_TvTItemCOUNT = eventSettings.getProperty("TvT_bonus_count", 5000.0D);
    EVENT_TvTTime = eventSettings.getProperty("TvT_time", 3);
    EVENT_TvT_rate = eventSettings.getProperty("TvT_rate", true);
    EVENT_TvTChanceToStart = eventSettings.getProperty("TvT_ChanceToStart", 5);

    EVENT_CtFItemID = eventSettings.getProperty("CtF_bonus_id", 57);
    EVENT_CtFItemCOUNT = eventSettings.getProperty("CtF_bonus_count", 5000.0D);
    EVENT_CtFTime = eventSettings.getProperty("CtF_time", 3);
    EVENT_CtF_rate = eventSettings.getProperty("CtF_rate", true);
    EVENT_CtFChanceToStart = eventSettings.getProperty("CtF_ChanceToStart", 5);

    EVENT_TFH_POLLEN_CHANCE = eventSettings.getProperty("TFH_POLLEN_CHANCE", 5.0D);

    EVENT_GLITTMEDAL_NORMAL_CHANCE = eventSettings.getProperty("MEDAL_CHANCE", 10.0D);
    EVENT_GLITTMEDAL_GLIT_CHANCE = eventSettings.getProperty("GLITTMEDAL_CHANCE", 0.1D);

    EVENT_L2DAY_LETTER_CHANCE = eventSettings.getProperty("L2DAY_LETTER_CHANCE", 1.0D);
    EVENT_CHANGE_OF_HEART_CHANCE = eventSettings.getProperty("EVENT_CHANGE_OF_HEART_CHANCE", 5.0D);

    EVENT_APIL_FOOLS_DROP_CHANCE = eventSettings.getProperty("AprilFollsDropChance", 50.0D);

    EVENT_BOUNTY_HUNTERS_ENABLED = eventSettings.getProperty("BountyHuntersEnabled", true);

    EVENT_SAVING_SNOWMAN_LOTERY_PRICE = eventSettings.getProperty("SavingSnowmanLoteryPrice", 50000);
    EVENT_SAVING_SNOWMAN_REWARDER_CHANCE = eventSettings.getProperty("SavingSnowmanRewarderChance", 2);

    EVENT_TRICK_OF_TRANS_CHANCE = eventSettings.getProperty("TRICK_OF_TRANS_CHANCE", 10.0D);

    EVENT_MARCH8_DROP_CHANCE = eventSettings.getProperty("March8DropChance", 10.0D);
    EVENT_MARCH8_PRICE_RATE = eventSettings.getProperty("March8PriceRate", 1.0D);

    ENCHANT_CHANCE_MASTER_YOGI_STAFF = eventSettings.getProperty("MasterYogiEnchantChance", 66);
    ENCHANT_MAX_MASTER_YOGI_STAFF = eventSettings.getProperty("MasterYogiEnchantMaxWeapon", 28);
    SAFE_ENCHANT_MASTER_YOGI_STAFF = eventSettings.getProperty("MasterYogiSafeEnchant", 3);

    TMEVENTINTERVAL = eventSettings.getProperty("TMEventInterval", 0);
    TMTIME1 = eventSettings.getProperty("TMTime1", 120000);
    TMWAVE1COUNT = eventSettings.getProperty("TMWave1Count", 2);
    TMWAVE2 = eventSettings.getProperty("TMWave2", 18855);

    ALLOW_WEDDING = eventSettings.getProperty("AllowWedding", false);
    WEDDING_PRICE = eventSettings.getProperty("WeddingPrice", 500000);
    WEDDING_PUNISH_INFIDELITY = eventSettings.getProperty("WeddingPunishInfidelity", true);
    WEDDING_TELEPORT = eventSettings.getProperty("WeddingTeleport", true);
    WEDDING_TELEPORT_PRICE = eventSettings.getProperty("WeddingTeleportPrice", 500000);
    WEDDING_TELEPORT_INTERVAL = eventSettings.getProperty("WeddingTeleportInterval", 120);
    WEDDING_SAMESEX = eventSettings.getProperty("WeddingAllowSameSex", true);
    WEDDING_FORMALWEAR = eventSettings.getProperty("WeddingFormalWear", true);
    WEDDING_DIVORCE_COSTS = eventSettings.getProperty("WeddingDivorceCosts", 20);
  }

  public static void loadServicesConfig()
  {
    ExProperties servicesSettings = load("config/services.properties");

    for (int id : servicesSettings.getProperty("AllowClassMasters", ArrayUtils.EMPTY_INT_ARRAY)) {
      if (id != 0)
        ALLOW_CLASS_MASTERS_LIST.add(Integer.valueOf(id));
    }
    CLASS_MASTERS_PRICE = servicesSettings.getProperty("ClassMastersPrice", "0,0,0");
    if (CLASS_MASTERS_PRICE.length() >= 5)
    {
      int level = 1;
      for (String id : CLASS_MASTERS_PRICE.split(","))
      {
        CLASS_MASTERS_PRICE_LIST[level] = Integer.parseInt(id);
        level++;
      }
    }
    CLASS_MASTERS_PRICE_ITEM = servicesSettings.getProperty("ClassMastersPriceItem", 57);

    SERVICES_CHANGE_NICK_ENABLED = servicesSettings.getProperty("NickChangeEnabled", false);
    SERVICES_CHANGE_NICK_PRICE = servicesSettings.getProperty("NickChangePrice", 100);
    SERVICES_CHANGE_NICK_ITEM = servicesSettings.getProperty("NickChangeItem", 4037);

    SERVICES_CHANGE_CLAN_NAME_ENABLED = servicesSettings.getProperty("ClanNameChangeEnabled", false);
    SERVICES_CHANGE_CLAN_NAME_PRICE = servicesSettings.getProperty("ClanNameChangePrice", 100);
    SERVICES_CHANGE_CLAN_NAME_ITEM = servicesSettings.getProperty("ClanNameChangeItem", 4037);

    SERVICES_CHANGE_PET_NAME_ENABLED = servicesSettings.getProperty("PetNameChangeEnabled", false);
    SERVICES_CHANGE_PET_NAME_PRICE = servicesSettings.getProperty("PetNameChangePrice", 100);
    SERVICES_CHANGE_PET_NAME_ITEM = servicesSettings.getProperty("PetNameChangeItem", 4037);

    SERVICES_EXCHANGE_BABY_PET_ENABLED = servicesSettings.getProperty("BabyPetExchangeEnabled", false);
    SERVICES_EXCHANGE_BABY_PET_PRICE = servicesSettings.getProperty("BabyPetExchangePrice", 100);
    SERVICES_EXCHANGE_BABY_PET_ITEM = servicesSettings.getProperty("BabyPetExchangeItem", 4037);

    SERVICES_CHANGE_SEX_ENABLED = servicesSettings.getProperty("SexChangeEnabled", false);
    SERVICES_CHANGE_SEX_PRICE = servicesSettings.getProperty("SexChangePrice", 100);
    SERVICES_CHANGE_SEX_ITEM = servicesSettings.getProperty("SexChangeItem", 4037);

    SERVICES_CHANGE_BASE_ENABLED = servicesSettings.getProperty("BaseChangeEnabled", false);
    SERVICES_CHANGE_BASE_PRICE = servicesSettings.getProperty("BaseChangePrice", 100);
    SERVICES_CHANGE_BASE_ITEM = servicesSettings.getProperty("BaseChangeItem", 4037);

    SERVICES_SEPARATE_SUB_ENABLED = servicesSettings.getProperty("SeparateSubEnabled", false);
    SERVICES_SEPARATE_SUB_PRICE = servicesSettings.getProperty("SeparateSubPrice", 100);
    SERVICES_SEPARATE_SUB_ITEM = servicesSettings.getProperty("SeparateSubItem", 4037);

    SERVICES_CHANGE_NICK_COLOR_ENABLED = servicesSettings.getProperty("NickColorChangeEnabled", false);
    SERVICES_CHANGE_NICK_COLOR_PRICE = servicesSettings.getProperty("NickColorChangePrice", 100);
    SERVICES_CHANGE_NICK_COLOR_ITEM = servicesSettings.getProperty("NickColorChangeItem", 4037);
    SERVICES_CHANGE_NICK_COLOR_LIST = servicesSettings.getProperty("NickColorChangeList", new String[] { "00FF00" });

    SERVICES_BASH_ENABLED = servicesSettings.getProperty("BashEnabled", false);
    SERVICES_BASH_SKIP_DOWNLOAD = servicesSettings.getProperty("BashSkipDownload", false);
    SERVICES_BASH_RELOAD_TIME = servicesSettings.getProperty("BashReloadTime", 24);

    SERVICES_RATE_TYPE = servicesSettings.getProperty("RateBonusType", 0);
    SERVICES_RATE_BONUS_PRICE = servicesSettings.getProperty("RateBonusPrice", new int[] { 1500 });
    SERVICES_RATE_BONUS_ITEM = servicesSettings.getProperty("RateBonusItem", new int[] { 4037 });
    SERVICES_RATE_BONUS_VALUE = servicesSettings.getProperty("RateBonusValue", new double[] { 1.25D });
    SERVICES_RATE_BONUS_DAYS = servicesSettings.getProperty("RateBonusTime", new int[] { 30 });

    SERVICES_NOBLESS_SELL_ENABLED = servicesSettings.getProperty("NoblessSellEnabled", false);
    SERVICES_NOBLESS_SELL_PRICE = servicesSettings.getProperty("NoblessSellPrice", 1000);
    SERVICES_NOBLESS_SELL_ITEM = servicesSettings.getProperty("NoblessSellItem", 4037);

    SERVICES_EXPAND_INVENTORY_ENABLED = servicesSettings.getProperty("ExpandInventoryEnabled", false);
    SERVICES_EXPAND_INVENTORY_PRICE = servicesSettings.getProperty("ExpandInventoryPrice", 1000);
    SERVICES_EXPAND_INVENTORY_ITEM = servicesSettings.getProperty("ExpandInventoryItem", 4037);
    SERVICES_EXPAND_INVENTORY_MAX = servicesSettings.getProperty("ExpandInventoryMax", 250);

    SERVICES_EXPAND_WAREHOUSE_ENABLED = servicesSettings.getProperty("ExpandWarehouseEnabled", false);
    SERVICES_EXPAND_WAREHOUSE_PRICE = servicesSettings.getProperty("ExpandWarehousePrice", 1000);
    SERVICES_EXPAND_WAREHOUSE_ITEM = servicesSettings.getProperty("ExpandWarehouseItem", 4037);

    SERVICES_EXPAND_CWH_ENABLED = servicesSettings.getProperty("ExpandCWHEnabled", false);
    SERVICES_EXPAND_CWH_PRICE = servicesSettings.getProperty("ExpandCWHPrice", 1000);
    SERVICES_EXPAND_CWH_ITEM = servicesSettings.getProperty("ExpandCWHItem", 4037);

    SERVICES_SELLPETS = servicesSettings.getProperty("SellPets", "");

    SERVICES_OFFLINE_TRADE_ALLOW = servicesSettings.getProperty("AllowOfflineTrade", false);
    SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE = servicesSettings.getProperty("AllowOfflineTradeOnlyOffshore", true);
    SERVICES_OFFLINE_TRADE_MIN_LEVEL = servicesSettings.getProperty("OfflineMinLevel", 0);
    SERVICES_OFFLINE_TRADE_NAME_COLOR = Integer.decode("0x" + servicesSettings.getProperty("OfflineTradeNameColor", "B0FFFF")).intValue();
    SERVICES_OFFLINE_TRADE_PRICE_ITEM = servicesSettings.getProperty("OfflineTradePriceItem", 0);
    SERVICES_OFFLINE_TRADE_PRICE = servicesSettings.getProperty("OfflineTradePrice", 0);
    SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK = servicesSettings.getProperty("OfflineTradeDaysToKick", 14) * 86400L;
    SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART = servicesSettings.getProperty("OfflineRestoreAfterRestart", true);

    SERVICES_NO_TRADE_ONLY_OFFLINE = servicesSettings.getProperty("NoTradeOnlyOffline", false);
    SERVICES_TRADE_TAX = servicesSettings.getProperty("TradeTax", 0.0D);
    SERVICES_OFFSHORE_TRADE_TAX = servicesSettings.getProperty("OffshoreTradeTax", 0.0D);
    SERVICES_TRADE_TAX_ONLY_OFFLINE = servicesSettings.getProperty("TradeTaxOnlyOffline", false);
    SERVICES_OFFSHORE_NO_CASTLE_TAX = servicesSettings.getProperty("NoCastleTaxInOffshore", false);
    SERVICES_TRADE_ONLY_FAR = servicesSettings.getProperty("TradeOnlyFar", false);
    SERVICES_TRADE_MIN_LEVEL = servicesSettings.getProperty("MinLevelForTrade", 0);
    SERVICES_TRADE_RADIUS = servicesSettings.getProperty("TradeRadius", 30);

    SERVICES_GIRAN_HARBOR_ENABLED = servicesSettings.getProperty("GiranHarborZone", false);
    SERVICES_PARNASSUS_ENABLED = servicesSettings.getProperty("ParnassusZone", false);
    SERVICES_PARNASSUS_NOTAX = servicesSettings.getProperty("ParnassusNoTax", false);
    SERVICES_PARNASSUS_PRICE = servicesSettings.getProperty("ParnassusPrice", 500000);

    SERVICES_ALLOW_LOTTERY = servicesSettings.getProperty("AllowLottery", false);
    SERVICES_LOTTERY_PRIZE = servicesSettings.getProperty("LotteryPrize", 50000);
    SERVICES_ALT_LOTTERY_PRICE = servicesSettings.getProperty("AltLotteryPrice", 2000);
    SERVICES_LOTTERY_TICKET_PRICE = servicesSettings.getProperty("LotteryTicketPrice", 2000);
    SERVICES_LOTTERY_5_NUMBER_RATE = servicesSettings.getProperty("Lottery5NumberRate", 0.6D);
    SERVICES_LOTTERY_4_NUMBER_RATE = servicesSettings.getProperty("Lottery4NumberRate", 0.4D);
    SERVICES_LOTTERY_3_NUMBER_RATE = servicesSettings.getProperty("Lottery3NumberRate", 0.2D);
    SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE = servicesSettings.getProperty("Lottery2and1NumberPrize", 200);

    SERVICES_ALLOW_ROULETTE = servicesSettings.getProperty("AllowRoulette", false);
    SERVICES_ROULETTE_MIN_BET = servicesSettings.getProperty("RouletteMinBet", 1L);
    SERVICES_ROULETTE_MAX_BET = servicesSettings.getProperty("RouletteMaxBet", 9223372036854775807L);

    SERVICES_ENABLE_NO_CARRIER = servicesSettings.getProperty("EnableNoCarrier", false);
    SERVICES_NO_CARRIER_MIN_TIME = servicesSettings.getProperty("NoCarrierMinTime", 0);
    SERVICES_NO_CARRIER_MAX_TIME = servicesSettings.getProperty("NoCarrierMaxTime", 90);
    SERVICES_NO_CARRIER_DEFAULT_TIME = servicesSettings.getProperty("NoCarrierDefaultTime", 60);

    ITEM_BROKER_ITEM_SEARCH = servicesSettings.getProperty("UseItemBrokerItemSearch", false);

    ALLOW_EVENT_GATEKEEPER = servicesSettings.getProperty("AllowEventGatekeeper", false);

    ADD_ACTIVATE_SUB = servicesSettings.getProperty("ActivateSubService", false);
    ADD_ACTIVATE_SUB_ITEM = servicesSettings.getProperty("ActivateSubItem", 57);
    ADD_ACTIVATE_SUB_PRICE = servicesSettings.getProperty("ActivateSubItemCount", 10000000);

    LEVEL_UP_ENABLED = servicesSettings.getProperty("LevelUpEnabled", true);
    LEVEL_UP_PRICE = servicesSettings.getProperty("LevelUpPrice", 200000);
    LEVEL_UP_ITEM = servicesSettings.getProperty("LevelUpItem", 57);
    LEVEL_UP_MAX = servicesSettings.getProperty("LevelUpMax", 40);

    HERO_SELL_ENABLED = servicesSettings.getProperty("HeroSellEnabled", true);
    HERO_SELL_ITEM = servicesSettings.getProperty("HeroSellItem", 57);
    HERO_SELL_PRICE = servicesSettings.getProperty("HeroSellPrice", 100000);

    CB_CLASS_ENABLED = servicesSettings.getProperty("CBClassEnabled", true);
  }

  public static void loadSpoilConfig()
  {
    ExProperties spoilSettings = load("config/spoil.properties");

    BASE_SPOIL_RATE = spoilSettings.getProperty("BasePercentChanceOfSpoilSuccess", 78.0D);
    MINIMUM_SPOIL_RATE = spoilSettings.getProperty("MinimumPercentChanceOfSpoilSuccess", 1.0D);
    ALT_SPOIL_FORMULA = spoilSettings.getProperty("AltFormula", false);
    MANOR_SOWING_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfSowingSuccess", 100.0D);
    MANOR_SOWING_ALT_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfSowingAltSuccess", 10.0D);
    MANOR_HARVESTING_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfHarvestingSuccess", 90.0D);
    MANOR_DIFF_PLAYER_TARGET = spoilSettings.getProperty("MinDiffPlayerMob", 5);
    MANOR_DIFF_PLAYER_TARGET_PENALTY = spoilSettings.getProperty("DiffPlayerMobPenalty", 5.0D);
    MANOR_DIFF_SEED_TARGET = spoilSettings.getProperty("MinDiffSeedMob", 5);
    MANOR_DIFF_SEED_TARGET_PENALTY = spoilSettings.getProperty("DiffSeedMobPenalty", 5.0D);
    ALLOW_MANOR = spoilSettings.getProperty("AllowManor", true);
    MANOR_REFRESH_TIME = spoilSettings.getProperty("AltManorRefreshTime", 20);
    MANOR_REFRESH_MIN = spoilSettings.getProperty("AltManorRefreshMin", 0);
    MANOR_APPROVE_TIME = spoilSettings.getProperty("AltManorApproveTime", 6);
    MANOR_APPROVE_MIN = spoilSettings.getProperty("AltManorApproveMin", 0);
    MANOR_MAINTENANCE_PERIOD = spoilSettings.getProperty("AltManorMaintenancePeriod", 360000);
  }

  public static void loadGeodataConfig()
  {
    ExProperties geodataSettings = load("config/geodata.properties");

    GEO_X_FIRST = geodataSettings.getProperty("GeoFirstX", 11);
    GEO_Y_FIRST = geodataSettings.getProperty("GeoFirstY", 10);
    GEO_X_LAST = geodataSettings.getProperty("GeoLastX", 26);
    GEO_Y_LAST = geodataSettings.getProperty("GeoLastY", 26);

    GEOFILES_PATTERN = geodataSettings.getProperty("GeoFilesPattern", "(\\d{2}_\\d{2})\\.l2j");
    ALLOW_GEODATA = geodataSettings.getProperty("AllowGeodata", true);
    ALLOW_FALL_FROM_WALLS = geodataSettings.getProperty("AllowFallFromWalls", false);
    ALLOW_KEYBOARD_MOVE = geodataSettings.getProperty("AllowMoveWithKeyboard", true);
    COMPACT_GEO = geodataSettings.getProperty("CompactGeoData", false);
    CLIENT_Z_SHIFT = geodataSettings.getProperty("ClientZShift", 16);

    PATHFIND_BOOST = geodataSettings.getProperty("PathFindBoost", 2);
    PATHFIND_DIAGONAL = geodataSettings.getProperty("PathFindDiagonal", true);
    PATH_CLEAN = geodataSettings.getProperty("PathClean", true);
    PATHFIND_MAX_Z_DIFF = geodataSettings.getProperty("PathFindMaxZDiff", 32);

    MAX_Z_DIFF = geodataSettings.getProperty("MaxZDiff", 64);
    MIN_LAYER_HEIGHT = geodataSettings.getProperty("MinLayerHeight", 64);

    PATHFIND_MAX_TIME = geodataSettings.getProperty("PathFindMaxTime", 10000000);
    PATHFIND_BUFFERS = geodataSettings.getProperty("PathFindBuffers", "8x96;8x128;8x160;8x192;4x224;4x256;4x288;2x320;2x384;2x352;1x512");
  }

  public static void loadAIConfig()
  {
    ExProperties aiSettings = load("config/ai.properties");

    AI_TASK_MANAGER_COUNT = aiSettings.getProperty("AiTaskManagers", 1);
    AI_TASK_ATTACK_DELAY = aiSettings.getProperty("AiTaskDelay", 1000);
    AI_TASK_ACTIVE_DELAY = aiSettings.getProperty("AiTaskActiveDelay", 1000);
    BLOCK_ACTIVE_TASKS = aiSettings.getProperty("BlockActiveTasks", false);
    ALWAYS_TELEPORT_HOME = aiSettings.getProperty("AlwaysTeleportHome", false);

    RND_WALK = aiSettings.getProperty("RndWalk", true);
    RND_WALK_RATE = aiSettings.getProperty("RndWalkRate", 1);
    RND_ANIMATION_RATE = aiSettings.getProperty("RndAnimationRate", 2);

    AGGRO_CHECK_INTERVAL = aiSettings.getProperty("AggroCheckInterval", 250);
    NONAGGRO_TIME_ONTELEPORT = aiSettings.getProperty("NonAggroTimeOnTeleport", 15000);

    MAX_DRIFT_RANGE = aiSettings.getProperty("MaxDriftRange", 100);
    MAX_PURSUE_RANGE = aiSettings.getProperty("MaxPursueRange", 4000);
    MAX_PURSUE_UNDERGROUND_RANGE = aiSettings.getProperty("MaxPursueUndergoundRange", 2000);
    MAX_PURSUE_RANGE_RAID = aiSettings.getProperty("MaxPursueRangeRaid", 5000);
  }

  public static void loadTelnetConfig()
  {
    ExProperties telnetSettings = load("config/telnet.properties");

    IS_TELNET_ENABLED = telnetSettings.getProperty("EnableTelnet", false);
    TELNET_DEFAULT_ENCODING = telnetSettings.getProperty("TelnetEncoding", "UTF-8");
    TELNET_PORT = telnetSettings.getProperty("Port", 7000);
    TELNET_HOSTNAME = telnetSettings.getProperty("BindAddress", "127.0.0.1");
    TELNET_PASSWORD = telnetSettings.getProperty("Password", "");
  }

  public static void abuseLoad()
  {
    List tmp = new ArrayList();

    LineNumberReader lnr = null;
    try
    {
      lnr = new LineNumberReader(new InputStreamReader(new FileInputStream("config/abusewords.txt"), "UTF-8"));
      String line;
      while ((line = lnr.readLine()) != null)
      {
        StringTokenizer st = new StringTokenizer(line, "\n\r");
        if (st.hasMoreTokens()) {
          tmp.add(Pattern.compile(".*" + st.nextToken() + ".*", 98));
        }
      }
      ABUSEWORD_LIST = (Pattern[])tmp.toArray(new Pattern[tmp.size()]);
      tmp.clear();
      _log.info("Abuse: Loaded " + ABUSEWORD_LIST.length + " abuse words.");
    }
    catch (IOException e2)
    {
      _log.warn("Error reading abuse: " + e1);
    }
    finally
    {
      try
      {
        if (lnr != null)
          lnr.close();
      }
      catch (Exception e2)
      {
      }
    }
  }

  public static void loadGMAccess()
  {
    gmlist.clear();
    loadGMAccess(new File("config/GMAccess.xml"));
    File dir = new File("config/GMAccess.d/");
    if ((!dir.exists()) || (!dir.isDirectory()))
    {
      _log.info("Dir " + dir.getAbsolutePath() + " not exists.");
      return;
    }
    for (File f : dir.listFiles())
      if ((!f.isDirectory()) && (f.getName().endsWith(".xml")))
        loadGMAccess(f);
  }

  public static void loadGMAccess(File file)
  {
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);
      Document doc = factory.newDocumentBuilder().parse(file);

      for (Node z = doc.getFirstChild(); z != null; z = z.getNextSibling())
        for (Node n = z.getFirstChild(); n != null; n = n.getNextSibling())
        {
          if (!n.getNodeName().equalsIgnoreCase("char")) {
            continue;
          }
          PlayerAccess pa = new PlayerAccess();
          for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
          {
            Class cls = pa.getClass();
            String node = d.getNodeName();

            if (node.equalsIgnoreCase("#text")) continue;
            Field fld;
            try {
              fld = cls.getField(node);
            }
            catch (NoSuchFieldException e)
            {
              _log.info("Not found desclarate ACCESS name: " + node + " in XML Player access Object");
              continue;
            }

            if (fld.getType().getName().equalsIgnoreCase("boolean"))
              fld.setBoolean(pa, Boolean.parseBoolean(d.getAttributes().getNamedItem("set").getNodeValue()));
            else if (fld.getType().getName().equalsIgnoreCase("int"))
              fld.setInt(pa, Integer.valueOf(d.getAttributes().getNamedItem("set").getNodeValue()).intValue());
          }
          gmlist.put(Integer.valueOf(pa.PlayerID), pa);
        }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void load()
  {
    loadItemsConfig();
    loadOlympiadConfig();
    loadSkillsConfig();
    loadClanConfig();
    loadNpcConfig();
    loadQuestConfig();
    loadCommunityConfig();
    loadOtherConfig();
    loadServerConfig();
    loadCharConfig();
    loadResidenceConfig();
    loadRatesConfig();
    loadSiegesConfig();
    loadEventsConfig();
    loadServicesConfig();
    loadSpoilConfig();
    loadGeodataConfig();
    loadAIConfig();
    loadTelnetConfig();

    abuseLoad();
    loadGMAccess();
  }

  public static String getField(String fieldName)
  {
    Field field = FieldUtils.getField(Config.class, fieldName);

    if (field == null) {
      return null;
    }
    try
    {
      return String.valueOf(field.get(null));
    }
    catch (IllegalArgumentException e)
    {
    }
    catch (IllegalAccessException e)
    {
    }

    return null;
  }

  public static boolean setField(String fieldName, String value)
  {
    Field field = FieldUtils.getField(Config.class, fieldName);

    if (field == null) {
      return false;
    }
    try
    {
      if (field.getType() == Boolean.TYPE)
        field.setBoolean(null, BooleanUtils.toBoolean(value));
      else if (field.getType() == Integer.TYPE)
        field.setInt(null, NumberUtils.toInt(value));
      else if (field.getType() == Long.TYPE)
        field.setLong(null, NumberUtils.toLong(value));
      else if (field.getType() == Double.TYPE)
        field.setDouble(null, NumberUtils.toDouble(value));
      else if (field.getType() == String.class)
        field.set(null, value);
      else
        return false;
    }
    catch (IllegalArgumentException e)
    {
      return false;
    }
    catch (IllegalAccessException e)
    {
      return false;
    }

    return true;
  }

  public static ExProperties load(String filename)
  {
    return load(new File(filename));
  }

  public static ExProperties load(File file)
  {
    ExProperties result = new ExProperties();
    try
    {
      result.load(file);
    }
    catch (IOException e)
    {
      _log.error("Error loading config : " + file.getName() + "!");
    }

    return result;
  }

  public static boolean containsAbuseWord(String s)
  {
    for (Pattern pattern : ABUSEWORD_LIST)
      if (pattern.matcher(s).matches())
        return true;
    return false;
  }
}