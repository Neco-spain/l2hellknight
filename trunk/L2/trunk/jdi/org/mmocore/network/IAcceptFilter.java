package org.mmocore.network;

import java.nio.channels.SocketChannel;

public abstract interface IAcceptFilter
{
  public abstract boolean accept(SocketChannel paramSocketChannel);
}