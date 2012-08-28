package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.skills.Env;

public final class LambdaCalc extends Lambda
{
  public Func[] funcs;

  public LambdaCalc()
  {
    funcs = new Func[0];
  }

  public double calc(Env env) {
    double saveValue = env.value;
    try
    {
      env.value = 0.0D;
      for (Func f : funcs)
        f.calc(env);
      ??? = env.value;
      return ???; } finally { env.value = saveValue; } throw localObject;
  }

  public void addFunc(Func f)
  {
    int len = funcs.length;
    Func[] tmp = new Func[len + 1];
    for (int i = 0; i < len; i++)
      tmp[i] = funcs[i];
    tmp[len] = f;
    funcs = tmp;
  }
}