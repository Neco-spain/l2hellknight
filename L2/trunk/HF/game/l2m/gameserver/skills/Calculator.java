package l2m.gameserver.skills;

import l2p.commons.lang.ArrayUtils;
import l2m.gameserver.model.Creature;
import l2m.gameserver.skills.conditions.Condition;
import l2m.gameserver.skills.funcs.Func;
import l2m.gameserver.skills.funcs.FuncOwner;

public final class Calculator
{
  private Func[] _functions;
  private double _base;
  private double _last;
  public final Stats _stat;
  public final Creature _character;

  public Calculator(Stats stat, Creature character)
  {
    _stat = stat;
    _character = character;
    _functions = Func.EMPTY_FUNC_ARRAY;
  }

  public int size()
  {
    return _functions.length;
  }

  public void addFunc(Func f)
  {
    _functions = ((Func[])ArrayUtils.add(_functions, f));
    ArrayUtils.eqSort(_functions);
  }

  public void removeFunc(Func f)
  {
    _functions = ((Func[])ArrayUtils.remove(_functions, f));
    if (_functions.length == 0)
      _functions = Func.EMPTY_FUNC_ARRAY;
    else
      ArrayUtils.eqSort(_functions);
  }

  public void removeOwner(Object owner)
  {
    Func[] tmp = _functions;
    for (Func element : tmp)
      if (element.owner == owner)
        removeFunc(element);
  }

  public void calc(Env env)
  {
    Func[] funcs = _functions;
    _base = env.value;

    boolean overrideLimits = false;
    for (Func func : funcs)
    {
      if (func == null) {
        continue;
      }
      if ((func.owner instanceof FuncOwner))
      {
        if (!((FuncOwner)func.owner).isFuncEnabled())
          continue;
        if (((FuncOwner)func.owner).overrideLimits())
          overrideLimits = true;
      }
      if ((func.getCondition() == null) || (func.getCondition().test(env))) {
        func.calc(env);
      }
    }
    if (!overrideLimits) {
      env.value = _stat.validate(env.value);
    }
    if (env.value != _last)
    {
      double last = _last;
      _last = env.value;
    }
  }

  public Func[] getFunctions()
  {
    return _functions;
  }

  public double getBase()
  {
    return _base;
  }

  public double getLast()
  {
    return _last;
  }
}