package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class NpcInfo extends L2GameServerPacket
{
  private boolean can_writeImpl = false;
  private L2Character _activeChar;
  private int _x;
  private int _y;
  private int _z;
  private int _heading;
  private int _idTemplate;
  private boolean _isAttackable;
  private boolean _isSummoned;
  private int _mAtkSpd;
  private int _pAtkSpd;
  private int _showSpawnAnimation = 0;
  private int _runSpd;
  private int _walkSpd;
  private int _swimRunSpd;
  private int _swimWalkSpd;
  private int _flRunSpd;
  private int _flWalkSpd;
  private int _flyRunSpd;
  private int _flyWalkSpd;
  private int _rhand;
  private int _lhand;
  private int _chest;
  private int _val;
  private int _pvpFlag;
  private int _collisionHeight;
  private int _collisionRadius;
  private String _name = "";
  private L2Summon _summon;
  private String _title = "";
  private int _form = 0;
  private boolean _isChampion = false;
  private boolean _champShowAura = false;
  private int _weaponEnhcant = 0;

  public NpcInfo(L2NpcInstance cha, L2Character attacker)
  {
    _activeChar = cha;
    _idTemplate = cha.getTemplate().idTemplate;
    _isAttackable = cha.isAutoAttackable(attacker);
    _rhand = cha.getRightHandItem();
    _lhand = cha.getLeftHandItem();
    _isSummoned = cha.isVis();
    _isChampion = cha.isChampion();
    _collisionHeight = cha.getCollisionHeight();
    _collisionRadius = cha.getCollisionRadius();
    if (cha.getTemplate().serverSideName) {
      _name = cha.getTemplate().name;
    }

    if ((Config.L2JMOD_CHAMPION_ENABLE) && (_isChampion))
      _title = "Champion";
    else if (cha.getTemplate().serverSideTitle)
      _title = cha.getTemplate().title;
    else {
      _title = cha.getTitle();
    }
    if ((!Config.SHOW_NPC_LVL) && (_activeChar.isL2Monster()) && (!_isChampion)) {
      _title = " ";
    }

    if ((Config.L2JMOD_CHAMPION_AURA) && (_isChampion)) {
      _champShowAura = true;
    }

    _x = _activeChar.getX();
    _y = _activeChar.getY();
    _z = _activeChar.getZ();
    _heading = _activeChar.getHeading();
    _mAtkSpd = _activeChar.getMAtkSpd();
    _pAtkSpd = _activeChar.getPAtkSpd();
    _runSpd = _activeChar.getRunSpeed();
    _walkSpd = _activeChar.getWalkSpeed();
    _swimRunSpd = (this._flRunSpd = this._flyRunSpd = _runSpd);
    _swimWalkSpd = (this._flWalkSpd = this._flyWalkSpd = _walkSpd);
    _showSpawnAnimation = cha.isShowSpawnAnimation();
    _weaponEnhcant = cha.getWeaponEnchant();
    can_writeImpl = true;
  }

  public NpcInfo(L2Summon cha, L2Character attacker, int val) {
    _activeChar = cha;
    _summon = cha;
    _idTemplate = cha.getTemplate().idTemplate;
    _isAttackable = cha.isAutoAttackable(attacker);
    _rhand = cha.getWeapon();
    _lhand = 0;
    _chest = cha.getArmor();
    _val = val;
    _collisionHeight = _activeChar.getTemplate().collisionHeight;
    _collisionRadius = _activeChar.getTemplate().collisionRadius;
    _name = cha.getName();
    _title = (cha.getOwner() != null ? cha.getOwner().getName() : cha.getOwner().isOnline() == 0 ? "" : "");

    if (cha.getNpcId() == Config.SOB_NPC) {
      _name = " ";
      _title = " ";
    }

    _x = _activeChar.getX();
    _y = _activeChar.getY();
    _z = _activeChar.getZ();
    _heading = _activeChar.getHeading();
    _mAtkSpd = _activeChar.getMAtkSpd();
    _pAtkSpd = _activeChar.getPAtkSpd();
    _runSpd = _summon.getPetSpeed();
    _walkSpd = (_summon.isMountable() ? 45 : 30);
    _swimRunSpd = (this._flRunSpd = this._flyRunSpd = _runSpd);
    _swimWalkSpd = (this._flWalkSpd = this._flyWalkSpd = _walkSpd);
    _pvpFlag = (Config.FREE_PVP ? 0 : _summon.getOwner().getPvpFlag());

    can_writeImpl = true;
  }

  public NpcInfo(L2Summon cha, L2Character attacker) {
    _activeChar = cha;
    _summon = cha;
    _idTemplate = cha.getTemplate().idTemplate;
    _isAttackable = cha.isAutoAttackable(attacker);
    _rhand = 0;
    _lhand = 0;
    _isSummoned = cha.isShowSummonAnimation();
    _collisionHeight = _activeChar.getTemplate().collisionHeight;
    _collisionRadius = _activeChar.getTemplate().collisionRadius;
    _name = cha.getName();
    _title = (cha.getOwner() != null ? cha.getOwner().getName() : cha.getOwner().isOnline() == 0 ? "" : "");

    if (cha.getNpcId() == Config.SOB_NPC) {
      _name = " ";
      _title = " ";
    }

    _x = _activeChar.getX();
    _y = _activeChar.getY();
    _z = _activeChar.getZ();
    _heading = _activeChar.getHeading();
    _mAtkSpd = _activeChar.getMAtkSpd();
    _pAtkSpd = _activeChar.getPAtkSpd();
    _runSpd = _activeChar.getRunSpeed();
    _walkSpd = _activeChar.getWalkSpeed();
    _swimRunSpd = (this._flRunSpd = this._flyRunSpd = _runSpd);
    _swimWalkSpd = (this._flWalkSpd = this._flyWalkSpd = _walkSpd);
    _pvpFlag = (Config.FREE_PVP ? 0 : _summon.getOwner().getPvpFlag());
    can_writeImpl = true;
  }

  protected final void writeImpl()
  {
    if (!can_writeImpl) {
      return;
    }

    if ((_activeChar.isL2Summon()) && 
      (_activeChar.getOwner() != null) && (_activeChar.getOwner().isInvisible())) {
      return;
    }

    writeC(22);
    writeD(_activeChar.getObjectId());
    writeD(_idTemplate + 1000000);
    writeD(_isAttackable ? 1 : 0);
    writeD(_x);
    writeD(_y);
    writeD(_z);
    writeD(_heading);
    writeD(0);
    writeD(_mAtkSpd);
    writeD(_pAtkSpd);
    writeD(_runSpd);
    writeD(_walkSpd);
    writeD(_swimRunSpd);
    writeD(_swimWalkSpd);
    writeD(_flRunSpd);
    writeD(_flWalkSpd);
    writeD(_flyRunSpd);
    writeD(_flyWalkSpd);
    writeF(1.1D);

    writeF(_pAtkSpd / 277.47834071900002D);
    writeF(_collisionRadius);
    writeF(_collisionHeight);
    writeD(_rhand);
    writeD(0);
    writeD(_lhand);
    writeC(1);
    writeC(_activeChar.isRunning() ? 1 : 0);
    writeC(_activeChar.isInCombat() ? 1 : 0);
    writeC(_activeChar.isAlikeDead() ? 1 : 0);
    writeC(_showSpawnAnimation);
    writeS(_name);
    writeS(_title);

    if (_activeChar.isL2Summon())
      writeD(1);
    else {
      writeD(_isChampion ? 1 : 0);
    }

    writeD(0);
    if (_activeChar.isL2Summon())
      writeD(_pvpFlag);
    else {
      writeD(_isChampion ? 3 : 0);
    }

    writeD(_activeChar.getAbnormalEffect());
    if (_activeChar.isL2Summon())
      writeD(1);
    else {
      writeD(0);
    }

    writeD(0);
    writeD(0);
    writeD(0);
    writeC(0);

    if (_activeChar.isL2Summon())
      writeC(_summon.getOwner().getTeam());
    else {
      writeC(_champShowAura ? 2 : 0);
    }

    writeF(_collisionRadius);
    writeF(_collisionHeight);
    writeD(_weaponEnhcant);

    writeD(0);
  }
}