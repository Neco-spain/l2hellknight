package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import javolution.util.FastList;

public class AbnormalStatusUpdate extends L2GameServerPacket
{
  private List<Effect> _effects;

  public AbnormalStatusUpdate()
  {
    _effects = new FastList();
  }

  public void addEffect(int skillId, int level, int duration)
  {
    if ((skillId == 2031) || (skillId == 2032) || (skillId == 2037))
      return;
    _effects.add(new Effect(skillId, level, duration));
  }

  protected final void writeImpl()
  {
    writeC(133);

    writeH(_effects.size());

    for (Effect temp : _effects)
    {
      writeD(temp._skillId);
      writeH(temp._level);

      if (temp._duration == -1)
        writeD(-1);
      else
        writeD(temp._duration / 1000);
    }
  }

  public void gc()
  {
    _effects.clear();
    _effects = null;
  }

  private static class Effect
  {
    protected int _skillId;
    protected int _level;
    protected int _duration;

    public Effect(int pSkillId, int pLevel, int pDuration)
    {
      _skillId = pSkillId;
      _level = pLevel;
      _duration = pDuration;
    }
  }
}