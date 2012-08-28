package l2m.commons.net.nio.impl;

public abstract interface IMMOExecutor<T extends MMOClient>
{
  public abstract void execute(Runnable paramRunnable);
}