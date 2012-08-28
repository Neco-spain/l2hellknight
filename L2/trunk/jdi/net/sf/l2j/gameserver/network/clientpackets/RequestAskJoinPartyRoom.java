package net.sf.l2j.gameserver.network.clientpackets;

import java.io.PrintStream;

public class RequestAskJoinPartyRoom extends L2GameClientPacket
{
  private static final String _C__D0_14_REQUESTASKJOINPARTYROOM = "[C] D0:14 RequestAskJoinPartyRoom";
  private String _player;

  protected void readImpl()
  {
    _player = readS();
  }

  protected void runImpl()
  {
    System.out.println("C5:RequestAskJoinPartyRoom: S: " + _player);
  }

  public String getType()
  {
    return "[C] D0:14 RequestAskJoinPartyRoom";
  }
}