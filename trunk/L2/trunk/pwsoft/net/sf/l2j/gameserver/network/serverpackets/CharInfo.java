package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2CubicInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.poly.ObjectPoly;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.network.L2GameClient;

public class CharInfo extends L2GameServerPacket
{
  private static final Logger _log = Logger.getLogger(CharInfo.class.getName());
  private L2PcInstance _cha;
  private Inventory _inv;
  private int _x;
  private int _y;
  private int _z;
  private int _heading;
  private int _mAtkSpd;
  private int _pAtkSpd;
  private int _runSpd;
  private int _walkSpd;
  private int _swimRunSpd;
  private int _swimWalkSpd;
  private int _flRunSpd;
  private int _flWalkSpd;
  private int _flyRunSpd;
  private int _flyWalkSpd;
  private float _moveMultiplier;
  private float _attackSpeedMultiplier;
  private int _maxCp;
  private int _curCp;
  private String _name;
  private String _title;
  private int _objId;
  private int _race;
  private int _sex;
  private int base_class;
  private int pvp_flag;
  private int karma;
  private int rec_have;
  private int rec_left;
  private float speed_move;
  private float speed_atack;
  private float col_radius;
  private float col_height;
  private int hair_style;
  private int hair_color;
  private int face;
  private int abnormal_effect;
  private int clan_id;
  private int clan_crest_id;
  private int large_clan_crest_id;
  private int ally_id;
  private int ally_crest_id;
  private int class_id;
  private int _sit;
  private int _run;
  private int _combat;
  private int _dead;
  private int _invis;
  private int private_store;
  private int _enchant;
  private int _team;
  private int _noble;
  private int _hero;
  private int _fishing;
  private int mount_type;
  private int _lfp;
  private int plg_class;
  private int pledge_type;
  private int clan_rep_score;
  private int cw_level;
  private int mount_id;
  private int _nameColor;
  private int title_color;
  private FastList<L2CubicInstance> _cubics;
  private boolean can_writeImpl = false;

  public CharInfo(L2PcInstance cha)
  {
    if (((this._cha = cha) == null) || (_cha.isInvisible()) || (_cha.isDeleting())) {
      return;
    }

    _name = _cha.getName();
    _title = _cha.getTitle();

    if (_cha.isInOfflineMode()) {
      if (_cha.getPrivateStoreType() == 1)
        _title = "OFF: \u041F\u0440\u043E\u0434\u0430\u044E";
      else if (_cha.getPrivateStoreType() == 3) {
        _title = "OFF: \u0421\u043A\u0443\u043F\u0430\u044E";
      }

      title_color = Integer.decode("0x1b7ccf").intValue();
      _nameColor = Integer.decode("0x1b7ccf").intValue();
    } else {
      if (_cha.isInvisible()) {
        _title = ("*" + _title);
      }
      title_color = _cha.getAppearance().getTitleColor();
      _nameColor = _cha.getAppearance().getNameColor();
    }

    clan_id = _cha.getClanId();
    if ((clan_id > 0) && (_cha.getClan() != null)) {
      clan_rep_score = _cha.getClan().getReputationScore();
      clan_crest_id = _cha.getClanCrestId();
      ally_id = _cha.getAllyId();
      ally_crest_id = _cha.getAllyCrestId();
      large_clan_crest_id = _cha.getClanCrestLargeId();
    } else {
      clan_rep_score = 0;
      clan_crest_id = 0;
      ally_id = 0;
      ally_crest_id = 0;
      large_clan_crest_id = 0;
    }

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

    _inv = _cha.getInventory();
    _mAtkSpd = _cha.getMAtkSpd();
    _pAtkSpd = _cha.getPAtkSpd();
    _moveMultiplier = _cha.getMovementSpeedMultiplier();
    _runSpd = (int)(_cha.getRunSpeed() / _moveMultiplier);
    _walkSpd = (int)(_cha.getWalkSpeed() / _moveMultiplier);
    _flRunSpd = _runSpd;
    _flWalkSpd = _walkSpd;
    _swimRunSpd = _runSpd;
    _swimWalkSpd = _walkSpd;
    _objId = _cha.getObjectId();
    _race = _cha.getRace().ordinal();
    _sex = (_cha.getAppearance().getSex() ? 1 : 0);

    if (_cha.getClassIndex() == 0)
      base_class = _cha.getClassId().getId();
    else {
      base_class = _cha.getBaseClass();
    }

    pvp_flag = (Config.FREE_PVP ? 0 : _cha.getPvpFlag());
    karma = _cha.getKarma();
    speed_move = _cha.getMovementSpeedMultiplier();
    speed_atack = _cha.getAttackSpeedMultiplier();
    col_radius = _cha.getColRadius();
    col_height = _cha.getColHeight();
    hair_style = _cha.getAppearance().getHairStyle();
    hair_color = _cha.getAppearance().getHairColor();
    face = _cha.getAppearance().getFace();
    _sit = (_cha.isSitting() ? 0 : 1);
    _run = (_cha.isRunning() ? 1 : 0);
    _combat = (_cha.isInCombat() ? 1 : 0);
    _dead = (_cha.isAlikeDead() ? 1 : 0);

    _invis = (_cha.inObserverMode() ? 1 : 0);

    private_store = _cha.getPrivateStoreType();

    _cubics = new FastList();
    _cubics.addAll(_cha.getCubics().values());

    _lfp = (_cha.isLFP() ? 1 : 0);
    abnormal_effect = _cha.getAbnormalEffect();
    if (_cha.isInvisible()) {
      abnormal_effect = 1048576;
    }

    rec_left = _cha.getRecomLeft();
    rec_have = _cha.getRecomHave();
    class_id = _cha.getClassId().getId();
    _team = _cha.getTeam();

    _noble = (_cha.isNoble() ? 1 : 0);
    _hero = ((_cha.isHero()) || ((_cha.isGM()) && (Config.GM_HERO_AURA)) ? 1 : 0);
    _fishing = (_cha.isFishing() ? 1 : 0);
    plg_class = _cha.getPledgeClass();
    pledge_type = _cha.getPledgeType();
    _maxCp = _cha.getMaxCp();
    _curCp = (int)_cha.getCurrentCp();
    can_writeImpl = true;
  }

