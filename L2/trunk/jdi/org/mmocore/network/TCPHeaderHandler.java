package org.mmocore.network;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public abstract class TCPHeaderHandler<T extends MMOClient> extends HeaderHandler<T, TCPHeaderHandler<T>>
{
  private final HeaderInfo<T> _headerInfoReturn = new HeaderInfo();

  public TCPHeaderHandler(TCPHeaderHandler<T> subHeaderHandler)
  {
    super(subHeaderHandler);
  }

  public abstract HeaderInfo handleHeader(SelectionKey paramSelectionKey, ByteBuffer paramByteBuffer);

  protected final HeaderInfo<T> getHeaderInfoReturn()
  {
    return _headerInfoReturn;
  }
}