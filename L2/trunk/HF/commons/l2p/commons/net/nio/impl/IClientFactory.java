package l2m.commons.net.nio.impl;

public abstract interface IClientFactory<T extends MMOClient>
{
  public abstract T create(MMOConnection<T> paramMMOConnection);
}