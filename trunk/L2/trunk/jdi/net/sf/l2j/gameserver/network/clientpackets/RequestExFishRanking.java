package net.sf.l2j.gameserver.network.clientpackets;

import java.io.PrintStream;

public final class RequestExFishRanking extends L2GameClientPacket
{
  private static final String _C__D0_1F_REQUESTEXFISHRANKING = "[C] D0:1F RequestExFishRanking";

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    System.out.println("C5: RequestExFishRanking");
  }

  public String getType()
  {
    return "[C] D0:1F RequestExFishRanking";
  }
}