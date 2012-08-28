package net.sf.l2j.gameserver.network.gameserverpackets;

import java.io.IOException;

public class AuthRequest extends GameServerBasePacket
{
  public AuthRequest(int id, boolean acceptAlternate, byte[] hexid, String externalHost, String internalHost, int port, boolean reserveHost, int maxplayer, String key)
  {
    writeC(175);
    writeC(id);
    writeC(acceptAlternate ? 1 : 0);
    writeC(reserveHost ? 1 : 0);
    writeH(port);
    writeD(maxplayer);
    writeS(key);
    writeD(hexid.length);
    writeB(hexid);
    writeS(externalHost);
    writeS(internalHost);
  }

  public byte[] getContent()
    throws IOException
  {
    return getBytes();
  }
}