package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.poly.ObjectPoly;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class NpcInfoPoly extends L2GameServerPacket
{
  private boolean can_writeImpl = false;
  private L2Character _activeChar;
  private L2Object _obj;
  private int _x;
  private int _y;
  private int _z;
  private int _heading;
  private int _npcId;
  private boolean _isAttackable;
  private boolean _isSummoned;
  private boolean _isRunning;
  private boolean _isInCombat;
  private boolean _isAlikeDead;
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
  private int _rhand;
  private int _lhand;
  private String _name;
  private String _title;
  private int _abnormalEffect;
  L2NpcTemplate _template;
  private int _collisionRadius;
  private int _collisionHeight;

  public NpcInfoPoly(L2Object obj, L2Character attacker)
  {
    _obj = obj;
    _npcId = obj.getPoly().getPolyId();
    _template = NpcTable.getInstance().getTemplate(_npcId);
    _isAttackable = true;
    _rhand = 0;
    _lhand = 0;
    _isSummoned = false;
    _collisionRadius = _template.collisionRadius;
    _collisionHeight = _template.collisionHeight;
    if (_obj.isL2Character()) {
      _activeChar = ((L2Character)obj);
      _isAttackable = obj.isAutoAttackable(attacker);
      _rhand = _template.rhand;
      _lhand = _template.lhand;
    }

    if (_obj.isL2Item())
    {
      _x = _obj.getX();
      _y = _obj.getY();
      _z = _obj.getZ();
      _heading = 0;
      _mAtkSpd = 100;
      _pAtkSpd = 100;
      _runSpd = 120;
      _walkSpd = 80;
      _swimRunSpd = (this._flRunSpd = this._flyRunSpd = _runSpd);
      _swimWalkSpd = (this._flWalkSpd = this._flyWalkSpd = _walkSpd);
      _isRunning = (this._isInCombat = this._isAlikeDead = 0);
      _name = "item";
      _title = "polymorphed";
      _abnormalEffect = 0;
    }
    else if (_obj.isPlayer())
    {
      L2PcInstance player = _obj.getPlayer();
      _x = player.getX();
      _y = player.getY();
      _z = player.getZ();
      _heading = player.getHeading();
      _mAtkSpd = player.getMAtkSpd();
      _pAtkSpd = player.getPAtkSpd();
      _runSpd = player.getRunSpeed();
      _walkSpd = player.getWalkSpeed();
      _swimRunSpd = (this._flRunSpd = this._flyRunSpd = _runSpd);
      _swimWalkSpd = (this._flWalkSpd = this._flyWalkSpd = _walkSpd);
      _isRunning = player.isRunning();
      _isInCombat = player.isInCombat();
      _isAlikeDead = player.isAlikeDead();

      if (player.isInOfflineMode())
      {
        if (player.getPrivateStoreType() == 1)
          _title = "OFF: \u041F\u0440\u043E\u0434\u0430\u044E";
        else if (player.getPrivateStoreType() == 3)
          _title = "OFF: \u0421\u043A\u0443\u043F\u0430\u044E";
      }
      else {
        _title = player.getTitle();
      }
      _name = player.getName();
      _abnormalEffect = _activeChar.getAbnormalEffect();
    }
    else
    {
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
      _isRunning = _activeChar.isRunning();
      _isInCombat = _activeChar.isInCombat();
      _isAlikeDead = _activeChar.isAlikeDead();
      _name = _activeChar.getName();
      _title = _activeChar.getTitle();
      _abnormalEffect = _activeChar.getAbnormalEffect();
    }
    can_writeImpl = true;
  }

  protected final void writeImpl()
  {
    if (!can_writeImpl) {
      return;
    }
    writeC(22);
    writeD(_obj.getObjectId());
    writeD(_npcId + 1000000);
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
    writeF(1.0D);
    writeF(1.0D);
    writeF(_collisionRadius);
    writeF(_collisionHeight);
    writeD(_rhand);
    writeD(0);
    writeD(_lhand);
    writeC(1);
    writeC(_isRunning ? 1 : 0);
    writeC(_isInCombat ? 1 : 0);
    writeC(_isAlikeDead ? 1 : 0);
    writeC(_isSummoned ? 2 : 0);
    writeS(_name);
    writeS(_title);
    writeD(0);
    writeD(0);
    writeD(0);

    writeH(_abnormalEffect);
    writeH(0);
    writeD(0);
    writeD(0);
    writeD(0);
    writeD(0);
    writeC(0);
  }
}