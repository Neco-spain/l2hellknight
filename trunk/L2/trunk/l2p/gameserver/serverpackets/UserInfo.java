package l2p.gameserver.serverpackets;

import java.util.Collection;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.NpcHolder;
import l2p.gameserver.instancemanager.CursedWeaponsManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.base.Element;
import l2p.gameserver.model.base.Experience;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.base.Race;
import l2p.gameserver.model.base.TeamType;
import l2p.gameserver.model.entity.boat.Boat;
import l2p.gameserver.model.entity.events.GlobalEvent;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.matching.MatchingRoom;
import l2p.gameserver.model.pledge.Alliance;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.skills.effects.EffectCubic;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.Location;

public class UserInfo extends L2GameServerPacket
{
  private boolean can_writeImpl = false;
  private boolean partyRoom;
  private int _runSpd;
  private int _walkSpd;
  private int _swimRunSpd;
  private int _swimWalkSpd;
  private int _flRunSpd;
  private int _flWalkSpd;
  private int _flyRunSpd;
  private int _flyWalkSpd;
  private int _relation;
  private double move_speed;
  private double attack_speed;
  private double col_radius;
  private double col_height;
  private int[][] _inv;
  private Location _loc;
  private Location _fishLoc;
  private int obj_id;
  private int vehicle_obj_id;
  private int _race;
  private int sex;
  private int base_class;
  private int level;
  private int curCp;
  private int maxCp;
  private int _enchant;
  private int _weaponFlag;
  private long _exp;
  private int curHp;
  private int maxHp;
  private int curMp;
  private int maxMp;
  private int curLoad;
  private int maxLoad;
  private int rec_left;
  private int rec_have;
  private int _str;
  private int _con;
  private int _dex;
  private int _int;
  private int _wit;
  private int _men;
  private int _sp;
  private int ClanPrivs;
  private int InventoryLimit;
  private int _patk;
  private int _patkspd;
  private int _pdef;
  private int evasion;
  private int accuracy;
  private int crit;
  private int _matk;
  private int _matkspd;
  private int _mdef;
  private int pvp_flag;
  private int karma;
  private int hair_style;
  private int hair_color;
  private int face;
  private int gm_commands;
  private int fame;
  private int vitality;
  private int clan_id;
  private int clan_crest_id;
  private int ally_id;
  private int ally_crest_id;
  private int large_clan_crest_id;
  private int private_store;
  private int can_crystalize;
  private int pk_kills;
  private int pvp_kills;
  private int class_id;
  private int agathion;
  private int _abnormalEffect;
  private int _abnormalEffect2;
  private int noble;
  private int hero;
  private int mount_id;
  private int cw_level;
  private int name_color;
  private int running;
  private int pledge_class;
  private int pledge_type;
  private int title_color;
  private int transformation;
  private int defenceFire;
  private int defenceWater;
  private int defenceWind;
  private int defenceEarth;
  private int defenceHoly;
  private int defenceUnholy;
  private int mount_type;
  private String _name;
  private String title;
  private EffectCubic[] cubics;
  private Element attackElement;
  private int attackElementValue;
  private boolean isFlying;
  private boolean _allowMap;
  private int talismans;
  private boolean openCloak;
  private double _expPercent;
  private TeamType _team;

