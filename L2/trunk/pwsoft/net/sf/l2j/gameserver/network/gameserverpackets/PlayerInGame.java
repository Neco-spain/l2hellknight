package net.sf.l2j.gameserver.network.gameserverpackets;

import java.io.IOException;
import javolution.util.FastList;

public class PlayerInGame extends GameServerBasePacket
{
  public PlayerInGame(String player)
  {
    writeC(186);
    writeH(1);
    writeS(player);
  }

  public PlayerInGame(FastList<String> players)
  {
    writeC(186);
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