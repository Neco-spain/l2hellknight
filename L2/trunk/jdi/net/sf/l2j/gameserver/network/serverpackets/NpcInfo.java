package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.zone.type.L2TownZone;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class NpcInfo extends L2GameServerPacket
{
  private static final String _S__22_NPCINFO = "[S] 16 NpcInfo";
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
  private int _collisionHeight;
  private int _collisionRadius;
  private String _name = "";
  private String _title = "";
  private int _clanCrest;
  private int _clanId;
  private int _allyCrest;
  private int _allyId;

  public NpcInfo(L2NpcInstance cha, L2Character attacker)
  {
    if (cha.getCustomNpcInstance() != null)
    {
      attacker.sendPacket(new CustomNpcInfo(cha));
      attacker.broadcastPacket(new FinishRotation(cha));
      return;
    }
    _activeChar = cha;
    _idTemplate = cha.getTemplate().idTemplate;
    _isAttackable = cha.isAutoAttackable(attacker);
    _rhand = cha.getRightHandItem();
    _lhand = cha.getLeftHandItem();
    _isSummoned = false;
    _collisionHeight = cha.getCollisionHeight();
    _collisionRadius = cha.getCollisionRadius();
    if (cha.getTemplate().serverSideName) {
      _name = cha.getTemplate().name;
    }
    if ((Config.CHAMPION_ENABLE) && (cha.isChampion()))
      _title = Config.CHAMPION_TITLE;
    if (cha.getTemplate().serverSideTitle)
      _title = new StringBuilder().append(_title).append(" ").append(cha.getTemplate().title).toString();
    else if (cha.getTitle() != null) {
      _title = new StringBuilder().append(_title).append(" ").append(cha.getTitle()).toString();
    }
    if ((Config.SHOW_NPC_CREST) && ((cha instanceof L2NpcInstance)) && (cha.isInsideZone(2)) && (cha.getCastle().getOwnerId() != 0))
    {
      int _x = cha.getX();
      int _y = cha.getY();
      int _z = cha.getZ();

      L2TownZone Town = TownManager.getInstance().getTown(_x, _y, _z);
      if (Town != null)
      {
        int townId = Town.getTownId();
        if ((townId != 33) && (townId != 22))
        {
          L2Clan clan = ClanTable.getInstance().getClan(cha.getCastle().getOwnerId());
          _clanCrest = clan.getCrestId();
          _clanId = clan.getClanId();
          _allyCrest = clan.getAllyCrestId();
          _allyId = clan.getAllyId();
        }
      }
    }

    if ((Config.SHOW_NPC_LVL) && ((_activeChar instanceof L2MonsterInstance)))
    {
      String t = new StringBuilder().append("Lv ").append(cha.getLevel()).append(cha.getAggroRange() > 0 ? "*" : "").toString();
      if ((_title != null) && (!_title.isEmpty()))
        t = new StringBuilder().append(t).append(" ").append(_title).toString();
      _title = t;
    }
    this._x = _activeChar.getX();
    this._y = _activeChar.getY();
    this._z = _activeChar.getZ();
    _heading = _activeChar.getHeading();
    _mAtkSpd = _activeChar.getMAtkSpd();
    _pAtkSpd = _activeChar.getPAtkSpd();
    _runSpd = _activeChar.getRunSpeed();
    _walkSpd = _activeChar.getWalkSpeed();
    _swimRunSpd = (this._flRunSpd = this._flyRunSpd = _runSpd);
    _swimWalkSpd = (this._flWalkSpd = this._flyWalkSpd = _walkSpd);
  }

  public NpcInfo(L2Summon cha, L2Character attacker)
  {
    _activeChar = cha;
    _idTemplate = cha.getTemplate().idTemplate;
    _isAttackable = cha.isAutoAttackable(attacker);
    _rhand = 0;
    _lhand = 0;
    _isSummoned = cha.isShowSummonAnimation();
    _collisionHeight = _activeChar.getTemplate().collisionHeight;
    _collisionRadius = _activeChar.getTemplate().collisionRadius;
    _name = cha.getName();
    _title = (cha.getOwner() != null ? cha.getOwner().getName() : cha.getOwner().isOnline() == 0 ? "" : "");

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
  }

  protected final void writeImpl()
  {
    if (_activeChar == null) {
      return;
    }
    if (((_activeChar instanceof L2Summon)) && 
      (((L2Summon)_activeChar).getOwner() != null) && (((L2Summon)_activeChar).getOwner().getAppearance().getInvisible()))
    {
      return;
    }writeC(22);
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
    writeC(_isSummoned ? 2 : 0);
    writeS(_name);
    writeS(_title);
    writeD(0);
    writeD(0);
    writeD(0);

    writeD(_activeChar.getAbnormalEffect());
    if (Config.SHOW_NPC_CREST)
    {
      writeD(_clanId);
      writeD(_clanCrest);
      writeD(_allyId);
      writeD(_allyCrest);
      writeC(0);
    }
    else
    {
      writeD(0);
      writeD(0);
      writeD(0);
      writeD(0);
      writeC(0);
    }
    if ((Config.CHAMPION_AURA) && (_activeChar.isChampion()))
      writeC(2);
    else
      writeC(0);
    writeF(_collisionRadius);
    writeF(_collisionHeight);
    writeD(0);
    writeD(0);
  }

  public String getType()
  {
    return "[S] 16 NpcInfo";
  }
}