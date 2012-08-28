package net.sf.l2j.loginserver.loginserverpackets;

import java.io.IOException;
import net.sf.l2j.loginserver.GameServerTable;
import net.sf.l2j.loginserver.serverpackets.ServerBasePacket;

public class AuthResponse extends ServerBasePacket
{
  public AuthResponse(int serverId)
  {
    writeC(2);
    writeC(serverId);
    writeS(GameServerTable.getInstance().getServerNameById(serverId));
  }

  public byte[] getContent()
    throws IOException
  {
    return getBytes();
  }
}