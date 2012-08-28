package net.sf.l2j.gameserver.network.clientpackets;

import java.io.PrintStream;

public final class RequestExMPCCShowPartyMembersInfo extends L2GameClientPacket
{
  private static final String _C__D0_26_REQUESTMPCCSHOWPARTYMEMBERINFO = "[C] D0:26 RequestExMPCCShowPartyMembersInfo";
  private int _unk;

  protected void readImpl()
  {
    _unk = readD();
  }

  protected void runImpl()
  {
    System.out.println("C6: RequestExMPCCShowPartyMembersInfo. unk: " + _unk);
  }

  public String getType()
  {
    return "[C] D0:26 RequestExMPCCShowPartyMembersInfo";
  }
}