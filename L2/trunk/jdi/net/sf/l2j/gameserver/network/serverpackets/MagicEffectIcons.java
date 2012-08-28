package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import javolution.util.FastList;

public class MagicEffectIcons extends L2GameServerPacket
{
  private static final String _S__97_MAGICEFFECTICONS = "[S] 7f MagicEffectIcons";
  private List<Effect> _effects;

  public MagicEffectIcons()
  {
    _effects = new FastList();
  }

  public void addEffect(int skillId, int level, int duration)
  {
    _effects.add(new Effect(skillId, level, duration));
  }

  protected final void writeImpl()
  {
    writeC(127);

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

  public String getType()
  {
    return "[S] 7f MagicEffectIcons";
  }

  private class Effect
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