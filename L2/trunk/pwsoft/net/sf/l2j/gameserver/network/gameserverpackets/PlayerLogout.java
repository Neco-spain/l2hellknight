package net.sf.l2j.gameserver.network.gameserverpackets;

import java.io.IOException;

public class PlayerLogout extends GameServerBasePacket
{
  public PlayerLogout(String player)
  {
    writeC(187);
    writeS(player);
  }

  public byte[] getContent()
    throws IOException
  {
    return getBytes();
  }
}