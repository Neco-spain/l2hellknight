package net.sf.l2j.gameserver.gameserverpackets;

import java.io.IOException;

public class PlayerLogout extends GameServerBasePacket
{
  public PlayerLogout(String player)
  {
    writeC(3);
    writeS(player);
  }

  public byte[] getContent()
    throws IOException
  {
    return getBytes();
  }
}