package l2m.commons.net.nio.impl;

import java.nio.channels.SocketChannel;

public abstract interface IAcceptFilter
{
  public abstract boolean accept(SocketChannel paramSocketChannel);
}