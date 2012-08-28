package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;

public class PartySpelled extends L2GameServerPacket
{
  private static final String _S__EE_PartySpelled = "[S] EE PartySpelled";
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
    writeD((_activeChar instanceof L2PetInstance) ? 1 : (_activeChar instanceof L2SummonInstance) ? 2 : 0);
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

  public String getType()
  {
    return "[S] EE PartySpelled";
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