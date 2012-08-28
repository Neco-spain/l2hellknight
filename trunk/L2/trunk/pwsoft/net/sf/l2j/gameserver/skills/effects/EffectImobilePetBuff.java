package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.skills.Env;

final class EffectImobilePetBuff extends L2Effect
{
  private L2Summon _pet;

  public EffectImobilePetBuff(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.BUFF;
  }

  public void onStart()
  {
    _pet = null;

    if ((getEffected().isL2Summon()) && (getEffector().isPlayer()) && (((L2Summon)getEffected()).getOwner() == getEffector()))
    {
      _pet = ((L2Summon)getEffected());
      _pet.setIsImobilised(true);
    }
  }

  public void onExit()
  {
    if (_pet != null)
      _pet.setIsImobilised(false);
  }

  public boolean onActionTime()
  {
    return false;
  }
}