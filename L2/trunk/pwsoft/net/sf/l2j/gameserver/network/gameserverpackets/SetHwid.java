package net.sf.l2j.gameserver.network.gameserverpackets;

import java.io.IOException;

public class SetHwid extends GameServerBasePacket
{
  public SetHwid(String player, String hwid)
  {
    writeC(171);
    writeS(player);
    writeS(hwid);
  }

  public byte[] getContent()
    throws IOException
  {
    return getBytes();
  }
}