package org.mmocore.network;

public abstract interface IMMOExecutor<T extends MMOClient>
{
  public abstract void execute(ReceivablePacket<T> paramReceivablePacket);
}