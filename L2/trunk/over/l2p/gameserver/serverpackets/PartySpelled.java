package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.Playable;
import l2p.gameserver.utils.EffectsComparator;

public class PartySpelled extends L2GameServerPacket
{
  private final int _type;
  private final int _objId;
  private final List<Effect> _effects;

  public PartySpelled(Playable activeChar, boolean full)
  {
    _objId = activeChar.getObjectId();
    _type = (activeChar.isSummon() ? 2 : activeChar.isPet() ? 1 : 0);

    _effects = new ArrayList();
    if (full)
    {
      Effect[] effects = activeChar.getEffectList().getAllFirstEffects();
      Arrays.sort(effects, EffectsComparator.getInstance());
      for (Effect effect : effects)
        if ((effect != null) && (effect.isInUse()))
          effect.addPartySpelledIcon(this);
    }
  }

  protected final void writeImpl()
  {
    writeC(244);
    writeD(_type);
    writeD(_objId);
    writeD(_effects.size());
    for (Effect temp : _effects)
    {
      writeD(temp._skillId);
      writeH(temp._level);
      writeD(temp._duration);
    }
  }

  public void addPartySpelledEffect(int skillId, int level, int duration)
  {
    _effects.add(new Effect(skillId, level, duration));
  }
  static class Effect {
    final int _skillId;
    final int _level;
    final int _duration;

    public Effect(int skillId, int level, int duration) {
      _skillId = skillId;
      _level = level;
      _duration = duration;
    }
  }
}