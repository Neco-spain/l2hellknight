package net.sf.l2j.gameserver.gameserverpackets;

import java.io.IOException;

public class AuthRequest extends GameServerBasePacket
{
  public AuthRequest(int id, boolean acceptAlternate, byte[] hexid, String externalHost, String internalHost, int port, boolean reserveHost, int maxplayer)
  {
    writeC(1);
    writeC(id);
    writeC(acceptAlternate ? 1 : 0);
    writeC(reserveHost ? 1 : 0);
    writeS(externalHost);
    writeS(internalHost);
    writeH(port);
    writeD(maxplayer);
    writeD(hexid.length);
    writeB(hexid);
  }

  public byte[] getContent()
    throws IOException
  {
    return getBytes();
  }
}