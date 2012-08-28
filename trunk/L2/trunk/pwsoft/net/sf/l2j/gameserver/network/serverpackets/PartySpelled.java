package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Character;

public class PartySpelled extends L2GameServerPacket
{
  private List<Effect> _effects;
  private L2Character _activeChar;

  public PartySpelled(L2Character cha)
  {
    _effects = new FastList();
    _activeChar = cha;
  }

  protected final void writeImpl()
  {
    if (_activeChar == null) return;
    writeC(238);
    writeD(_activeChar.isPet() ? 1 : _activeChar.isSummon() ? 2 : 0);
    writeD(_activeChar.getObjectId());
    writeD(_effects.size());
    for (Effect temp : _effects)
    {
      writeD(temp._skillId);
      writeH(temp._dat);
      writeD(temp._duration / 1000);
    }
  }

  public void addPartySpelledEffect(int skillId, int dat, int duration)
  {
    _effects.add(new Effect(skillId, dat, duration));
  }

  public void gcb()
  {
    _effects.clear();
  }

  private static class Effect
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