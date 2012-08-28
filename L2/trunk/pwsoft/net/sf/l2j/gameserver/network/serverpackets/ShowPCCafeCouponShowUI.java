package net.sf.l2j.gameserver.network.serverpackets;

public class ShowPCCafeCouponShowUI extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(254);
    writeH(67);
  }

  public String getType()
  {
    return "S.ShowPCCafeCouponShowUI";
  }
}