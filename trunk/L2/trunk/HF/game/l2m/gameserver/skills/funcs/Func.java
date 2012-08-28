package l2m.gameserver.skills.funcs;

import l2m.gameserver.skills.Env;
import l2m.gameserver.skills.Stats;
import l2m.gameserver.skills.conditions.Condition;

public abstract class Func
  implements Comparable<Func>
{
  public static final Func[] EMPTY_FUNC_ARRAY = new Func[0];
  public final Stats stat;
  public final int order;
  public final Object owner;
  public final double value;
  protected Condition cond;

  public Func(Stats stat, int order, Object owner)
  {
    this(stat, order, owner, 0.0D);
  }

  public Func(Stats stat, int order, Object owner, double value)
  {
    this.stat = stat;
    this.order = order;
    this.owner = owner;
    this.value = value;
  }

  public void setCondition(Condition cond)
  {
    this.cond = cond;
  }

  public Condition getCondition()
  {
    return cond;
  }

  public abstract void calc(Env paramEnv);

  public int compareTo(Func f) throws NullPointerException
  {
    return order - f.order;
  }
}