  public UserInfo(Player player)
  {
    if (player.getTransformationName() != null)
    {
      _name = player.getTransformationName();
      title = "";
      clan_crest_id = 0;
      ally_crest_id = 0;
      large_clan_crest_id = 0;
      cw_level = CursedWeaponsManager.getInstance().getLevel(player.getCursedWeaponEquippedId());
    }
    else
    {
      _name = player.getName();

      Clan clan = player.getClan();
      Alliance alliance = clan == null ? null : clan.getAlliance();

      clan_id = (clan == null ? 0 : clan.getClanId());
      clan_crest_id = (clan == null ? 0 : clan.getCrestId());
      large_clan_crest_id = (clan == null ? 0 : clan.getCrestLargeId());

      ally_id = (alliance == null ? 0 : alliance.getAllyId());
      ally_crest_id = (alliance == null ? 0 : alliance.getAllyCrestId());

      cw_level = 0;
      title = player.getTitle();
    }

    if ((player.getPlayerAccess().GodMode) && (player.isInvisible()))
      title += "[I]";
    if (player.isPolymorphed()) {
      if (NpcHolder.getInstance().getTemplate(player.getPolyId()) != null)
        title = (title + " - " + NpcHolder.getInstance().getTemplate(player.getPolyId()).name);
      else
        title += " - Polymorphed";
    }
    if (player.isMounted())
    {
      _enchant = 0;
      mount_id = (player.getMountNpcId() + 1000000);
      mount_type = player.getMountType();
    }
    else
    {
      _enchant = player.getEnchantEffect();
      mount_id = 0;
      mount_type = 0;
    }

    _weaponFlag = (player.getActiveWeaponInstance() == null ? 20 : 40);

    move_speed = player.getMovementSpeedMultiplier();
    _runSpd = (int)(player.getRunSpeed() / move_speed);
    _walkSpd = (int)(player.getWalkSpeed() / move_speed);

    _flRunSpd = 0;
    _flWalkSpd = 0;

    if (player.isFlying())
    {
      _flyRunSpd = _runSpd;
      _flyWalkSpd = _walkSpd;
    }
    else
    {
      _flyRunSpd = 0;
      _flyWalkSpd = 0;
    }

    _swimRunSpd = player.getSwimSpeed();
    _swimWalkSpd = player.getSwimSpeed();

    _inv = new int[26][3];
    for (int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
    {
      _inv[PAPERDOLL_ID][0] = player.getInventory().getPaperdollObjectId(PAPERDOLL_ID);
      _inv[PAPERDOLL_ID][1] = player.getInventory().getPaperdollItemId(PAPERDOLL_ID);
      _inv[PAPERDOLL_ID][2] = player.getInventory().getPaperdollAugmentationId(PAPERDOLL_ID);
    }

    _relation = (player.isClanLeader() ? 64 : 0);
    for (GlobalEvent e : player.getEvents()) {
      _relation = e.getUserRelation(player, _relation);
    }
    _loc = player.getLoc();
    obj_id = player.getObjectId();
    vehicle_obj_id = (player.isInBoat() ? player.getBoat().getObjectId() : 0);
    _race = player.getRace().ordinal();
    sex = player.getSex();
    base_class = player.getBaseClassId();
    level = player.getLevel();
    _exp = player.getExp();
    _expPercent = Experience.getExpPercent(player.getLevel(), player.getExp());
    _str = player.getSTR();
    _dex = player.getDEX();
    _con = player.getCON();
    _int = player.getINT();
    _wit = player.getWIT();
    _men = player.getMEN();
    curHp = (int)player.getCurrentHp();
    maxHp = player.getMaxHp();
    curMp = (int)player.getCurrentMp();
    maxMp = player.getMaxMp();
    curLoad = player.getCurrentLoad();
    maxLoad = player.getMaxLoad();
    _sp = player.getIntSp();
    _patk = player.getPAtk(null);
    _patkspd = player.getPAtkSpd();
    _pdef = player.getPDef(null);
    evasion = player.getEvasionRate(null);
    accuracy = player.getAccuracy();
    crit = player.getCriticalHit(null, null);
    _matk = player.getMAtk(null, null);
    _matkspd = player.getMAtkSpd();
    _mdef = player.getMDef(null, null);
    pvp_flag = player.getPvpFlag();
    karma = player.getKarma();
    attack_speed = player.getAttackSpeedMultiplier();
    col_radius = player.getColRadius();
    col_height = player.getColHeight();
    hair_style = player.getHairStyle();
    hair_color = player.getHairColor();
    face = player.getFace();
    gm_commands = ((player.isGM()) || (player.getPlayerAccess().CanUseGMCommand) ? 1 : 0);

    clan_id = player.getClanId();
    ally_id = player.getAllyId();
    private_store = player.getPrivateStoreType();
    can_crystalize = (player.getSkillLevel(Integer.valueOf(248)) > 0 ? 1 : 0);
    pk_kills = player.getPkKills();
    pvp_kills = player.getPvpKills();
    cubics = ((EffectCubic[])player.getCubics().toArray(new EffectCubic[player.getCubics().size()]));
    _abnormalEffect = player.getAbnormalEffect();
    _abnormalEffect2 = player.getAbnormalEffect2();
    ClanPrivs = player.getClanPrivileges();
    rec_left = player.getRecomLeft();
    rec_have = player.getRecomHave();
    InventoryLimit = player.getInventoryLimit();
    class_id = player.getClassId().getId();
    maxCp = player.getMaxCp();
    curCp = (int)player.getCurrentCp();
    _team = player.getTeam();
    noble = ((player.isNoble()) || ((player.isGM()) && (Config.GM_HERO_AURA)) ? 1 : 0);
    hero = ((player.isHero()) || ((player.isGM()) && (Config.GM_HERO_AURA)) ? 1 : 0);

    _fishLoc = player.getFishLoc();
    name_color = player.getNameColor();
    running = (player.isRunning() ? 1 : 0);
    pledge_class = player.getPledgeClass();
    pledge_type = player.getPledgeType();
    title_color = player.getTitleColor();
    transformation = player.getTransformation();
    attackElement = player.getAttackElement();
    attackElementValue = player.getAttack(attackElement);
    defenceFire = player.getDefence(Element.FIRE);
    defenceWater = player.getDefence(Element.WATER);
    defenceWind = player.getDefence(Element.WIND);
    defenceEarth = player.getDefence(Element.EARTH);
    defenceHoly = player.getDefence(Element.HOLY);
    defenceUnholy = player.getDefence(Element.UNHOLY);
    agathion = player.getAgathionId();
    fame = player.getFame();
    vitality = (int)player.getVitality();
    partyRoom = ((player.getMatchingRoom() != null) && (player.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING) && (player.getMatchingRoom().getLeader() == player));
    isFlying = player.isInFlyingTransform();
    talismans = player.getTalismanCount();
    openCloak = player.getOpenCloak();
    _allowMap = player.isActionBlocked("open_minimap");

    can_writeImpl = true;
  }

  protected final void writeImpl()
  {
    if (!can_writeImpl) {
      return;
    }
    writeC(50);

    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z + Config.CLIENT_Z_SHIFT);
    writeD(vehicle_obj_id);
    writeD(obj_id);
    writeS(_name);
    writeD(_race);
    writeD(sex);
    writeD(base_class);
    writeD(level);
    writeQ(_exp);
    writeF(_expPercent);
    writeD(_str);
    writeD(_dex);
    writeD(_con);
    writeD(_int);
    writeD(_wit);
    writeD(_men);
    writeD(maxHp);
    writeD(curHp);
    writeD(maxMp);
    writeD(curMp);
    writeD(_sp);
    writeD(curLoad);
    writeD(maxLoad);
    writeD(_weaponFlag);

    for (int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER) {
      writeD(_inv[PAPERDOLL_ID][0]);
    }
    for (int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER) {
      writeD(_inv[PAPERDOLL_ID][1]);
    }
    for (int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER) {
      writeD(_inv[PAPERDOLL_ID][2]);
    }
    writeD(talismans);
    writeD(openCloak ? 1 : 0);

    writeD(_patk);
    writeD(_patkspd);
    writeD(_pdef);
    writeD(evasion);
    writeD(accuracy);
    writeD(crit);
    writeD(_matk);
    writeD(_matkspd);
    writeD(_patkspd);
    writeD(_mdef);
    writeD(pvp_flag);
    writeD(karma);
    writeD(_runSpd);
    writeD(_walkSpd);
    writeD(_swimRunSpd);
    writeD(_swimWalkSpd);
    writeD(_flRunSpd);
    writeD(_flWalkSpd);
    writeD(_flyRunSpd);
    writeD(_flyWalkSpd);
    writeF(move_speed);
    writeF(attack_speed);
    writeF(col_radius);
    writeF(col_height);
    writeD(hair_style);
    writeD(hair_color);
    writeD(face);
    writeD(gm_commands);
    writeS(title);
    writeD(clan_id);
    writeD(clan_crest_id);
    writeD(ally_id);
    writeD(ally_crest_id);

    writeD(_relation);
    writeC(mount_type);
    writeC(private_store);
    writeC(can_crystalize);
    writeD(pk_kills);
    writeD(pvp_kills);
    writeH(cubics.length);
    for (EffectCubic cubic : cubics)
      writeH(cubic == null ? 0 : cubic.getId());
    writeC(partyRoom ? 1 : 0);
    writeD(_abnormalEffect);
    writeC(isFlying ? 2 : 0);
    writeD(ClanPrivs);
    writeH(rec_left);
    writeH(rec_have);
    writeD(mount_id);
    writeH(InventoryLimit);
    writeD(class_id);
    writeD(0);
    writeD(maxCp);
    writeD(curCp);
    writeC(_enchant);
    writeC(_team.ordinal());
    writeD(large_clan_crest_id);
    writeC(noble);
    writeC(hero);
    writeC(0);
    writeD(_fishLoc.x);
    writeD(_fishLoc.y);
    writeD(_fishLoc.z);
    writeD(name_color);
    writeC(running);
    writeD(pledge_class);
    writeD(pledge_type);
    writeD(title_color);
    writeD(cw_level);
    writeD(transformation);

    writeH(attackElement.getId());
    writeH(attackElementValue);
    writeH(defenceFire);
    writeH(defenceWater);
    writeH(defenceWind);
    writeH(defenceEarth);
    writeH(defenceHoly);
    writeH(defenceUnholy);

    writeD(agathion);

    writeD(fame);
    writeD(_allowMap ? 1 : 0);

    writeD(vitality);
    writeD(_abnormalEffect2);
  }
}