package net.sf.l2j.gameserver.skills.effects;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.conditions.Condition;
import net.sf.l2j.gameserver.skills.funcs.FuncTemplate;
import net.sf.l2j.gameserver.skills.funcs.Lambda;
import net.sf.l2j.util.log.AbstractLogger;

public final class EffectTemplate
{
  static Logger _log = AbstractLogger.getLogger(EffectTemplate.class.getName());
  private final Class<?> _func;
  private final Constructor<?> _constructor;
  public final Condition attachCond;
  public final Condition applayCond;
  public final Lambda lambda;
  public final int counter;
  public int period;
  public final int abnormalEffect;
  public FuncTemplate[] funcTemplates;
  public final String stackType;
  public final float stackOrder;

  public EffectTemplate(Condition pAttachCond, Condition pApplayCond, String func, Lambda pLambda, int pCounter, int pPeriod, int pAbnormalEffect, String pStackType, float pStackOrder)
  {
    attachCond = pAttachCond;
    applayCond = pApplayCond;
    lambda = pLambda;
    counter = pCounter;
    period = pPeriod;
    abnormalEffect = pAbnormalEffect;
    stackType = pStackType;
    stackOrder = pStackOrder;
    try {
      _func = Class.forName("net.sf.l2j.gameserver.skills.effects.Effect" + func);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    try {
      _constructor = _func.getConstructor(new Class[] { Env.class, EffectTemplate.class });
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  public L2Effect getEffect(Env env)
  {
    if ((attachCond != null) && (!attachCond.test(env)))
      return null;
    try {
      L2Effect effect = (L2Effect)_constructor.newInstance(new Object[] { env, this });

      return effect;
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      return null;
    } catch (InstantiationException e) {
      e.printStackTrace();
      return null;
    }
    catch (InvocationTargetException e) {
    }
    return null;
  }

  public void attach(FuncTemplate f)
  {
    if (funcTemplates == null)
    {
      funcTemplates = new FuncTemplate[] { f };
    }
    else
    {
      int len = funcTemplates.length;
      FuncTemplate[] tmp = new FuncTemplate[len + 1];
      System.arraycopy(funcTemplates, 0, tmp, 0, len);
      tmp[len] = f;
      funcTemplates = tmp;
    }
  }
}