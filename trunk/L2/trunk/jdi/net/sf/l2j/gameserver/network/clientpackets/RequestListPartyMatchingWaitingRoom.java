package net.sf.l2j.gameserver.network.clientpackets;

import java.io.PrintStream;

public class RequestListPartyMatchingWaitingRoom extends L2GameClientPacket
{
  private static final String _C__D0_16_REQUESTLISTPARTYMATCHINGWAITINGROOM = "[C] D0:16 RequestListPartyMatchingWaitingRoom";

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    System.out.println("C5: RequestListPartyMatchingWaitingRoom");
  }

  public String getType()
  {
    return "[C] D0:16 RequestListPartyMatchingWaitingRoom";
  }
}