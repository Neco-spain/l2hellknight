package net.sf.l2j.gameserver.gameserverpackets;

import java.io.IOException;
import javolution.util.FastList;

public class PlayerInGame extends GameServerBasePacket
{
  public PlayerInGame(String player)
  {
    writeC(2);
    writeH(1);
    writeS(player);
  }

  public PlayerInGame(FastList<String> players)
  {
    writeC(2);
    writeH(players.size());
    for (String pc : players)
      writeS(pc);
  }

  public byte[] getContent()
    throws IOException
  {
    return getBytes();
  }
}