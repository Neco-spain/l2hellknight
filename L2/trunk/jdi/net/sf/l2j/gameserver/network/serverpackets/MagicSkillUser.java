package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;

public class MagicSkillUser extends L2GameServerPacket
{
  private static final String _S__5A_MAGICSKILLUSER = "[S] 5A MagicSkillUser";
  private int _targetId;
  private int _skillId;
  private int _skillLevel;
  private int _hitTime;
  private int _reuseDelay;
  private int _charObjId;
  private int _x;
  private int _y;
  private int _z;

  public MagicSkillUser(L2Character cha, L2Character target, int skillId, int skillLevel, int hitTime, int reuseDelay)
  {
    _charObjId = cha.getObjectId();
    _targetId = target.getObjectId();
    _skillId = skillId;
    _skillLevel = skillLevel;
    _hitTime = hitTime;
    _reuseDelay = reuseDelay;
    _x = cha.getX();
    _y = cha.getY();
    _z = cha.getZ();
  }

  public MagicSkillUser(L2Character cha, int skillId, int skillLevel, int hitTime, int reuseDelay)
  {
    _charObjId = cha.getObjectId();
    _targetId = cha.getTargetId();
    _skillId = skillId;
    _skillLevel = skillLevel;
    _hitTime = hitTime;
    _reuseDelay = reuseDelay;
    _x = cha.getX();
    _y = cha.getY();
    _z = cha.getZ();
  }

  protected final void writeImpl()
  {
    writeC(72);
    writeD(_charObjId);
    writeD(_targetId);
    writeD(_skillId);
    writeD(_skillLevel);
    writeD(_hitTime);
    writeD(_reuseDelay);
    writeD(_x);
    writeD(_y);
    writeD(_z);
    if (_skillId == 16) writeH(32);
    else if (_skillId == 30) writeH(32);
    else if (_skillId == 263) writeH(32);
    else if (_skillId == 321) writeH(32);
    else if (_skillId == 344) writeH(32);
    else if (_skillId == 409) writeH(32);
    else
      writeH(0);
  }

  public String getType()
  {
    return "[S] 5A MagicSkillUser";
  }
}