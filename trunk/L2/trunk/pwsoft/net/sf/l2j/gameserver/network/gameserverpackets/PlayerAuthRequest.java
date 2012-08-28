package net.sf.l2j.gameserver.network.gameserverpackets;

import java.io.IOException;
import net.sf.l2j.gameserver.LoginServerThread.SessionKey;

public class PlayerAuthRequest extends GameServerBasePacket
{
  public PlayerAuthRequest(String account, LoginServerThread.SessionKey key)
  {
    writeC(173);
    writeS(account);
    writeD(key.playOkID1);
    writeD(key.playOkID2);
    writeD(key.loginOkID1);
    writeD(key.loginOkID2);
  }

  public byte[] getContent()
    throws IOException
  {
    return getBytes();
  }
}