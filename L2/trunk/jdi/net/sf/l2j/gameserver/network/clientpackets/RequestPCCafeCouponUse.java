package net.sf.l2j.gameserver.network.clientpackets;

import java.io.PrintStream;

public final class RequestPCCafeCouponUse extends L2GameClientPacket
{
  private static final String _C__D0_20_REQUESTPCCAFECOUPONUSE = "[C] D0:20 RequestPCCafeCouponUse";
  private String _str;

  protected void readImpl()
  {
    _str = readS();
  }

  protected void runImpl()
  {
    System.out.println("C5: RequestPCCafeCouponUse: S: " + _str);
  }

  public String getType()
  {
    return "[C] D0:20 RequestPCCafeCouponUse";
  }
}