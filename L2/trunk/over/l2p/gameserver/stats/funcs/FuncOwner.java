package l2p.gameserver.stats.funcs;

public abstract interface FuncOwner
{
  public abstract boolean isFuncEnabled();

  public abstract boolean overrideLimits();
}