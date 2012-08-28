package net.sf.l2j.gameserver.skills.effects;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.util.log.AbstractLogger;

public class EffectImmobileUntilAttacked extends L2Effect
{
  static final Logger _log = AbstractLogger.getLogger(EffectImmobileUntilAttacked.class.getName());

  public EffectImmobileUntilAttacked(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.IMMOBILEUNTILATTACKED;
  }

  public void onStart()
  {
    getEffector().startImmobileUntilAttacked();
  }

  public void onExit()
  {
    getEffected().stopImmobileUntilAttacked(this);
  }

  public boolean onActionTime()
  {
    return false;
  }
}