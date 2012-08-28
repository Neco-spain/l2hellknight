package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ExOlympiadSpelledInfo extends L2GameServerPacket
{
  private L2PcInstance _player;
  private List<Effect> _effects;

  public ExOlympiadSpelledInfo(L2PcInstance player)
  {
    _effects = new FastList();
    _player = player;
  }

  public void addEffect(int skillId, int level, int duration)
  {
    _effects.add(new Effect(skillId, level, duration));
  }

  protected final void writeImpl()
  {
    if (_player == null)
      return;
    writeC(254);
    writeH(42);
    writeD(_player.getObjectId());
    writeD(_effects.size());
    for (Effect temp : _effects)
    {
      writeD(temp._skillId);
      writeH(temp._level);
      writeD(temp._duration / 1000);
    }
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