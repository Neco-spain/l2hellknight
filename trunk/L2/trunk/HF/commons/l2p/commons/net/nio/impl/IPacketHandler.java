package l2m.commons.net.nio.impl;

import java.nio.ByteBuffer;

public abstract interface IPacketHandler<T extends MMOClient>
{
  public abstract ReceivablePacket<T> handlePacket(ByteBuffer paramByteBuffer, T paramT);
}