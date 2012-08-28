package net.sf.l2j.gameserver.network.gameserverpackets;

import java.io.IOException;

public class SetNewPassword extends GameServerBasePacket
{
  public SetNewPassword(String player, String pwd)
  {
    writeC(172);
    writeS(player);
    writeS(pwd);
  }

  public byte[] getContent()
    throws IOException
  {
    return getBytes();
  }
}