package net.sf.l2j.gameserver.network.serverpackets;

public class ExShowVariationMakeWindow extends L2GameServerPacket
{
  private static final String _S__FE_50_EXSHOWVARIATIONMAKEWINDOW = "[S] FE:50 ExShowVariationMakeWindow";

  protected void writeImpl()
  {
    writeC(254);
    writeH(80);
  }

  public String getType()
  {
    return "[S] FE:50 ExShowVariationMakeWindow";
  }
}