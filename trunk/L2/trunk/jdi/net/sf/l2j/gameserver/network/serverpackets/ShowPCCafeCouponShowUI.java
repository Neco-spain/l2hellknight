package net.sf.l2j.gameserver.network.serverpackets;

public class ShowPCCafeCouponShowUI extends L2GameServerPacket
{
  private static final String _S__FE_43_SHOWPCCAFECOUPONSHOWUI = "[S] FE:43 ShowPCCafeCouponShowUI";

  protected void writeImpl()
  {
    writeC(254);
    writeH(67);
  }

  public String getType()
  {
    return "[S] FE:43 ShowPCCafeCouponShowUI";
  }
}