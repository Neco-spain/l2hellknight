package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Creature;

public class MagicSkillUse extends L2GameServerPacket
{
  private int _targetId;
  private int _skillId;
  private int _skillLevel;
  private int _hitTime;
  private int _reuseDelay;
  private int _chaId;
  private int _x;
  private int _y;
  private int _z;
  private int _tx;
  private int _ty;
  private int _tz;

  public MagicSkillUse(Creature cha, Creature target, int skillId, int skillLevel, int hitTime, long reuseDelay)
  {
    _chaId = cha.getObjectId();
    _targetId = target.getObjectId();
    _skillId = skillId;
    _skillLevel = skillLevel;
    _hitTime = hitTime;
    _reuseDelay = (int)reuseDelay;
    _x = cha.getX();
    _y = cha.getY();
    _z = cha.getZ();
    _tx = target.getX();
    _ty = target.getY();
    _tz = target.getZ();
  }

  public MagicSkillUse(Creature cha, int skillId, int skillLevel, int hitTime, long reuseDelay)
  {
    _chaId = cha.getObjectId();
    _targetId = cha.getTargetId();
    _skillId = skillId;
    _skillLevel = skillLevel;
    _hitTime = hitTime;
    _reuseDelay = (int)reuseDelay;
    _x = cha.getX();
    _y = cha.getY();
    _z = cha.getZ();
    _tx = cha.getX();
    _ty = cha.getY();
    _tz = cha.getZ();
  }

  protected final void writeImpl()
  {
    writeC(72);
    writeD(_chaId);
    writeD(_targetId);
    writeD(_skillId);
    writeD(_skillLevel);
    writeD(_hitTime);
    writeD(_reuseDelay);
    writeD(_x);
    writeD(_y);
    writeD(_z);
    writeD(0);
    writeD(_tx);
    writeD(_ty);
    writeD(_tz);
  }
}