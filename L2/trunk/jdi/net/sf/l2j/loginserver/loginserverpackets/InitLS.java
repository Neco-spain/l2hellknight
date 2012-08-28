package net.sf.l2j.loginserver.loginserverpackets;

import net.sf.l2j.loginserver.serverpackets.ServerBasePacket;

public class InitLS extends ServerBasePacket
{
  public InitLS(byte[] publickey)
  {
    writeC(0);
    writeD(258);
    writeD(publickey.length);
    writeB(publickey);
  }

  public byte[] getContent()
  {
    return getBytes();
  }
}