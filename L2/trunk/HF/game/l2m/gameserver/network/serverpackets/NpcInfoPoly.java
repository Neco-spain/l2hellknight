package l2m.gameserver.serverpackets;

import l2m.gameserver.data.xml.holder.NpcHolder;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.TeamType;
import l2m.gameserver.templates.npc.NpcTemplate;

public class NpcInfoPoly extends L2GameServerPacket
{
  private Creature _obj;
  private int _x;
  private int _y;
  private int _z;
  private int _heading;
  private int _npcId;
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
  private int _abnormalEffect2;
  private double colRadius;
  private double colHeight;
  private TeamType _team;

  public NpcInfoPoly(Player cha)
  {
    _obj = cha;
    _npcId = cha.getPolyId();
    NpcTemplate template = NpcHolder.getInstance().getTemplate(_npcId);
    _rhand = 0;
    _lhand = 0;
    _isSummoned = false;
    colRadius = template.collisionRadius;
    colHeight = template.collisionHeight;
    _x = _obj.getX();
    _y = _obj.getY();
    _z = _obj.getZ();
    _rhand = template.rhand;
    _lhand = template.lhand;
    _heading = cha.getHeading();
    _mAtkSpd = cha.getMAtkSpd();
    _pAtkSpd = cha.getPAtkSpd();
    _runSpd = cha.getRunSpeed();
    _walkSpd = cha.getWalkSpeed();
    _swimRunSpd = (this._flRunSpd = this._flyRunSpd = _runSpd);
    _swimWalkSpd = (this._flWalkSpd = this._flyWalkSpd = _walkSpd);
    _isRunning = cha.isRunning();
    _isInCombat = cha.isInCombat();
    _isAlikeDead = cha.isAlikeDead();
    _name = cha.getName();
    _title = cha.getTitle();
    _abnormalEffect = cha.getAbnormalEffect();
    _abnormalEffect2 = cha.getAbnormalEffect2();
    _team = cha.getTeam();
  }

  protected final void writeImpl()
  {
    writeC(12);
    writeD(_obj.getObjectId());
    writeD(_npcId + 1000000);
    writeD(0);
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
    writeF(colRadius);
    writeF(colHeight);
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

    writeD(_abnormalEffect);

    writeD(0);
    writeD(0);
    writeD(0);
    writeD(0);
    writeC(0);
    writeC(_team.ordinal());
    writeF(colRadius);
    writeF(colHeight);
    writeD(0);
    writeD(0);
    writeD(0);
    writeD(0);

    writeC(0);
    writeC(0);
    writeD(_abnormalEffect2);
  }
}