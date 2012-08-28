package net.sf.l2j.gameserver.network.clientpackets;

public final class RequestPCCafeCouponUse extends L2GameClientPacket
{
  private String _str;

  protected void readImpl()
  {
    _str = readS();
  }

  protected void runImpl()
  {
  }
}