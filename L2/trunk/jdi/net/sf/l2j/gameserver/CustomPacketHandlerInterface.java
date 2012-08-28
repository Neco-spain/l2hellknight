package net.sf.l2j.gameserver;

import java.nio.ByteBuffer;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.clientpackets.L2GameClientPacket;

public abstract interface CustomPacketHandlerInterface
{
  public abstract L2GameClientPacket handlePacket(ByteBuffer paramByteBuffer, L2GameClient paramL2GameClient);
}