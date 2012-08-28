package net.sf.l2j.gameserver.skills;

import javolution.util.FastList;
import net.sf.l2j.gameserver.skills.funcs.Func;

public final class Calculator
{
  private static final Func[] _emptyFuncs = new Func[0];
  private volatile Func[] _functions;

  public Calculator()
  {
    _functions = _emptyFuncs;
  }

  public Calculator(Calculator c)
  {
    _functions = c._functions;
  }

  public static boolean equalsCals(Calculator c1, Calculator c2)
  {
    if (c1 == c2) {
      return true;
    }
    if ((c1 == null) || (c2 == null)) {
      return false;
    }
    Func[] funcs1 = _functions;
    Func[] funcs2 = c2._functions;

    if (funcs1 == funcs2) {
      return true;
    }
    if (funcs1.length != funcs2.length) {
      return false;
    }
    if (funcs1.length == 0) {
      return true;
    }
    for (int i = 0; i < funcs1.length; i++)
    {
      if (funcs1[i] != funcs2[i])
        return false;
    }
    return true;
  }

  public int size()
  {
    return _functions.length;
  }

  public synchronized void addFunc(Func f)
  {
    Func[] funcs = _functions;
    Func[] tmp = new Func[funcs.length + 1];

    int order = f.order;

    for (int i = 0; (i < funcs.length) && (order >= funcs[i].order); i++) {
      tmp[i] = funcs[i];
    }
    tmp[i] = f;

    for (; i < funcs.length; i++) {
      tmp[(i + 1)] = funcs[i];
    }
    _functions = tmp;
  }

  public synchronized void removeFunc(Func f)
  {
    Func[] funcs = _functions;
    Func[] tmp = new Func[funcs.length - 1];

    for (int i = 0; (i < funcs.length) && (f != funcs[i]); i++) {
      tmp[i] = funcs[i];
    }
    if (i == funcs.length) {
      return;
    }
    for (i++; i < funcs.length; i++) {
      tmp[(i - 1)] = funcs[i];
    }
    if (tmp.length == 0)
      _functions = _emptyFuncs;
    else
      _functions = tmp;
  }

  public synchronized FastList<Stats> removeOwner(Object owner)
  {
    Func[] funcs = _functions;
    FastList modifiedStats = new FastList();

    for (int i = 0; i < funcs.length; i++)
    {
      if (funcs[i].funcOwner != owner)
        continue;
      modifiedStats.add(funcs[i].stat);
      removeFunc(funcs[i]);
    }

    return modifiedStats;
  }

  public void calc(Env env)
  {
    Func[] funcs = _functions;

    for (int i = 0; i < funcs.length; i++)
      funcs[i].calc(env);
  }
}