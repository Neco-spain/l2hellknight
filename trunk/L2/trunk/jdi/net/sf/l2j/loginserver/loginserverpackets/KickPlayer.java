package net.sf.l2j.loginserver.loginserverpackets;

import java.io.IOException;
import net.sf.l2j.loginserver.serverpackets.ServerBasePacket;

public class KickPlayer extends ServerBasePacket
{
  public KickPlayer(String account)
  {
    writeC(4);
    writeS(account);
  }

  public byte[] getContent()
    throws IOException
  {
    return getBytes();
  }
}