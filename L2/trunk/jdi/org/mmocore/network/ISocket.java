package org.mmocore.network;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public abstract interface ISocket
{
  public abstract void close()
    throws IOException;

  public abstract WritableByteChannel getWritableByteChannel();

  public abstract ReadableByteChannel getReadableByteChannel();

  public abstract InetAddress getInetAddress();

  public abstract int getPort();
}