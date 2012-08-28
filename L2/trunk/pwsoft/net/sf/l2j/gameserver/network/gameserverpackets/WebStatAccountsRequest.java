package net.sf.l2j.gameserver.network.gameserverpackets;

import java.io.IOException;

public class WebStatAccountsRequest extends GameServerBasePacket
{
  public WebStatAccountsRequest()
  {
    writeC(190);
  }

  public byte[] getContent()
    throws IOException
  {
    return getBytes();
  }
}