  protected final void writeImpl()
  {
    if (!can_writeImpl) {
      return;
    }

    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }

    if (activeChar.equals(_cha)) {
      _log.severe("You cant send CharInfo about his character to active user!!!");
      return;
    }

    if ((_cha.getPoly().isMorphed()) && (!_cha.isInOfflineMode())) {
      activeChar.sendPacket(new NpcInfoPoly(_cha, activeChar));
      return;
    }
    if ((_team == 0) && (_cha.getOsTeam() > 0) && 
      (activeChar.getOsTeam() > 0) && (activeChar.getOsTeam() != _cha.getOsTeam())) {
      _team = 2;
    }

    writeC(3);
    writeD(_cha.getX());
    writeD(_cha.getY());
    writeD(_cha.getZ());
    writeD(_cha.getHeading());
    writeD(_objId);
    writeS(_name);
    writeD(_race);
    writeD(_sex);
    writeD(base_class);

    writeD(_inv.getPaperdollItemId(17));
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

    writeD(pvp_flag);
    writeD(karma);

    writeD(_mAtkSpd);
    writeD(_pAtkSpd);

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
    writeF(speed_move);
    writeF(speed_atack);

    writeF(col_radius);
    writeF(col_height);

    writeD(hair_style);
    writeD(hair_color);
    writeD(face);
    writeS(_title);
    writeD(clan_id);
    writeD(clan_crest_id);
    writeD(ally_id);
    writeD(ally_crest_id);

    writeD(0);

    writeC(_sit);
    writeC(_run);
    writeC(_combat);
    writeC(_dead);
    writeC(_invis);
    writeC(mount_type);
    writeC(private_store);

    writeH(_cubics.size());

    for (L2CubicInstance cubic : _cubics) {
      writeH(cubic == null ? 0 : cubic.getId());
    }

    writeC(_lfp);
    writeD(abnormal_effect);
    writeC(rec_left);
    writeH(rec_have);
    writeD(class_id);
    writeD(_maxCp);
    writeD(_curCp);
    writeC(_enchant);

    writeC(_team);

    writeD(large_clan_crest_id);

    writeC(_noble);
    writeC(_hero);

    writeC(_fishing);
    writeD(_cha.GetFishx());
    writeD(_cha.GetFishy());
    writeD(_cha.GetFishz());

    writeD(_nameColor);

    writeD(_cha.getHeading());

    writeD(plg_class);
    writeD(pledge_type);

    writeD(title_color);
    writeD(cw_level);
    writeD(clan_rep_score);
  }

  public boolean isCharInfo()
  {
    return true;
  }
}