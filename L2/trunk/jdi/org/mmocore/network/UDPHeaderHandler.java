package org.mmocore.network;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public abstract class UDPHeaderHandler<T extends MMOClient> extends HeaderHandler<T, UDPHeaderHandler<T>>
{
  private final HeaderInfo<T> _headerInfoReturn = new HeaderInfo();

  public UDPHeaderHandler(UDPHeaderHandler<T> subHeaderHandler)
  {
    super(subHeaderHandler);
  }

  protected abstract HeaderInfo handleHeader(ByteBuffer paramByteBuffer);

  protected abstract void onUDPConnection(SelectorThread<T> paramSelectorThread, DatagramChannel paramDatagramChannel, SocketAddress paramSocketAddress, ByteBuffer paramByteBuffer);

  protected final HeaderInfo<T> getHeaderInfoReturn()
  {
    return _headerInfoReturn;
  }
}