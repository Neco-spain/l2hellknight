package org.mmocore.network;

public abstract interface IClientFactory<T extends MMOClient>
{
  public abstract T create(MMOConnection<T> paramMMOConnection);
}