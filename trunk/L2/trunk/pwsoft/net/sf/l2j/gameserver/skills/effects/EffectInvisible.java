package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

public class EffectInvisible extends L2Effect
{
  public EffectInvisible(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.BUFF;
  }

  public void onStart()
  {
    getEffected().setChannel(0);
    getEffected().teleToLocation(getEffected().getX(), getEffected().getY(), getEffected().getZ());
  }

  public boolean onActionTime()
  {
    getEffected().setChannel(1);
    getEffected().teleToLocation(getEffected().getX(), getEffected().getY(), getEffected().getZ());
    return false;
  }

  public void onExit()
  {
    getEffected().setChannel(1);
    getEffected().teleToLocation(getEffected().getX(), getEffected().getY(), getEffected().getZ());
  }
}