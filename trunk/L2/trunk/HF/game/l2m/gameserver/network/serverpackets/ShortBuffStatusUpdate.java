package l2m.gameserver.serverpackets;

import l2m.gameserver.model.Effect;
import l2m.gameserver.model.Skill;

public class ShortBuffStatusUpdate extends L2GameServerPacket
{
  int _skillId;
  int _skillLevel;
  int _skillDuration;

  public ShortBuffStatusUpdate(Effect effect)
  {
    _skillId = effect.getSkill().getDisplayId();
    _skillLevel = effect.getSkill().getDisplayLevel();
    _skillDuration = effect.getTimeLeft();
  }

  public ShortBuffStatusUpdate()
  {
    _skillId = 0;
    _skillLevel = 0;
    _skillDuration = 0;
  }

  protected final void writeImpl()
  {
    writeC(250);
    writeD(_skillId);
    writeD(_skillLevel);
    writeD(_skillDuration);
  }
}