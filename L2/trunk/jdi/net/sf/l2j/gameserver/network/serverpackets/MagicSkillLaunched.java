package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;

public class MagicSkillLaunched extends L2GameServerPacket
{
  private static final String _S__8E_MAGICSKILLLAUNCHED = "[S] 8E MagicSkillLaunched";
  private int _charObjId;
  private int _skillId;
  private int _skillLevel;
  private int _numberOfTargets;
  private L2Object[] _targets;
  private int _singleTargetId;

  public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel, L2Object[] targets)
  {
    _charObjId = cha.getObjectId();
    _skillId = skillId;
    _skillLevel = skillLevel;
    _numberOfTargets = targets.length;
    _targets = targets;
    _singleTargetId = 0;
  }

  public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel)
  {
    _charObjId = cha.getObjectId();
    _skillId = skillId;
    _skillLevel = skillLevel;
    _numberOfTargets = 1;
    _singleTargetId = cha.getTargetId();
  }

  protected final void writeImpl()
  {
    writeC(118);
    writeD(_charObjId);
    writeD(_skillId);
    writeD(_skillLevel);
    writeD(_numberOfTargets);
    if ((_singleTargetId != 0) || (_numberOfTargets == 0))
    {
      writeD(_singleTargetId);
    }
    else {
      L2Object[] arr$ = _targets;
      int len$ = arr$.length;
      for (int i$ = 0; i$ < len$; i$++)
      {
        L2Object target = arr$[i$];
        try
        {
          writeD(target.getObjectId());
        }
        catch (NullPointerException e)
        {
          writeD(0);
        }
      }
    }
  }

  public String getType()
  {
    return "[S] 8E MagicSkillLaunched";
  }
}