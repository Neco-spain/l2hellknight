package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.poly.ObjectPoly;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.network.L2GameClient;

public class UserInfo extends L2GameServerPacket
{
  private boolean can_writeImpl = false;
  private L2PcInstance _cha;
  private int _runSpd;
  private int _walkSpd;
  private int _swimRunSpd;
  private int _swimWalkSpd;
  private int _flRunSpd;
  private int _flWalkSpd;
  private int _flyRunSpd;
  private int _flyWalkSpd;
  private int _relation;
  private float move_speed;
  private float attack_speed;
  private float col_radius;
  private float col_height;
  private Inventory _inv;
  private int obj_id;
  private int _race;
  private int sex;
  private int base_class;
  private int level;
  private int curCp;
  private int maxCp;
  private int _enchant;
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
  private int team;
  private int AbnormalEffect;
  private int noble;
  private int hero;
  private int fishing;
  private int cw_level;
  private int name_color;
  private int running;
  private int pledge_class;
  private int pledge_type;
  private int title_color;
  private int _lfp;
  private byte mount_type;
  private String _name;
  private String title;
  private FastList<Integer> _cubics;

  public UserInfo(L2PcInstance cha)
  {
    _cha = cha;
  }

  public final void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (!activeChar.equals(_cha)) {
      return;
    }

    _name = _cha.getName();
    clan_crest_id = _cha.getClanCrestId();
    ally_crest_id = _cha.getAllyCrestId();
    large_clan_crest_id = _cha.getClanCrestLargeId();

    if (_cha.isCursedWeaponEquiped())
      cw_level = CursedWeaponsManager.getInstance().getLevel(_cha.getCursedWeaponEquipedId());
    else {
      cw_level = 0;
    }

    if (_cha.isMounted()) {
      _enchant = 0;

      mount_type = (byte)_cha.getMountType();
    } else {
      _enchant = (byte)_cha.getEnchantEffect();

      mount_type = 0;
    }

    move_speed = _cha.getMovementSpeedMultiplier();
    _runSpd = (int)(_cha.getRunSpeed() / move_speed);
    _walkSpd = (int)(_cha.getWalkSpeed() / move_speed);
    _swimRunSpd = (this._flRunSpd = this._flyRunSpd = _runSpd);
    _swimWalkSpd = (this._flWalkSpd = this._flyWalkSpd = _walkSpd);
    _inv = _cha.getInventory();
    _relation = (_cha.isClanLeader() ? 64 : 0);
    if (_cha.getSiegeState() == 1)
      _relation |= 384;
    else if (_cha.getSiegeState() == 2) {
      _relation |= 128;
    }

    obj_id = _cha.getObjectId();
    _race = _cha.getRace().ordinal();
    sex = (_cha.getAppearance().getSex() ? 1 : 0);
    base_class = _cha.getBaseClass();
    level = _cha.getLevel();
    _exp = _cha.getExp();
    _str = _cha.getSTR();
    _dex = _cha.getDEX();
    _con = _cha.getCON();
    _int = _cha.getINT();
    _wit = _cha.getWIT();
    _men = _cha.getMEN();
    curHp = (int)_cha.getCurrentHp();
    maxHp = _cha.getMaxHp();
    curMp = (int)_cha.getCurrentMp();
    maxMp = _cha.getMaxMp();
    curLoad = _cha.getCurrentLoad();
    maxLoad = _cha.getMaxLoad();
    _sp = _cha.getSp();
    _patk = _cha.getPAtk(null);
    _patkspd = _cha.getPAtkSpd();
    _pdef = _cha.getPDef(null);
    evasion = _cha.getEvasionRate(null);
    accuracy = _cha.getAccuracy();
    crit = _cha.getCriticalHit(null, null);
    _matk = _cha.getMAtk(null, null);
    _matkspd = _cha.getMAtkSpd();
    _mdef = _cha.getMDef(null, null);
    pvp_flag = (Config.FREE_PVP ? 0 : _cha.getPvpFlag());
    karma = _cha.getKarma();
    attack_speed = _cha.getAttackSpeedMultiplier();
    col_radius = _cha.getColRadius();
    col_height = _cha.getColHeight();
    hair_style = _cha.getAppearance().getHairStyle();
    hair_color = _cha.getAppearance().getHairColor();
    face = _cha.getAppearance().getFace();

    gm_commands = (_cha.isGM() ? 1 : 0);

    title = _cha.getTitle();
    if (_cha.isInvisible())
      title = ("*" + title);
    else if (_cha.getPoly().isMorphed()) {
      title = "*,..,*";
    }

    name_color = _cha.getAppearance().getNameColor();
    title_color = _cha.getAppearance().getTitleColor();

