package net.sf.l2j.gameserver.network.gameserverpackets;

import java.io.IOException;

public class SetLastHwid extends GameServerBasePacket
{
  public SetLastHwid(String player, String hwid)
  {
    writeC(170);
    writeS(player);
    writeS(hwid);
  }

  public byte[] getContent()
    throws IOException
  {
    return getBytes();
  }
}