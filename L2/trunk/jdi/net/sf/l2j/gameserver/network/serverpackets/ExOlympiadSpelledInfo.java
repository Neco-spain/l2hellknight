package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ExOlympiadSpelledInfo extends L2GameServerPacket
{
  private static final String _S__FE_2A_OLYMPIADSPELLEDINFO = "[S] FE:2A ExOlympiadSpelledInfo";
  private L2PcInstance _player;
  private List<Effect> _effects;

  public ExOlympiadSpelledInfo(L2PcInstance player)
  {
    _effects = new FastList();
    _player = player;
  }

  public void addEffect(int skillId, int dat, int duration)
  {
    _effects.add(new Effect(skillId, dat, duration));
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
      writeH(temp._dat);
      writeD(temp._duration / 1000);
    }
  }

  public String getType()
  {
    return "[S] FE:2A ExOlympiadSpelledInfo";
  }

  private class Effect
  {
    protected int _skillId;
    protected int _dat;
    protected int _duration;

    public Effect(int pSkillId, int pDat, int pDuration)
    {
      _skillId = pSkillId;
      _dat = pDat;
      _duration = pDuration;
    }
  }
}