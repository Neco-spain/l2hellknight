package net.sf.l2j.gameserver.network.gameserverpackets;

import java.io.IOException;

public class SetNewEmail extends GameServerBasePacket
{
  public SetNewEmail(String player, String email)
  {
    writeC(191);
    writeS(player);
    writeS(email);
  }

  public byte[] getContent()
    throws IOException
  {
    return getBytes();
  }
}