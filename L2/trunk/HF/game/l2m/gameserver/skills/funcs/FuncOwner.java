package l2m.gameserver.skills.funcs;

public abstract interface FuncOwner
{
  public abstract boolean isFuncEnabled();

  public abstract boolean overrideLimits();
}