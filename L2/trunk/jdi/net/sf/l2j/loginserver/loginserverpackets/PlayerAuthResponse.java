package net.sf.l2j.loginserver.loginserverpackets;

import java.io.IOException;
import net.sf.l2j.loginserver.serverpackets.ServerBasePacket;

public class PlayerAuthResponse extends ServerBasePacket
{
  public PlayerAuthResponse(String account, boolean response)
  {
    writeC(3);
    writeS(account);
    writeC(response ? 1 : 0);
  }

  public byte[] getContent()
    throws IOException
  {
    return getBytes();
  }
}