    clan_id = _cha.getClanId();
    ally_id = _cha.getAllyId();
    private_store = _cha.getPrivateStoreType();
    can_crystalize = (_cha.hasDwarvenCraft() ? 1 : 0);
    pk_kills = _cha.getPkKills();
    pvp_kills = _cha.getPvpKills();
    _cubics = new FastList();
    Iterator i$;
    if (_cha.getCubics() != null) {
      for (i$ = _cha.getCubics().keySet().iterator(); i$.hasNext(); ) { int id = ((Integer)i$.next()).intValue();
        _cubics.add(Integer.valueOf(id));
      }
    }
    _lfp = (_cha.isLFP() ? 1 : 0);
    AbnormalEffect = _cha.getAbnormalEffect();
    ClanPrivs = _cha.getClanPrivileges();
    rec_left = _cha.getRecomLeft();
    rec_have = _cha.getRecomHave();
    InventoryLimit = _cha.getInventoryLimit();
    class_id = _cha.getClassId().getId();
    maxCp = _cha.getMaxCp();
    curCp = (int)_cha.getCurrentCp();
    team = _cha.getTeam();
    noble = (_cha.isNoble() ? 1 : 0);
    hero = ((_cha.isHero()) || ((_cha.isGM()) && (Config.GM_HERO_AURA)) ? 1 : 0);
    fishing = (_cha.isFishing() ? 1 : 0);

    running = (_cha.isRunning() ? 1 : 0);
    pledge_class = _cha.getPledgeClass();
    pledge_type = _cha.getPledgeType();

    _cha.refreshSavedStats();

    can_writeImpl = true;
  }

  protected final void writeImpl()
  {
    if (!can_writeImpl) {
      return;
    }

    writeC(4);

    writeD(_cha.getX());
    writeD(_cha.getY());
    writeD(_cha.getZ());
    writeD(_cha.getHeading());
    writeD(obj_id);
    writeS(_name);
    writeD(_race);
    writeD(sex);
    writeD(base_class);
    writeD(level);
    writeQ(_exp);
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
    writeD(40);

    writeD(_inv.getPaperdollObjectId(17));
    writeD(_inv.getPaperdollObjectId(2));
    writeD(_inv.getPaperdollObjectId(1));
    writeD(_inv.getPaperdollObjectId(3));
    writeD(_inv.getPaperdollObjectId(5));
    writeD(_inv.getPaperdollObjectId(4));
    writeD(_inv.getPaperdollObjectId(6));
    writeD(_inv.getPaperdollObjectId(7));
    writeD(_inv.getPaperdollObjectId(8));
    writeD(_inv.getPaperdollObjectId(9));
    writeD(_inv.getPaperdollObjectId(10));
    writeD(_inv.getPaperdollObjectId(11));
    writeD(_inv.getPaperdollObjectId(12));
    writeD(_inv.getPaperdollObjectId(13));
    writeD(_inv.getPaperdollObjectId(14));
    writeD(_inv.getPaperdollObjectId(16));
    writeD(_inv.getPaperdollObjectId(15));

    writeD(_inv.getPaperdollItemId(17));
    writeD(_inv.getPaperdollItemId(2));
    writeD(_inv.getPaperdollItemId(1));
    writeD(_inv.getPaperdollItemId(3));
    writeD(_inv.getPaperdollItemId(5));
    writeD(_inv.getPaperdollItemId(4));
    writeD(_inv.getPaperdollItemId(6));
    writeD(_inv.getPaperdollItemId(7));
    writeD(_inv.getPaperdollItemId(8));
    writeD(_inv.getPaperdollItemId(9));
    writeD(_inv.getPaperdollItemId(10));
    writeD(_inv.getPaperdollItemId(11));
    writeD(_inv.getPaperdollItemId(12));
    writeD(_inv.getPaperdollItemId(13));
    writeD(_inv.getPaperdollItemId(14));
    writeD(_inv.getPaperdollItemId(16));
    writeD(_inv.getPaperdollItemId(15));

    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeD(_inv.getPaperdollAugmentationId(7));
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeD(_inv.getPaperdollAugmentationId(14));
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);

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
    writeH(_cubics.size());
    while (_cubics.size() > 0) {
      writeH(((Integer)_cubics.removeFirst()).intValue());
    }

    writeC(_lfp);

    writeD(AbnormalEffect);
    writeC(17);
    writeD(ClanPrivs);
    writeH(rec_left);
    writeH(rec_have);
    writeD(0);
    writeH(InventoryLimit);
    writeD(class_id);
    writeD(16777216);
    writeD(maxCp);
    writeD(curCp);
    writeC(_enchant);
    writeC(team);
    writeD(large_clan_crest_id);
    writeC(noble);
    writeC(hero);
    writeC(fishing);
    writeD(_cha.GetFishx());
    writeD(_cha.GetFishy());
    writeD(_cha.GetFishz());
    writeD(name_color);

    writeC(running);
    writeD(pledge_class);
    writeD(pledge_type);
    writeD(title_color);
    writeD(cw_level);
  }
}