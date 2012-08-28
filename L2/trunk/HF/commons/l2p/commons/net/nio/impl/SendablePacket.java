package l2m.commons.net.nio.impl;

import java.nio.ByteBuffer;

public abstract class SendablePacket<T extends MMOClient> extends l2m.commons.net.nio.SendablePacket<T>
{
  protected ByteBuffer getByteBuffer()
  {
    return ((SelectorThread)Thread.currentThread()).getWriteBuffer();
  }

  public T getClient()
  {
    return ((SelectorThread)Thread.currentThread()).getWriteClient();
  }

  protected abstract boolean write();
}