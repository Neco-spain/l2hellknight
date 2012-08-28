package net.sf.l2j.gameserver.gameserverpackets;

import java.io.IOException;

public class ChangeAccessLevel extends GameServerBasePacket
{
  public ChangeAccessLevel(String player, int access)
  {
    writeC(4);
    writeD(access);
    writeS(player);
  }

  public byte[] getContent()
    throws IOException
  {
    return getBytes();
  }
}