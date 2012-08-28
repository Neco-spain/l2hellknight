package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.ClassId;
import l2m.gameserver.model.base.Element;
import l2m.gameserver.model.base.Experience;
import l2m.gameserver.model.base.Race;
import l2m.gameserver.model.items.Inventory;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.model.pledge.Alliance;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.utils.Location;

public class GMViewCharacterInfo extends L2GameServerPacket
{
  private Location _loc;
  private int[][] _inv;
  private int obj_id;
  private int _race;
  private int _sex;
  private int class_id;
  private int pvp_flag;
  private int karma;
  private int level;
  private int mount_type;
  private int _str;
  private int _con;
  private int _dex;
  private int _int;
  private int _wit;
  private int _men;
  private int _sp;
  private int curHp;
  private int maxHp;
  private int curMp;
  private int maxMp;
  private int curCp;
  private int maxCp;
  private int curLoad;
  private int maxLoad;
  private int rec_left;
  private int rec_have;
  private int _patk;
  private int _patkspd;
  private int _pdef;
  private int evasion;
  private int accuracy;
  private int crit;
  private int _matk;
  private int _matkspd;
  private int _mdef;
  private int hair_style;
  private int hair_color;
  private int face;
  private int gm_commands;
  private int clan_id;
  private int clan_crest_id;
  private int ally_id;
  private int title_color;
  private int noble;
  private int hero;
  private int private_store;
  private int name_color;
  private int pk_kills;
  private int pvp_kills;
  private int _runSpd;
  private int _walkSpd;
  private int _swimSpd;
  private int DwarvenCraftLevel;
  private int running;
  private int pledge_class;
  private String _name;
  private String title;
  private long _exp;
  private double move_speed;
  private double attack_speed;
  private double col_radius;
  private double col_height;
  private Element attackElement;
  private int attackElementValue;
  private int defenceFire;
  private int defenceWater;
  private int defenceWind;
  private int defenceEarth;
  private int defenceHoly;
  private int defenceUnholy;
  private int fame;
  private int vitality;
  private int talismans;
  private boolean openCloak;
  private double _expPercent;

  public GMViewCharacterInfo(Player cha)
  {
    _loc = cha.getLoc();
    obj_id = cha.getObjectId();
    _name = cha.getName();
    _race = cha.getRace().ordinal();
    _sex = cha.getSex();
    class_id = cha.getClassId().getId();
    level = cha.getLevel();
    _exp = cha.getExp();
    _str = cha.getSTR();
    _dex = cha.getDEX();
    _con = cha.getCON();
    _int = cha.getINT();
    _wit = cha.getWIT();
    _men = cha.getMEN();
    curHp = (int)cha.getCurrentHp();
    maxHp = cha.getMaxHp();
    curMp = (int)cha.getCurrentMp();
    maxMp = cha.getMaxMp();
    _sp = cha.getIntSp();
    curLoad = cha.getCurrentLoad();
    maxLoad = cha.getMaxLoad();
    _patk = cha.getPAtk(null);
    _patkspd = cha.getPAtkSpd();
    _pdef = cha.getPDef(null);
    evasion = cha.getEvasionRate(null);
    accuracy = cha.getAccuracy();
    crit = cha.getCriticalHit(null, null);
    _matk = cha.getMAtk(null, null);
    _matkspd = cha.getMAtkSpd();
    _mdef = cha.getMDef(null, null);
    pvp_flag = cha.getPvpFlag();
    karma = cha.getKarma();
    _runSpd = cha.getRunSpeed();
    _walkSpd = cha.getWalkSpeed();
    _swimSpd = cha.getSwimSpeed();
    move_speed = cha.getMovementSpeedMultiplier();
    attack_speed = cha.getAttackSpeedMultiplier();
    mount_type = cha.getMountType();
    col_radius = cha.getColRadius();
    col_height = cha.getColHeight();
    hair_style = cha.getHairStyle();
    hair_color = cha.getHairColor();
    face = cha.getFace();
    gm_commands = (cha.isGM() ? 1 : 0);
    title = cha.getTitle();
    _expPercent = Experience.getExpPercent(cha.getLevel(), cha.getExp());

    Clan clan = cha.getClan();
    Alliance alliance = clan == null ? null : clan.getAlliance();

    clan_id = (clan == null ? 0 : clan.getClanId());
    clan_crest_id = (clan == null ? 0 : clan.getCrestId());

    ally_id = (alliance == null ? 0 : alliance.getAllyId());

    private_store = (cha.isInObserverMode() ? 7 : cha.getPrivateStoreType());
    DwarvenCraftLevel = Math.max(cha.getSkillLevel(Integer.valueOf(1320)), 0);
    pk_kills = cha.getPkKills();
    pvp_kills = cha.getPvpKills();
    rec_left = cha.getRecomLeft();
    rec_have = cha.getRecomHave();
    curCp = (int)cha.getCurrentCp();
    maxCp = cha.getMaxCp();
    running = (cha.isRunning() ? 1 : 0);
    pledge_class = cha.getPledgeClass();
    noble = (cha.isNoble() ? 1 : 0);
    hero = (cha.isHero() ? 1 : 0);
    name_color = cha.getNameColor();
    title_color = cha.getTitleColor();
    attackElement = cha.getAttackElement();
    attackElementValue = cha.getAttack(attackElement);
    defenceFire = cha.getDefence(Element.FIRE);
    defenceWater = cha.getDefence(Element.WATER);
    defenceWind = cha.getDefence(Element.WIND);
    defenceEarth = cha.getDefence(Element.EARTH);
    defenceHoly = cha.getDefence(Element.HOLY);
    defenceUnholy = cha.getDefence(Element.UNHOLY);
    fame = cha.getFame();
    vitality = (int)cha.getVitality();
    talismans = cha.getTalismanCount();
    openCloak = cha.getOpenCloak();
    _inv = new int[26][3];
    for (int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
    {
      _inv[PAPERDOLL_ID][0] = cha.getInventory().getPaperdollObjectId(PAPERDOLL_ID);
      _inv[PAPERDOLL_ID][1] = cha.getInventory().getPaperdollItemId(PAPERDOLL_ID);
      _inv[PAPERDOLL_ID][2] = cha.getInventory().getPaperdollAugmentationId(PAPERDOLL_ID);
    }
  }

  protected final void writeImpl()
  {
    writeC(149);

    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
    writeD(_loc.h);
    writeD(obj_id);
    writeS(_name);
    writeD(_race);
    writeD(_sex);
    writeD(class_id);
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
    writeD(pk_kills);

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
    writeD(_swimSpd);
    writeD(_swimSpd);
    writeD(_runSpd);
    writeD(_walkSpd);
    writeD(_runSpd);
    writeD(_walkSpd);
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
    writeC(mount_type);
    writeC(private_store);
    writeC(DwarvenCraftLevel);
    writeD(pk_kills);
    writeD(pvp_kills);
    writeH(rec_left);
    writeH(rec_have);
    writeD(class_id);
    writeD(0);
    writeD(maxCp);
    writeD(curCp);
    writeC(running);
    writeC(321);
    writeD(pledge_class);
    writeC(noble);
    writeC(hero);
    writeD(name_color);
    writeD(title_color);

    writeH(attackElement.getId());
    writeH(attackElementValue);
    writeH(defenceFire);
    writeH(defenceWater);
    writeH(defenceWind);
    writeH(defenceEarth);
    writeH(defenceHoly);
    writeH(defenceUnholy);

    writeD(fame);
    writeD(vitality);
  }
}