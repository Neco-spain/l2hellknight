package net.sf.l2j.loginserver.serverpackets;

import net.sf.l2j.loginserver.L2LoginClient;
import org.mmocore.network.SendablePacket;

public abstract class L2LoginServerPacket extends SendablePacket<L2LoginClient>
{
  protected int getHeaderSize()
  {
    return 2;
  }

  protected void writeHeader(int dataSize)
  {
    writeH(dataSize + getHeaderSize());
  }
}