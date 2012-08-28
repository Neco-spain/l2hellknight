package org.mmocore.network;

import java.nio.ByteBuffer;

public abstract interface IPacketHandler<T extends MMOClient>
{
  public abstract ReceivablePacket<T> handlePacket(ByteBuffer paramByteBuffer, T paramT);
}